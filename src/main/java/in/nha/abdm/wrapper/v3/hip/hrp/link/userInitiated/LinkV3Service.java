/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.models.VerifyOTP;
import in.nha.abdm.wrapper.v1.common.requests.RequestOtp;
import in.nha.abdm.wrapper.v1.common.responses.*;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.requests.*;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.ConfirmResponse;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hip.HIPV3Client;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.helpers.PatientCareContextHIType;
import in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.requests.OnConfirmV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.requests.OnInitV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.responses.InitV3Response;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

@Service
public class LinkV3Service implements LinkV3Interface {

  @Autowired PatientRepo patientRepo;
  private final RequestV3Manager requestV3Manager;
  @Autowired RequestLogV3Service requestLogV3Service;
  @Autowired PatientV3Service patientV3Service;
  private final HIPV3Client hipClient;

  @Value("${onInitLinkPath}")
  public String onInitLinkPath;

  @Value("${onConfirmLinkPath}")
  public String onConfirmLinkPath;

  @Value("${requestOtp}")
  public String requestOtp;

  @Value("${verifyOtpPath}")
  public String verifyOtpPath;

  @Autowired
  public LinkV3Service(RequestV3Manager requestV3Manager, HIPV3Client hipClient) {
    this.requestV3Manager = requestV3Manager;
    this.hipClient = hipClient;
  }

