/* (C) 2024 */
package in.nha.abdm.wrapper.v1.patient;

import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.PatientService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/patient")
public class PatientController {
  private final PatientService patientService;

  @Autowired
  public PatientController(PatientService patientService) {
    this.patientService = patientService;
  }

  @GetMapping({"/{patientId}"})
  public ResponseEntity<Patient> getPatientDetails(@PathVariable("patientId") String patientId) {
    Patient patient = patientService.getPatientDetails(patientId);
    if (Objects.isNull(patient)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(patient, HttpStatus.OK);
  }
}
