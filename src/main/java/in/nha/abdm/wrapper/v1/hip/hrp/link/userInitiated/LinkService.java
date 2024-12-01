/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated;

import in.nha.abdm.wrapper.v1.common.RequestManager;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.models.VerifyOTP;
import in.nha.abdm.wrapper.v1.common.requests.RequestOtp;
import in.nha.abdm.wrapper.v1.common.responses.*;
import in.nha.abdm.wrapper.v1.hip.HIPClient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.PatientService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.RequestLogService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.requests.*;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.ConfirmResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.InitResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

@Service
public class LinkService implements LinkInterface {

  @Autowired PatientRepo patientRepo;
  private final RequestManager requestManager;
  @Autowired RequestLogService requestLogService;
  @Autowired PatientService patientService;
  private final HIPClient hipClient;

  @Value("${onInitLinkPath}")
  public String onInitLinkPath;

  @Value("${onConfirmLinkPath}")
  public String onConfirmLinkPath;

  @Value("${requestOtp}")
  public String requestOtp;

  @Value("${verifyOtpPath}")
  public String verifyOtpPath;

  @Autowired
  public LinkService(RequestManager requestManager, HIPClient hipClient) {
    this.requestManager = requestManager;
    this.hipClient = hipClient;
  }

  private static final Logger log = LogManager.getLogger(LinkService.class);

  /**
   * <B>userInitiatedLinking</B>
   *
   * <p>The Response has list of careContext.<br>
   * 1) HIP needs to send the OTP to respective user.<br>
   * 2) build the onInitRequest body with OTP expiry.<br>
   * 3) POST Method to "/link/on-init"
   *
   * @param initResponse Response from ABDM gateway for linking of careContexts.
   */
  @Override
  public void onInit(InitResponse initResponse) {
    OnInitRequest onInitRequest;
    String linkReferenceNumber = UUID.randomUUID().toString();
    String requestId = UUID.randomUUID().toString();

    OnInitLinkMeta onInitLinkMeta =
        OnInitLinkMeta.builder()
            .communicationMedium("MOBILE")
            .communicationHint("string")
            .communicationExpiry(Utils.getSmsExpiry())
            .build();

    OnInitLink onInitLink =
        OnInitLink.builder()
            .referenceNumber(linkReferenceNumber)
            .authenticationType("DIRECT")
            .meta(onInitLinkMeta)
            .build();

    onInitRequest =
        OnInitRequest.builder()
            .requestId(requestId)
            .timestamp(Utils.getCurrentTimeStamp())
            .transactionId(initResponse.getTransactionId())
            .link(onInitLink)
            .resp(RespRequest.builder().requestId(initResponse.getRequestId()).build())
            .build();

    log.info("onInit body : " + onInitRequest.toString());
    try {
      log.info("Sending otp request to HIP");
      RequestOtp requestOtp =
          RequestOtp.builder()
              .abhaAddress(initResponse.getPatient().getId())
              .patientReference(initResponse.getPatient().getReferenceNumber())
              .build();
      ResponseEntity<ResponseOtp> hipResponse = hipClient.requestOtp(this.requestOtp, requestOtp);
      log.info(this.requestOtp + " : requestOtp: " + hipResponse.getStatusCode());

      if (Objects.requireNonNull(hipResponse.getBody()).getStatus().equalsIgnoreCase("SUCCESS")
          || Objects.isNull(hipResponse.getBody().getError())) {
        onInitRequest.getLink().setReferenceNumber(hipResponse.getBody().getLinkRefNumber());
        ResponseEntity<GenericResponse> responseEntity =
            requestManager.fetchResponseFromGateway(onInitLinkPath, onInitRequest);
        log.info(onInitLinkPath + " : onInitCall: " + responseEntity.getStatusCode());

      } else {
        onInitRequest.setError(
            ErrorResponse.builder()
                .code("1000")
                .message(
                    Objects.nonNull(hipResponse.getBody().getError())
                        ? hipResponse.getBody().getError().getMessage()
                        : "Unable to send SMS")
                .build());
        ResponseEntity<GenericResponse> responseEntity =
            requestManager.fetchResponseFromGateway(onInitLinkPath, onInitRequest);
        log.info(onInitLinkPath + " : onInitCall: " + responseEntity.getStatusCode());
      }
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorV3Response.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception e) {
      log.info(onInitLinkPath + " : OnInitCall -> Error : " + Arrays.toString(e.getStackTrace()));
    }
    try {
      requestLogService.setLinkResponse(
          initResponse, requestId, onInitRequest.getLink().getReferenceNumber());
    } catch (Exception e) {
      log.info("onInitCall -> Error: unable to set content : " + Exceptions.unwrap(e));
    }
  }

