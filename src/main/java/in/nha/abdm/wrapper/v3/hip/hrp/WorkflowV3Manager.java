/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hip.hrp.WorkflowManager;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPNotifyRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.DiscoverRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.link.deepLinking.requests.DeepLinkingRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.ConfirmResponse;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.common.models.RequestStatusV3Response;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hip.hrp.consent.ConsentV3Interface;
import in.nha.abdm.wrapper.v3.hip.hrp.dataTransfer.HIPHealthInformationV3Interface;
import in.nha.abdm.wrapper.v3.hip.hrp.discover.DiscoveryV3Interface;
import in.nha.abdm.wrapper.v3.hip.hrp.link.deepLinking.DeepLinkingV3Interface;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.HIPLinkV3Interface;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.LinkRecordsV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.responses.OnGenerateTokenResponse;
import in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.LinkV3Interface;
import in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.responses.InitV3Response;
import in.nha.abdm.wrapper.v3.hip.hrp.share.ProfileShareV3Interface;
import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.ProfileShareV3Request;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Profile(WrapperConstants.V3)
@Component
public class WorkflowV3Manager {
  private static final Logger log = LogManager.getLogger(WorkflowManager.class);
  @Autowired DiscoveryV3Interface discoveryV3Interface;
  @Autowired PatientV3Service patientService;
  @Autowired LinkV3Interface linkV3Interface;
  @Autowired HIPLinkV3Interface hipV3LinkV3Interface;
  @Autowired ConsentV3Interface consentV3Interface;
  @Autowired HIPHealthInformationV3Interface hipHealthInformationV3Interface;
  @Autowired RequestLogV3Service requestLogV3Service;
  @Autowired ProfileShareV3Interface profileShareV3Interface;
  @Autowired DeepLinkingV3Interface deepLinkingV3Interface;

  /**
   * userInitiated linking
   *
   * <p>Routing the Discover request to discovery interface for Making POST on-discover
   *
   * @param discoverRequest Response from ABDM gateway for patient discovery
   */
  public ResponseEntity<GenericV3Response> discover(
      DiscoverRequest discoverRequest, HttpHeaders headers) {
    return discoveryV3Interface.discover(discoverRequest, headers);
  }

  /**
   * userInitiated linking
   *
   * <p>Routing the initResponse to linkInterface for making POST on-init request.
   *
   * @param initResponse Response from ABDM gateway after successful on-Discover request.
   */
  public void initiateOnInit(InitV3Response initResponse, HttpHeaders headers) {
    if (initResponse != null) {
      linkV3Interface.onInit(initResponse, headers);
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
  public void initiateOnConfirmCall(ConfirmResponse confirmResponse, HttpHeaders headers) {
    if (confirmResponse != null) {
      linkV3Interface.onConfirm(confirmResponse, headers);
    } else {
      log.error("Error in Confirm response from gateWay");
    }
  }

  public FacadeV3Response initiateCareContextLinking(LinkRecordsV3Request linkRecordsV3Request) {
    log.info("Initiating careContext Linking");
    return hipV3LinkV3Interface.addCareContexts(linkRecordsV3Request);
  }

  /**
   * hipInitiatedLinking
   *
   * <p>Fetching the status from requestLogs using clientId.
   *
   * @param requestId clientRequestId for tracking the linking status.
   * @return "Success", "Initiated", "appropriate error message".
   */
  public RequestStatusV3Response getCareContextRequestStatus(String requestId)
      throws IllegalDataStateException {
    return requestLogV3Service.getStatus(requestId);
  }

  /**
   * Adds or updates a list of patients in database.
   *
   * @param patients List of patients with reference and demographic details.
   * @return status of adding or modifying patients in database.
   */
  public FacadeV3Response addPatients(List<Patient> patients) {
    return patientService.upsertPatients(patients);
  }

  public void hipNotify(HIPNotifyRequest hipNotifyRequest, HttpHeaders headers)
      throws IllegalDataStateException {
    log.debug(hipNotifyRequest.toString());
    consentV3Interface.hipNotify(hipNotifyRequest, headers);
  }

  public void healthInformation(
      HIPHealthInformationRequest hipHealthInformationRequest, HttpHeaders headers)
      throws IllegalDataStateException,
          InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException {
    log.debug(hipHealthInformationRequest.toString());
    hipHealthInformationV3Interface.healthInformation(hipHealthInformationRequest, headers);
  }

  /**
   * profileShare
   *
   * <p>Routing the profileShare request to shareInterface for generating the token number and
   * sharing with ABDM
   *
   * @param profileShare request body which has demographic details.
   */
  public void profileShare(ProfileShareV3Request profileShare, HttpHeaders headers) {
    log.debug(profileShare.toString());
    profileShareV3Interface.shareProfile(profileShare, headers);
  }

  /**
   * DeepLinking
   *
   * <p>Sending the sms to patient via ABDM saying that there are some records present at facility.
   *
   * @param deepLinkingRequest request body which has hipId and patient mobile number.
   */
  public FacadeV3Response sendDeepLinkingSms(DeepLinkingRequest deepLinkingRequest) {
    return deepLinkingV3Interface.sendDeepLinkingSms(deepLinkingRequest);
  }

  /**
   * When the linkToken is received this will be handled to send the careContext to ABDM
   *
   * @param onGenerateTokenResponse
   * @param headers
   */
  public void handleAddCareContexts(
      OnGenerateTokenResponse onGenerateTokenResponse, HttpHeaders headers) {
    hipV3LinkV3Interface.handleAddCareContexts(onGenerateTokenResponse, headers);
  }
}