  private static final Logger log = LogManager.getLogger(LinkV3Service.class);

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
  public void onInit(InitV3Response initResponse, HttpHeaders headers) {
    OnInitV3Request onInitRequest;
    String linkReferenceNumber = UUID.randomUUID().toString();
    String requestId = UUID.randomUUID().toString();

    Patient patient =
        patientRepo.findByAbhaAddress(
            initResponse.getAbhaAddress(), headers.getFirst(GatewayConstants.X_HIP_ID));
    String patientMobile =
        Optional.ofNullable(patient).map(Patient::getPatientMobile).orElse("MOBILE_NUMBER");

    OnInitLinkMeta onInitLinkMeta =
        OnInitLinkMeta.builder()
            .communicationMedium("MOBILE")
            .communicationHint(patientMobile)
            .communicationExpiry(Utils.getSmsExpiry())
            .build();

    OnInitLink onInitLink =
        OnInitLink.builder()
            .referenceNumber(linkReferenceNumber)
            .authenticationType("MEDIATE")
            .meta(onInitLinkMeta)
            .build();

    onInitRequest =
        OnInitV3Request.builder()
            .transactionId(initResponse.getTransactionId())
            .link(onInitLink)
            .response(
                RespRequest.builder()
                    .requestId(headers.getFirst(GatewayConstants.REQUEST_ID))
                    .build())
            .build();

    log.info("onInit body : " + onInitRequest.toString());
    try {
      log.info("Sending otp request to HIP");
      RequestOtp requestOtp =
          RequestOtp.builder()
              .abhaAddress(initResponse.getAbhaAddress())
              .patientReference(initResponse.getPatient().get(0).getReferenceNumber())
              .hipId(headers.getFirst(GatewayConstants.X_HIP_ID))
              .build();
      ResponseEntity<ResponseOtp> hipResponse = hipClient.requestOtp(this.requestOtp, requestOtp);
      log.info(this.requestOtp + " : requestOtp: " + hipResponse.getStatusCode());

      if (Objects.requireNonNull(hipResponse.getBody()).getStatus().equalsIgnoreCase("SUCCESS")
          || Objects.isNull(hipResponse.getBody().getError())) {
        onInitRequest.getLink().setReferenceNumber(hipResponse.getBody().getLinkRefNumber());
        ResponseEntity<GenericV3Response> responseEntity =
            requestV3Manager.fetchResponseFromGateway(
                onInitLinkPath,
                onInitRequest,
                Utils.getCustomHeaders(
                    GatewayConstants.X_HIP_ID,
                    headers.getFirst(GatewayConstants.X_HIP_ID),
                    UUID.randomUUID().toString()));
        log.info(onInitLinkPath + " : onInitCall: " + responseEntity.getStatusCode());

      } else {
        onInitRequest.setError(
            ErrorResponse.builder()
                .code(GatewayConstants.ERROR_CODE)
                .message(
                    Objects.nonNull(hipResponse.getBody().getError())
                        ? hipResponse.getBody().getError().getMessage()
                        : "Unable to send SMS")
                .build());
        ResponseEntity<GenericV3Response> responseEntity =
            requestV3Manager.fetchResponseFromGateway(
                onInitLinkPath,
                onInitRequest,
                Utils.getCustomHeaders(
                    GatewayConstants.X_HIP_ID,
                    headers.getFirst(GatewayConstants.X_HIP_ID),
                    UUID.randomUUID().toString()));
        log.info(onInitLinkPath + " : onInitCall: " + responseEntity.getStatusCode());
      }
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception e) {
      log.info(onInitLinkPath + " : OnInitCall -> Error : " + Arrays.toString(e.getStackTrace()));
    }
    try {
      requestLogV3Service.setLinkResponse(
          initResponse, requestId, onInitRequest.getLink().getReferenceNumber(), headers);
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
  public void onConfirm(ConfirmResponse confirmResponse, HttpHeaders headers) {
    String display;
    String linkRefNumber = confirmResponse.getConfirmation().getLinkRefNumber();

    String abhaAddress = requestLogV3Service.getPatientId(linkRefNumber);
    String patientReference = requestLogV3Service.getPatientReference(linkRefNumber);
    Patient patientWithAbha =
        patientRepo.findByAbhaAddress(abhaAddress, headers.getFirst(GatewayConstants.X_HIP_ID));

    if (patientWithAbha != null) {
      display = patientWithAbha.getPatientDisplay();
    } else {
      display = patientReference;
    }
    log.info("onConfirm Abha address is: " + abhaAddress);
    if (abhaAddress == null) {
      log.info("OnConfirmCall -> patient with abhaAddress not found in logs.");
    }
    List<CareContext> careContexts =
        requestLogV3Service.getSelectedCareContexts(abhaAddress, linkRefNumber);
    log.info("Selected careContexts: " + careContexts);
    OnConfirmPatient onConfirmPatient = null;
    ResponseEntity<RequestStatusResponse> hipResponse = null;
    OnConfirmV3Request onConfirmV3Request = null;
    if (careContexts == null) {
      onConfirmV3Request =
          OnConfirmV3Request.builder()
              .error(
                  ErrorResponse.builder()
                      .code(GatewayConstants.ERROR_CODE)
                      .message("CareContexts doesn't match")
                      .build())
              .response(RespRequest.builder().requestId(confirmResponse.getRequestId()).build())
              .build();
    } else {
      try {
        log.info("Requesting HIP for verify otp in discovery");
        VerifyOTP verifyOTP =
            VerifyOTP.builder()
                .hipId(headers.getFirst(GatewayConstants.X_HIP_ID))
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
        Map<String, List<CareContext>> groupedByHiType =
            careContexts.stream().collect(Collectors.groupingBy(CareContext::getHiType));
        String finalDisplay = display;
        List<PatientCareContextHIType> patients =
            groupedByHiType.entrySet().stream()
                .map(
                    entry -> {
                      String hiType = entry.getKey();
                      List<CareContext> careContextList =
                          entry.getValue().stream()
                              .map(
                                  context ->
                                      CareContext.builder()
                                          .display(context.getDisplay())
                                          .referenceNumber(context.getReferenceNumber())
                                          .build())
                              .collect(Collectors.toList());

                      return PatientCareContextHIType.builder()
                          .referenceNumber(patientReference)
                          .display(finalDisplay)
                          .hiType(hiType == null ? "HealthDocumentRecord" : hiType)
                          .count(careContexts.size())
                          .careContexts(careContexts)
                          .build();
                    })
                .collect(Collectors.toList());
        onConfirmV3Request =
            OnConfirmV3Request.builder()
                .patient(patients)
                .response(
                    RespRequest.builder()
                        .requestId((headers.getFirst(GatewayConstants.REQUEST_ID)))
                        .build())
                .build();
        log.info("onConfirm : " + onConfirmV3Request.toString());
      } else {
        onConfirmPatient =
            OnConfirmPatient.builder().referenceNumber(patientReference).display(display).build();
        onConfirmV3Request =
            OnConfirmV3Request.builder()
                .error(
                    ErrorResponse.builder()
                        .code(GatewayConstants.INVALID_OTP_CODE)
                        .message(GatewayConstants.INVALID_OTP)
                        .build())
                .response(
                    RespRequest.builder()
                        .requestId(String.valueOf(headers.get(GatewayConstants.REQUEST_ID)))
                        .build())
                .build();
      }
    }
    try {
      ResponseEntity<GenericV3Response> responseEntity =
          requestV3Manager.fetchResponseFromGateway(
              onConfirmLinkPath,
              onConfirmV3Request,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIP_ID,
                  headers.getFirst(GatewayConstants.X_HIP_ID),
                  UUID.randomUUID().toString()));
      log.info(onConfirmLinkPath + " : onConfirmCall: " + responseEntity.getStatusCode());
      patientV3Service.updateCareContextStatus(
          abhaAddress, careContexts, headers.getFirst(GatewayConstants.X_HIP_ID));
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception e) {
      log.error(onConfirmLinkPath + " : OnConfirmCall -> Error :" + Exceptions.unwrap(e));
    }
  }
}
