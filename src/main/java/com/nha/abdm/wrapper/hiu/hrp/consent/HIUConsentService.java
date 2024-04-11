/* (C) 2024 */
package com.nha.abdm.wrapper.hiu.hrp.consent;

import com.nha.abdm.wrapper.common.RequestManager;
import com.nha.abdm.wrapper.common.Utils;
import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.models.Consent;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.ErrorResponseWrapper;
import com.nha.abdm.wrapper.common.responses.FacadeResponse;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.LogsRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.PatientRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.ConsentPatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.PatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.RequestLogService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.RequestLog;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.*;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.callback.ConsentArtefact;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.callback.ConsentStatus;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.callback.Notification;
import com.nha.abdm.wrapper.hiu.hrp.consent.responses.ConsentResponse;
import com.nha.abdm.wrapper.hiu.hrp.consent.responses.ConsentStatusResponse;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

@Service
public class HIUConsentService implements HIUConsentInterface {

  private static final Logger log = LogManager.getLogger(HIUConsentService.class);

  @Value("${consentInitPath}")
  private String consentInitPath;

  @Value("${consentStatusPath}")
  private String consentStatusPath;

  @Value("${consentHiuOnNotifyPath}")
  private String consentHiuOnNotifyPath;

  @Value("${fetchConsentPath}")
  private String fetchConsentPath;

  private final RequestManager requestManager;
  private final RequestLogService requestLogService;
  private final LogsRepo logsRepo;
  private final PatientRepo patientRepo;
  private final PatientService patientService;
  private final ConsentPatientService consentPatientService;

  @Autowired
  public HIUConsentService(
      RequestManager requestManager,
      RequestLogService requestLogService,
      LogsRepo logsRepo,
      PatientRepo patientRepo,
      PatientService patientService,
      ConsentPatientService consentPatientService) {
    this.requestManager = requestManager;
    this.requestLogService = requestLogService;
    this.logsRepo = logsRepo;
    this.patientRepo = patientRepo;
    this.patientService = patientService;
    this.consentPatientService = consentPatientService;
  }

  @Override
  public FacadeResponse initiateConsentRequest(InitConsentRequest initConsentRequest) {
    requestLogService.saveRequest(
        initConsentRequest.getRequestId(), RequestStatus.INITIATING, null);
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(consentInitPath, initConsentRequest);
      if (response.getStatusCode().is2xxSuccessful()) {
        requestLogService.updateStatus(
            initConsentRequest.getRequestId(), RequestStatus.CONSENT_INIT_ACCEPTED);
      } else {
        String error =
            (Objects.nonNull(response.getBody())
                    && Objects.nonNull(response.getBody().getErrorResponse()))
                ? response.getBody().getErrorResponse().getMessage()
                : "Error from gateway while initiating consent request: "
                    + initConsentRequest.toString();
        log.error(error);
        requestLogService.updateError(
            initConsentRequest.getRequestId(), error, RequestStatus.CONSENT_INIT_ERROR);
      }
      return FacadeResponse.builder()
          .clientRequestId(initConsentRequest.getRequestId())
          .httpStatusCode(response.getStatusCode())
          .build();
      // Catching BadRequest response from post request
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogService.updateError(
          initConsentRequest.getRequestId(), error.getMessage(), RequestStatus.CONSENT_INIT_ERROR);
      return FacadeResponse.builder()
          .clientRequestId(initConsentRequest.getRequestId())
          .error(error)
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .build();
    } catch (Exception ex) {
      String error =
          "Exception while initiating consent request: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.error(error);
      requestLogService.saveRequest(
          initConsentRequest.getRequestId(), RequestStatus.CONSENT_INIT_ERROR, error);
      return FacadeResponse.builder()
          .clientRequestId(initConsentRequest.getRequestId())
          .message(error)
          .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  @Override
  public ConsentStatusResponse consentRequestStatus(String clientRequestId)
      throws IllegalDataStateException {

    RequestLog requestLog = logsRepo.findByClientRequestId(clientRequestId);
    if (requestLog == null) {
      throw new IllegalDataStateException(
          "Client request not found in database: " + clientRequestId);
    }
    try {
      // Check whether we have got consent response as part of 'consent hiu-notify'.
      if (Objects.nonNull(requestLog.getResponseDetails())
          && Objects.nonNull(
              requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_NOTIFY_RESPONSE))) {
        return consentOnNotifyResponse(requestLog);
      }
      if (requestLog.getStatus().equals(RequestStatus.CONSENT_NOTIFY_ERROR)) {
        return consentOnNotifyResponse(requestLog);
      }

      // Check whether we have got consent response as part of 'consent on-status'.
      if (Objects.nonNull(requestLog.getResponseDetails())
          && Objects.nonNull(
              requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_STATUS_RESPONSE))) {
        return consentOnStatusResponse(requestLog);
      }

      // Issue a consent status request if it has not been issued earlier, and we have received
      // consent request id
      // as part of 'consent on init' response or the request is in some error state.
      if (requestLog.getStatus() != RequestStatus.CONSENT_STATUS_ACCEPTED
          && Objects.nonNull(requestLog.getResponseDetails())
          && (Objects.nonNull(
              requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_INIT_RESPONSE)))) {
        return fetchConsentStatus(requestLog);
      }

