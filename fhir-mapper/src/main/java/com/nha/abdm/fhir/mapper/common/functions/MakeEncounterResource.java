/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeEncounterResource {
  public Encounter getEncounter(Patient patient, String encounterName) {
    HumanName patientName = patient.getName().get(0);
    Encounter encounter = new Encounter();
    encounter.setId(UUID.randomUUID().toString());
    encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
    encounter.setClass_(
        new Coding()
            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
            .setCode("AMB")
            .setDisplay(!encounterName.isEmpty() ? encounterName : "ambulatory"));
    encounter.setSubject(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    return encounter;
  }
}
