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

    // Setting Meta of the Medication Resource
    medicationRequest.setMeta(
        new Meta()
            .addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/MedicationRequest")
            .setLastUpdated(Utils.getCurrentTimeStamp()));

    // Setting Medications
    medicationRequest.setMedication(
        new CodeableConcept()
            .setText(prescriptionResource.getMedicine())
            .addCoding(
                new Coding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("261665006")
                    .setDisplay(prescriptionResource.getMedicine())));
    if (prescriptionResource.getDosage() != null) {
      Dosage dosage = new Dosage();
      dosage.setText(prescriptionResource.getDosage());
      if (!prescriptionResource.getAdditionalInstructions().isBlank()) {
        dosage.addAdditionalInstruction(
            new CodeableConcept()
                .setText(prescriptionResource.getAdditionalInstructions())
                .addCoding(
                    new Coding()
                        .setSystem("http://snomed.info/sct")
                        .setCode("261665006")
                        .setDisplay(prescriptionResource.getAdditionalInstructions())));
      }
      if (prescriptionResource.getRoute() != null) {
        dosage.setRoute(
            new CodeableConcept()
                .setText(prescriptionResource.getRoute())
                .addCoding(
                    new Coding()
                        .setSystem("http://snomed.info/sct")
                        .setCode("261665006")
                        .setDisplay(prescriptionResource.getRoute())));
      }
      if (prescriptionResource.getMethod() != null) {
        dosage.setMethod(
            new CodeableConcept()
                .setText(prescriptionResource.getMethod())
                .addCoding(
                    new Coding()
                        .setSystem("http://snomed.info/sct")
                        .setCode("261665006")
                        .setDisplay(prescriptionResource.getMethod())));
      }
      if (prescriptionResource.getTiming() != null) {
        String[] parts = prescriptionResource.getTiming().split("-");
        dosage.setTiming(
            new Timing()
                .setRepeat(
                    new Timing.TimingRepeatComponent()
                        .setFrequency(Integer.parseInt(parts[0]))
                        .setPeriod(Integer.parseInt(parts[1]))
                        .setPeriodUnit(Timing.UnitsOfTime.valueOf(parts[2]))));
      }
      medicationRequest.setDosageInstruction(Collections.singletonList(dosage));
    }
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