      // In all other scenarios, send the status of request as is.
      return ConsentStatusResponse.builder()
          .status(requestLog.getStatus())
          .httpStatusCode(HttpStatus.OK)
          .build();
      // Catching BadRequest response from post request
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogService.updateError(
          requestLog.getGatewayRequestId(), error.getMessage(), RequestStatus.CONSENT_STATUS_ERROR);
      return ConsentStatusResponse.builder()
          .error(ErrorResponse.builder().code(1000).message(error.getMessage()).build())
          .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    } catch (Exception ex) {
      String error =
          "Exception while fetching consent status: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.error(error);
      requestLogService.updateError(
          requestLog.getGatewayRequestId(), error, RequestStatus.CONSENT_STATUS_ERROR);
      return ConsentStatusResponse.builder()
          .error(ErrorResponse.builder().code(1000).message(error).build())
          .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  @Override
  public void hiuOnNotify(OnNotifyRequest onNotifyRequest) {
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(consentHiuOnNotifyPath, onNotifyRequest);
      // If something goes wrong while acknowledging notification from gateway, then we can just log
      // it,
      // and we don't need to throw exception.
      if (!response.getStatusCode().is2xxSuccessful()) {
        String error =
            (Objects.nonNull(response.getBody())
                    && Objects.nonNull(response.getBody().getErrorResponse()))
                ? response.getBody().getErrorResponse().getMessage()
                : "Error from gateway while getting consent status: " + onNotifyRequest.toString();
        log.error(error);
      }
      // Catching BadRequest response from post request
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
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

  @Override
  public ConsentResponse fetchConsent(
      FetchConsentRequest fetchConsentRequest, RequestLog requestLog) {
    try {
      fetchConsentRequest.setRequestId(UUID.randomUUID().toString());
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(fetchConsentPath, fetchConsentRequest);
      if (response.getStatusCode().is2xxSuccessful()) {
        requestLogService.updateStatus(
            requestLog.getGatewayRequestId(), RequestStatus.CONSENT_FETCH_ACCEPTED);
        return ConsentResponse.builder()
            .status(RequestStatus.CONSENT_FETCH_ACCEPTED)
            .httpStatusCode(HttpStatus.OK)
            .build();
      } else {
        requestLogService.updateStatus(
            requestLog.getGatewayRequestId(), RequestStatus.CONSENT_FETCH_ERROR);
        return ConsentResponse.builder()
            .status(RequestStatus.CONSENT_FETCH_ERROR)
            .httpStatusCode(response.getStatusCode())
            .build();
      }
      // Catching BadRequest response from post request
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogService.updateStatus(
          requestLog.getGatewayRequestId(), RequestStatus.CONSENT_FETCH_ERROR);
      return ConsentResponse.builder()
          .error(error.getMessage())
          .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    } catch (Exception ex) {
      String error =
          "Exception while fetching consent: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
      requestLogService.updateStatus(
          requestLog.getGatewayRequestId(), RequestStatus.CONSENT_FETCH_ERROR);
      return ConsentResponse.builder()
          .error(error)
          .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }

  private ConsentStatusResponse consentOnNotifyResponse(RequestLog requestLog)
      throws IllegalDataStateException {
    if (Objects.nonNull(requestLog) && requestLog.getError() != null) {
      return ConsentStatusResponse.builder()
          .status(requestLog.getStatus())
          .error(ErrorResponse.builder().code(1000).message(requestLog.getError()).build())
          .httpStatusCode(HttpStatus.OK)
          .build();
    }
    Notification notification =
        (Notification)
            requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_NOTIFY_RESPONSE);
    if (Objects.nonNull(notification) && notification.getStatus().equalsIgnoreCase("DENIED")) {
      return ConsentStatusResponse.builder()
          .status(requestLog.getStatus())
          .httpStatusCode(HttpStatus.OK)
          .consent(
              Collections.singletonList(
                  ConsentStatus.builder().status(notification.getStatus()).build()))
          .build();
    }
    if (Objects.nonNull(notification)) {
      String abhaAddress =
          consentPatientService
              .findMappingByConsentId(notification.getConsentArtefacts().get(0).getId())
              .getAbhaAddress();
      if (abhaAddress != null) {
        return ConsentStatusResponse.builder()
            .status(requestLog.getStatus())
            .httpStatusCode(HttpStatus.OK)
            .consent(makeConsentArtifactList(abhaAddress, notification))
            .build();
      }
    }
    return ConsentStatusResponse.builder()
        .status(requestLog.getStatus())
        .httpStatusCode(HttpStatus.OK)
        .error(ErrorResponse.builder().code(1000).message("Consents not found").build())
        .build();
  }
  // Displaying the hip details and the careContexts associated with consentId
  // Fetching the consents from request-logs,
  // Getting the abhaAddress using consentPatient repo
  // Using the abhaAddress fetching the consent from patient repo
  // Sorting them according to the consent status
  // In patient repo the consent status is updates like revoke and expired
  // In request-logs first granted consents are un disturbed for tracking when revoked and expired.
  // TODO

  private List<ConsentStatus> makeConsentArtifactList(String abhaAddress, Notification notification)
      throws IllegalDataStateException {
    List<Consent> consentList =
        notification.getConsentArtefacts().stream()
            .map(
                item -> {
                  try {
                    return patientService.getConsentDetails(abhaAddress, item.getId());
                  } catch (IllegalDataStateException e) {
                    throw new RuntimeException(e);
                  }
                })
            .toList();
    List<ConsentArtefact> grantedList = new ArrayList<>();
    List<ConsentArtefact> revokedList = new ArrayList<>();
    List<ConsentArtefact> expiredList = new ArrayList<>();
    Consent consentForDateRange = null;
    for (Consent consent : consentList) {
      if (consent == null) {
        continue;
      }
      if (consentForDateRange == null) {
        consentForDateRange = consent;
      }
      List<String> careContextsReferenceList =
          consent.getConsentDetail().getCareContexts().stream()
              .map(ConsentCareContexts::getCareContextReference)
              .collect(Collectors.toList());

      ConsentArtefact consentArtefact =
          ConsentArtefact.builder()
              .id(consent.getConsentDetail().getConsentId())
              .hipId(consent.getConsentDetail().getHip().getId())
              .careContextReference(careContextsReferenceList)
              .build();

      switch (consent.getStatus()) {
        case "GRANTED":
          if (Utils.checkExpiry(
              consentForDateRange.getConsentDetail().getPermission().getDataEraseAt())) {
            expiredList.add(consentArtefact);
            patientService.updatePatientConsent(
                abhaAddress, consent.getConsentDetail().getConsentId(), "EXPIRED");
            log.info(
                "Updating consent status: EXPIRY for :"
                    + abhaAddress
                    + " consentId: "
                    + consent.getConsentDetail().getConsentId());
          } else {
            grantedList.add(consentArtefact);
          }
          break;
        case "REVOKED":
          revokedList.add(consentArtefact);
          break;
        case "EXPIRED":
          expiredList.add(consentArtefact);
          break;
      }
    }
    return buildConsentStatusList(grantedList, revokedList, expiredList, consentForDateRange);
  }

  private List<ConsentStatus> buildConsentStatusList(
      List<ConsentArtefact> grantedList,
      List<ConsentArtefact> revokedList,
      List<ConsentArtefact> expiredList,
      Consent consentForDateRange) {
    List<ConsentStatus> consentStatusList = new ArrayList<>();
    consentStatusList.add(
        ConsentStatus.builder()
            .status("GRANTED")
            .dateRange(
                consentForDateRange != null
                    ? consentForDateRange.getConsentDetail().getPermission().getDateRange()
                    : null)
            .dataEraseAt(
                consentForDateRange != null
                    ? consentForDateRange.getConsentDetail().getPermission().getDataEraseAt()
                    : null)
            .consentArtefacts(grantedList)
            .build());
    consentStatusList.add(
        ConsentStatus.builder()
            .status("REVOKED")
            .dateRange(
                consentForDateRange != null
                    ? consentForDateRange.getConsentDetail().getPermission().getDateRange()
                    : null)
            .dataEraseAt(
                consentForDateRange != null
                    ? consentForDateRange.getConsentDetail().getPermission().getDataEraseAt()
                    : null)
            .consentArtefacts(revokedList)
            .build());
    consentStatusList.add(
        ConsentStatus.builder()
            .status("EXPIRED")
            .dateRange(
                consentForDateRange != null
                    ? consentForDateRange.getConsentDetail().getPermission().getDateRange()
                    : null)
            .dataEraseAt(
                consentForDateRange != null
                    ? consentForDateRange.getConsentDetail().getPermission().getDataEraseAt()
                    : null)
            .consentArtefacts(expiredList)
            .build());
    return consentStatusList;
  }

  private ConsentStatusResponse consentOnStatusResponse(RequestLog requestLog) {
    ConsentStatus consentStatus =
        (ConsentStatus)
            requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_STATUS_RESPONSE);
    return ConsentStatusResponse.builder()
        .status(requestLog.getStatus())
        .httpStatusCode(HttpStatus.OK)
        .consent(Collections.singletonList(consentStatus))
        .build();
  }

  private ConsentStatusResponse fetchConsentStatus(RequestLog requestLog) {
    String consentRequestId =
        (String) requestLog.getResponseDetails().get(FieldIdentifiers.CONSENT_ON_INIT_RESPONSE);
    ConsentStatusRequest consentStatusRequest =
        ConsentStatusRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp())
            .consentRequestId(consentRequestId)
            .build();
    ResponseEntity<GenericResponse> response =
        requestManager.fetchResponseFromGateway(consentStatusPath, consentStatusRequest);
    if (response.getStatusCode().is2xxSuccessful()) {
      requestLogService.updateStatus(
          requestLog.getGatewayRequestId(), RequestStatus.CONSENT_STATUS_ACCEPTED);
      if (requestLog.getError() != null) {
        return ConsentStatusResponse.builder()
            .status(RequestStatus.CONSENT_STATUS_ACCEPTED)
            .error(ErrorResponse.builder().message(requestLog.getError()).code(1000).build())
            .httpStatusCode(HttpStatus.OK)
            .build();
      }
      return ConsentStatusResponse.builder()
          .status(RequestStatus.CONSENT_STATUS_ACCEPTED)
          .httpStatusCode(HttpStatus.OK)
          .build();
    } else {
      String error =
          (Objects.nonNull(response.getBody())
                  && Objects.nonNull(response.getBody().getErrorResponse()))
              ? response.getBody().getErrorResponse().getMessage()
              : "Error from gateway while getting consent status: "
                  + consentStatusRequest.toString();
      log.error(error);
      requestLogService.updateError(
          requestLog.getGatewayRequestId(), error, RequestStatus.CONSENT_STATUS_ERROR);
      return ConsentStatusResponse.builder()
          .error(ErrorResponse.builder().code(1000).message(error).build())
          .httpStatusCode(response.getStatusCode())
          .build();
    }
  }
}
