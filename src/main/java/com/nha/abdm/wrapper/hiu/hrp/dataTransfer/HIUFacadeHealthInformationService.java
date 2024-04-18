/* (C) 2024 */
package com.nha.abdm.wrapper.hiu.hrp.dataTransfer;

import com.nha.abdm.wrapper.common.RequestManager;
import com.nha.abdm.wrapper.common.Utils;
import com.nha.abdm.wrapper.common.cipher.CipherKeyManager;
import com.nha.abdm.wrapper.common.cipher.Key;
import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.models.Consent;
import com.nha.abdm.wrapper.common.requests.*;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.FacadeResponse;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.helpers.HealthInformationBundle;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.LogsRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.ConsentCipherMappingService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.ConsentPatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.PatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.RequestLogService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.ConsentCipherMapping;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.RequestLog;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.DateRange;
import com.nha.abdm.wrapper.hiu.hrp.consent.responses.HealthInformationResponse;
import com.nha.abdm.wrapper.hiu.hrp.dataTransfer.requests.HIUClientHealthInformationRequest;
import com.nha.abdm.wrapper.hiu.hrp.dataTransfer.requests.HIUGatewayHealthInformationRequest;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.*;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class HIUFacadeHealthInformationService implements HIUFacadeHealthInformationInterface {

  private static final Logger log = LogManager.getLogger(HIUFacadeHealthInformationService.class);

  @Value("${healthInformationConsentManagerPath}")
  private String healthInformationConsentManagerPath;

  @Value("${dataPushUrl}")
  private String dataPushUrl;

  private final RequestManager requestManager;
  private final RequestLogService requestLogService;
  private final CipherKeyManager cipherKeyManager;
  private final ConsentCipherMappingService consentCipherMappingService;
  private final LogsRepo logsRepo;
  private final PatientService patientService;
  private final ConsentPatientService consentPatientService;

  private final DecryptionManager decryptionManager;

  @Autowired
  public HIUFacadeHealthInformationService(
      RequestManager requestManager,
      RequestLogService requestLogService,
      CipherKeyManager cipherKeyManager,
      ConsentCipherMappingService consentCipherMappingService,
      LogsRepo logsRepo,
      ConsentPatientService consentPatientService,
      PatientService patientService,
      DecryptionManager decryptionManager) {
    this.requestManager = requestManager;
    this.requestLogService = requestLogService;
    this.cipherKeyManager = cipherKeyManager;
    this.consentCipherMappingService = consentCipherMappingService;
    this.logsRepo = logsRepo;
    this.consentPatientService = consentPatientService;
    this.patientService = patientService;
    this.decryptionManager = decryptionManager;
  }

  @Override
  public FacadeResponse healthInformation(
      HIUClientHealthInformationRequest hiuClientHealthInformationRequest)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException,
          IllegalDataStateException, ParseException {
    if (Objects.isNull(hiuClientHealthInformationRequest)) {
      return FacadeResponse.builder()
          .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .build();
    }
    String consentId = hiuClientHealthInformationRequest.getConsentId();
    // Fetching dateRange from consent present in db.
    Consent consentDetails =
        patientService.getConsentDetails(
            consentPatientService.findMappingByConsentId(consentId, "HIU").getAbhaAddress(),
            consentId);
    if (Objects.isNull(consentDetails)) {
      return FacadeResponse.builder()
          .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
          .httpStatusCode(HttpStatus.NOT_FOUND)
          .error(ErrorResponse.builder().message("ConsentId not found in database").build())
          .build();
    }
    if (!consentDetails.getStatus().equals("GRANTED")) {
      return FacadeResponse.builder()
          .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
          .httpStatusCode(HttpStatus.OK)
          .error(
              ErrorResponse.builder()
                  .message("Consent status : " + consentDetails.getStatus())
                  .build())
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
      return FacadeResponse.builder()
          .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
          .httpStatusCode(HttpStatus.OK)
          .error(ErrorResponse.builder().message("Consent status : EXPIRED").build())
          .build();
    }
    HIUGatewayHealthInformationRequest hiuGatewayHealthInformationRequest =
        getHiuGatewayHealthInformationRequest(hiuClientHealthInformationRequest, consentDetails);
    ResponseEntity<GenericResponse> response =
        requestManager.fetchResponseFromGateway(
            healthInformationConsentManagerPath, hiuGatewayHealthInformationRequest);
    if (response.getStatusCode().is2xxSuccessful()) {
      requestLogService.saveHIUHealthInformationRequest(
          hiuGatewayHealthInformationRequest.getRequestId(),
          hiuGatewayHealthInformationRequest.getHiRequest().getConsent().getId(),
          RequestStatus.HEALTH_INFORMATION_REQUEST_SUCCESS,
          null);
      return FacadeResponse.builder()
          .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
          .httpStatusCode(HttpStatus.ACCEPTED)
          .build();
    } else {
      String error = "Something went wrong while posting health information request to gateway";
      log.error(error);
      requestLogService.saveHIUHealthInformationRequest(
          hiuGatewayHealthInformationRequest.getRequestId(),
          hiuGatewayHealthInformationRequest.getHiRequest().getConsent().getId(),
          RequestStatus.HEALTH_INFORMATION_REQUEST_ERROR,
          error);
      return FacadeResponse.builder()
          .clientRequestId(hiuClientHealthInformationRequest.getRequestId())
          .error(ErrorResponse.builder().message(error).build())
          .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
          .build();
    }
  }
  // Implementing the status check because, if the status is revoked after /fetch-records,
  // The HIU can still fetch the records from wrapper using requestId.
  // So returning the bundle only when the status is GRANTED.
  @Override
  public HealthInformationResponse getHealthInformation(String requestId)
      throws IllegalDataStateException, InvalidCipherTextException, NoSuchAlgorithmException,
          InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
    RequestLog requestLog = logsRepo.findByClientRequestId(requestId);
    if (Objects.isNull(requestLog)) {
      throw new IllegalDataStateException("Request no found for request id: " + requestId);
    }
    String abhaAddress =
        consentPatientService
            .findMappingByConsentId(requestLog.getConsentId(), "HIU")
            .getAbhaAddress();
    String consentStatus = null;
    if (abhaAddress != null) {
      consentStatus =
          patientService.getConsentDetails(abhaAddress, requestLog.getConsentId()).getStatus();
    }
    if (abhaAddress != null && consentStatus != null && consentStatus.equalsIgnoreCase("GRANTED")) {

      Map<String, Object> responseDetails = requestLog.getResponseDetails();
      if (Objects.isNull(responseDetails)
          || Objects.isNull(responseDetails.get(FieldIdentifiers.ENCRYPTED_HEALTH_INFORMATION))) {
        return HealthInformationResponse.builder().status(requestLog.getStatus()).build();
      }
      HealthInformationPushRequest healthInformationPushRequest =
          (HealthInformationPushRequest)
              responseDetails.get(FieldIdentifiers.ENCRYPTED_HEALTH_INFORMATION);
      List<HealthInformationBundle> decryptedHealthInformationEntries =
          getDecryptedHealthInformation(healthInformationPushRequest);
      return HealthInformationResponse.builder()
          .status(requestLog.getStatus())
          .httpStatusCode(HttpStatus.OK)
          .decryptedHealthInformationEntries(decryptedHealthInformationEntries)
          .build();
    } else {
      return HealthInformationResponse.builder()
          .status(requestLog.getStatus())
          .httpStatusCode(HttpStatus.OK)
          .error("Consent status : " + consentStatus)
          .build();
    }
  }

  private HIUGatewayHealthInformationRequest getHiuGatewayHealthInformationRequest(
      HIUClientHealthInformationRequest hiuClientHealthInformationRequest, Consent consent)
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

  private List<HealthInformationBundle> getDecryptedHealthInformation(
      HealthInformationPushRequest healthInformationPushRequest)
      throws IllegalDataStateException, InvalidCipherTextException, NoSuchAlgorithmException,
          InvalidKeySpecException, NoSuchProviderException, InvalidKeyException {
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
    List<HealthInformationBundle> decryptedHealthInformationEntries = new ArrayList<>();
    for (HealthInformationEntry healthInformationEntry : healthInformationEntries) {
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
    }
    return decryptedHealthInformationEntries;
  }
}
