/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.dto.compositions.MakeDiagnosticComposition;
import com.nha.abdm.fhir.mapper.dto.resources.*;
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
  private final MakeDiagnosticComposition makeDiagnosticComposition;
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
      MakeEncounterResource makeEncounterResource,
      MakeDiagnosticComposition makeDiagnosticComposition) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeObservationResource = makeObservationResource;
    this.makeDiagnosticLabResource = makeDiagnosticLabResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeDiagnosticComposition = makeDiagnosticComposition;
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
          makeDiagnosticComposition.makeCompositionResource(
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
              .setSystem(BundleUrlIdentifier.WRAPPER_URL)
              .setValue(diagnosticReportRequest.getCareContextReference()));

      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(BundleResourceIdentifier.COMPOSITION + "/" + composition.getId())
              .setResource(composition));
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
              .setResource(patient));
      for (Practitioner practitioner : practitionerList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
                .setResource(practitioner));
      }
      if (Objects.nonNull(organization)) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
                .setResource(organization));
      }
      if (Objects.nonNull(encounter)) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId())
                .setResource(encounter));
      }
      for (DiagnosticReport diagnosticReport : diagnosticReportList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.DIAGNOSTIC_REPORT + "/" + diagnosticReport.getId())
                .setResource(diagnosticReport));
      }
      for (Observation observation : diagnosticObservationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.OBSERVATION + "/" + observation.getId())
                .setResource(observation));
      }
      for (DocumentReference documentReference : documentReferenceList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.DOCUMENT_REFERENCE + "/" + documentReference.getId())
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
}
