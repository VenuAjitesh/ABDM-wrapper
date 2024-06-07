/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.requests.helpers.ProcedureResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeProcedureResource {
  public Procedure getProcedure(Patient patient, ProcedureResource procedureResource)
      throws ParseException {
    Procedure procedure = new Procedure();
    procedure.setId(UUID.randomUUID().toString());
    procedure.setMeta(
        new Meta().addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Procedure"));
    if (procedureResource.getStatus() != null) {
      procedure.setStatus(Procedure.ProcedureStatus.valueOf(procedureResource.getStatus()));
    } else {
      procedure.setStatus(Procedure.ProcedureStatus.COMPLETED);
    }
    procedure.setSubject(new Reference().setReference("Patient/" + patient.getId()));
    procedure.setCode(
        new CodeableConcept()
            .setText(procedureResource.getProcedureName())
            .addCoding(
                new Coding()
                    .setDisplay(procedureResource.getProcedureName())
                    .setCode("261665006")
                    .setSystem("http://snomed.info/sct")));
    if (procedureResource.getOutcome() != null) {
      procedure.setOutcome(
          new CodeableConcept()
              .setText(procedureResource.getOutcome())
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("261665006")
                      .setDisplay(procedureResource.getOutcome())));
    }
    procedure.addReasonCode(
        new CodeableConcept()
            .setText(procedureResource.getProcedureReason())
            .addCoding(
                new Coding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("261665006")
                    .setDisplay(procedureResource.getProcedureReason())));
    procedure.setPerformed(
        new DateTimeType(Utils.getFormattedDateTime(procedureResource.getDate())));
    return procedure;
  }
}
