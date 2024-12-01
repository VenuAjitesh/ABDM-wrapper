/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.facade.link;

import com.fasterxml.jackson.core.JsonProcessingException;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.VerifyOTP;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.FacadeResponse;
import in.nha.abdm.wrapper.v1.common.responses.RequestStatusResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.WorkflowManager;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.link.deepLinking.requests.DeepLinkingRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.LinkRecordsRequest;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
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
@RequestMapping(path = "/v1")
@Profile(WrapperConstants.V1)
public class HIPFacadeLinkController {
  private static final Logger log = LogManager.getLogger(HIPFacadeLinkController.class);
  @Autowired WorkflowManager workflowManager;

  /**
   * <B>Facade</B> GET method to facade for checking status of hipInitiatedLinking.
   *
   * @param requestId clientRequestId which is used in linkRecordsRequest as well as in auth/init.
   * @return acknowledgement of status.
   */
  @GetMapping({"/link-status/{requestId}"})
  public ResponseEntity<RequestStatusResponse> fetchCareContextStatus(
      @PathVariable("requestId") String requestId) throws IllegalDataStateException {
    RequestStatusResponse requestStatusResponse =
        workflowManager.getCareContextRequestStatus(requestId);
    if (Objects.isNull(requestStatusResponse.getError())) {
      return new ResponseEntity<>(requestStatusResponse, HttpStatus.ACCEPTED);
    } else {
      return new ResponseEntity<>(requestStatusResponse, HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * <B>Facade</B> POST method to facade for linking careContexts i.e. hipInitiatedLinking.
   *
   * @param linkRecordsRequest Response which has authMode, patient details and careContexts.
   * @return acknowledgement of status.
   */
  @PostMapping({"/link-carecontexts"})
  public ResponseEntity<FacadeResponse> linkRecords(
      @RequestBody LinkRecordsRequest linkRecordsRequest) {
    FacadeResponse facadeResponse = workflowManager.initiateHipAuthInit(linkRecordsRequest);
    if (Objects.isNull(facadeResponse.getError())) {
      return new ResponseEntity<>(facadeResponse, HttpStatus.ACCEPTED);
    } else {
      return new ResponseEntity<>(facadeResponse, HttpStatus.BAD_REQUEST);
    }
  }

  /**
   * <B>Facade</B> Post method to facade to verify the OTP for authentication.
   *
   * @param verifyOTP Response has OTP and clientRequestId.
   */
  @PostMapping({"/verify-otp"})
  public ResponseEntity<FacadeResponse> verifyOtp(@RequestBody VerifyOTP verifyOTP)
      throws IllegalDataStateException {
    log.debug(verifyOTP.toString());
    if (Objects.equals(verifyOTP.getLoginHint(), "hipLinking")) {
      FacadeResponse facadeResponse = workflowManager.confirmAuthOtp(verifyOTP);
      return new ResponseEntity<>(facadeResponse, HttpStatus.ACCEPTED);
    }
    return new ResponseEntity<>(
        FacadeResponse.builder()
            .error(
                ErrorResponse.builder()
                    .message("Unknown Login Hint")
                    .code(String.valueOf(HttpStatus.BAD_REQUEST))
                    .build())
            .build(),
        HttpStatus.BAD_REQUEST);
  }

  /**
   * <B>Facade</B> Put method for adding or modifying patients in database.
   *
   * @param patients Demographic details of the patient
   * @return acknowledgement of storing patient.
   */
  @PutMapping({"/add-patients"})
  public ResponseEntity<FacadeResponse> addPatients(@RequestBody List<Patient> patients) {

    FacadeResponse facadeResponse = workflowManager.addPatients(patients);
    if (Objects.isNull(facadeResponse.getError())) {
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
  @PostMapping({"/sms/notify"})
  public ResponseEntity<FacadeResponse> deepLinking(
      @RequestBody DeepLinkingRequest deepLinkingRequest) {
    FacadeResponse facadeResponse = workflowManager.sendDeepLinkingSms(deepLinkingRequest);
    return new ResponseEntity<>(facadeResponse, facadeResponse.getHttpStatusCode());
  }

  /**
   * Convert JsonProcessingException exceptions thrown by HIP Facade Link controller to API error
   * response.
   */
  @ExceptionHandler(JsonProcessingException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  private FacadeResponse handleJsonProcessingException(JsonProcessingException ex) {
    return FacadeResponse.builder()
        .message(ex.getMessage())
        .code(HttpStatus.BAD_REQUEST.value())
        .build();
  }

  @ExceptionHandler(IllegalDataStateException.class)
  private FacadeResponse handleIllegalDataStateException(IllegalDataStateException ex) {
    return FacadeResponse.builder()
        .message(ex.getMessage())
        .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .build();
  }
}
