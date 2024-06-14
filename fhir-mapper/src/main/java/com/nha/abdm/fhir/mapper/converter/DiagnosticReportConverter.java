/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.functions.*;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.requests.DiagnosticReportRequest;
import com.nha.abdm.fhir.mapper.requests.helpers.DiagnosticResource;
import com.nha.abdm.fhir.mapper.requests.helpers.ObservationResource;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class DiagnosticReportConverter {
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeObservationResource makeObservationResource;
  private final MakeDiagnosticLabResource makeDiagnosticLabResource;
  private final MakeEncounterResource makeEncounterResource;
  private String docName = "";
  private String docCode = "4321000179101";

  public DiagnosticReportConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeDocumentResource makeDocumentResource,
      MakeObservationResource makeObservationResource,
      MakeDiagnosticLabResource makeDiagnosticLabResource,
      MakeEncounterResource encounterResource,
      MakeEncounterResource makeEncounterResource) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeObservationResource = makeObservationResource;
    this.makeDiagnosticLabResource = makeDiagnosticLabResource;
    this.makeEncounterResource = makeEncounterResource;
  }

  public BundleResponse convertToDiagnosticBundle(DiagnosticReportRequest diagnosticReportRequest)
      throws ParseException {
    try {
      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
      Organization organization =
          makeOrganisationResource.getOrganization(diagnosticReportRequest.getOrganisation());
      Patient patient = makePatientResource.getPatient(diagnosticReportRequest.getPatient());
      List<Practitioner> practitionerList =
          Optional.ofNullable(diagnosticReportRequest.getPractitioners())
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
      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              diagnosticReportRequest.getEncounter() != null
                  ? diagnosticReportRequest.getEncounter()
                  : null,
              diagnosticReportRequest.getAuthoredOn());
      List<DiagnosticReport> diagnosticReportList = new ArrayList<>();
      List<Observation> diagnosticObservationList = new ArrayList<>();
      for (DiagnosticResource diagnosticResource : diagnosticReportRequest.getDiagnostics()) {
        List<Observation> observationList = new ArrayList<>();
        for (ObservationResource observationResource : diagnosticResource.getResult()) {
          Observation observation =
              makeObservationResource.getObservation(
                  patient, practitionerList, observationResource);
          observationList.add(observation);
          diagnosticObservationList.add(observation);
        }
        diagnosticReportList.add(
            makeDiagnosticLabResource.getDiagnosticReport(
                patient, practitionerList, observationList, encounter, diagnosticResource));
      }

      List<DocumentReference> documentReferenceList = new ArrayList<>();
      for (DocumentResource documentResource : diagnosticReportRequest.getDocuments()) {
        documentReferenceList.add(
            makeDocumentResource.getDocument(
                patient, organization, documentResource, docCode, documentResource.getType()));
      }
      Composition composition =
          makeCompositionResource(
              patient,
              diagnosticReportRequest.getAuthoredOn(),
              practitionerList,
              organization,
              encounter,
              diagnosticReportList,
              documentReferenceList);
      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestamp(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem("https://ABDM_WRAPPER/bundle")
              .setValue(diagnosticReportRequest.getCareContextReference()));

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
      for (DiagnosticReport diagnosticReport : diagnosticReportList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("DiagnosticReport/" + diagnosticReport.getId())
                .setResource(diagnosticReport));
      }
      for (Observation observation : diagnosticObservationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Observation/" + observation.getId())
                .setResource(observation));
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
      String authoredOn,
      List<Practitioner> practitionerList,
      Organization organization,
      Encounter encounter,
      List<DiagnosticReport> diagnosticReportList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    Composition composition = new Composition();
    Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdated(Utils.getCurrentTimeStamp());
    meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DiagnosticReportRecord");
    composition.setMeta(meta);
    CodeableConcept sectionCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem("http://snomed.info/sct");
    typeCoding.setCode("721981007");
    typeCoding.setDisplay("Diagnostic studies report");
    sectionCode.addCoding(typeCoding);
    composition.setType(sectionCode);
    composition.setTitle("Diagnostic Report-Lab");
    sectionComponent.setCode(
        new CodeableConcept().addCoding(typeCoding).setText("Diagnostic studies report"));
    for (DiagnosticReport diagnosticReport : diagnosticReportList) {
      sectionComponent.addEntry(
          new Reference().setReference("DiagnosticReport/" + diagnosticReport.getId()));
    }
    for (DocumentReference documentReference : documentReferenceList) {
      sectionComponent.addEntry(
          new Reference().setReference("DocumentReference/" + documentReference.getId()));
    }
    composition.addSection(sectionComponent);
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
    if (Objects.nonNull(encounter))
      composition.setEncounter(new Reference().setReference("Encounter/" + encounter.getId()));
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(
        new Reference()
            .setDisplay(patientName.getText())
            .setReference("Patient/" + patient.getId()));
    composition.setDateElement(new DateTimeType(Utils.getFormattedDateTime(authoredOn)));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem("https://ABDM_WRAPPER/document");
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    return composition;
  }
}
