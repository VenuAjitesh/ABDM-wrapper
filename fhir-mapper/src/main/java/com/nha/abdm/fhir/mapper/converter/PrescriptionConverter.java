/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.functions.*;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.requests.PrescriptionRequest;
import com.nha.abdm.fhir.mapper.requests.helpers.PrescriptionResource;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PrescriptionConverter {
  private static final Logger log = LoggerFactory.getLogger(PrescriptionConverter.class);
  private final MakeOrganisationResource makeOrganisationResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeBundleMetaResource makeBundleMetaResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeMedicationRequestResource makeMedicationRequestResource;
  private final MakeEncounterResource makeEncounterResource;

  public PrescriptionConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakeDocumentResource makeDocumentResource,
      MakeMedicationRequestResource makeMedicationRequestResource,
      MakeEncounterResource makeEncounterResource) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeMedicationRequestResource = makeMedicationRequestResource;
    this.makeEncounterResource = makeEncounterResource;
  }

  public BundleResponse convertToPrescriptionBundle(PrescriptionRequest prescriptionRequest)
      throws ParseException {
    try {
      Organization organization =
          Objects.nonNull(prescriptionRequest.getOrganisation())
              ? makeOrganisationResource.getOrganization(prescriptionRequest.getOrganisation())
              : null;
      Patient patient = makePatientResource.getPatient(prescriptionRequest.getPatient());
      List<Practitioner> practitionerList =
          Optional.ofNullable(prescriptionRequest.getPractitioners())
              .map(
                  practitioners ->
                      practitioners.stream()
                          .map(
                              practitioner -> {
                                try {
                                  return makePractitionerResource.getPractitioner(practitioner);
                                } catch (ParseException e) {
                                  throw new RuntimeException(e);
                                }
                              })
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);
      List<MedicationRequest> medicationRequestList = new ArrayList<>();
      for (PrescriptionResource item : prescriptionRequest.getPrescriptions()) {
        medicationRequestList.add(
            makeMedicationRequestResource.getMedicationResource(
                prescriptionRequest.getAuthoredOn(),
                item,
                organization,
                practitionerList,
                patient));
      }
      Encounter encounter = null;
      if (prescriptionRequest.getEncounter() != null)
        encounter = makeEncounterResource.getEncounter(patient, prescriptionRequest.getEncounter());
      List<Binary> documentList = new ArrayList<>();
      if (prescriptionRequest.getDocuments() != null) {
        for (DocumentResource documentResource : prescriptionRequest.getDocuments()) {
          Binary binary = new Binary();
          binary.setMeta(
              new Meta()
                  .setLastUpdated(Utils.getCurrentTimeStamp())
                  .addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/Binary"));
          binary.setContent(documentResource.getData().getBytes());
          binary.setContentType(documentResource.getContentType());
          binary.setId(UUID.randomUUID().toString());
          documentList.add(binary);
        }
      }
      Composition composition =
          makeCompositionResource(
              patient,
              practitionerList,
              organization,
              prescriptionRequest.getAuthoredOn(),
              encounter,
              medicationRequestList,
              documentList);
      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestamp(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem("https://ABDM_WRAPPER/bundle")
              .setValue(prescriptionRequest.getCareContextReference()));
      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl("Composition/" + composition.getId())
              .setResource(composition));
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl("Patient/" + patient.getId())
              .setResource(patient));
      for (Practitioner practitioner : practitionerList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Practitioner/" + practitioner.getId())
                .setResource(practitioner));
      }
      if (Objects.nonNull(organization)) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Organisation/" + organization.getId())
                .setResource(organization));
      }
      if (Objects.nonNull(encounter)) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Encounter/" + encounter.getId())
                .setResource(encounter));
      }
      for (MedicationRequest medicationRequest : medicationRequestList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("MedicationRequest/" + medicationRequest.getId())
                .setResource(medicationRequest));
      }
      for (Binary binary : documentList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Binary/" + binary.getId())
                .setResource(binary));
      }
      bundle.setEntry(entries);
      return BundleResponse.builder().bundle(bundle).build();
    } catch (Exception e) {
      return BundleResponse.builder()
          .error(ErrorResponse.builder().code(1000).message(e.getMessage()).build())
          .build();
    }
  }

  private Composition makeCompositionResource(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      String authoredOn,
      Encounter encounter,
      List<MedicationRequest> medicationRequestList,
      List<Binary> documentList)
      throws ParseException {
    Composition composition = new Composition();
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdated(Utils.getCurrentTimeStamp());
    meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/PrescriptionRecord");
    composition.setMeta(meta);
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem("http://snomed.info/sct");
    typeCoding.setCode("440545006");
    typeCoding.setDisplay("Prescription record");
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle("Prescription record");
    if (Objects.nonNull(organization))
      composition.setCustodian(
          new Reference().setReference("Organisation/" + organization.getId()));
    if (Objects.nonNull(encounter))
      composition.setEncounter(
          new Reference()
              .setReference("Encounter/" + encounter.getId())
              .setDisplay(encounter.getClass_().getDisplay()));
    List<Reference> authorList = new ArrayList<>();
    HumanName practitionerName = null;
    for (Practitioner author : practitionerList) {
      practitionerName = author.getName().get(0);
      authorList.add(
          new Reference()
              .setReference("Practitioner/" + author.getId())
              .setDisplay(practitionerName.getText()));
    }
    composition.setAuthor(authorList);
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    composition.setDateElement(new DateTimeType(Utils.getFormattedDateTime(authoredOn)));
    Composition.SectionComponent medicationComponent = new Composition.SectionComponent();
    medicationComponent.setTitle("Medications");
    medicationComponent.setCode(
        new CodeableConcept()
            .setText("Medications")
            .addCoding(
                new Coding()
                    .setCode("440545006")
                    .setDisplay("Prescription record")
                    .setSystem("http://snomed.info/sct")));
    for (MedicationRequest medicationRequest : medicationRequestList) {
      Reference entryReference =
          new Reference()
              .setReference("MedicationRequest/" + medicationRequest.getId())
              .setType("MedicationRequest");
      medicationComponent.addEntry(entryReference);
    }
    composition.addSection(medicationComponent);
    for (Binary binary : documentList)
      medicationComponent.addEntry(
          new Reference().setReference("Binary/" + binary.getId()).setType("Binary"));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem("https://ABDM_WRAPPER/document");
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    return composition;
  }
}
