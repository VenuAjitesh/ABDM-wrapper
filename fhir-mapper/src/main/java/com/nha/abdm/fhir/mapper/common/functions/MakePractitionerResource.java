/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.helpers.PractitionerResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakePractitionerResource {
  public Practitioner getPractitioner(PractitionerResource practitionerResource)
      throws ParseException {
    Coding coding = new Coding();
    coding.setCode("MR");
    coding.setSystem("http://terminology.hl7.org/CodeSystem/v2-0203");
    coding.setDisplay("Medical record number");
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding(coding);
    Identifier identifier = new Identifier();
    identifier.setType(codeableConcept);
    identifier.setSystem("https://doctor.abdm.gov.in");
    identifier.setValue(practitionerResource.getPractitionerId());

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdated(Utils.getCurrentTimeStamp());
    meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Practitioner");

    Practitioner practitioner = new Practitioner();
    practitioner.addName(new HumanName().setText(practitionerResource.getName()));
    practitioner.setMeta(meta);
    practitioner.addIdentifier(identifier);
    practitioner.setId(UUID.randomUUID().toString());
    return practitioner;
  }
}
