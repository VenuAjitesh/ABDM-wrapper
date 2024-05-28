/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.common.helpers.FieldIdentifiers;
import com.nha.abdm.fhir.mapper.requests.helpers.WellnessObservationResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeWellnessObservationResource {
  public Observation getObservation(
      Patient patient,
      List<Practitioner> practitionerList,
      WellnessObservationResource observationResource,
      String type) {
    HumanName patientName = patient.getName().get(0);
    Observation observation = new Observation();
    observation.setStatus(Observation.ObservationStatus.FINAL);
    CodeableConcept typeCode = new CodeableConcept();
    Coding coding = new Coding();
    switch (type) {
      case "vitalSigns":
        coding.setSystem(FieldIdentifiers.getVitals("system"));
        coding.setCode(FieldIdentifiers.getVitals(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case "bodyMeasurement":
        coding.setSystem(FieldIdentifiers.getBodyMeasurement("system"));
        coding.setCode(FieldIdentifiers.getBodyMeasurement(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case "physicalActivity":
        coding.setSystem(FieldIdentifiers.getPhysicalActivity("system"));
        coding.setCode(FieldIdentifiers.getPhysicalActivity(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case "generalAssessment":
        coding.setSystem(FieldIdentifiers.getGeneralAssessment("system"));
        coding.setCode(FieldIdentifiers.getGeneralAssessment(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case "womanHealth":
        coding.setSystem(FieldIdentifiers.getWomanHealth("system"));
        coding.setCode(FieldIdentifiers.getWomanHealth(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case "lifeStyle":
        coding.setSystem(FieldIdentifiers.getLifeStyle("system"));
        coding.setCode(FieldIdentifiers.getLifeStyle(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
    }

    typeCode.setText(observationResource.getObservation());
    observation.setCode(typeCode);
    observation.setSubject(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    List<Reference> performerList = new ArrayList<>();
    HumanName practitionerName = null;
    for (Practitioner practitioner : practitionerList) {
      practitionerName = practitioner.getName().get(0);
      performerList.add(
          new Reference()
              .setReference("Practitioner/" + practitioner.getId())
              .setDisplay(patientName.getText()));
    }
    observation.setPerformer(performerList);
    if (Objects.nonNull(observationResource.getValueQuantity()))
      observation.setValue(
          new Quantity()
              .setValue(observationResource.getValueQuantity().getValue())
              .setUnit(observationResource.getValueQuantity().getUnit()));
    //    if (observation.getValueQuantity() != null || observationResource.getResult() != null)
    //      observation.setValue(new CodeableConcept().setText(observationResource.getResult()));
    observation.setId(UUID.randomUUID().toString());
    return observation;
  }
}