  /**
   * <B>userInitiatedLinking</B>
   *
   * <p>The confirmResponse has the OTP entered by the user for authentication.<br>
   * 1) Validate the OTP and send the response of careContexts or error. <br>
   * 2) build the request body of OnConfirmRequest.<br>
   * 3) POST method to link/on-confirm
   *
   * @param confirmResponse Response from ABDM gateway with OTP entered by user.
   */
  @Override
  public void onConfirm(ConfirmResponse confirmResponse) {
    String display = null;
    String linkRefNumber = confirmResponse.getConfirmation().getLinkRefNumber();

    String abhaAddress = requestLogService.getPatientId(linkRefNumber);
    String patientReference = requestLogService.getPatientReference(linkRefNumber);
    Patient patientWithAbha = patientRepo.findByAbhaAddress(abhaAddress);
    // Commenting patientReference because of redundant query since we have the same
    // patientReference in requestLogs
    //    Patient patientWithPatientRef = patientRepo.findByPatientReference(patientReference);

    if (patientWithAbha != null) {
      display = patientWithAbha.getPatientDisplay();
    } else if (patientReference != null) {
      display = patientReference;
    }
    log.info("onConfirm Abha address is: " + abhaAddress);
    if (abhaAddress == null) {
      log.info("OnConfirmCall -> patient with abhaAddress not found in logs.");
    }
    List<CareContext> careContexts =
        requestLogService.getSelectedCareContexts(abhaAddress, linkRefNumber);
    log.info(careContexts);
    OnConfirmPatient onConfirmPatient = null;
    ResponseEntity<RequestStatusResponse> hipResponse = null;
    OnConfirmRequest onConfirmRequest = null;
    if (careContexts == null) {
      onConfirmRequest =
          OnConfirmRequest.builder()
              .requestId(UUID.randomUUID().toString())
              .timestamp(Utils.getCurrentTimeStamp())
              .error(
                  ErrorResponse.builder()
                      .code("1000")
                      .message("CareContexts doesn't match")
                      .build())
              .resp(RespRequest.builder().requestId(confirmResponse.getRequestId()).build())
              .build();
    } else {
      try {
        log.info("Requesting HIP for verify otp in discovery");
        VerifyOTP verifyOTP =
            VerifyOTP.builder()
                .authCode(confirmResponse.getConfirmation().getToken())
                .loginHint("Discovery OTP request")
                .linkRefNumber(confirmResponse.getConfirmation().getLinkRefNumber())
                .build();

        hipResponse = hipClient.fetchResponseFromHIP(verifyOtpPath, verifyOTP);
        log.info(verifyOtpPath + " : verifyOtp: " + hipResponse.getStatusCode());
      } catch (Exception e) {
        log.error(verifyOtpPath + " : verifyOtp -> Error :" + Exceptions.unwrap(e));
      }
      String tokenNumber = confirmResponse.getConfirmation().getToken();
      RequestStatusResponse requestStatusResponse = Objects.requireNonNull(hipResponse.getBody());
      if (requestStatusResponse.getError() == null) {
        onConfirmPatient =
            OnConfirmPatient.builder()
                .referenceNumber(patientReference)
                .display(display)
                .careContexts(careContexts)
                .build();
        onConfirmRequest =
            OnConfirmRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .timestamp(Utils.getCurrentTimeStamp())
                .patient(onConfirmPatient)
                .resp(RespRequest.builder().requestId(confirmResponse.getRequestId()).build())
                .build();
        log.info("onConfirm : " + onConfirmRequest.toString());
      } else {
        onConfirmPatient =
            OnConfirmPatient.builder().referenceNumber(patientReference).display(display).build();
        onConfirmRequest =
            OnConfirmRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .timestamp(Utils.getCurrentTimeStamp())
                .error(ErrorResponse.builder().code("1000").message("Incorrect Otp").build())
                .resp(RespRequest.builder().requestId(confirmResponse.getRequestId()).build())
                .build();
      }
    }
    try {
      ResponseEntity responseEntity =
          requestManager.fetchResponseFromGateway(onConfirmLinkPath, onConfirmRequest);
      log.info(onConfirmLinkPath + " : onConfirmCall: " + responseEntity.getStatusCode());
      //      patientService.updateCareContextStatus(abhaAddress, careContexts);
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorV3Response.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception e) {
      log.error(onConfirmLinkPath + " : OnConfirmCall -> Error :" + Exceptions.unwrap(e));
    }
  }
}
