/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.consent;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.Consent;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v1.hip.HIPPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.LogsRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.*;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.ConsentArtefact;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.ConsentStatus;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.Notification;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.NotifyHIURequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.responses.FacadeConsentDetails;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.config.ErrorHandler;
import in.nha.abdm.wrapper.v3.database.mongo.services.ConsentPatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hip.HIPV3Client;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests.ConsentOnNotifyV3Request;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.responses.ConsentStatusV3Response;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.responses.ConsentV3Response;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

@Service
public class HIUConsentV3Service implements HIUConsentV3Interface {

  private static final Logger log = LogManager.getLogger(HIUConsentV3Service.class);

  @Value("${consentInitPath}")
  private String consentInitPath;

  @Value("${consentStatusPath}")
  private String consentStatusPath;

  @Value("${consentHiuOnNotifyPath}")
  private String consentHiuOnNotifyPath;

  @Value("${fetchConsentPath}")
  private String fetchConsentPath;

  private final RequestV3Manager requestV3Manager;
  private final RequestLogV3Service requestLogService;
  private final LogsRepo logsRepo;
  private final PatientRepo patientRepo;
  private final PatientV3Service patientV3Service;
  private final ConsentPatientV3Service consentPatientService;
  private final HIPV3Client hipClient;

  @Autowired
  public HIUConsentV3Service(
      RequestV3Manager requestV3Manager,
      RequestLogV3Service requestLogService,
      LogsRepo logsRepo,
      PatientRepo patientRepo,
      PatientV3Service patientV3Service,
      ConsentPatientV3Service consentPatientService,
      HIPV3Client hipClient) {
    this.requestV3Manager = requestV3Manager;
    this.requestLogService = requestLogService;
    this.logsRepo = logsRepo;
    this.patientRepo = patientRepo;
    this.patientV3Service = patientV3Service;
    this.consentPatientService = consentPatientService;
    this.hipClient = hipClient;
  }

