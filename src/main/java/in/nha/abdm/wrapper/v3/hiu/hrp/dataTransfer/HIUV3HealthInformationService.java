/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
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
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentCipherMapping;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.database.mongo.services.ConsentPatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

@Service
public class HIUV3HealthInformationService implements HealthInformationV3Interface {

  private static final Logger log = LogManager.getLogger(HIUV3HealthInformationService.class);

  @Value("${healthInformationPushNotificationPath}")
  public String healthInformationPushNotificationPath;

  private final RequestLogV3Service requestLogV3Service;
  private final RequestV3Manager requestManager;
  private final ConsentCipherMappingService consentCipherMappingService;
  private final ConsentPatientV3Service consentPatientService;
  private final PatientRepo patientRepo;

  @Autowired
  public HIUV3HealthInformationService(
      RequestLogV3Service requestLogService,
      RequestLogV3Service requestLogV3Service,
      RequestManager requestManager,
      RequestV3Manager requestManager1,
      ConsentCipherMappingService consentCipherMappingService,
      ConsentPatientV3Service consentPatientService,
      PatientRepo patientRepo) {
    this.requestLogV3Service = requestLogV3Service;
    this.requestManager = requestManager1;
    this.consentCipherMappingService = consentCipherMappingService;
    this.consentPatientService = consentPatientService;
    this.patientRepo = patientRepo;
  }

  /**
   * Checking and Storing the FHIR bundles
   *
   * @param healthInformationPushRequest
   * @return
   * @throws IllegalDataStateException
   */
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
    RequestLog requestLog = requestLogV3Service.findRequestLogByTransactionId(transactionId);
    if (Objects.isNull(requestLog)) {
      return GenericResponse.builder()
          .httpStatus(HttpStatus.BAD_REQUEST)
          .errorResponse(ErrorResponse.builder().message("Transaction id not found").build())
          .build();
    }

    GenericResponse genericResponse =
        requestLogV3Service.saveEncryptedHealthInformation(
            healthInformationPushRequest, RequestStatus.ENCRYPTED_HEALTH_INFORMATION_RECEIVED);
    notifyGateway(healthInformationPushRequest, genericResponse, requestLog.getHipId());

    return genericResponse;
  }

  /**
   * Notifying ABDM that the data has been received
   *
   * @param healthInformationPushRequest
   * @param genericResponse
   * @param hipId
   * @throws IllegalDataStateException
   */
  private void notifyGateway(
      HealthInformationPushRequest healthInformationPushRequest,
      GenericResponse genericResponse,
      String hipId)
      throws IllegalDataStateException {
    try {
      ConsentDetail consentDetail = getConsentDetails(healthInformationPushRequest, hipId);

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
              .id(consentDetail.getHiu().getId())
              .build();
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
              .notification(healthInformationNotificationStatus)
              .build();
      ResponseEntity<GenericV3Response> response =
          requestManager.fetchResponseFromGateway(
              healthInformationPushNotificationPath,
              healthInformationPushNotification,
              Utils.getCustomHeaders(
                  GatewayConstants.HIU,
                  consentDetail.getHiu().getId(),
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
          "Exception while fetching consent: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
    }
  }

  private ConsentDetail getConsentDetails(
      HealthInformationPushRequest healthInformationPushRequest, String hipId)
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
    ConsentPatient consentPatient =
        consentPatientService.findMappingByConsentId(consentId, GatewayConstants.HIU, hipId);
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
