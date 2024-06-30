/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.database.mongo.services.SnomedService;
import com.nha.abdm.fhir.mapper.requests.helpers.ProcedureResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeProcedureResource {
  @Autowired SnomedService snomedService;

  public Procedure getProcedure(Patient patient, ProcedureResource procedureResource)
      throws ParseException {
    Procedure procedure = new Procedure();
    procedure.setId(UUID.randomUUID().toString());
    procedure.setMeta(new Meta().addProfile(ResourceProfileIdentifier.PROFILE_PROCEDURE));
    if (procedureResource.getStatus() != null) {
      procedure.setStatus(Procedure.ProcedureStatus.valueOf(procedureResource.getStatus()));
    } else {
      procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
    }
    procedure.setSubject(
        new Reference().setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId()));
    procedure.setCode(
        new CodeableConcept()
            .setText(procedureResource.getProcedureName())
            .addCoding(
                new Coding()
                    .setDisplay(procedureResource.getProcedureName())
                    .setCode(
                        snomedService.getConditionProcedureCode(
                            procedureResource.getProcedureName()))
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)));
    if (procedureResource.getOutcome() != null) {
      procedure.setOutcome(
          new CodeableConcept()
              .setText(procedureResource.getOutcome())
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(
                          snomedService.getConditionProcedureCode(procedureResource.getOutcome()))
                      .setDisplay(procedureResource.getOutcome())));
    }
    procedure.addReasonCode(
        new CodeableConcept()
            .setText(procedureResource.getProcedureReason())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(
                        snomedService.getConditionProcedureCode(
                            procedureResource.getProcedureReason()))
                    .setDisplay(procedureResource.getProcedureReason())));
    procedure.setPerformed((Utils.getFormattedDateTime(procedureResource.getDate())));
    return procedure;
  }
}
