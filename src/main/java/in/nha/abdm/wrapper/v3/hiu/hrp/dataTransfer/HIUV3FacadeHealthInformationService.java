/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.RequestManager;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.cipher.CipherKeyManager;
import in.nha.abdm.wrapper.v1.common.cipher.Key;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.Consent;
import in.nha.abdm.wrapper.v1.common.requests.*;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers.HealthInformationBundle;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.LogsRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.ConsentCipherMappingService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentCipherMapping;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.DateRange;
import in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer.DecryptionManager;
import in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer.requests.HIUGatewayHealthInformationRequest;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.config.ErrorHandler;
import in.nha.abdm.wrapper.v3.database.mongo.services.ConsentPatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.requests.HIUClientHealthInformationV3Request;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.responses.HealthInformationV3Response;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.ParseException;
import java.util.*;
import org.apache.commons.lang3.time.DateUtils;
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
public class HIUV3FacadeHealthInformationService implements HIUV3FacadeHealthInformationInterface {

  private static final Logger log = LogManager.getLogger(HIUV3FacadeHealthInformationService.class);

  @Value("${healthInformationConsentManagerPath}")
  private String healthInformationConsentManagerPath;

  @Value("${dataPushUrl}")
  private String dataPushUrl;

  private final RequestV3Manager requestV3Manager;
  private final RequestLogV3Service requestLogService;
  private final CipherKeyManager cipherKeyManager;
  private final ConsentCipherMappingService consentCipherMappingService;
  private final LogsRepo logsRepo;
  private final PatientV3Service patientService;
  private final ConsentPatientV3Service consentPatientService;

  private final DecryptionManager decryptionManager;

  @Autowired
  public HIUV3FacadeHealthInformationService(
      RequestManager requestManagerV3,
      RequestLogV3Service requestLogService,
      CipherKeyManager cipherKeyManager,
      ConsentCipherMappingService consentCipherMappingService,
      LogsRepo logsRepo,
      RequestV3Manager requestV31Manager,
      LogsRepo logsRepo1,
      ConsentPatientV3Service consentPatientService,
      PatientV3Service patientService,
      DecryptionManager decryptionManager) {
    this.requestLogService = requestLogService;
    this.cipherKeyManager = cipherKeyManager;
    this.consentCipherMappingService = consentCipherMappingService;
    this.requestV3Manager = requestV31Manager;
    this.logsRepo = logsRepo1;
    this.consentPatientService = consentPatientService;
    this.patientService = patientService;
    this.decryptionManager = decryptionManager;
  }

