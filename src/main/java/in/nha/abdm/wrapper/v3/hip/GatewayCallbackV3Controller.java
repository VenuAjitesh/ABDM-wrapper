/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.StatusResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.GatewayCallbackResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPNotifyRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.LogsRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.DiscoverRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.ConfirmResponse;
import in.nha.abdm.wrapper.v3.common.constants.GatewayURL;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hip.hrp.WorkflowV3Manager;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.responses.LinkOnAddCareContextsV3Response;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.responses.OnGenerateTokenResponse;
import in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.responses.InitV3Response;
import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.ProfileShareV3Request;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@RestController
@Profile(WrapperConstants.V3)
public class GatewayCallbackV3Controller {

  @Autowired WorkflowV3Manager workflowV3Manager;
  @Autowired RequestLogV3Service requestLogV3Service;
  @Autowired LogsRepo logsRepo;

  private static final Logger log = LogManager.getLogger(GatewayCallbackV3Controller.class);

  /**
   * discovery
   *
   * <p>Routing to workFlowManager for using service interface.
   *
   * @param discoverRequest response body with demographic details and abhaAddress of patient.
   */
  @PostMapping(GatewayURL.DISCOVER_PATH)
  public ResponseEntity<GenericV3Response> discover(
      @RequestBody DiscoverRequest discoverRequest, @RequestHeader HttpHeaders headers) {
    if (discoverRequest != null && discoverRequest.getError() == null) {
      String hipId = headers.getFirst(GatewayConstants.X_HIP_ID);
      discoverRequest.setHipId(hipId);
      return workflowV3Manager.discover(discoverRequest, headers);
    } else {
      log.error(GatewayURL.DISCOVER_PATH + discoverRequest.getError().getMessage());
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * userInitiatedLinking
   *
   * <p>Routing to workFlowManager for using service interface.
   *
   * @param initResponse Response from ABDM gateway which has careContexts.
   */
  @PostMapping(GatewayURL.INIT_LINKING_PATH)
  public void initCall(
      @RequestHeader HttpHeaders headers, @RequestBody InitV3Response initResponse) {
    if (initResponse != null) {
      log.info(GatewayURL.INIT_LINKING_PATH + initResponse);
      workflowV3Manager.initiateOnInit(initResponse, headers);
    } else {
      log.error(GatewayURL.INIT_LINKING_PATH + ": ERROR");
    }
  }

  /**
   * userInitiatedLinking
   *
   * <p>Routing to workFlowManager for using service interface.
   *
   * @param confirmResponse Response from ABDM gateway which has OTP sent by facility to user for
   *     authentication.
   */
  @PostMapping(GatewayURL.CONFIRM_LINKING_PATH)
  public void confirmCall(
      @RequestHeader HttpHeaders headers, @RequestBody ConfirmResponse confirmResponse) {
    if (confirmResponse != null && confirmResponse.getError() == null) {
      log.info(GatewayURL.CONFIRM_LINKING_PATH + confirmResponse);
      workflowV3Manager.initiateOnConfirmCall(confirmResponse, headers);
    } else {
      log.error(GatewayURL.CONFIRM_LINKING_PATH + confirmResponse.getError().getMessage());
    }
  }

  private void updateRequestError(
      String requestId, String methodName, Object errors, RequestStatus requestStatus)
      throws IllegalDataStateException {
    RequestLog RequestLog = logsRepo.findByGatewayRequestId(requestId);
    if (RequestLog == null) {
      String error = "Illegal State - Request Id not found in database: " + requestId;
      log.error(error);
      throw new IllegalDataStateException(error);
    }
    log.error(errors);
    requestLogV3Service.updateError(RequestLog.getGatewayRequestId(), errors, requestStatus);
  }

  private void updateLinkTokenRequestError(
      String requestId, String methodName, Object errors, RequestStatus requestStatus)
      throws IllegalDataStateException {
    RequestLog RequestLog = logsRepo.findByLinkTokenRequestId(requestId);
    if (RequestLog == null) {
      String error = "Illegal State - Request Id not found in database: " + requestId;
      log.error(error);
      throw new IllegalDataStateException(error);
    }
    log.error(errors);
    requestLogV3Service.linkTokenUpdateError(
        RequestLog.getGatewayRequestId(), errors, requestStatus);
  }

  @ExceptionHandler(IllegalDataStateException.class)
  private ResponseEntity<GatewayCallbackResponse> handleIllegalDataStateException(
      IllegalDataStateException ex) {
    return new ResponseEntity<>(
        GatewayCallbackResponse.builder()
            .error(ErrorResponse.builder().message(ex.getMessage()).build())
            .build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * Callback from ABDM gateway of consent to dataTransfer
   *
   * @param hipNotifyRequest Has careContexts details for dataTransfer
   */
  @PostMapping(GatewayURL.HIP_CONSENT_NOTIFY_PATH)
  public ResponseEntity<GatewayCallbackResponse> hipNotify(
      @RequestBody HIPNotifyRequest hipNotifyRequest, @RequestHeader HttpHeaders headers)
      throws IllegalDataStateException {
    if (hipNotifyRequest != null) {
      workflowV3Manager.hipNotify(hipNotifyRequest, headers);
    } else {
      log.debug("Error in response of Consent Notify");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  /**
   * Callback from ABDM gateway for dataTransfer to HIU
   *
   * @param hipHealthInformationRequest Has keys for encryption and dataPushURL of HIU
   */
  @PostMapping(GatewayURL.HIP_HEALTH_INFORMATION_REQUEST_PATH)
  public ResponseEntity<GatewayCallbackResponse> healthInformation(
      @RequestBody HIPHealthInformationRequest hipHealthInformationRequest,
      @RequestHeader HttpHeaders headers)
      throws IllegalDataStateException,
          InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException {
    if (hipHealthInformationRequest != null) {
      workflowV3Manager.healthInformation(hipHealthInformationRequest, headers);
    } else {
      log.debug("Invalid Data request response");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @PostMapping(GatewayURL.PROFILE_SHARE_PATH)
  public ResponseEntity<GatewayCallbackResponse> profileShare(
      @RequestHeader HttpHeaders headers, @RequestBody ProfileShareV3Request profileShare) {
    if (profileShare != null) {
      workflowV3Manager.profileShare(profileShare, headers);
    } else {
      log.debug("Invalid profile share request");
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  /**
   * Acknowledgement of context/notify
   *
   * @param statusResponse
   * @return
   */
  @PostMapping(GatewayURL.LINK_CONTEXT_ON_NOTIFY_PATH)
  public ResponseEntity<GatewayCallbackResponse> contextOnNotify(
      @RequestBody StatusResponse statusResponse) {
    log.info(statusResponse);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  /**
   * Acknowledgement of sms/on-notify2
   *
   * @param statusResponse
   * @param headers
   * @return
   */
  @PostMapping(GatewayURL.DEEP_LINKING_ON_NOTIFY_PATH)
  public ResponseEntity<GatewayCallbackResponse> deepLinkingOnNotify(
      @RequestBody StatusResponse statusResponse, @RequestHeader HttpHeaders headers) {
    log.info(statusResponse);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  /**
   * Acknowledgement of Add CareContexts
   *
   * @param linkOnAddCareContextsV3Response
   * @param headers
   * @return
   * @throws IllegalDataStateException
   */
  @PostMapping(GatewayURL.ON_ADD_CARE_CONTEXT_PATH)
  public ResponseEntity<GatewayCallbackResponse> onAddCareContext(
      @RequestBody LinkOnAddCareContextsV3Response linkOnAddCareContextsV3Response,
      @RequestHeader HttpHeaders headers)
      throws IllegalDataStateException {
    if (linkOnAddCareContextsV3Response != null
        && linkOnAddCareContextsV3Response.getError() != null) {
      updateRequestError(
          linkOnAddCareContextsV3Response.getResponse().getRequestId(),
          "onAddCareContext",
          linkOnAddCareContextsV3Response.getError(),
          RequestStatus.AUTH_ON_ADD_CARE_CONTEXT_ERROR);
    } else if (linkOnAddCareContextsV3Response != null) {
      log.debug(linkOnAddCareContextsV3Response.toString());
      requestLogV3Service.setHipOnAddCareContextResponse(linkOnAddCareContextsV3Response);
    } else {
      String error = "Got Error in onAddCareContext callback: gateway response was null";
      return new ResponseEntity<>(
          GatewayCallbackResponse.builder()
              .error(
                  ErrorResponse.builder().code(GatewayConstants.ERROR_CODE).message(error).build())
              .build(),
          HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  /**
   * Acknowledgement of generate token
   *
   * @param onGenerateTokenResponse
   * @param headers
   * @return
   * @throws IllegalDataStateException
   */
  @PostMapping(GatewayURL.ON_GENERATE_LINK_TOKEN_PATH)
  public ResponseEntity<GatewayCallbackResponse> onGenerateToken(
      @RequestBody OnGenerateTokenResponse onGenerateTokenResponse,
      @RequestHeader HttpHeaders headers)
      throws IllegalDataStateException {
    if (onGenerateTokenResponse != null && onGenerateTokenResponse.getError() != null) {
      log.info("Updating error");
      updateLinkTokenRequestError(
          onGenerateTokenResponse.getResponse().getRequestId(),
          "onAddCareContext",
          onGenerateTokenResponse.getError(),
          RequestStatus.LINK_TOKEN_REQUEST_ERROR);
    } else if (onGenerateTokenResponse != null) {
      workflowV3Manager.handleAddCareContexts(onGenerateTokenResponse, headers);
    } else {
      String error = "Got Error in on-generate-token callback: gateway response was null";
      return new ResponseEntity<>(
          GatewayCallbackResponse.builder()
              .error(
                  ErrorResponse.builder().code(GatewayConstants.ERROR_CODE).message(error).build())
              .build(),
          HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
