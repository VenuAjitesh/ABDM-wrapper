/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeAllergyToleranceResource {
  public AllergyIntolerance getAllergy(
      Patient patient, List<Practitioner> practitionerList, String allergy, String authoredOn)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    AllergyIntolerance allergyIntolerance = new AllergyIntolerance();
    allergyIntolerance.setId(UUID.randomUUID().toString());
    allergyIntolerance.setMeta(
        new Meta()
            .setLastUpdated(Utils.getCurrentTimeStamp())
            .addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/AllergyIntolerance"));
    Coding coding = new Coding();
    coding.setSystem("http://snomed.info/sct");
    coding.setCode("609328004");
    coding.setDisplay(allergy);
    CodeableConcept code = new CodeableConcept();
    code.addCoding(coding);
    code.setText(allergy);
    allergyIntolerance.setCode(code);
    Coding clinicalStatusCoding = new Coding();
    clinicalStatusCoding.setSystem(
        "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical");
    clinicalStatusCoding.setCode("active");
    clinicalStatusCoding.setDisplay("Active");
    CodeableConcept clinicalStatus = new CodeableConcept();
    clinicalStatus.addCoding(clinicalStatusCoding);
    allergyIntolerance.setClinicalStatus(clinicalStatus);
    if (authoredOn != null) allergyIntolerance.setRecordedDate(Utils.getFormattedDate(authoredOn));
    allergyIntolerance.setType(AllergyIntolerance.AllergyIntoleranceType.ALLERGY);
    allergyIntolerance.setPatient(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (!(practitionerList.isEmpty())) {
      allergyIntolerance.setRecorder(
          new Reference()
              .setReference("Practitioner/" + practitionerList.get(0).getId())
              .setDisplay(patientName.getText()));
    }
    return allergyIntolerance;
  }
}
