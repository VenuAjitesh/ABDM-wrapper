/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.database.mongo.services.SnomedService;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeEncounterResource {
  @Autowired SnomedService snomedService;

  public Encounter getEncounter(Patient patient, String encounterName, String visitDate)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    Encounter encounter = new Encounter();
    encounter.setId(UUID.randomUUID().toString());
    encounter.setStatus(Encounter.EncounterStatus.INPROGRESS);
    encounter.setMeta(
        new Meta()
            .setLastUpdated(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_ENCOUNTER));
    encounter.setClass_(
        new Coding()
            .setSystem(ResourceProfileIdentifier.PROFILE_BUNDLE_META)
            .setCode(snomedService.getSnomedEncounterCode(encounterName))
            .setDisplay(
                (encounterName != null && !encounterName.isEmpty())
                    ? encounterName
                    : BundleFieldIdentifier.AMBULATORY));
    encounter.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    encounter.setPeriod(new Period().setStart(Utils.getFormattedDateTime(visitDate).getValue()));
    return encounter;
  }
}
