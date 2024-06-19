/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.requests.helpers.ImmunizationResource;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeImmunizationResource {
  public Immunization getImmunization(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      ImmunizationResource immunizationResource)
      throws ParseException {
    Immunization immunization = new Immunization();
    immunization.setId(UUID.randomUUID().toString());
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdated(Utils.getCurrentTimeStamp());
    meta.addProfile(ResourceProfileIdentifier.PROFILE_IMMUNIZATION);
    immunization.setMeta(meta);
    immunization.setStatus(Immunization.ImmunizationStatus.COMPLETED);
    immunization.setPatient(
        new Reference().setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId()));
    immunization.setOccurrence((Utils.getFormattedDateTime(immunizationResource.getDate())));
    immunization.addExtension(
        new Extension()
            .setValue(new StringType().setValue(immunizationResource.getVaccineName()))
            .setUrl(ResourceProfileIdentifier.PROFILE_VACCINE_BRAND_NAME));
    immunization.setPrimarySource(true);
    immunization.setVaccineCode(
        new CodeableConcept()
            .setText(immunizationResource.getVaccineName())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode("609328004")
                    .setDisplay(immunizationResource.getVaccineName())));
    immunization.setManufacturer(
        new Reference()
            .setReference(BundleResourceIdentifier.MANUFACTURER + "/" + organization.getId()));
    immunization.setLotNumber(immunizationResource.getLotNumber());
    immunization.setDoseQuantity(new Quantity().setValue(immunizationResource.getDoseNumber()));
    for (Practitioner practitioner : practitionerList) {
      immunization.addPerformer(
          new Immunization.ImmunizationPerformerComponent()
              .setActor(
                  new Reference()
                      .setReference(
                          BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())));
    }
    immunization.setProtocolApplied(
        Collections.singletonList(
            new Immunization.ImmunizationProtocolAppliedComponent()
                .setDoseNumber(
                    new PositiveIntType().setValue(immunizationResource.getDoseNumber()))));
    return immunization;
  }
}