  /**
   * Initiating consent request to ABDM gateway
   *
   * @param initConsentRequest
   * @return
   * @throws IllegalDataStateException
   */
  @Override
  public FacadeV3Response initiateConsentRequest(InitConsentRequest initConsentRequest)
      throws IllegalDataStateException {
    try {
      requestLogService.saveConsentRequest(initConsentRequest);
      Patient patient =
          patientRepo.findByAbhaAddress(
              initConsentRequest.getConsent().getPatient().getId(),
              initConsentRequest.getConsent().getHiu().getId());
      if (Objects.isNull(patient)) {
        patient =
            getPatient(
                initConsentRequest.getConsent().getPatient().getId(),
                initConsentRequest.getConsent().getHiu().getId());
        if (Objects.isNull(patient)) {
          ErrorResponse error =
              ErrorResponse.builder()
                  .code(GatewayConstants.ERROR_CODE)
                  .message("Patient not found to raise a consent")
                  .build();
          requestLogService.updateError(
              initConsentRequest.getRequestId(), error, RequestStatus.PATIENT_NOT_FOUND);
          return FacadeV3Response.builder()
              .message("Patient not found")
              .errors(ErrorHandler.getErrors(error))
              .clientRequestId(initConsentRequest.getRequestId())
              .httpStatusCode(HttpStatus.BAD_REQUEST)
              .build();
        }
      }

      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              consentInitPath,
              initConsentRequest,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIU_ID,
                  initConsentRequest.getConsent().getHiu().getId(),
                  initConsentRequest.getRequestId()));
      if (response.getStatusCode().is2xxSuccessful()) {
        requestLogService.updateConsentStatus(
            initConsentRequest.getRequestId(), RequestStatus.CONSENT_INIT_ACCEPTED);
      } else {
        String error =
            (Objects.nonNull(response.getBody()) && Objects.nonNull(response.getBody().getError()))
                ? response.getBody().getError().toString()
                : "Error from gateway while initiating consent request: "
                    + initConsentRequest.toString();
        log.error(error);
        requestLogService.updateError(
            initConsentRequest.getRequestId(),
            Collections.singletonList(
                ErrorV3Response.builder()
                    .error(
                        ErrorResponse.builder()
                            .code(GatewayConstants.ERROR_CODE)
                            .message(error)
                            .build())
                    .build()),
            RequestStatus.CONSENT_INIT_ERROR);
      }
      return FacadeV3Response.builder()
          .clientRequestId(initConsentRequest.getRequestId())
          .httpStatusCode(response.getStatusCode())
          .message(RequestStatus.CONSENT_INIT_ACCEPTED.getValue())
          .build();
      // Catching BadRequest response from post request
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogService.updateError(
          initConsentRequest.getRequestId(), error, RequestStatus.CONSENT_INIT_ERROR);
      return FacadeV3Response.builder()
          .clientRequestId(initConsentRequest.getRequestId())
          .errors(ErrorHandler.getErrors(error))
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .build();
    } catch (Exception ex) {
      String error =
          "Exception while initiating consent request: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.error(error);
      requestLogService.updateError(
          initConsentRequest.getRequestId(),
          Collections.singletonList(
              ErrorV3Response.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(GatewayConstants.ERROR_CODE)
                          .message(error)
                          .build())
                  .build()),
          RequestStatus.CONSENT_INIT_ERROR);
      return FacadeV3Response.builder()
          .clientRequestId(initConsentRequest.getRequestId())
          .message(error)
          .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Getting the status of the consent by filtering the status GRANTED, DENIED, REVOKED, EXPIRED and
   * all related audit
   *
   * @param clientRequestId
   * @return
   * @throws IllegalDataStateException
   */
  @Override
  public ConsentStatusV3Response consentRequestStatus(String clientRequestId)
      throws IllegalDataStateException {

    RequestLog requestLog = logsRepo.findByClientRequestId(clientRequestId);
    if (Objects.isNull(requestLog)) {
      throw new IllegalDataStateException(
          "Client request not found in database: " + clientRequestId);
    }
    try {
      if (requestLog.getStatus().equals(RequestStatus.CONSENT_INIT_ERROR)) {
        log.info("Got: " + RequestStatus.CONSENT_INIT_ERROR);
        return ConsentStatusV3Response.builder()
            .status(requestLog.getStatus())
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .initConsentRequest(
                (InitConsentRequest)
                    requestLog.getRequestDetails().get(FieldIdentifiers.CONSENT_INIT_REQUEST))
            .errors(ErrorHandler.getErrors(requestLog.getError()))
            .build();
      }
      // Check whether we have got consent response as part of 'consent hiu-notify'.
      if (Objects.nonNull(requestLog.getResponseDetails())
          && Objects.nonNull(
              requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_NOTIFY_RESPONSE))) {
        log.info("Got: " + FieldIdentifiers.CONSENT_ON_NOTIFY_RESPONSE);
        return consentOnNotifyResponse(requestLog);
      }
      if (requestLog.getStatus().equals(RequestStatus.CONSENT_NOTIFY_ERROR)) {
        log.info("Got: " + RequestStatus.CONSENT_NOTIFY_ERROR);
        return consentOnNotifyResponse(requestLog);
      }

      // Check whether we have got consent response as part of 'consent on-status'.
      if (Objects.nonNull(requestLog.getResponseDetails())
          && Objects.nonNull(
              requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_STATUS_RESPONSE))) {
        log.info("Got: " + FieldIdentifiers.CONSENT_ON_STATUS_RESPONSE);
        return consentOnStatusResponse(requestLog);
      }

      // Issue a consent status request if it has not been issued earlier, and we have received
      // consent request id
      // as part of 'consent on init' response or the request is in some error state.
      if (requestLog.getStatus() != RequestStatus.CONSENT_STATUS_ACCEPTED
          && Objects.nonNull(requestLog.getResponseDetails())
          && (Objects.nonNull(
              requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_INIT_RESPONSE)))) {
        log.info("Got: " + FieldIdentifiers.CONSENT_ON_INIT_RESPONSE);
        return fetchConsentStatus(requestLog);
      }

      // In all other scenarios, send the status of request as is.
      return ConsentStatusV3Response.builder()
          .status(requestLog.getStatus())
          .httpStatusCode(HttpStatus.OK)
          .build();
      // Catching BadRequest response from post request
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogService.updateError(
          requestLog.getGatewayRequestId(), error, RequestStatus.CONSENT_STATUS_ERROR);
      return ConsentStatusV3Response.builder()
          .errors(ErrorHandler.getErrors(error))
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .build();
    } catch (Exception ex) {
      String error =
          "Exception while fetching consent status: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.error(error);
      requestLogService.updateError(
          requestLog.getGatewayRequestId(),
          Collections.singletonList(
              ErrorV3Response.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(GatewayConstants.ERROR_CODE)
                          .message(error)
                          .build())
                  .build()),
          RequestStatus.CONSENT_STATUS_ERROR);
      return ConsentStatusV3Response.builder()
          .errors(
              Collections.singletonList(
                  ErrorV3Response.builder()
                      .error(
                          ErrorResponse.builder()
                              .code(GatewayConstants.ERROR_CODE)
                              .message(error)
                              .build())
                      .build()))
          .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Acknowledgement of consent notify
   *
   * @param onNotifyRequest
   * @param headers
   */
  @Override
  public void hiuOnNotify(ConsentOnNotifyV3Request onNotifyRequest, HttpHeaders headers) {
    try {
      log.info("Initiating hiu/on-notify");
      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              consentHiuOnNotifyPath,
              onNotifyRequest,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIU_ID,
                  headers.getFirst(GatewayConstants.X_HIU_ID),
                  UUID.randomUUID().toString()));
      // If something goes wrong while acknowledging notification from gateway, then we can just log
      // it,
      // and we don't need to throw exception.
      if (!response.getStatusCode().is2xxSuccessful()) {
        String error =
            (Objects.nonNull(response.getBody()) && Objects.nonNull(response.getBody().getError()))
                ? response.getBody().getError().toString()
                : "Error from gateway while getting consent status: " + onNotifyRequest.toString();
        log.error(error);
      }
      // Catching BadRequest response from post request
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error on-notify {}: {}", ex.getStatusCode(), error);
    } catch (Exception ex) {
      String error =
          "Exception while executing on notify: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.error(error);
    }
  }

  /**
   * Getting the consent details using consentId.
   *
   * @param fetchConsentRequest
   * @param requestLog
   * @param headers
   * @return
   */
  @Override
  public ConsentV3Response fetchConsent(
      FetchConsentRequest fetchConsentRequest, RequestLog requestLog, HttpHeaders headers) {
    try {
      log.info("Initiating Fetching consent");
      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              fetchConsentPath,
              fetchConsentRequest,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIU_ID,
                  headers.getFirst(GatewayConstants.X_HIU_ID),
                  UUID.randomUUID().toString()));
      if (response.getStatusCode().is2xxSuccessful()) {
        requestLogService.updateStatus(
            requestLog.getGatewayRequestId(), RequestStatus.CONSENT_FETCH_ACCEPTED);
        return ConsentV3Response.builder()
            .status(RequestStatus.CONSENT_FETCH_ACCEPTED)
            .httpStatusCode(HttpStatus.OK)
            .build();
      } else {
        requestLogService.updateError(
            requestLog.getGatewayRequestId(),
            Collections.singletonList(
                ErrorV3Response.builder()
                    .error(
                        ErrorResponse.builder()
                            .code(GatewayConstants.ERROR_CODE)
                            .message("unable to fetch consent")
                            .build())
                    .build()),
            RequestStatus.CONSENT_FETCH_ERROR);
        return ConsentV3Response.builder()
            .status(RequestStatus.CONSENT_FETCH_ERROR)
            .httpStatusCode(response.getStatusCode())
            .build();
      }
      // Catching BadRequest response from post request
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogService.updateError(
          requestLog.getGatewayRequestId(), error, RequestStatus.CONSENT_FETCH_ERROR);
      return ConsentV3Response.builder()
          .errors(error)
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .build();
    } catch (Exception ex) {
      String error =
          "Exception while fetching consent: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
      requestLogService.updateError(
          requestLog.getGatewayRequestId(),
          Collections.singletonList(
              ErrorV3Response.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(GatewayConstants.ERROR_CODE)
                          .message(error)
                          .build())
                  .build()),
          RequestStatus.CONSENT_FETCH_ERROR);
      return ConsentV3Response.builder()
          .errors(
              Collections.singletonList(
                  ErrorV3Response.builder()
                      .error(
                          ErrorResponse.builder()
                              .code(GatewayConstants.ERROR_CODE)
                              .message(error)
                              .build())
                      .build()))
          .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  /**
   * Acknowledgement of consent/notify
   *
   * @param requestLog
   * @return
   * @throws IllegalDataStateException
   */
  private ConsentStatusV3Response consentOnNotifyResponse(RequestLog requestLog)
      throws IllegalDataStateException {
    if (Objects.nonNull(requestLog) && requestLog.getError() != null) {
      return ConsentStatusV3Response.builder()
          .status(requestLog.getStatus())
          .errors(ErrorHandler.getErrors(requestLog.getError()))
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .build();
    }
    NotifyHIURequest notifyHIURequest =
        (NotifyHIURequest)
            requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_NOTIFY_RESPONSE);
    Notification notification = notifyHIURequest.getNotification();
    if (Objects.nonNull(notification) && notification.getStatus().equalsIgnoreCase("DENIED")) {
      return ConsentStatusV3Response.builder()
          .status(requestLog.getStatus())
          .httpStatusCode(HttpStatus.OK)
          .initConsentRequest(
              requestLog.getRequestDetails() != null
                  ? (InitConsentRequest)
                      requestLog.getRequestDetails().get(FieldIdentifiers.CONSENT_INIT_REQUEST)
                  : null)
          .consentDetails(
              FacadeConsentDetails.builder()
                  .deniedOn(notifyHIURequest.getTimestamp())
                  .consent(
                      Collections.singletonList(
                          ConsentStatus.builder().status(notification.getStatus()).build()))
                  .build())
          .build();
    }
    if (Objects.nonNull(notification)) {
      String abhaAddress =
          consentPatientService
              .findMappingByConsentId(
                  notification.getConsentArtefacts().get(0).getId(), "HIU", requestLog.getHipId())
              .getAbhaAddress();
      if (abhaAddress != null) {
        return ConsentStatusV3Response.builder()
            .status(requestLog.getStatus())
            .httpStatusCode(HttpStatus.OK)
            .initConsentRequest(
                (InitConsentRequest)
                    requestLog.getRequestDetails().get(FieldIdentifiers.CONSENT_INIT_REQUEST))
            .consentDetails(
                makeConsentArtifactList(abhaAddress, notification, requestLog.getHipId()))
            .build();
      }
    }
    return ConsentStatusV3Response.builder()
        .status(requestLog.getStatus())
        .httpStatusCode(HttpStatus.BAD_REQUEST)
        .errors(
            Collections.singletonList(
                ErrorV3Response.builder()
                    .error(
                        ErrorResponse.builder()
                            .code(GatewayConstants.ERROR_CODE)
                            .message("Consent not found")
                            .build())
                    .build()))
        .build();
  }

  /**
   * Displaying the hip details and the careContexts associated with consentId Fetching the consents
   * from request-logs, Getting the abhaAddress using consentPatient repo Using the abhaAddress
   * fetching the consent from patient repo Sorting them according to the consent status In patient
   * repo the consent status is updates like revoke and expired In request-logs first granted
   * consents are un disturbed for tracking when revoked and expired.
   */
  private FacadeConsentDetails makeConsentArtifactList(
      String abhaAddress, Notification notification, String hipId)
      throws IllegalDataStateException {
    String expiredTimeStamp = null;
    List<ConsentArtefact> consentArtefacts = notification.getConsentArtefacts();
    List<Consent> consentList = new ArrayList<>();

    for (ConsentArtefact item : consentArtefacts) {
      try {
        Consent consent = patientV3Service.getConsentDetails(abhaAddress, item.getId(), hipId);
        consentList.add(consent);
      } catch (IllegalDataStateException e) {
        throw new RuntimeException(e);
      }
    }
    return buildConsentDetails(consentList, hipId);
  }

  /**
   * Step 1: First the expired consents are made into a separate list and the update their status in
   * patient Step 2: Grouping the consents using the status in Map. Step 3: Building the
   * consentStatus list which has GRANTED, REVOKED and EXPIRED.
   *
   * @param consentList
   * @return
   */
  private FacadeConsentDetails buildConsentDetails(List<Consent> consentList, String hipId) {
    List<Consent> expiredConsents =
        consentList.stream()
            .filter(
                consent ->
                    consent.getStatus().equalsIgnoreCase("GRANTED")
                        && Utils.checkExpiry(
                            consent.getConsentDetail().getPermission().getDataEraseAt()))
            .toList();
    updateExpiredConsents(expiredConsents, hipId);
    Map<String, List<Consent>> groupedConsents =
        consentList.stream().collect(Collectors.groupingBy(Consent::getStatus));
    List<ConsentStatus> consentStatusList =
        new ArrayList<>(
            groupedConsents.entrySet().stream()
                .map(
                    entry -> {
                      String status = entry.getKey();
                      List<ConsentArtefact> consentArtifacts =
                          entry.getValue().stream()
                              .map(this::createConsentArtefact)
                              .collect(Collectors.toList());
                      return ConsentStatus.builder()
                          .status(status)
                          .consentArtefacts(consentArtifacts)
                          .build();
                    })
                .toList());
    Consent consentForDateRange = consentList.get(0);
    return FacadeConsentDetails.builder()
        .consent(consentStatusList)
        .grantedOn(consentForDateRange.getGrantedOn())
        .hiTypes(consentForDateRange.getConsentDetail().getHiTypes())
        .dataEraseAt(consentForDateRange.getConsentDetail().getPermission().getDataEraseAt())
        .dateRange(consentForDateRange.getConsentDetail().getPermission().getDateRange())
        .build();
  }

  private ConsentArtefact createConsentArtefact(Consent consent) {
    return ConsentArtefact.builder()
        .id(consent.getConsentDetail().getConsentId())
        .lastUpdated(consent.getLastUpdatedOn())
        .hipId(consent.getConsentDetail().getHip().getId())
        .careContextReference(
            consent.getConsentDetail().getCareContexts().stream()
                .map(ConsentCareContexts::getCareContextReference)
                .collect(Collectors.toList()))
        .build();
  }

  private void updateExpiredConsents(List<Consent> expiredConsents, String hipId) {
    expiredConsents.forEach(
        consent -> {
          patientV3Service.updatePatientConsent(
              consent.getConsentDetail().getPatient().getId(),
              consent.getConsentDetail().getConsentId(),
              "EXPIRED",
              consent.getConsentDetail().getPermission().getDataEraseAt(),
              hipId);
          log.info(
              "Updated consent status to EXPIRED for consentId: "
                  + consent.getConsentDetail().getConsentId());
        });
  }

  private ConsentStatusV3Response consentOnStatusResponse(RequestLog requestLog) {
    ConsentStatus consentStatus =
        (ConsentStatus)
            requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_STATUS_RESPONSE);
    return ConsentStatusV3Response.builder()
        .status(requestLog.getStatus())
        .httpStatusCode(HttpStatus.OK)
        .consentDetails(
            FacadeConsentDetails.builder()
                .consent(Collections.singletonList(consentStatus))
                .build())
        .build();
  }

  /**
   * Checking the status of the consent
   *
   * @param requestLog
   * @return
   */
  private ConsentStatusV3Response fetchConsentStatus(RequestLog requestLog) {
    String consentRequestId =
        (String) requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_INIT_RESPONSE);
    ConsentStatusRequest consentStatusRequest =
        ConsentStatusRequest.builder().consentRequestId(consentRequestId).build();
    ResponseEntity<GenericV3Response> response =
        requestV3Manager.fetchResponseFromGateway(
            consentStatusPath,
            consentStatusRequest,
            Utils.getCustomHeaders(
                GatewayConstants.X_HIU_ID, requestLog.getHipId(), UUID.randomUUID().toString()));
    if (response.getStatusCode().is2xxSuccessful()) {
      requestLogService.updateStatus(
          requestLog.getGatewayRequestId(), RequestStatus.CONSENT_STATUS_ACCEPTED);
      if (requestLog.getError() != null) {
        return ConsentStatusV3Response.builder()
            .status(RequestStatus.CONSENT_STATUS_ACCEPTED)
            .errors(ErrorHandler.getErrors(requestLog.getError()))
            .httpStatusCode(HttpStatus.OK)
            .build();
      }
      return ConsentStatusV3Response.builder()
          .status(RequestStatus.CONSENT_STATUS_ACCEPTED)
          .httpStatusCode(HttpStatus.OK)
          .build();
    } else {
      String error =
          (Objects.nonNull(response.getBody()) && Objects.nonNull(response.getBody().getError()))
              ? response.getBody().getError().toString()
              : "Error from gateway while getting consent status: "
                  + consentStatusRequest.toString();
      log.error(error);
      requestLogService.updateError(
          requestLog.getGatewayRequestId(),
          Collections.singletonList(
              ErrorV3Response.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(GatewayConstants.ERROR_CODE)
                          .message(error)
                          .build())
                  .build()),
          RequestStatus.CONSENT_STATUS_ERROR);
      return ConsentStatusV3Response.builder()
          .errors(
              Collections.singletonList(
                  ErrorV3Response.builder()
                      .error(
                          ErrorResponse.builder()
                              .code(GatewayConstants.ERROR_CODE)
                              .message(error)
                              .build())
                      .build()))
          .httpStatusCode(response.getStatusCode())
          .build();
    }
  }

  private Patient getPatient(String abhaAddress, String hipId) {
    log.debug("Patient not found in database, sending request to HIP.");
    HIPPatient hipPatient = hipClient.getPatient(abhaAddress, hipId);
    if (Objects.nonNull(hipPatient)) {
      if (Objects.isNull(hipPatient.getError())) {
        Patient patient = new Patient();
        patient.setAbhaAddress(hipPatient.getAbhaAddress());
        patient.setGender(hipPatient.getGender());
        patient.setName(hipPatient.getName());
        patient.setDateOfBirth(hipPatient.getDateOfBirth());
        patient.setPatientDisplay(hipPatient.getPatientDisplay());
        patient.setPatientReference(hipPatient.getPatientReference());
        patient.setPatientMobile(hipPatient.getPatientMobile());
        patient.setHipId(hipId);
        patientRepo.save(patient);
        return patient;
      }
    }
    return null;
  }
}
