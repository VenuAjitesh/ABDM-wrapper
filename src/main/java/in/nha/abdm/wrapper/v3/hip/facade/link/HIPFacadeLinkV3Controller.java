/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.facade.link;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.link.deepLinking.requests.DeepLinkingRequest;
import in.nha.abdm.wrapper.v3.common.constants.FacadeURL;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.common.models.RequestStatusV3Response;
import in.nha.abdm.wrapper.v3.hip.hrp.WorkflowV3Manager;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.LinkRecordsV3Request;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Profile(WrapperConstants.V3)
@RequestMapping(path = "/v3")
public class HIPFacadeLinkV3Controller {

  private static final Logger log = LogManager.getLogger(HIPFacadeLinkV3Controller.class);
  @Autowired WorkflowV3Manager workflowV3Manager;

  /**
   * <B>Facade</B> POST method to facade for linking careContexts i.e. hipInitiatedLinking.
   *
   * @param linkRecordsV3Request request which has authMode, patient details and careContexts.
   * @return acknowledgement of status.
   */
  @PostMapping(FacadeURL.HIP_LINK_CARE_CONTEXT_PATH)
  public ResponseEntity<FacadeV3Response> linkRecords(
      @Valid @RequestBody LinkRecordsV3Request linkRecordsV3Request) {
    FacadeV3Response facadeV3Response =
        workflowV3Manager.initiateCareContextLinking(linkRecordsV3Request);
    if (Objects.isNull(facadeV3Response.getErrors())) {
      return new ResponseEntity<>(facadeV3Response, HttpStatus.ACCEPTED);
    } else {
      return new ResponseEntity<>(facadeV3Response, HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * <B>Facade</B> GET method to facade for checking status of hipInitiatedLinking.
   *
   * @param requestId clientRequestId which is used in linkRecordsRequest
   * @return acknowledgement of status.
   */
  @GetMapping(FacadeURL.HIP_LINK_STATUS_PATH)
  public ResponseEntity<RequestStatusV3Response> fetchCareContextStatus(
      @PathVariable("requestId") String requestId) throws IllegalDataStateException {
    RequestStatusV3Response requestStatusV3Response =
        workflowV3Manager.getCareContextRequestStatus(requestId);
    if (Objects.isNull(requestStatusV3Response.getErrors())) {
      return new ResponseEntity<>(requestStatusV3Response, HttpStatus.ACCEPTED);
    } else {
      return new ResponseEntity<>(requestStatusV3Response, HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * <B>Facade</B> Put method for adding or modifying patients in database.
   *
   * @param patients Demographic details of the patient
   * @return acknowledgement of storing patient.
   */
  @PutMapping(FacadeURL.ADD_PATIENT_PATH)
  public ResponseEntity<FacadeV3Response> addPatients(@Valid @RequestBody List<Patient> patients) {

    FacadeV3Response facadeResponse = workflowV3Manager.addPatients(patients);
    if (Objects.isNull(facadeResponse.getErrors())) {
      return new ResponseEntity<>(facadeResponse, HttpStatus.ACCEPTED);
    } else {
      return new ResponseEntity<>(facadeResponse, HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * This API is used for sending a request for ABDM to send a sms to the patient Telling the
   * patient that there are records present at facility link them with user-initiatedLinking.
   *
   * @param deepLinkingRequest has the hipId and patient mobile number for sending sms.
   * @return the success or failure of the request to ABDM gateway.
   */
  @PostMapping(FacadeURL.SMS_NOTIFY_PATH)
  public ResponseEntity<FacadeV3Response> deepLinking(
      @RequestBody DeepLinkingRequest deepLinkingRequest) {
    FacadeV3Response facadeV3Response = workflowV3Manager.sendDeepLinkingSms(deepLinkingRequest);
    return new ResponseEntity<>(facadeV3Response, facadeV3Response.getHttpStatusCode());
  }
}
