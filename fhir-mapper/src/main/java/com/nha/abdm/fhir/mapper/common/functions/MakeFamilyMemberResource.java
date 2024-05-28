/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.requests.helpers.FamilyObservationResource;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeFamilyMemberResource {
  public FamilyMemberHistory getFamilyHistory(
      Patient patient, FamilyObservationResource familyObservationResource) {
    HumanName patientName = patient.getName().get(0);
    FamilyMemberHistory familyMemberHistory = new FamilyMemberHistory();
    familyMemberHistory.setId(UUID.randomUUID().toString());
    familyMemberHistory.setStatus(FamilyMemberHistory.FamilyHistoryStatus.COMPLETED);
    familyMemberHistory.setPatient(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    familyMemberHistory.setRelationship(
        new CodeableConcept().setText(familyObservationResource.getRelationship()));
    familyMemberHistory.addCondition(
        new FamilyMemberHistory.FamilyMemberHistoryConditionComponent()
            .setCode(new CodeableConcept().setText(familyObservationResource.getObservation())));
    return familyMemberHistory;
  }
}
