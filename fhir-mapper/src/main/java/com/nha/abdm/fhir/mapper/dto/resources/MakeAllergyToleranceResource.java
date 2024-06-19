/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.*;
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
            .addProfile(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE));
    Coding coding = new Coding();
    coding.setSystem(BundleUrlIdentifier.SNOMED_URL);
    coding.setCode(SnomedCodeIdentifier.SNOMED_ALLERGY_INTOLERANCE);
    coding.setDisplay(BundleFieldIdentifier.ALLERGY);
    CodeableConcept code = new CodeableConcept();
    code.addCoding(coding);
    code.setText(allergy);
    allergyIntolerance.setCode(code);
    Coding clinicalStatusCoding = new Coding();
    clinicalStatusCoding.setSystem(ResourceProfileIdentifier.PROFILE_ALLERGY_INTOLERANCE_SYSTEM);
    clinicalStatusCoding.setCode(BundleFieldIdentifier.ACTIVE);
    clinicalStatusCoding.setDisplay(BundleFieldIdentifier.ACTIVE);
    CodeableConcept clinicalStatus = new CodeableConcept();
    clinicalStatus.addCoding(clinicalStatusCoding);
    allergyIntolerance.setClinicalStatus(clinicalStatus);
    if (authoredOn != null) allergyIntolerance.setRecordedDate(Utils.getFormattedDate(authoredOn));
    allergyIntolerance.setType(AllergyIntolerance.AllergyIntoleranceType.ALLERGY);
    allergyIntolerance.setPatient(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (!(practitionerList.isEmpty())) {
      allergyIntolerance.setRecorder(
          new Reference()
              .setReference(
                  BundleResourceIdentifier.PRACTITIONER + "/" + practitionerList.get(0).getId())
              .setDisplay(patientName.getText()));
    }
    return allergyIntolerance;
  }
}
