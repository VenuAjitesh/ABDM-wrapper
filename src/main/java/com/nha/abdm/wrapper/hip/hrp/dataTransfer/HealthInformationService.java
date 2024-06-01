/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.dataTransfer;

import com.nha.abdm.wrapper.common.GatewayConstants;
import com.nha.abdm.wrapper.common.RequestManager;
import com.nha.abdm.wrapper.common.Utils;
import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.models.RespRequest;
import com.nha.abdm.wrapper.common.requests.*;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.ErrorResponseV3;
import com.nha.abdm.wrapper.common.responses.ErrorResponseWrapper;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import com.nha.abdm.wrapper.hip.HIPClient;
import com.nha.abdm.wrapper.hip.HIUClient;
import com.nha.abdm.wrapper.hip.hrp.consent.requests.HIPNotifyRequest;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.encryption.EncryptionResponse;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.encryption.EncryptionService;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.HealthInformationBundleRequest;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.HealthInformationBundleResponse;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.HealthInformationPushNotification;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.helpers.HealthInformationBundle;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.helpers.HealthInformationNotifier;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.helpers.HealthInformationRequestStatus;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.LogsRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.PatientRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.ConsentCareContextsService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.ConsentPatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.RequestLogService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.ConsentCareContextMapping;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.ConsentPatient;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.RequestLog;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.ConsentCareContexts;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
public class HealthInformationService implements HealthInformationInterface {
  private static final Logger log = LogManager.getLogger(HealthInformationService.class);

  @Value("${healthInformationOnRequestPath}")
  public String healthInformationOnRequestPath;

  @Value("${healthInformationPushNotificationPath}")
  public String healthInformationPushNotificationPath;

  @Autowired PatientRepo patientRepo;
  @Autowired LogsRepo logsRepo;
  private final RequestManager requestManager;
  private final HIPClient hipClient;
  private final HIUClient hiuClient;
  @Autowired RequestLogService requestLogService;
  @Autowired ConsentPatientService consentPatientService;
  @Autowired EncryptionService encryptionService;
  @Autowired ConsentCareContextsService consentCareContextsService;

  @Autowired
  public HealthInformationService(
      HIPClient hipClient, HIUClient hiuClient, RequestManager requestManager) {
    this.hipClient = hipClient;
    this.requestManager = requestManager;
    this.hiuClient = hiuClient;
  }

  /**
   * POST /on-request as an acknowledgement for agreeing to make dataTransfer to ABDM gateway.
   *
   * @param hipHealthInformationRequest HIU public keys and dataPush URL is provided
   */
  @Override
  public void healthInformation(HIPHealthInformationRequest hipHealthInformationRequest)
      throws IllegalDataStateException, InvalidAlgorithmParameterException,
          NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException,
          InvalidKeyException {
    OnHealthInformationRequest onHealthInformationRequest = null;
    RespRequest responseRequestId =
        RespRequest.builder().requestId(hipHealthInformationRequest.getRequestId()).build();
    String consentId = hipHealthInformationRequest.getHiRequest().getConsent().getId();
    HealthInformationRequestStatus hiRequestStatus =
        HealthInformationRequestStatus.builder()
            .sessionStatus("ACKNOWLEDGED")
            .transactionId(hipHealthInformationRequest.getTransactionId())
            .build();
    // Lookup in consent patient table is good enough as we are saving mapping when we are saving
    // consent in patient table.
    ConsentPatient consentPatient = consentPatientService.findMappingByConsentId(consentId, "HIP");
    String gatewayRequestId = UUID.randomUUID().toString();
    if (Objects.nonNull(consentPatient)) {
      onHealthInformationRequest =
          OnHealthInformationRequest.builder()
              .requestId(gatewayRequestId)
              .timestamp(Utils.getCurrentTimeStamp())
              .hiRequest(hiRequestStatus)
              .resp(responseRequestId)
              .build();
    } else {
      String error = "ConsentId not found in database " + consentId;
      ErrorResponse errorResponse = new ErrorResponse();
      errorResponse.setMessage(error);
      errorResponse.setCode(GatewayConstants.ERROR_CODE);
      log.error(error);
      onHealthInformationRequest =
          OnHealthInformationRequest.builder()
              .requestId(UUID.randomUUID().toString())
              .timestamp(Utils.getCurrentTimeStamp())
              .error(errorResponse)
              .resp(responseRequestId)
              .build();
    }
    log.debug(
        "health information acknowledgment request body : "
            + onHealthInformationRequest.toString());
    // Acknowledge to gateway that health information request has been received.
    healthInformationAcknowledgementRequest(
        hipHealthInformationRequest, onHealthInformationRequest);
    // Prepare health information bundle request which needs to be sent to HIU.
    HealthInformationBundleResponse healthInformationBundleResponse =
        fetchHealthInformationBundle(hipHealthInformationRequest, gatewayRequestId);
    // Push the health information to HIU.
    ResponseEntity<GenericResponse> pushHealthInformationResponse =
        pushHealthInformation(healthInformationBundleResponse, consentId);
    // Notify Gateway that health information was pushed to HIU.
    healthInformationPushNotify(
        hipHealthInformationRequest, consentId, pushHealthInformationResponse);
  }

