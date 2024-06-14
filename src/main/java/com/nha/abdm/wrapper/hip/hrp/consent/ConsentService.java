/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.consent;

import com.nha.abdm.wrapper.common.RequestManager;
import com.nha.abdm.wrapper.common.Utils;
import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.models.Consent;
import com.nha.abdm.wrapper.common.models.RespRequest;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.ErrorResponseWrapper;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import com.nha.abdm.wrapper.hip.HIPClient;
import com.nha.abdm.wrapper.hip.hrp.consent.requests.HIPNotification;
import com.nha.abdm.wrapper.hip.hrp.consent.requests.HIPNotifyRequest;
import com.nha.abdm.wrapper.hip.hrp.consent.requests.HIPOnNotifyRequest;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.helpers.ConsentAcknowledgement;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.ConsentCareContextsService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.ConsentPatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.PatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.RequestLogService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.RequestStatus;
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
public class ConsentService implements ConsentInterface {
  private static final Logger log = LogManager.getLogger(ConsentService.class);
  private final RequestManager requestManager;
  private final HIPClient hipClient;
  @Autowired RequestLogService requestLogService;
  @Autowired PatientService patientService;
  @Autowired ConsentPatientService consentPatientService;
  @Autowired ConsentCareContextsService consentCareContextsService;

  @Value("${consentOnNotifyPath}")
  private String consentOnNotifyPath;

  public ConsentService(RequestManager requestManager, HIPClient hipClient) {
    this.requestManager = requestManager;
    this.hipClient = hipClient;
  }

  /**
   * The callback from ABDM gateway after consentGrant by the user , POST method for /on-notify as
   * acknowledgement
   *
   * @param hipNotifyRequest careContext and demographics details are provided, and implement a
   *     logic to check the existence of the careContexts.
   */
  public void hipNotify(HIPNotifyRequest hipNotifyRequest) throws IllegalDataStateException {
    HIPOnNotifyRequest hipOnNotifyRequest = null;
    if (hipNotifyRequest != null
        && hipNotifyRequest.getNotification() != null
        && hipNotifyRequest.getNotification().getConsentDetail() != null
        && hipNotifyRequest.getNotification().getConsentDetail().getPatient() != null) {
      HIPNotification hipNotification = hipNotifyRequest.getNotification();

      RespRequest responseRequestId =
          RespRequest.builder().requestId(hipNotifyRequest.getRequestId()).build();
      Consent consent =
          Consent.builder()
              .grantedOn(hipNotifyRequest.getTimestamp())
              .lastUpdatedOn(hipNotifyRequest.getTimestamp())
              .status(hipNotification.getStatus())
              .consentDetail(hipNotification.getConsentDetail())
              .signature(hipNotification.getSignature())
              .build();

      patientService.addConsent(hipNotification.getConsentDetail().getPatient().getId(), consent);
      consentCareContextsService.saveConsentContextsMapping(
          hipNotification.getConsentDetail().getConsentId(),
          consent.getConsentDetail().getCareContexts());
      // Save the consent patient mapping because on health information request gateway doesn't
      // provide the patient abhaAddress
      consentPatientService.saveConsentPatientMapping(
          consent.getConsentDetail().getConsentId(),
          hipNotification.getConsentDetail().getPatient().getId(),
          "HIP");
      log.info(
          "successfully saved consent in consent-patient: "
              + consent.getConsentDetail().getConsentId());
      log.info(
          "Consent in HIP: "
              + consentPatientService.findMappingByConsentId(
                  consent.getConsentDetail().getConsentId(), "HIP"));
      ConsentAcknowledgement dataAcknowledgement =
          ConsentAcknowledgement.builder()
              .status("OK")
              .consentId(hipNotifyRequest.getNotification().getConsentId())
              .build();
      hipOnNotifyRequest =
          HIPOnNotifyRequest.builder()
              .requestId(UUID.randomUUID().toString())
              .timestamp(Utils.getCurrentTimeStamp())
              .acknowledgement(dataAcknowledgement)
              .resp(responseRequestId)
              .build();
      try {
        log.info(hipOnNotifyRequest.toString());
        ResponseEntity<GenericResponse> response =
            requestManager.fetchResponseFromGateway(consentOnNotifyPath, hipOnNotifyRequest);
        log.debug(consentOnNotifyPath + " : consentOnNotify: " + response.getStatusCode());
        if (response.getStatusCode() == HttpStatus.ACCEPTED) {
          requestLogService.dataTransferNotify(
              hipNotifyRequest, RequestStatus.HIP_ON_NOTIFY_SUCCESS, hipOnNotifyRequest);
        } else if (Objects.nonNull(response.getBody())
            && Objects.nonNull(response.getBody().getErrorResponse())) {
          requestLogService.dataTransferNotify(
              hipNotifyRequest, RequestStatus.HIP_ON_NOTIFY_ERROR, hipOnNotifyRequest);
        }
      } catch (WebClientResponseException.BadRequest ex) {
        ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
        log.error("HTTP error {}: {}", ex.getStatusCode(), error);
        requestLogService.dataTransferNotify(
            hipNotifyRequest, RequestStatus.HIP_ON_NOTIFY_ERROR, hipOnNotifyRequest);
      } catch (Exception ex) {
        String error =
            "Exception while Initiating consentOnNotify onNotify: "
                + ex.getMessage()
                + " unwrapped exception: "
                + Exceptions.unwrap(ex);
        log.debug(error);
      }
    }
  }
}
