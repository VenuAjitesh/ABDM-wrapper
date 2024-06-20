/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
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
      case BundleFieldIdentifier.VITAL_SIGNS:
        coding.setSystem(FieldIdentifiers.getVitals(BundleFieldIdentifier.SYSTEM));
        coding.setCode(FieldIdentifiers.getVitals(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case BundleFieldIdentifier.BODY_MEASUREMENT:
        coding.setSystem(FieldIdentifiers.getBodyMeasurement(BundleFieldIdentifier.SYSTEM));
        coding.setCode(FieldIdentifiers.getBodyMeasurement(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case BundleFieldIdentifier.PHYSICAL_ACTIVITY:
        coding.setSystem(FieldIdentifiers.getPhysicalActivity(BundleFieldIdentifier.SYSTEM));
        coding.setCode(FieldIdentifiers.getPhysicalActivity(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case BundleFieldIdentifier.GENERAL_ASSESSMENT:
        coding.setSystem(FieldIdentifiers.getGeneralAssessment(BundleFieldIdentifier.SYSTEM));
        coding.setCode(FieldIdentifiers.getGeneralAssessment(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case BundleFieldIdentifier.WOMAN_HEALTH:
        coding.setSystem(FieldIdentifiers.getWomanHealth(BundleFieldIdentifier.SYSTEM));
        coding.setCode(FieldIdentifiers.getWomanHealth(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
      case BundleFieldIdentifier.LIFE_STYLE:
        coding.setSystem(FieldIdentifiers.getLifeStyle(BundleFieldIdentifier.SYSTEM));
        coding.setCode(FieldIdentifiers.getLifeStyle(observationResource.getObservation()));
        coding.setDisplay(observationResource.getObservation());
        typeCode.addCoding(coding);
        break;
    }

    typeCode.setText(observationResource.getObservation());
    observation.setCode(typeCode);
    observation.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    List<Reference> performerList = new ArrayList<>();
    HumanName practitionerName = null;
    for (Practitioner practitioner : practitionerList) {
      practitionerName = practitioner.getName().get(0);
      performerList.add(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(practitionerName.getText()));
    }
    observation.setPerformer(performerList);
    if (Objects.nonNull(observationResource.getValueQuantity())) {
      observation.setValue(
          new Quantity()
              .setValue(observationResource.getValueQuantity().getValue())
              .setUnit(observationResource.getValueQuantity().getUnit()));
    }
    if (Objects.nonNull(observation.getValueQuantity())
        || observationResource.getResult() != null) {
      observation.setValue(new CodeableConcept().setText(observationResource.getResult()));
    }
    observation.setId(UUID.randomUUID().toString());
    return observation;
  }
}
