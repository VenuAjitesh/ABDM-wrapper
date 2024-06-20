/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.ResourceProfileIdentifier;
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
      ServiceRequestResource serviceRequestResource,
      String authoredOn)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    ServiceRequest serviceRequest = new ServiceRequest();
    serviceRequest.setId(UUID.randomUUID().toString());
    serviceRequest.setStatus(
        ServiceRequest.ServiceRequestStatus.valueOf(serviceRequestResource.getStatus()));
    serviceRequest.setIntent(ServiceRequest.ServiceRequestIntent.PROPOSAL);
    serviceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);
    serviceRequest.setAuthoredOn(Utils.getFormattedDate(authoredOn));
    serviceRequest.setMeta(
        new Meta()
            .setLastUpdated(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_SERVICE_REQUEST));
    serviceRequest.setCode(new CodeableConcept().setText(serviceRequestResource.getDetails()));
    serviceRequest.setSubject(
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
    if (!performerList.isEmpty()) {
      Practitioner practitioner = practitionerList.get(0);
      practitionerName = practitioner.getName().get(0);
      serviceRequest.setRequester(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(practitionerName.getText()));
    }
    serviceRequest.setPerformer(performerList);
    if (serviceRequestResource.getSpecimen() != null)
      serviceRequest.addSpecimen(new Reference().setDisplay(serviceRequestResource.getSpecimen()));
    return serviceRequest;
  }
}