  private void healthInformationAcknowledgementRequest(
      HIPHealthInformationRequest hipHealthInformationRequest,
      OnHealthInformationRequest onHealthInformationRequest) {
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(
              healthInformationOnRequestPath, onHealthInformationRequest);
      log.debug(healthInformationOnRequestPath + " : dataOnRequest: " + response.getStatusCode());
      if (response.getStatusCode().is2xxSuccessful()) {
        requestLogService.saveHealthInformationRequest(
            hipHealthInformationRequest, RequestStatus.HEALTH_INFORMATION_ON_REQUEST_SUCCESS);
      } else if (Objects.nonNull(response.getBody())
          && Objects.nonNull(response.getBody().getErrorResponse())) {
        requestLogService.saveHealthInformationRequest(
            hipHealthInformationRequest, RequestStatus.HEALTH_INFORMATION_ON_REQUEST_ERROR);
      }
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
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
      HIPHealthInformationRequest hipHealthInformationRequest, String gatewayRequestId)
      throws IllegalDataStateException {
    ConsentCareContextMapping existingLog =
        consentCareContextsService.findMappingByConsentId(
            hipHealthInformationRequest.getHiRequest().getConsent().getId());
    if (existingLog == null) {
      throw new IllegalDataStateException("consent id not found in db");
    }
    HealthInformationBundleRequest healthInformationBundleRequest =
        HealthInformationBundleRequest.builder()
            .careContextsWithPatientReferences(existingLog.getCareContexts())
            .build();
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
  private ResponseEntity<GenericResponse> pushHealthInformation(
      HealthInformationBundleResponse healthInformationBundleResponse, String consentId)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException,
          NoSuchProviderException, InvalidKeyException, IllegalDataStateException {
    try {
      log.debug("HealthInformationBundle : " + healthInformationBundleResponse);
      RequestLog requestLog = requestLogService.findByConsentId(consentId, "HIP");

      HIPNotifyRequest hipNotifyRequest =
              (HIPNotifyRequest) requestLog.getRequestDetails().get(FieldIdentifiers.HIP_NOTIFY_REQUEST);

      HIPHealthInformationRequest hipHealthInformationRequest =
              (HIPHealthInformationRequest)
                      requestLog.getRequestDetails().get(FieldIdentifiers.HEALTH_INFORMATION_REQUEST);
      HealthInformationPushRequest healthInformationPushRequest =
              fetchHealthInformationPushRequest(
                      hipNotifyRequest, hipHealthInformationRequest, healthInformationBundleResponse);

      log.debug("Health Information push request: " + healthInformationPushRequest);
      log.info("initiating the dataTransfer to HIU");
      return hiuClient.pushHealthInformation(
              hipHealthInformationRequest.getHiRequest().getDataPushUrl(), healthInformationPushRequest);
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponseV3 error = ex.getResponseBodyAs(ErrorResponseV3.class);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception ex) {
      String error =
              "An unknown error occurred while calling Gateway API: "
                      + ex.getMessage()
                      + " unwrapped exception: "
                      + Exceptions.unwrap(ex);
      log.debug(error);
    }
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  private HealthInformationPushRequest fetchHealthInformationPushRequest(
      HIPNotifyRequest hipNotifyRequest,
      HIPHealthInformationRequest hipHealthInformationRequest,
      HealthInformationBundleResponse healthInformationBundleResponse)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeySpecException,
          NoSuchProviderException, InvalidKeyException, IllegalDataStateException {
    EncryptionResponse encryptedData =
        encryptionService.encrypt(hipHealthInformationRequest, healthInformationBundleResponse);

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
    List<HealthInformationEntry> entries = new ArrayList<>();
    for (HealthInformationBundle healthInformationBundle :
        encryptedData.getHealthInformationBundles()) {
      HealthInformationEntry healthInformationEntry =
          HealthInformationEntry.builder()
              .content(healthInformationBundle.getBundleContent())
              .media("application/fhir+json")
              .checksum("string")
              .careContextReference(healthInformationBundle.getCareContextReference())
              .build();
      entries.add(healthInformationEntry);
    }
    return HealthInformationPushRequest.builder()
        .keyMaterial(keyMaterial)
        .entries(entries)
        .pageCount(1)
        .pageNumber(0)
        .transactionId(hipHealthInformationRequest.getTransactionId())
        .build();
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
      ResponseEntity<GenericResponse> pushHealthInformationResponse) {
    String healthInformationStatus =
        pushHealthInformationResponse.getStatusCode().is2xxSuccessful() ? "DELIVERED" : "ERRORED";
    String sessionStatus =
        pushHealthInformationResponse.getStatusCode().is2xxSuccessful() ? "TRANSFERRED" : "FAILED";
    HIPNotifyRequest hipNotifyRequest =
        (HIPNotifyRequest)
            requestLogService
                .findByConsentId(consentId, "HIP")
                .getRequestDetails()
                .get(FieldIdentifiers.HIP_NOTIFY_REQUEST);
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
            .type("HIP")
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
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(
              healthInformationPushNotificationPath, healthInformationPushNotification);
      log.debug(
          healthInformationPushNotificationPath
              + " : healthInformationPushNotify: "
              + response.getStatusCode());
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
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
