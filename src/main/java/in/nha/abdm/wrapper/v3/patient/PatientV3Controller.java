/* (C) 2024 */
package in.nha.abdm.wrapper.v3.patient;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v3.common.constants.FacadeURL;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = FacadeURL.PATIENT_V3_PATH)
@Validated
public class PatientV3Controller {
  private final PatientV3Service patientService;

  public PatientV3Controller(PatientV3Service patientService) {
    this.patientService = patientService;
  }

  /**
   * This controller is used to fetch all the details of the patient which includes
   * careContext+consent
   *
   * @param patientId abhaAddress
   * @param hipId facilityId
   * @return patient
   */
  @GetMapping(FacadeURL.PATIENT_ID_PATH)
  public ResponseEntity<Object> getPatientDetails(
      @PathVariable("patientId") String patientId,
      @RequestParam(WrapperConstants.HIP_ID) @NotNull(message = "hipId is mandatory") String hipId) {
    Patient patient = patientService.getPatientDetails(patientId, hipId);
    if (Objects.isNull(patient)) {
      FacadeV3Response facadeV3Response =
          FacadeV3Response.builder()
              .httpStatusCode(HttpStatus.BAD_REQUEST)
              .message("No Patient found")
              .errors(
                  Collections.singletonList(
                      ErrorV3Response.builder()
                          .error(
                              ErrorResponse.builder()
                                  .code(GatewayConstants.ERROR_CODE)
                                  .message(
                                      String.format(
                                          "%s not found in %s facility", patientId, hipId))
                                  .build())
                          .build()))
              .build();
      return new ResponseEntity<>(facadeV3Response, HttpStatus.BAD_REQUEST);
    }
    return new ResponseEntity<>(patient, HttpStatus.OK);
  }
}
