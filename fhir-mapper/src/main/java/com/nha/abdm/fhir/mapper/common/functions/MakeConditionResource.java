/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.helpers.DateRange;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeConditionResource {
  public Condition getCondition(
      String conditionDetails, Patient patient, String recordedDate, DateRange dateRange)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    Condition condition = new Condition();
    condition.setId(UUID.randomUUID().toString());
    condition.setCode(new CodeableConcept().setText(conditionDetails));
    condition.setMeta(
        new Meta()
            .setLastUpdated(Utils.getCurrentTimeStamp())
            .addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Condition"));
    condition.setSubject(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (recordedDate != null) condition.setRecordedDate(Utils.getFormattedDate(recordedDate));
    if (dateRange != null) {
      condition.setOnset(
          new Period()
              .setStart(Utils.getFormattedDateTime(dateRange.getFrom()))
              .setEnd(Utils.getFormattedDateTime(dateRange.getTo())));
    }
    return condition;
  }
}