  @Override
  public FacadeV3Response healthInformation(
      HIUClientHealthInformationV3Request hiuClientHealthInformationRequest)
      throws InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          NoSuchProviderException,
          IllegalDataStateException,
          ParseException {
    try {
      if (Objects.isNull(hiuClientHealthInformationRequest)) {
        return FacadeV3Response.builder()
            .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .build();
      }
      String consentId = hiuClientHealthInformationRequest.getConsentId();
      // Fetching dateRange from consent present in db.

      ConsentPatient consentPatient =
          consentPatientService.findMappingByConsentId(
              consentId, GatewayConstants.HIU, hiuClientHealthInformationRequest.getRequesterId());
      if (Objects.isNull(consentPatient)) {
        return FacadeV3Response.builder()
            .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .errors(
                ErrorHandler.getErrors(
                    ErrorV3Response.builder()
                        .error(
                            ErrorResponse.builder()
                                .code(GatewayConstants.ERROR_CODE)
                                .message("ConsentId not found in database")
                                .build())
                        .build()))
            .build();
      }
      Consent consentDetails =
          patientService.getConsentDetails(
              consentPatient.getAbhaAddress(),
              consentId,
              hiuClientHealthInformationRequest.getRequesterId());
      if (Objects.isNull(consentDetails)) {
        return FacadeV3Response.builder()
            .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .errors(
                Collections.singletonList(
                    ErrorV3Response.builder()
                        .error(
                            ErrorResponse.builder()
                                .code(GatewayConstants.ERROR_CODE)
                                .message("ConsentId not found in database")
                                .build())
                        .build()))
            .build();
      }
      if (!consentDetails.getStatus().equals("GRANTED")) {
        return FacadeV3Response.builder()
            .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
            .httpStatusCode(HttpStatus.OK)
            .errors(
                Collections.singletonList(
                    ErrorV3Response.builder()
                        .error(
                            ErrorResponse.builder()
                                .code(GatewayConstants.ERROR_CODE)
                                .message("Consent status : " + consentDetails.getStatus())
                                .build())
                        .build()))
            .build();
      }
      String dataExpired = consentDetails.getConsentDetail().getPermission().getDataEraseAt();
      Date expiredDate =
          DateUtils.parseDateStrictly(
              dataExpired,
              new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss'Z'"
              });
      if (expiredDate.compareTo(new Date()) < 0) {
        return FacadeV3Response.builder()
            .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
            .httpStatusCode(HttpStatus.OK)
            .errors(
                Collections.singletonList(
                    ErrorV3Response.builder()
                        .error(
                            ErrorResponse.builder()
                                .code(GatewayConstants.ERROR_CODE)
                                .message("Consent status : EXPIRED")
                                .build())
                        .build()))
            .build();
      }
      HIUGatewayHealthInformationRequest hiuGatewayHealthInformationRequest =
          getHiuGatewayHealthInformationRequest(hiuClientHealthInformationRequest, consentDetails);
      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              healthInformationConsentManagerPath,
              hiuGatewayHealthInformationRequest,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIU_ID,
                  consentDetails.getConsentDetail().getHiu().getId(),
                  hiuClientHealthInformationRequest.getRequestId()));
      if (response.getStatusCode().is2xxSuccessful()) {
        requestLogService.saveHIUHealthInformationRequest(
            consentPatient.getAbhaAddress(),
            hiuClientHealthInformationRequest.getRequesterId(),
            hiuGatewayHealthInformationRequest.getRequestId(),
            hiuGatewayHealthInformationRequest.getHiRequest().getConsent().getId(),
            RequestStatus.HEALTH_INFORMATION_REQUEST_SUCCESS,
            null);
        return FacadeV3Response.builder()
            .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
            .httpStatusCode(HttpStatus.ACCEPTED)
            .build();
      } else {
        String error = "Something went wrong while posting health information request to gateway";
        log.error(error);
        requestLogService.saveHIUHealthInformationRequest(
            consentPatient.getAbhaAddress(),
            hiuClientHealthInformationRequest.getRequesterId(),
            hiuGatewayHealthInformationRequest.getRequestId(),
            hiuGatewayHealthInformationRequest.getHiRequest().getConsent().getId(),
            RequestStatus.HEALTH_INFORMATION_REQUEST_ERROR,
            error);
        return FacadeV3Response.builder()
            .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
            .errors(
                Collections.singletonList(
                    ErrorV3Response.builder()
                        .error(
                            ErrorResponse.builder()
                                .code(GatewayConstants.ERROR_CODE)
                                .message(error)
                                .build())
                        .build()))
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .build();
      }
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      return FacadeV3Response.builder()
          .errors(ErrorHandler.getErrors(error))
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .build();
    } catch (Exception ex) {
      String error =
          "Exception while fetching consent: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
      return FacadeV3Response.builder()
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

  // Implementing the status check because, if the status is revoked after /fetch-records,
  // The HIU can still fetch the records from wrapper using requestId.
  // So returning the bundle only when the status is GRANTED.
  @Override
  public HealthInformationV3Response getHealthInformation(String requestId)
      throws IllegalDataStateException {
    RequestLog RequestLog = logsRepo.findByClientRequestId(requestId);
    if (Objects.isNull(RequestLog)) {
      throw new IllegalDataStateException("Request not found for request id: " + requestId);
    }
    String abhaAddress =
        consentPatientService
            .findMappingByConsentId(
                RequestLog.getConsentId(), GatewayConstants.HIU, RequestLog.getHipId())
            .getAbhaAddress();
    String consentStatus = null;
    if (abhaAddress != null) {
      consentStatus =
          patientService
              .getConsentDetails(abhaAddress, RequestLog.getConsentId(), RequestLog.getHipId())
              .getStatus();
    }
    if (abhaAddress != null && consentStatus != null && consentStatus.equalsIgnoreCase("GRANTED")) {

      Map<String, Object> responseDetails = RequestLog.getResponseDetails();
      if (Objects.isNull(responseDetails)
          || Objects.isNull(responseDetails.get(FieldIdentifiers.ENCRYPTED_HEALTH_INFORMATION))) {
        return HealthInformationV3Response.builder().status(RequestLog.getStatus()).build();
      }
      try {
        List<HealthInformationPushRequest> healthInformationPushRequest =
            (List<HealthInformationPushRequest>)
                responseDetails.get(FieldIdentifiers.ENCRYPTED_HEALTH_INFORMATION);
        List<HealthInformationBundle> decryptedHealthInformationEntries =
            getDecryptedHealthInformation(healthInformationPushRequest);
        return HealthInformationV3Response.builder()
            .status(RequestStatus.ENCRYPTED_HEALTH_INFORMATION_RECEIVED)
            .httpStatusCode(HttpStatus.OK)
            .decryptedHealthInformationEntries(decryptedHealthInformationEntries)
            .build();
      } catch (Exception e) {
        log.error(e.getMessage());
        return HealthInformationV3Response.builder()
            .status(RequestStatus.DECRYPTION_ERROR)
            .httpStatusCode(HttpStatus.UNPROCESSABLE_ENTITY)
            .errors(
                Collections.singletonList(
                    ErrorV3Response.builder()
                        .error(
                            ErrorResponse.builder()
                                .code(GatewayConstants.ERROR_CODE)
                                .message("Unable to decrypt the data sent by HIP")
                                .build())
                        .build()))
            .build();
      }
    } else {
      return HealthInformationV3Response.builder()
          .status(RequestLog.getStatus())
          .httpStatusCode(HttpStatus.OK)
          .errors(
              Collections.singletonList(
                  ErrorV3Response.builder()
                      .error(
                          ErrorResponse.builder()
                              .code(GatewayConstants.ERROR_CODE)
                              .message("Consent status : " + consentStatus)
                              .build())
                      .build()))
          .build();
    }
  }

  /**
   * Fetching the consent key material for further operation like sending to HIP and decryption
   *
   * @param hiuClientHealthInformationRequest
   * @param consent
   * @return
   */
  private HIUGatewayHealthInformationRequest getHiuGatewayHealthInformationRequest(
      HIUClientHealthInformationV3Request hiuClientHealthInformationRequest, Consent consent)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    Key key = cipherKeyManager.fetchKeys();
    HealthInformationDhPublicKey healthInformationDhPublicKey =
        HealthInformationDhPublicKey.builder()
            .expiry(consent.getConsentDetail().getPermission().getDataEraseAt())
            .parameters(CipherKeyManager.PARAMETERS)
            .keyValue(key.getPublicKey())
            .build();
    HealthInformationKeyMaterial healthInformationKeyMaterial =
        HealthInformationKeyMaterial.builder()
            .cryptoAlg(CipherKeyManager.ALGORITHM)
            .curve(CipherKeyManager.CURVE)
            .dhPublicKey(healthInformationDhPublicKey)
            .nonce(key.getNonce())
            .build();
    HealthInformationRequest healthInformationRequest =
        HealthInformationRequest.builder()
            .consent(
                IdRequest.builder().id(hiuClientHealthInformationRequest.getConsentId()).build())
            .dateRange(
                DateRange.builder()
                    .from(consent.getConsentDetail().getPermission().getDateRange().getFrom())
                    .to(consent.getConsentDetail().getPermission().getDateRange().getTo())
                    .build())
            .dataPushUrl(dataPushUrl)
            .keyMaterial(healthInformationKeyMaterial)
            .build();
    consentCipherMappingService.saveConsentPrivateKeyMapping(
        hiuClientHealthInformationRequest.getConsentId(), key.getPrivateKey(), key.getNonce());
    return HIUGatewayHealthInformationRequest.builder()
        .requestId(hiuClientHealthInformationRequest.getRequestId())
        .timestamp(Utils.getCurrentTimeStamp())
        .hiRequest(healthInformationRequest)
        .build();
  }

  /**
   * Processing the received FHIR bundles
   *
   * @param healthInformationPushRequestList
   * @return
   */
  private List<HealthInformationBundle> getDecryptedHealthInformation(
      List<HealthInformationPushRequest> healthInformationPushRequestList)
      throws IllegalDataStateException {
    List<HealthInformationBundle> decryptedHealthInformationEntries = new ArrayList<>();
    for (HealthInformationPushRequest healthInformationPushRequest :
        healthInformationPushRequestList) {
      String hipPublicKey =
          healthInformationPushRequest.getKeyMaterial().getDhPublicKey().getKeyValue();
      String hipNonce = healthInformationPushRequest.getKeyMaterial().getNonce();
      String transactionId = healthInformationPushRequest.getTransactionId();
      ConsentCipherMapping consentCipherMapping =
          consentCipherMappingService.getConsentCipherMapping(transactionId);
      if (Objects.isNull(consentCipherMapping)) {
        throw new IllegalDataStateException(
            "Cipher keys not found in HIU Wrapper database for transactionId: " + transactionId);
      }
      String hiuPrivateKey = consentCipherMapping.getPrivateKey();
      String hiuNonce = consentCipherMapping.getNonce();
      List<HealthInformationEntry> healthInformationEntries =
          healthInformationPushRequest.getEntries();
      for (HealthInformationEntry healthInformationEntry : healthInformationEntries) {
        try {
          decryptedHealthInformationEntries.add(
              HealthInformationBundle.builder()
                  .bundleContent(
                      decryptionManager.decryptedHealthInformation(
                          hipNonce,
                          hiuNonce,
                          hiuPrivateKey,
                          hipPublicKey,
                          healthInformationEntry.getContent()))
                  .careContextReference(healthInformationEntry.getCareContextReference())
                  .build());
        } catch (Exception e) {
          log.error(e.getMessage());
          decryptedHealthInformationEntries.add(
              HealthInformationBundle.builder()
                  .bundleContent("ERROR: " + e.getMessage())
                  .careContextReference(healthInformationEntry.getCareContextReference())
                  .build());
        }
      }
    }
    return decryptedHealthInformationEntries;
  }
}
