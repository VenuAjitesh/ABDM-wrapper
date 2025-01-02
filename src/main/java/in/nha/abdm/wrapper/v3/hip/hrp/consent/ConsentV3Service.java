/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.consent;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.Consent;
import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPNotification;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPNotifyRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPOnNotifyRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers.ConsentAcknowledgement;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.ConsentCareContextsService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.database.mongo.services.ConsentPatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import java.util.Objects;
import java.util.UUID;
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
public class ConsentV3Service implements ConsentV3Interface {
  private static final Logger log = LogManager.getLogger(ConsentV3Service.class);
  private final RequestV3Manager requestV3Manager;
  @Autowired RequestLogV3Service requestLogV3Service;
  @Autowired PatientV3Service patientService;
  @Autowired ConsentPatientV3Service consentPatientService;
  @Autowired ConsentCareContextsService consentCareContextsService;

  @Value("${consentOnNotifyPath}")
  private String consentOnNotifyPath;

  public ConsentV3Service(RequestV3Manager requestV3Manager) {
    this.requestV3Manager = requestV3Manager;
  }

  /**
   * The callback from ABDM gateway after consentGrant by the user , POST method for /on-notify as
   * acknowledgement
   *
   * @param hipNotifyRequest careContext and demographics details are provided, and implement a
   *     logic to check the existence of the careContexts.
   */
  public void hipNotify(HIPNotifyRequest hipNotifyRequest, HttpHeaders headers)
      throws IllegalDataStateException {
    HIPOnNotifyRequest hipOnNotifyRequest = null;
    if (hipNotifyRequest != null
        && hipNotifyRequest.getNotification() != null
        && hipNotifyRequest.getNotification().getConsentDetail() != null
        && hipNotifyRequest.getNotification().getConsentDetail().getPatient() != null) {

      hipOnNotifyRequest = checkAndSaveConsent(hipNotifyRequest, headers);

    } else {
      ErrorResponse error =
          ErrorResponse.builder()
              .message("Invalid hip/notify request")
              .code(GatewayConstants.ERROR_CODE)
              .build();
      hipOnNotifyRequest =
          HIPOnNotifyRequest.builder()
              .error(error)
              .acknowledgement(
                  ConsentAcknowledgement.builder()
                      .consentId(hipNotifyRequest.getNotification().getConsentId())
                      .status("FAILURE")
                      .build())
              .response(
                  RespRequest.builder()
                      .requestId(headers.getFirst(GatewayConstants.REQUEST_ID))
                      .build())
              .build();
    }
    try {
      log.info(hipOnNotifyRequest.toString());
      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              consentOnNotifyPath,
              hipOnNotifyRequest,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIP_ID,
                  headers.getFirst(GatewayConstants.X_HIP_ID),
                  UUID.randomUUID().toString()));
      log.debug(consentOnNotifyPath + " : consentOnNotify: " + response.getStatusCode());
      if (response.getStatusCode() == HttpStatus.ACCEPTED) {
        requestLogV3Service.dataTransferNotify(
            hipNotifyRequest, RequestStatus.HIP_ON_NOTIFY_SUCCESS, hipOnNotifyRequest, headers);
      } else if (Objects.nonNull(response.getBody())
          && Objects.nonNull(response.getBody().getError())) {
        requestLogV3Service.dataTransferNotify(
            hipNotifyRequest, RequestStatus.HIP_ON_NOTIFY_ERROR, hipOnNotifyRequest, headers);
      }
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogV3Service.dataTransferNotify(
          hipNotifyRequest, RequestStatus.HIP_ON_NOTIFY_ERROR, hipOnNotifyRequest, headers);
    } catch (Exception ex) {
      String error =
          "Exception while Initiating consentOnNotify onNotify: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
    }
  }

  public HIPOnNotifyRequest checkAndSaveConsent(
      HIPNotifyRequest hipNotifyRequest, HttpHeaders headers) throws IllegalDataStateException {
    HIPNotification hipNotification = hipNotifyRequest.getNotification();
    RespRequest responseRequestId =
        RespRequest.builder().requestId(headers.getFirst(GatewayConstants.REQUEST_ID)).build();
    if (hipNotification.getStatus().equalsIgnoreCase("REVOKED"))
      return updateConsentStatus(hipNotification, headers);
    if (patientService.isCareContextPresent(
        hipNotification.getConsentDetail().getCareContexts(),
        headers.getFirst(GatewayConstants.X_HIP_ID))) {
      Consent consent =
          Consent.builder()
              .grantedOn(headers.getFirst(GatewayConstants.TIMESTAMP))
              .lastUpdatedOn(headers.getFirst(GatewayConstants.TIMESTAMP))
              .status(hipNotification.getStatus())
              .consentDetail(hipNotification.getConsentDetail())
              .signature(hipNotification.getSignature())
              .build();

      patientService.addConsent(
          hipNotification.getConsentDetail().getPatient().getId(),
          consent,
          headers.getFirst(GatewayConstants.X_HIP_ID));
      consentCareContextsService.saveConsentContextsMapping(
          hipNotification.getConsentDetail().getConsentId(),
          consent.getConsentDetail().getCareContexts());

      // Save the consent patient mapping because on health information request gateway doesn't
      // provide the patient abhaAddress
      consentPatientService.saveConsentPatientMapping(
          consent.getConsentDetail().getConsentId(),
          hipNotification.getConsentDetail().getPatient().getId(),
          GatewayConstants.HIP,
          headers.getFirst(GatewayConstants.X_HIP_ID));
      log.info(
          "successfully saved consent in consent-patient: "
              + consent.getConsentDetail().getConsentId());

    } else {
      ErrorResponse errorResponse = new ErrorResponse();
      errorResponse.setMessage("care contexts provided : Does not match");
      errorResponse.setCode(GatewayConstants.ERROR_CODE);
      log.error("care contexts provided : Does not match");
      ConsentAcknowledgement consentAcknowledgement =
          ConsentAcknowledgement.builder()
              .consentId(hipNotifyRequest.getNotification().getConsentId())
              .status("ERROR")
              .build();
      return HIPOnNotifyRequest.builder()
          .error(errorResponse)
          .acknowledgement(consentAcknowledgement)
          .response(responseRequestId)
          .build();
    }
    ConsentAcknowledgement dataAcknowledgement =
        ConsentAcknowledgement.builder()
            .status("OK")
            .consentId(hipNotifyRequest.getNotification().getConsentId())
            .build();
    return HIPOnNotifyRequest.builder()
        .acknowledgement(dataAcknowledgement)
        .response(responseRequestId)
        .build();
  }

  public HIPOnNotifyRequest updateConsentStatus(
      HIPNotification hipNotification, HttpHeaders headers) throws IllegalDataStateException {
    Consent existingConsent =
        patientService.getConsentDetails(
            hipNotification.getConsentDetail().getPatient().getId(),
            hipNotification.getConsentId(),
            headers.getFirst(GatewayConstants.X_HIP_ID));
    existingConsent.setRevokedOn(headers.getFirst(GatewayConstants.TIMESTAMP));
    existingConsent.setStatus(hipNotification.getStatus());
    existingConsent.setLastUpdatedOn(Utils.getCurrentTimeStamp());
    existingConsent.setConsentDetail(hipNotification.getConsentDetail());
    // Updating the revoked status timeStamp and status
    patientService.addConsent(
        hipNotification.getConsentDetail().getPatient().getId(),
        existingConsent,
        headers.getFirst(GatewayConstants.X_HIP_ID));
    RespRequest responseRequestId =
        RespRequest.builder().requestId(headers.getFirst(GatewayConstants.REQUEST_ID)).build();
    ConsentAcknowledgement dataAcknowledgement =
        ConsentAcknowledgement.builder()
            .status("OK")
            .consentId(hipNotification.getConsentDetail().getConsentId())
            .build();
    return HIPOnNotifyRequest.builder()
        .acknowledgement(dataAcknowledgement)
        .response(responseRequestId)
        .build();
  }
}
