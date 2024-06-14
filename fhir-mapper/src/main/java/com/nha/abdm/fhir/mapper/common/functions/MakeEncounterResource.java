/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeEncounterResource {
  public Encounter getEncounter(Patient patient, String encounterName, String visitDate)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    Encounter encounter = new Encounter();
    encounter.setId(UUID.randomUUID().toString());
    encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
    encounter.setMeta(
        new Meta()
            .setLastUpdated(Utils.getCurrentTimeStamp())
            .addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Encounter"));
    encounter.setClass_(
        new Coding()
            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
            .setCode("AMB")
            .setDisplay(
                (encounterName != null && !encounterName.isEmpty())
                    ? encounterName
                    : "ambulatory"));
    encounter.setSubject(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    encounter.setPeriod(new Period().setStart(Utils.getFormattedDateTime(visitDate)));
    return encounter;
  }
}
