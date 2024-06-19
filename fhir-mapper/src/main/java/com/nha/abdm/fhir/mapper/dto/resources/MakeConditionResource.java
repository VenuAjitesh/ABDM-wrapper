/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.ResourceProfileIdentifier;
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
            .addProfile(ResourceProfileIdentifier.PROFILE_CONDITION));
    condition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (recordedDate != null) condition.setRecordedDate(Utils.getFormattedDate(recordedDate));
    if (dateRange != null) {
      condition.setOnset(
          new Period()
              .setStart(Utils.getFormattedDateTime(dateRange.getFrom()).getValue())
              .setEnd(Utils.getFormattedDateTime(dateRange.getTo()).getValue()));
    }
    return condition;
  }
}
