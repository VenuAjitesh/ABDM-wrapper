/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.requests.helpers.PrescriptionResource;
import java.text.ParseException;
import java.util.*;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeMedicationRequestResource {
  public MedicationRequest getMedicationResource(
      String authoredOn,
      PrescriptionResource prescriptionResource,
      Organization organization,
      List<Practitioner> practitioners,
      Patient patient)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    MedicationRequest medicationRequest = new MedicationRequest();
    medicationRequest.setMeta(
        new Meta()
            .addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/MedicationRequest")
            .setLastUpdated(Utils.getCurrentTimeStamp()));
    medicationRequest.setMedication(
        new CodeableConcept()
            .setText(prescriptionResource.getMedicine())
            .addCoding(
                new Coding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("261665006")
                    .setDisplay(prescriptionResource.getMedicine())));
    if (prescriptionResource.getDosage() != null)
      medicationRequest.setDosageInstruction(
          Collections.singletonList(new Dosage().setText(prescriptionResource.getDosage())));
    if (!practitioners.isEmpty()) {
      Practitioner practitioner = practitioners.get(0);
      HumanName practitionerName = practitioner.getName().get(0);
      medicationRequest.setRequester(
          new Reference()
              .setReference("Practitioner/" + practitioner.getId())
              .setDisplay(practitionerName.getText()));
    }
    medicationRequest.setSubject(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (authoredOn != null) medicationRequest.setAuthoredOn(Utils.getFormattedDate(authoredOn));
    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
    medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
    medicationRequest.setId(UUID.randomUUID().toString());
    return medicationRequest;
  }
}
