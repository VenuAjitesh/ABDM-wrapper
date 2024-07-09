/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.helpers.FieldIdentifiers;
import com.nha.abdm.fhir.mapper.database.mongo.services.SnomedService;
import com.nha.abdm.fhir.mapper.requests.helpers.WellnessObservationResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeWellnessObservationResource {
  @Autowired SnomedService snomedService;

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
    coding.setSystem(FieldIdentifiers.getVitals(BundleFieldIdentifier.SYSTEM));
    coding.setCode(snomedService.getSnomedObservationCode(observationResource.getObservation()));
    coding.setDisplay(observationResource.getObservation());
    typeCode.addCoding(coding);

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
