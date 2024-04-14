/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp;

import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.models.VerifyOTP;
import com.nha.abdm.wrapper.common.responses.FacadeResponse;
import com.nha.abdm.wrapper.common.responses.GatewayCallbackResponse;
import com.nha.abdm.wrapper.common.responses.RequestStatusResponse;
import com.nha.abdm.wrapper.hip.hrp.consent.ConsentInterface;
import com.nha.abdm.wrapper.hip.hrp.consent.requests.HIPNotifyRequest;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.HealthInformationInterface;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.PatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.RequestLogService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.Patient;
import com.nha.abdm.wrapper.hip.hrp.discover.DiscoveryInterface;
import com.nha.abdm.wrapper.hip.hrp.discover.requests.DiscoverRequest;
import com.nha.abdm.wrapper.hip.hrp.link.deepLinking.DeepLinkingInterface;
import com.nha.abdm.wrapper.hip.hrp.link.deepLinking.requests.DeepLinkingRequest;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.HipLinkInterface;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.requests.LinkRecordsRequest;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.responses.LinkOnConfirmResponse;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.responses.LinkOnInitResponse;
import com.nha.abdm.wrapper.hip.hrp.link.userInitiated.LinkInterface;
import com.nha.abdm.wrapper.hip.hrp.link.userInitiated.responses.ConfirmResponse;
import com.nha.abdm.wrapper.hip.hrp.link.userInitiated.responses.InitResponse;
import com.nha.abdm.wrapper.hip.hrp.share.ProfileShareInterface;
import com.nha.abdm.wrapper.hip.hrp.share.reponses.ProfileShare;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class WorkflowManager {
  private static final Logger log = LogManager.getLogger(WorkflowManager.class);
  @Autowired DiscoveryInterface discoveryInterface;
  @Autowired PatientService patientService;
  @Autowired LinkInterface linkInterface;
  @Autowired HipLinkInterface hipLinkInterface;
  @Autowired ConsentInterface consentInterface;
  @Autowired HealthInformationInterface healthInformationInterface;
  @Autowired RequestLogService requestLogService;
  @Autowired ProfileShareInterface profileShareInterface;
  @Autowired DeepLinkingInterface deepLinkingInterface;

  /**
   * userInitiated linking
   *
   * <p>Routing the Discover request to discovery interface for Making POST on-discover
   *
   * @param discoverRequest Response from ABDM gateway for patient discovery
   */
  public ResponseEntity<GatewayCallbackResponse> discover(DiscoverRequest discoverRequest) {
    return discoveryInterface.discover(discoverRequest);
  }

  /**
   * userInitiated linking
   *
   * <p>Routing the initResponse to linkInterface for making POST on-init request.
   *
   * @param initResponse Response from ABDM gateway after successful on-Discover request.
   */
  public void initiateOnInit(InitResponse initResponse) {
    if (initResponse != null) {
      linkInterface.onInit(initResponse);
    } else {
      log.error("Error in Init response from gateWay");
    }
  }

  /**
   * userInitiated linking
   *
   * <p>Routing confirmResponse to linkInterface for making on on-confirm request.
   *
   * @param confirmResponse Response form ABDM gateway after successful on-init request.
   */
  public void initiateOnConfirmCall(ConfirmResponse confirmResponse) {
    if (confirmResponse != null) {
      linkInterface.onConfirm(confirmResponse);
    } else {
      log.error("Error in Confirm response from gateWay");
    }
  }

  /**
   * hipInitiatedLinking
   *
   * <p>Fetching the status from requestLogs using clientId.
   *
   * @param requestId clientRequestId for tracking the linking status.
   * @return "Success", "Initiated", "appropriate error message".
   */
  public RequestStatusResponse getCareContextRequestStatus(String requestId)
      throws IllegalDataStateException {
    return requestLogService.getStatus(requestId);
  }

  /**
   * hipInitiatedLinking
   *
   * <p>Routing linkRecordsRequest to hipLinkInterface for making authInit body and POST auth/init.
   *
   * @param linkRecordsRequest Response received from facility to facade to link careContext
   * @return clientRequestId and statusCode.
   */
  public FacadeResponse initiateHipAuthInit(LinkRecordsRequest linkRecordsRequest) {
    if (linkRecordsRequest != null) {
      return hipLinkInterface.hipAuthInit(linkRecordsRequest);
    }
    String error = "initiateHipAuthInit: Error in LinkRecordsRequest";
    log.debug(error);
    return FacadeResponse.builder().message(error).code(HttpStatus.BAD_REQUEST.value()).build();
  }

  /**
   * hipInitiatedLinking
   *
   * <p>if authMode == DEMOGRAPHICS start auth/confirm by routing via hipLinkInterface<br>
   * else if authMode == MOBILE_OTP, storing the response in requestLog <br>
   * when the otp is received via verifyOTP starting auth/confirm
   *
   * @param linkOnInitResponse Response from ABDM gateway after successful auth/init request.
   */
  public void initiateAuthConfirm(LinkOnInitResponse linkOnInitResponse)
      throws IllegalDataStateException {
    if (linkOnInitResponse != null && linkOnInitResponse.getError() == null) {
      if (linkOnInitResponse.getAuth().getMode().equals("DEMOGRAPHICS"))
        hipLinkInterface.confirmAuthDemographics(linkOnInitResponse);
      else if (linkOnInitResponse.getAuth().getMode().equals("MOBILE_OTP"))
        requestLogService.setHipOnInitResponseOTP(linkOnInitResponse);
    } else if (linkOnInitResponse != null) {
      log.error("OnInit error" + linkOnInitResponse.getError().getMessage());
    } else {
      log.error("Oninit -> error due to response");
    }
  }

  /**
   * hipInitiatedLinking
   *
   * <p>Routing the linkOnConfirmResponse to hipLinkInterface for making addCareContext body, and
   * POST /v0.5/links/link/add-contexts
   *
   * @param linkOnConfirmResponse Response from ABDM gateway after successful auth/confirm.<br>
   */
  public void addCareContext(LinkOnConfirmResponse linkOnConfirmResponse)
      throws IllegalDataStateException {
    if (linkOnConfirmResponse != null && linkOnConfirmResponse.getError() == null) {
      hipLinkInterface.hipAddCareContext(linkOnConfirmResponse);
    } else if (linkOnConfirmResponse != null) {
      log.error("OnConfirm error" + linkOnConfirmResponse.getError().getMessage());
    } else {
      log.error("OnConfirm -> error due to response");
    }
  }

  /**
   * Adds or updates a list of patients in database.
   *
   * @param patients List of patients with reference and demographic details.
   * @return status of adding or modifying patients in database.
   */
  public FacadeResponse addPatients(List<Patient> patients) {
    return patientService.upsertPatients(patients);
  }

  /**
   * hipInitiatedLinking
   *
   * <p>In HipInitiatedLinking if authMode is MOBILE_OTP, then start auth/confirm request with OTP.
   *
   * @param verifyOTP request body which has OTP and clientRequestId.
   */
  public FacadeResponse confirmAuthOtp(VerifyOTP verifyOTP) throws IllegalDataStateException {
    return hipLinkInterface.confirmAuthOtp(verifyOTP);
  }

  public void hipNotify(HIPNotifyRequest hipNotifyRequest) throws IllegalDataStateException {
    log.debug(hipNotifyRequest.toString());
    consentInterface.hipNotify(hipNotifyRequest);
  }

  public void healthInformation(HIPHealthInformationRequest hipHealthInformationRequest)
      throws IllegalDataStateException, InvalidAlgorithmParameterException,
          NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException,
          InvalidKeyException {
    log.debug(hipHealthInformationRequest.toString());
    healthInformationInterface.healthInformation(hipHealthInformationRequest);
  }

  /**
   * profileShare
   *
   * <p>Routing the profileShare request to shareInterface for generating the token number and
   * sharing with ABDM
   *
   * @param profileShare request body which has demographic details.
   */
  public void profileShare(ProfileShare profileShare, String hipId) {
    log.debug(profileShare.toString());
    profileShareInterface.shareProfile(profileShare, hipId);
  }

  /**
   * DeepLinking
   *
   * <p>Sending the sms to patient via ABDM saying that there are some records present at facility.
   *
   * @param deepLinkingRequest request body which has hipId and patient mobile number.
   */
  public FacadeResponse sendDeepLinkingSms(DeepLinkingRequest deepLinkingRequest) {
    log.debug(deepLinkingRequest.toString());
    return deepLinkingInterface.sendDeepLinkingSms(deepLinkingRequest);
  }
}
