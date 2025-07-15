/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.requests.*;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPNotifyRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.encryption.EncryptionResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.encryption.EncryptionService;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.HealthInformationBundleRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.HealthInformationBundleResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.HealthInformationPushNotification;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers.HealthInformationNotifier;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers.HealthInformationRequestStatus;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.ConsentCareContextsService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentCareContextMapping;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentCareContexts;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.config.ErrorHandler;
import in.nha.abdm.wrapper.v3.database.mongo.services.ConsentPatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hip.HIPV3Client;
import in.nha.abdm.wrapper.v3.hip.hrp.dataTransfer.requests.OnHealthInformationV3Request;
import in.nha.abdm.wrapper.v3.hiu.HIUV3Client;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
public class HIPHealthInformationV3Service implements HIPHealthInformationV3Interface {
  private static final Logger log = LogManager.getLogger(HIPHealthInformationV3Service.class);

  @Value("${healthInformationOnRequestPath}")
  public String healthInformationOnRequestPath;

  @Value("${healthInformationPushNotificationPath}")
  public String healthInformationPushNotificationPath;

  @Autowired RequestV3Manager requestV3Manager;
  private final HIPV3Client hipClient;
  private final HIUV3Client hiuClient;
  @Autowired RequestLogV3Service requestLogV3Service;
  @Autowired ConsentPatientV3Service consentPatientService;
  @Autowired EncryptionService encryptionService;
  @Autowired ConsentCareContextsService consentCareContextsService;
  @Autowired PatientV3Service patientV3Service;

  @Autowired
  public HIPHealthInformationV3Service(HIPV3Client hipClient, HIUV3Client hiuClient) {
    this.hipClient = hipClient;
    this.hiuClient = hiuClient;
  }

  /**
   * POST /on-request as an acknowledgement for agreeing to make dataTransfer to ABDM gateway.
   *
   * @param hipHealthInformationRequest HIU public keys and dataPush URL is provided
   */
  @Override
  public void healthInformation(
      HIPHealthInformationRequest hipHealthInformationRequest, HttpHeaders headers)
      throws IllegalDataStateException,
          InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException {
    OnHealthInformationV3Request onHealthInformationRequest = null;
    RespRequest responseRequestId =
        RespRequest.builder().requestId(headers.getFirst(GatewayConstants.REQUEST_ID)).build();
    String consentId = hipHealthInformationRequest.getHiRequest().getConsent().getId();
    // Lookup in consent patient table is good enough as we are saving mapping when we are saving
    // consent in patient table.
    ConsentPatient consentPatient =
        consentPatientService.findMappingByConsentId(
            consentId, GatewayConstants.HIP, headers.getFirst(GatewayConstants.X_HIP_ID));
    if (Objects.nonNull(consentPatient)) {
      boolean isNotExpired =
          patientV3Service.isConsentValid(
              consentPatient.getAbhaAddress(),
              consentId,
              headers.getFirst(GatewayConstants.X_HIP_ID));
      if (isNotExpired) {
        HealthInformationRequestStatus hiRequestStatus =
            HealthInformationRequestStatus.builder()
                .sessionStatus("ACKNOWLEDGED")
                .transactionId(hipHealthInformationRequest.getTransactionId())
                .build();
        onHealthInformationRequest =
            OnHealthInformationV3Request.builder()
                .hiRequest(hiRequestStatus)
                .response(responseRequestId)
                .build();
      } else {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage("Consent EXPIRED: exceeded dataEraseAt");
        errorResponse.setCode(GatewayConstants.ERROR_CODE);
        log.error(errorResponse);
        onHealthInformationRequest =
            OnHealthInformationV3Request.builder()
                .error(errorResponse)
                .response(responseRequestId)
                .build();
      }
    } else {
      String error = "ConsentId not found in database " + consentId;
      ErrorResponse errorResponse = new ErrorResponse();
      errorResponse.setMessage(error);
      errorResponse.setCode(GatewayConstants.ERROR_CODE);
      log.error(error);
      onHealthInformationRequest =
          OnHealthInformationV3Request.builder()
              .error(errorResponse)
              .response(responseRequestId)
              .build();
    }
    log.debug(
        "health information acknowledgment request body : "
            + onHealthInformationRequest.toString());
    // Acknowledge to gateway that health information request has been received.
    healthInformationAcknowledgementRequest(
        hipHealthInformationRequest, onHealthInformationRequest, headers);
    try {
      // Sending the data to HIU only if there is no errors
      if (Objects.isNull(onHealthInformationRequest.getError())) {
        // Prepare health information bundle request which needs to be sent to HIU.
        HealthInformationBundleResponse healthInformationBundleResponse =
            fetchHealthInformationBundle(hipHealthInformationRequest, headers);
        // Push the health information to HIU.
        List<ResponseEntity<GenericResponse>> pushHealthInformationResponse =
            pushHealthInformation(healthInformationBundleResponse, consentId, headers);
        // Notify Gateway that health information was pushed to HIU.
        healthInformationPushNotify(
            hipHealthInformationRequest, consentId, pushHealthInformationResponse, headers);
      } else {
        // Sending BAD_REQUEST since there are some errors earlier
        healthInformationPushNotify(
            hipHealthInformationRequest,
            consentId,
            Collections.singletonList(new ResponseEntity<>(HttpStatus.BAD_REQUEST)),
            headers);
      }
    } catch (Exception e) {
      List<ErrorV3Response> errors = ErrorHandler.getErrors(e.getMessage());
      log.debug(errors);
      requestLogV3Service.updateConsentStatus(consentId, errors, RequestStatus.DATA_TRANSFER_ERROR);

      // Notify Gateway that health information was pushed to HIU with BAD_REQUEST status.
      healthInformationPushNotify(
          hipHealthInformationRequest,
          consentId,
          Collections.singletonList(new ResponseEntity<>(HttpStatus.BAD_REQUEST)),
          headers);
    }
  }

