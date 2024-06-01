/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.functions.*;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.requests.HealthDocumentRecord;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class HealthDocumentConverter {
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeEncounterResource makeEncounterResource;
  private String docName = "Record artifact";
  private String docCode = "419891008";

  public HealthDocumentConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeDocumentResource makeDocumentResource,
      MakeEncounterResource makeEncounterResource) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeEncounterResource = makeEncounterResource;
  }

  public BundleResponse convertToHealthDocumentBundle(HealthDocumentRecord healthDocumentRecord)
      throws ParseException {
    try {
      Organization organization =
          makeOrganisationResource.getOrganization(healthDocumentRecord.getOrganisation());
      Patient patient = makePatientResource.getPatient(healthDocumentRecord.getPatient());
      List<Practitioner> practitionerList =
          Optional.ofNullable(healthDocumentRecord.getPractitioners())
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
      List<DocumentReference> documentReferenceList = new ArrayList<>();
      for (DocumentResource documentResource : healthDocumentRecord.getDocuments()) {
        documentReferenceList.add(
            makeDocumentResource.getDocument(
                patient, organization, documentResource, docCode, docName));
      }
      Encounter encounter =
          (healthDocumentRecord.getEncounter() != null)
              ? makeEncounterResource.getEncounter(patient, healthDocumentRecord.getEncounter())
              : null;
      Composition composition =
          makeCompositionResource(
              patient, practitionerList, organization, encounter, documentReferenceList);
      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestamp(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem("https://ABDM_WRAPPER/bundle")
              .setValue(healthDocumentRecord.getCareContextReference()));
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
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl("Organisation/" + organization.getId())
              .setResource(organization));
      if (Objects.nonNull(encounter)) {
        new Bundle.BundleEntryComponent()
            .setFullUrl("Encounter/" + encounter.getId())
            .setResource(encounter);
      }
      for (DocumentReference documentReference : documentReferenceList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("DocumentReference/" + documentReference.getId())
                .setResource(documentReference));
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
      Encounter encounter,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    Composition composition = new Composition();
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdated(Utils.getCurrentTimeStamp());
    meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/HealthDocumentRecord");
    composition.setMeta(meta);
    Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
    sectionComponent.setTitle("OPD Records");
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem("http://snomed.info/sct");
    typeCoding.setCode("419891008");
    typeCoding.setDisplay("Record artifact");
    typeCode.addCoding(typeCoding);
    typeCode.setText("Record artifact");
    composition.setType(typeCode);
    sectionComponent.setCode(typeCode);
    for (DocumentReference documentReference : documentReferenceList) {
      sectionComponent.addEntry(
          new Reference().setReference("DocumentReference/" + documentReference.getId()));
    }
    composition.addSection(sectionComponent);
    composition.setTitle("Health Document");
    composition.setEncounter(new Reference().setReference("Encounter/" + encounter.getId()));
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      HumanName practionerName = practitioner.getName().get(0);
      authorList.add(
          new Reference()
              .setDisplay(practionerName.getText())
              .setReference("Practitioner/" + practitioner.getId()));
    }
    composition.setCustodian(
        new Reference()
            .setDisplay(organization.getName())
            .setReference("Organisation/" + organization.getId()));
    composition.setAuthor(authorList);
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(
        new Reference()
            .setDisplay(patientName.getText())
            .setReference("Patient/" + patient.getId()));
    composition.setDateElement(new DateTimeType(Utils.getCurrentTimeStamp()));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem("https://ABDM_WRAPPER/document");
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    return composition;
  }
}
