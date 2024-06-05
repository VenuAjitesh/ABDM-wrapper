/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.helpers.PatientResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakePatientResource {

  public Patient getPatient(PatientResource patientResource) throws ParseException {
    Coding coding = new Coding();
    coding.setCode("MR");
    coding.setSystem("http://terminology.hl7.org/CodeSystem/v2-0203");
    coding.setDisplay("Medical record number");
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding(coding);
    Identifier identifier = new Identifier();
    identifier.setType(codeableConcept);
    identifier.setSystem("https://healthid.ndhm.gov.in");
    identifier.setValue(patientResource.getPatientReference());

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdated(Utils.getCurrentTimeStamp());
    meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Patient");

    Patient patient = new Patient();
    patient.addName(new HumanName().setText(patientResource.getName()));
    if (patientResource.getGender() != null) {
      patient.setGender(Enumerations.AdministrativeGender.fromCode(patientResource.getGender()));
    }
    if (patientResource.getBirthDate() != null) {
      patient.setBirthDate(Utils.getFormattedDateTime(patientResource.getBirthDate()));
    }
    patient.setMeta(meta);
    patient.addIdentifier(identifier);
    patient.setId(UUID.randomUUID().toString());
    return patient;
  }
}
