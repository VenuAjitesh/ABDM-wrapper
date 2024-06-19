/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.dto.compositions.MakeWellnessComposition;
import com.nha.abdm.fhir.mapper.dto.resources.*;
import com.nha.abdm.fhir.mapper.requests.WellnessRecordRequest;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class WellnessRecordConverter {
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeObservationResource makeObservationResource;
  private final MakeWellnessObservationResource makeWellnessObservationResource;
  private final MakeWellnessComposition makeWellnessComposition;
  private String docName = "Document Reference";
  private String docCode = "261665006";

  public WellnessRecordConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeDocumentResource makeDocumentResource,
      MakeEncounterResource makeEncounterResource,
      MakeObservationResource makeObservationResource,
      MakeWellnessObservationResource makeWellnessObservationResource,
      MakeWellnessComposition makeWellnessComposition) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeObservationResource = makeObservationResource;
    this.makeWellnessObservationResource = makeWellnessObservationResource;
    this.makeWellnessComposition = makeWellnessComposition;
  }

  public BundleResponse getWellnessBundle(WellnessRecordRequest wellnessRecordRequest) {
    try {
      Organization organization =
          makeOrganisationResource.getOrganization(wellnessRecordRequest.getOrganisation());
      Patient patient = makePatientResource.getPatient(wellnessRecordRequest.getPatient());
      List<Practitioner> practitionerList =
          Optional.ofNullable(wellnessRecordRequest.getPractitioners())
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
              wellnessRecordRequest.getEncounter() != null
                  ? wellnessRecordRequest.getEncounter()
                  : null,
              wellnessRecordRequest.getAuthoredOn());
      List<Observation> vitalSignsList =
          Optional.ofNullable(wellnessRecordRequest.getVitalSigns())
              .map(
                  vitalSigns ->
                      vitalSigns.stream()
                          .map(
                              vitalSign ->
                                  makeWellnessObservationResource.getObservation(
                                      patient, practitionerList, vitalSign, "vitalSigns"))
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);
      List<Observation> bodyMeasurementList =
          Optional.ofNullable(wellnessRecordRequest.getBodyMeasurements())
              .map(
                  bodyMeasurements ->
                      bodyMeasurements.stream()
                          .map(
                              bodyMeasurement ->
                                  makeWellnessObservationResource.getObservation(
                                      patient,
                                      practitionerList,
                                      bodyMeasurement,
                                      "bodyMeasurement"))
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);
      List<Observation> physicalActivityList =
          Optional.ofNullable(wellnessRecordRequest.getPhysicalActivities())
              .map(
                  physicalActivities ->
                      physicalActivities.stream()
                          .map(
                              physicalActivity ->
                                  makeWellnessObservationResource.getObservation(
                                      patient,
                                      practitionerList,
                                      physicalActivity,
                                      "physicalActivity"))
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);
      List<Observation> generalAssessmentList =
          Optional.ofNullable(wellnessRecordRequest.getGeneralAssessments())
              .map(
                  generalAssessments ->
                      generalAssessments.stream()
                          .map(
                              generalAssessment ->
                                  makeWellnessObservationResource.getObservation(
                                      patient,
                                      practitionerList,
                                      generalAssessment,
                                      "generalAssessment"))
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);
      List<Observation> womanHealthList =
          Optional.ofNullable(wellnessRecordRequest.getWomanHealths())
              .map(
                  womanHealths ->
                      womanHealths.stream()
                          .map(
                              womanHealth ->
                                  makeWellnessObservationResource.getObservation(
                                      patient, practitionerList, womanHealth, "womanHealth"))
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);
      List<Observation> lifeStyleList =
          Optional.ofNullable(wellnessRecordRequest.getLifeStyles())
              .map(
                  lifeStyles ->
                      lifeStyles.stream()
                          .map(
                              lifeStyle ->
                                  makeWellnessObservationResource.getObservation(
                                      patient, practitionerList, lifeStyle, "lifeStyle"))
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);

      List<Observation> otherObservationList =
          Optional.ofNullable(wellnessRecordRequest.getOtherObservations())
              .map(
                  observationResources ->
                      observationResources.stream()
                          .map(
                              otherObservation -> {
                                try {
                                  return makeObservationResource.getObservation(
                                      patient, practitionerList, otherObservation);
                                } catch (ParseException e) {
                                  throw new RuntimeException(e);
                                }
                              })
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);
      List<DocumentReference> documentReferenceList =
          Optional.ofNullable(wellnessRecordRequest.getDocuments())
              .map(
                  documentResources ->
                      documentResources.stream()
                          .map(
                              documentResource -> {
                                try {
                                  return makeDocumentResource.getDocument(
                                      patient, organization, documentResource, docCode, docName);
                                } catch (ParseException e) {
                                  throw new RuntimeException(e);
                                }
                              })
                          .collect(Collectors.toList()))
              .orElseGet(ArrayList::new);

      Composition composition =
          makeWellnessComposition.makeWellnessComposition(
              patient,
              wellnessRecordRequest.getAuthoredOn(),
              encounter,
              practitionerList,
              organization,
              vitalSignsList,
              bodyMeasurementList,
              physicalActivityList,
              generalAssessmentList,
              womanHealthList,
              lifeStyleList,
              otherObservationList,
              documentReferenceList);

      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestamp(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem("https://ABDM_WRAPPER/bundle")
              .setValue(wellnessRecordRequest.getCareContextReference()));
      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
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
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId())
              .setResource(encounter));
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
              .setResource(organization));

      for (Observation observation : vitalSignsList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.VITAL_SIGNS + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : bodyMeasurementList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.BODY_MEASUREMENT + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : physicalActivityList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.PHYSICAL_ACTIVITY + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : generalAssessmentList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.GENERAL_ASSESSMENT + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : womanHealthList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.WOMAN_HEALTH + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : lifeStyleList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.LIFE_STYLE + "/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : otherObservationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.OTHER_OBSERVATIONS + "/" + observation.getId())
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
