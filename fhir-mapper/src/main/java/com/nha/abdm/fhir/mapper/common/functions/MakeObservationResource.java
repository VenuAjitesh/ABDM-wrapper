/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.requests.helpers.ObservationResource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MakeObservationResource {
  private static final Logger log = LoggerFactory.getLogger(MakeObservationResource.class);

  public Observation getObservation(
      Patient patient,
      List<Practitioner> practitionerList,
      ObservationResource observationResource) {
    HumanName patientName = patient.getName().get(0);
    Observation observation = new Observation();
    observation.setStatus(Observation.ObservationStatus.FINAL);
    CodeableConcept typeCode = new CodeableConcept();
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
    log.info(observationResource.toString());
    return observation;
  }
}
