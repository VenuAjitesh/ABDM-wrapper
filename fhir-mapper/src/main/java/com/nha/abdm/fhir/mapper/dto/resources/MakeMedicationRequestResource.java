/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.ResourceProfileIdentifier;
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
            .addProfile(ResourceProfileIdentifier.PROFILE_MEDICATION_REQUEST)
            .setLastUpdated(Utils.getCurrentTimeStamp()));

    // Setting Medications
    medicationRequest.setMedication(
        new CodeableConcept()
            .setText(prescriptionResource.getMedicine())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
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
                        .setSystem(BundleUrlIdentifier.SNOMED_URL)
                        .setCode("261665006")
                        .setDisplay(prescriptionResource.getAdditionalInstructions())));
      }
      if (prescriptionResource.getRoute() != null) {
        dosage.setRoute(
            new CodeableConcept()
                .setText(prescriptionResource.getRoute())
                .addCoding(
                    new Coding()
                        .setSystem(BundleUrlIdentifier.SNOMED_URL)
                        .setCode("261665006")
                        .setDisplay(prescriptionResource.getRoute())));
      }
      if (prescriptionResource.getMethod() != null) {
        dosage.setMethod(
            new CodeableConcept()
                .setText(prescriptionResource.getMethod())
                .addCoding(
                    new Coding()
                        .setSystem(BundleUrlIdentifier.SNOMED_URL)
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
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(practitionerName.getText()));
    }
    medicationRequest.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (authoredOn != null) medicationRequest.setAuthoredOn(Utils.getFormattedDate(authoredOn));
    medicationRequest.setStatus(MedicationRequest.MedicationRequestStatus.COMPLETED);
    medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
    medicationRequest.setId(UUID.randomUUID().toString());
    return medicationRequest;
  }
}
