/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.requests.helpers.ServiceRequestResource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeServiceRequestResource {
  public ServiceRequest getServiceRequest(
      Patient patient,
      List<Practitioner> practitionerList,
      ServiceRequestResource serviceRequestResource)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    ServiceRequest serviceRequest = new ServiceRequest();
    serviceRequest.setId(UUID.randomUUID().toString());
    serviceRequest.setStatus(
        ServiceRequest.ServiceRequestStatus.valueOf(serviceRequestResource.getStatus()));
    serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.PROPOSAL);
    serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
    serviceRequest.setMeta(
        new Meta()
            .setLastUpdated(Utils.getCurrentTimeStamp())
            .addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/ServiceRequest"));
    serviceRequest.setCode(new CodeableConcept().setText(serviceRequestResource.getDetails()));
    serviceRequest.setSubject(
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
              .setDisplay(practitionerName.getText()));
    }
    serviceRequest.setPerformer(performerList);
    if (serviceRequestResource.getSpecimen() != null)
      serviceRequest.addSpecimen(new Reference().setDisplay(serviceRequestResource.getSpecimen()));
    return serviceRequest;
  }
}
