/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.helpers.OrganisationResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeOrganisationResource {
  public Organization getOrganization(OrganisationResource organisationResource)
      throws ParseException {
    Coding coding = new Coding();
    coding.setCode("PRN");
    coding.setSystem("http://terminology.hl7.org/CodeSystem/v2-0203");
    coding.setDisplay("Provider number");
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding(coding);
    Identifier identifier = new Identifier();
    identifier.setType(codeableConcept);
    identifier.setSystem("https://facility.abdm.gov.in");
    identifier.setValue(organisationResource.getFacilityId());

    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdated(Utils.getCurrentTimeStamp());
    meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Organization");

    Organization organization = new Organization();
    organization.setName(organisationResource.getFacilityName());
    organization.setMeta(meta);
    organization.addIdentifier(identifier);
    organization.setId(UUID.randomUUID().toString());
    return organization;
  }
}