  private void healthInformationAcknowledgementRequest(
      HIPHealthInformationRequest hipHealthInformationRequest,
      OnHealthInformationV3Request onHealthInformationRequest,
      HttpHeaders headers) {
    try {
      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              healthInformationOnRequestPath,
              onHealthInformationRequest,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIP_ID,
                  headers.getFirst(GatewayConstants.X_HIP_ID),
                  UUID.randomUUID().toString()));
      log.debug(healthInformationOnRequestPath + " : dataOnRequest: " + response.getStatusCode());
      if (response.getStatusCode().is2xxSuccessful()) {
        requestLogV3Service.saveHealthInformationRequest(
            hipHealthInformationRequest, RequestStatus.HEALTH_INFORMATION_ON_REQUEST_SUCCESS);
      } else if (Objects.nonNull(response.getBody())
          && Objects.nonNull(response.getBody().getError())) {
        requestLogV3Service.saveHealthInformationRequest(
            hipHealthInformationRequest, RequestStatus.HEALTH_INFORMATION_ON_REQUEST_ERROR);
      }
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception ex) {
      String error =
          "An unknown error occurred while calling Gateway API: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
    }
  }

  /**
   * Requesting HIP for FHIR bundle
   *
   * @param hipHealthInformationRequest use the requestId to fetch the careContexts from dump to
   *     request HIP.
   */
  private HealthInformationBundleResponse fetchHealthInformationBundle(
      HIPHealthInformationRequest hipHealthInformationRequest, HttpHeaders headers)
      throws IllegalDataStateException {
    ConsentCareContextMapping existingConsentLog =
        consentCareContextsService.findMappingByConsentId(
            hipHealthInformationRequest.getHiRequest().getConsent().getId());
    RequestLog existingLog =
        requestLogV3Service.findByConsentId(
            hipHealthInformationRequest.getHiRequest().getConsent().getId(),
            GatewayConstants.HIP,
            headers.getFirst(GatewayConstants.X_HIP_ID));
    if (existingLog == null) {
      throw new IllegalDataStateException("Request log not found for consent id");
    }
    HIPNotifyRequest hipNotifyRequest =
        (HIPNotifyRequest) existingLog.getRequestDetails().get(FieldIdentifiers.HIP_NOTIFY_REQUEST);
    String hipId = hipNotifyRequest.getNotification().getConsentDetail().getHip().getId();
    if (existingConsentLog == null) {
      throw new IllegalDataStateException("consent id not found in db");
    }
    HealthInformationBundleRequest healthInformationBundleRequest =
        HealthInformationBundleRequest.builder()
            .hipId(hipId)
            .careContextsWithPatientReferences(existingConsentLog.getCareContexts())
            .build();
    requestLogV3Service.updateConsentStatus(
        existingConsentLog.getConsentId(), null, RequestStatus.FETCHING_BUNDLE);
    log.debug(
        "Health information bundle request HIP : " + healthInformationBundleRequest.toString());
    return hipClient.healthInformationBundleRequest(healthInformationBundleRequest).getBody();
  }

  /**
   * Encrypt the bundle and POST to /dataPushUrl of HIU
   *
   * @param healthInformationBundleResponse FHIR bundle received from HIP for the particular
   *     patients
   */
  private List<ResponseEntity<GenericResponse>> pushHealthInformation(
      HealthInformationBundleResponse healthInformationBundleResponse,
      String consentId,
      HttpHeaders headers) {
    try {
      log.debug("HealthInformationBundle : " + healthInformationBundleResponse);
      RequestLog requestLog =
          requestLogV3Service.findByConsentId(
              consentId, GatewayConstants.HIP, headers.getFirst(GatewayConstants.X_HIP_ID));

      HIPNotifyRequest hipNotifyRequest =
          (HIPNotifyRequest)
              requestLog.getRequestDetails().get(FieldIdentifiers.HIP_NOTIFY_REQUEST);

      HIPHealthInformationRequest hipHealthInformationRequest =
          (HIPHealthInformationRequest)
              requestLog.getRequestDetails().get(FieldIdentifiers.HEALTH_INFORMATION_REQUEST);
      List<HealthInformationPushRequest> healthInformationPushRequestList =
          fetchHealthInformationPushRequest(
              hipNotifyRequest, hipHealthInformationRequest, healthInformationBundleResponse);

      log.debug("Health Information push request: " + healthInformationPushRequestList.toString());
      log.info("initiating the dataTransfer to HIU");
      return hiuClient.pushHealthInformation(
          hipHealthInformationRequest.getHiRequest().getDataPushUrl(),
          healthInformationPushRequestList);
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception ex) {
      String error =
          "An unknown error occurred while calling Gateway API: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
    }
    return Collections.singletonList(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
  }

  private List<HealthInformationPushRequest> fetchHealthInformationPushRequest(
      HIPNotifyRequest hipNotifyRequest,
      HIPHealthInformationRequest hipHealthInformationRequest,
      HealthInformationBundleResponse healthInformationBundleResponse)
      throws InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException,
          IllegalDataStateException,
          IllegalDataStateException {
    EncryptionResponse encryptedData =
        encryptionService.encrypt(hipHealthInformationRequest, healthInformationBundleResponse);

    requestLogV3Service.updateConsentStatus(
        hipNotifyRequest.getNotification().getConsentId(), null, RequestStatus.ENCRYPTION_SUCCESS);

    HealthInformationDhPublicKey receiverDhPublicKey =
        hipHealthInformationRequest.getHiRequest().getKeyMaterial().getDhPublicKey();

    HealthInformationDhPublicKey dhPublicKey =
        HealthInformationDhPublicKey.builder()
            .expiry(receiverDhPublicKey.getExpiry())
            .parameters(receiverDhPublicKey.getParameters())
            .keyValue(encryptedData.getKeyToShare())
            .build();

    HealthInformationKeyMaterial keyMaterial =
        HealthInformationKeyMaterial.builder()
            .cryptoAlg(hipHealthInformationRequest.getHiRequest().getKeyMaterial().getCryptoAlg())
            .curve(hipHealthInformationRequest.getHiRequest().getKeyMaterial().getCurve())
            .dhPublicKey(dhPublicKey)
            .nonce(encryptedData.getSenderNonce())
            .build();
    List<HealthInformationEntry> entries =
        encryptedData.getHealthInformationBundles().stream()
            .map(
                bundle ->
                    HealthInformationEntry.builder()
                        .content(bundle.getBundleContent())
                        .media("application/fhir+json")
                        .checksum("string") // Consider calculating the actual checksum if required
                        .careContextReference(bundle.getCareContextReference())
                        .build())
            .collect(Collectors.toList());
    // Setting up pages in dataPush URL
    int pageSize = 7;
    int totalEntries = entries.size();
    int pageCount = (int) Math.ceil((double) totalEntries / pageSize);

    // Use streams to create paginated requests
    return IntStream.range(0, pageCount)
        .mapToObj(
            pageNumber -> {
              int start = pageNumber * pageSize;
              int end = Math.min(start + pageSize, totalEntries);

              // Create the paginated request
              return HealthInformationPushRequest.builder()
                  .keyMaterial(keyMaterial)
                  .entries(entries.subList(start, end))
                  .pageCount(pageCount)
                  .pageNumber(pageNumber)
                  .transactionId(hipHealthInformationRequest.getTransactionId())
                  .build();
            })
        .collect(Collectors.toList());
  }

  /**
   * After successful dataTransfer we need to send an acknowledgment to ABDM gateway saying
   * "TRANSFERRED"
   *
   * @param hipHealthInformationRequest which has the transactionId used to POST acknowledgement
   * @param consentId to get the careContexts of the patient from requestLogs
   * @param pushHealthInformationResponse
   */
  private void healthInformationPushNotify(
      HIPHealthInformationRequest hipHealthInformationRequest,
      String consentId,
      List<ResponseEntity<GenericResponse>> pushHealthInformationResponse,
      HttpHeaders headers)
      throws IllegalDataStateException {
    boolean allSuccess =
        pushHealthInformationResponse.stream()
            .allMatch(response -> response.getStatusCode().is2xxSuccessful());

    String healthInformationStatus = allSuccess ? "DELIVERED" : "ERRORED";
    String sessionStatus = allSuccess ? "TRANSFERRED" : "FAILED";

    RequestLog existingLog =
        requestLogV3Service.findByConsentId(
            consentId, GatewayConstants.HIP, headers.getFirst(GatewayConstants.X_HIP_ID));
    if (existingLog == null) {
      throw new IllegalDataStateException("Request log not found for consent id: " + consentId);
    }
    HIPNotifyRequest hipNotifyRequest =
        (HIPNotifyRequest) existingLog.getRequestDetails().get(FieldIdentifiers.HIP_NOTIFY_REQUEST);
    List<ConsentCareContexts> listOfCareContexts =
        hipNotifyRequest.getNotification().getConsentDetail().getCareContexts();
    List<HealthInformationStatusResponse> healthInformationStatusResponseList = new ArrayList<>();
    for (ConsentCareContexts item : listOfCareContexts) {
      HealthInformationStatusResponse healthInformationStatusResponse =
          HealthInformationStatusResponse.builder()
              .careContextReference(item.getCareContextReference())
              .hiStatus(healthInformationStatus)
              .description("Done")
              .build();
      healthInformationStatusResponseList.add(healthInformationStatusResponse);
    }
    HealthInformationStatusNotification healthInformationStatusNotification =
        HealthInformationStatusNotification.builder()
            .sessionStatus(sessionStatus)
            .hipId(hipNotifyRequest.getNotification().getConsentDetail().getHip().getId())
            .statusResponses(healthInformationStatusResponseList)
            .build();
    HealthInformationNotifier healthInformationNotifier =
        HealthInformationNotifier.builder()
            .type(GatewayConstants.HIP)
            .id(hipNotifyRequest.getNotification().getConsentDetail().getHip().getId())
            .build();
    HealthInformationNotificationStatus healthInformationNotificationStatus =
        HealthInformationNotificationStatus.builder()
            .consentId(hipNotifyRequest.getNotification().getConsentId())
            .transactionId(hipHealthInformationRequest.getTransactionId())
            .doneAt(Utils.getCurrentTimeStamp())
            .notifier(healthInformationNotifier)
            .statusNotification(healthInformationStatusNotification)
            .build();
    HealthInformationPushNotification healthInformationPushNotification =
        HealthInformationPushNotification.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp())
            .notification(healthInformationNotificationStatus)
            .build();
    log.info(healthInformationPushNotification.toString());
    if (allSuccess) {
      requestLogV3Service.updateConsentStatus(consentId, null, RequestStatus.DATA_TRANSFER_SUCCESS);
    } else {
      requestLogV3Service.updateConsentStatus(consentId, null, RequestStatus.DATA_TRANSFER_ERROR);
      log.error("Data transfer failed for consent id: " + consentId);
    }
    try {
      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              healthInformationPushNotificationPath,
              healthInformationPushNotification,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIP_ID,
                  headers.getFirst(GatewayConstants.X_HIP_ID),
                  UUID.randomUUID().toString()));
      log.debug(
          healthInformationPushNotificationPath
              + " : healthInformationPushNotify: "
              + response.getStatusCode());
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception ex) {
      String error =
          "An unknown error occurred while calling Gateway API: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
    }
  }
}
