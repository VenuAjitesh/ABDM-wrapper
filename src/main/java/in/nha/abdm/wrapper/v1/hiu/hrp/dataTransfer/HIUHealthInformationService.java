/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.RequestManager;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.Consent;
import in.nha.abdm.wrapper.v1.common.models.ConsentDetail;
import in.nha.abdm.wrapper.v1.common.requests.*;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.HealthInformationPushNotification;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers.HealthInformationNotifier;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.ConsentCipherMappingService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.ConsentPatientService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.RequestLogService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentCipherMapping;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class HIUHealthInformationService implements HealthInformationInterface {

  private static final Logger log = LogManager.getLogger(HIUHealthInformationService.class);

  @Value("${healthInformationPushNotificationPath}")
  public String healthInformationPushNotificationPath;

  private final RequestLogService requestLogService;
  private final RequestManager requestManager;
  private final ConsentCipherMappingService consentCipherMappingService;
  private final ConsentPatientService consentPatientService;
  private final PatientRepo patientRepo;

  @Autowired
  public HIUHealthInformationService(
      RequestLogService requestLogService,
      RequestManager requestManager,
      ConsentCipherMappingService consentCipherMappingService,
      ConsentPatientService consentPatientService,
      PatientRepo patientRepo) {
    this.requestLogService = requestLogService;
    this.requestManager = requestManager;
    this.consentCipherMappingService = consentCipherMappingService;
    this.consentPatientService = consentPatientService;
    this.patientRepo = patientRepo;
  }

  @Override
  public GenericResponse processEncryptedHealthInformation(
      HealthInformationPushRequest healthInformationPushRequest) throws IllegalDataStateException {
    if (Objects.isNull(healthInformationPushRequest)
        || Objects.isNull(healthInformationPushRequest.getEntries())) {
      return GenericResponse.builder().httpStatus(HttpStatus.BAD_REQUEST).build();
    }
    String transactionId = healthInformationPushRequest.getTransactionId();
    HealthInformationKeyMaterial keyMaterial = healthInformationPushRequest.getKeyMaterial();
    if (StringUtils.isEmpty(transactionId)) {
      return GenericResponse.builder()
          .httpStatus(HttpStatus.BAD_REQUEST)
          .errorResponse(ErrorResponse.builder().message("Invalid transaction id").build())
          .build();
    }
    if (Objects.isNull(keyMaterial)
        || Objects.isNull(keyMaterial.getNonce())
        || Objects.isNull(keyMaterial.getDhPublicKey())
        || StringUtils.isEmpty(keyMaterial.getDhPublicKey().getKeyValue())) {
      return GenericResponse.builder()
          .httpStatus(HttpStatus.BAD_REQUEST)
          .errorResponse(ErrorResponse.builder().message("Invalid key material").build())
          .build();
    }
    // TODO: Expiry check, and also should it be done on HIP side?.
    RequestLog requestLog = requestLogService.findRequestLogByTransactionId(transactionId);
    if (Objects.isNull(requestLog)) {
      return GenericResponse.builder()
          .httpStatus(HttpStatus.BAD_REQUEST)
          .errorResponse(ErrorResponse.builder().message("Transaction id not found").build())
          .build();
    }

    GenericResponse genericResponse =
        requestLogService.saveEncryptedHealthInformation(
            healthInformationPushRequest, RequestStatus.ENCRYPTED_HEALTH_INFORMATION_RECEIVED);
    notifyGateway(healthInformationPushRequest, genericResponse);

    return genericResponse;
  }

  private void notifyGateway(
      HealthInformationPushRequest healthInformationPushRequest, GenericResponse genericResponse)
      throws IllegalDataStateException {

    ConsentDetail consentDetail = getConsentDetails(healthInformationPushRequest);

    String hiStatus = Objects.nonNull(genericResponse.getErrorResponse()) ? "OK" : "ERRORED";
    String sessionStatus =
        Objects.nonNull(genericResponse.getErrorResponse()) ? "TRANSFERRED" : "FAILED";
    List<HealthInformationEntry> healthInformationEntries =
        healthInformationPushRequest.getEntries();
    List<HealthInformationStatusResponse> healthInformationStatusResponseList = new ArrayList<>();
    for (HealthInformationEntry healthInformationEntry : healthInformationEntries) {
      HealthInformationStatusResponse healthInformationStatusResponse =
          HealthInformationStatusResponse.builder()
              .careContextReference(healthInformationEntry.getCareContextReference())
              .hiStatus(hiStatus)
              .description("Done")
              .build();
      healthInformationStatusResponseList.add(healthInformationStatusResponse);
    }
    HealthInformationStatusNotification healthInformationStatusNotification =
        HealthInformationStatusNotification.builder()
            .sessionStatus(sessionStatus)
            .hipId(consentDetail.getHip().getId())
            .statusResponses(healthInformationStatusResponseList)
            .build();
    HealthInformationNotifier healthInformationNotifier =
        HealthInformationNotifier.builder()
            .type("HIU")
            .id("")
            .build(); // TODO: It should be consentDetail.getHiu().getId() but this is coming as
    // null to hip notify, this needs to be investigated
    HealthInformationNotificationStatus healthInformationNotificationStatus =
        HealthInformationNotificationStatus.builder()
            .consentId(consentDetail.getConsentId())
            .transactionId(healthInformationPushRequest.getTransactionId())
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
    ResponseEntity<GenericResponse> response =
        requestManager.fetchResponseFromGateway(
            healthInformationPushNotificationPath, healthInformationPushNotification);
    log.debug(
        healthInformationPushNotificationPath
            + " : healthInformationPushNotify: "
            + response.getStatusCode());
  }

  private ConsentDetail getConsentDetails(HealthInformationPushRequest healthInformationPushRequest)
      throws IllegalDataStateException {
    String transactionId = healthInformationPushRequest.getTransactionId();
    ConsentCipherMapping consentCipherMapping =
        consentCipherMappingService.getConsentCipherMapping(transactionId);
    if (Objects.isNull(consentCipherMapping)
        || StringUtils.isEmpty(consentCipherMapping.getConsentId())) {
      throw new IllegalDataStateException(
          "Consent Id not found for transaction id: " + transactionId);
    }
    String consentId = consentCipherMapping.getConsentId();
    ConsentPatient consentPatient = consentPatientService.findMappingByConsentId(consentId, "HIU");
    String patientAbhaAddress = consentPatient.getAbhaAddress();
    if (StringUtils.isEmpty(patientAbhaAddress)) {
      throw new IllegalDataStateException(
          "Patient Abha address not found for consent id: " + consentId);
    }
    Patient patient = patientRepo.findByAbhaAddress(patientAbhaAddress);
    if (Objects.isNull(patient)) {
      throw new IllegalDataStateException(
          "Patient not found for abha address: " + patientAbhaAddress);
    }
    List<Consent> consents = patient.getConsents();
    if (CollectionUtils.isEmpty(consents)) {
      throw new IllegalDataStateException("Consent not found : " + consentId);
    }
    Optional<Consent> consent =
        consents.stream()
            .filter(x -> consentId.equals(x.getConsentDetail().getConsentId()))
            .findAny();
    if (consent.isEmpty()) {
      throw new IllegalDataStateException("Consent not found : " + consentId);
    }
    return consent.get().getConsentDetail();
  }
}
