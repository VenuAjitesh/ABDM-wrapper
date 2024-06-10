/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
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
            .addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/FamilyMemberHistory"));
    familyMemberHistory.setPatient(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    familyMemberHistory.setRelationship(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("261665006")
                    .setDisplay(familyObservationResource.getRelationship()))
            .setText(familyObservationResource.getRelationship()));
    familyMemberHistory.addCondition(
        new FamilyMemberHistory.FamilyMemberHistoryConditionComponent()
            .setCode(
                new CodeableConcept()
                    .addCoding(
                        new Coding()
                            .setSystem("http://snomed.info/sct")
                            .setCode("261665006")
                            .setDisplay(familyObservationResource.getObservation()))
                    .setText(familyObservationResource.getObservation())));
    return familyMemberHistory;
  }
}
