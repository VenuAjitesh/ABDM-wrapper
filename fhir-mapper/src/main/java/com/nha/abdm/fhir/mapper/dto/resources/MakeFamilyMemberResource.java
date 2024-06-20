/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.requests.helpers.FamilyObservationResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeFamilyMemberResource {
  public FamilyMemberHistory getFamilyHistory(
      Patient patient, FamilyObservationResource familyObservationResource) throws ParseException {
    HumanName patientName = patient.getName().get(0);
    FamilyMemberHistory familyMemberHistory = new FamilyMemberHistory();
    familyMemberHistory.setId(UUID.randomUUID().toString());
    familyMemberHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.COMPLETED);
    familyMemberHistory.setMeta(
        new Meta()
            .setLastUpdated(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_FAMILY_MEMBER_HISTORY));
    familyMemberHistory.setPatient(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    familyMemberHistory.setRelationship(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode("261665006")
                    .setDisplay(familyObservationResource.getRelationship()))
            .setText(familyObservationResource.getRelationship()));
    familyMemberHistory.addCondition(
        new FamilyMemberHistory.FamilyMemberHistoryConditionComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setSystem(BundleUrlIdentifier.SNOMED_URL)
                            .setCode("261665006")
                            .setDisplay(familyObservationResource.getObservation()))
                    .setText(familyObservationResource.getObservation())));
    return familyMemberHistory;
  }
}
