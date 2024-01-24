/* (C) 2024 */
package com.nha.abdm.wrapper.hip.facade.link;

import com.nha.abdm.wrapper.common.ErrorResponse;
import com.nha.abdm.wrapper.common.models.FacadeResponse;
import com.nha.abdm.wrapper.common.models.VerifyOTP;
import com.nha.abdm.wrapper.hip.hrp.WorkflowManager;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.Patient;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.requests.LinkRecordsRequest;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1")
public class HIPFacadeLinkController {
  private static final Logger log = LogManager.getLogger(HIPFacadeLinkController.class);
  @Autowired WorkflowManager workflowManager;

  /**
   * <B>Facade</B> GET method to facade for checking status of hipInitiatedLinking.
   *
   * @param requestId clientRequestId which is used in linkRecordsResponse as well as in auth/init.
   * @return acknowledgement of status.
   */
  @GetMapping({"/link-status/{requestId}"})
  public FacadeResponse fetchCareContextStatus(@PathVariable String requestId) {
    return workflowManager.getCareContextRequestStatus(requestId);
  }
  /**
   * <B>Facade</B> POST method to facade for linking careContexts i.e. hipInitiatedLinking.
   *
   * @param linkRecordsRequest Response which has authMode, patient details and careContexts.
   * @return acknowledgement of status.
   */
  @PostMapping({"/link-carecontexts"})
  public FacadeResponse linkRecords(@RequestBody LinkRecordsRequest linkRecordsRequest) {
    return workflowManager.initiateHipAuthInit(linkRecordsRequest);
  }

  /**
   * <B>Facade</B> Post method to facade to verify the OTP for authentication.
   *
   * @param verifyOTP Response has OTP and clientRequestId.
   */
  @PostMapping({"/verify-otp"})
  public FacadeResponse verifyOtp(@RequestBody VerifyOTP verifyOTP) {
    log.debug(verifyOTP.toString());
    if (Objects.equals(verifyOTP.getLoginHint(), "hipLinking")) {
      return workflowManager.initiateHipConfirmCallOTP(verifyOTP);
    }
    return FacadeResponse.builder()
        .error(ErrorResponse.builder().message("Unknown Login Hint").build())
        .build();
  }

  /**
   * <B>Facade</B> Put method for adding or modifying patients in database.
   *
   * @param patients Demographic details of the patient
   * @return acknowledgement of storing patient.
   */
  @PutMapping({"/add-patients"})
  public FacadeResponse addPatients(@RequestBody List<Patient> patients) {
    return workflowManager.addPatients(patients);
  }
}
