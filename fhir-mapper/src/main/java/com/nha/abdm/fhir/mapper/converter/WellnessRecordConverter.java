/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.functions.*;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
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
      MakeWellnessObservationResource makeWellnessObservationResource) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeObservationResource = makeObservationResource;
    this.makeWellnessObservationResource = makeWellnessObservationResource;
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
      Encounter encounter = makeEncounterResource.getEncounter(patient, "");
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
          makeWellnessComposition(
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
              .setFullUrl("Encounter/" + encounter.getId())
              .setResource(encounter));
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl("Organisation/" + organization.getId())
              .setResource(organization));

      for (Observation observation : vitalSignsList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("VitalSigns/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : bodyMeasurementList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("BodyMeasurement/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : physicalActivityList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("PhysicalActivity/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : generalAssessmentList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("GeneralAssessment/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : womanHealthList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("WomanHealth/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : lifeStyleList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("LifeStyle/" + observation.getId())
                .setResource(observation));
      }
      for (Observation observation : otherObservationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("OtherObservations/" + observation.getId())
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

  private Composition makeWellnessComposition(
      Patient patient,
      String authoredOn,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Observation> vitalSignsList,
      List<Observation> bodyMeasurementList,
      List<Observation> physicalActivityList,
      List<Observation> generalAssessmentList,
      List<Observation> womanHealthList,
      List<Observation> lifeStyleList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    HumanName practitionerName = null;
    Composition composition = new Composition();
    composition.setStatus(Composition.CompositionStatus.FINAL);
    composition.setType(new CodeableConcept().setText("Wellness Record"));
    composition.setTitle("Wellness Record");
    List<Reference> authorList = new ArrayList<>();
    for (Practitioner practitioner : practitionerList) {
      practitionerName = practitioner.getName().get(0);
      authorList.add(
          new Reference()
              .setReference("Practitioner/" + practitioner.getId())
              .setDisplay(practitionerName != null ? practitionerName.getText() : null));
    }
    composition.setEncounter(new Reference().setReference("Encounter/" + encounter.getId()));
    composition.setCustodian(
        new Reference()
            .setReference("Organisation/" + organization.getId())
            .setDisplay(organization.getName()));
    composition.setAuthor(authorList);
    composition.setSubject(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    composition.setDateElement(new DateTimeType(Utils.getFormattedDateTime(authoredOn)));
    List<Composition.SectionComponent> sectionComponentList =
        makeCompositionSection(
            patient,
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
    if (Objects.nonNull(sectionComponentList))
      for (Composition.SectionComponent sectionComponent : sectionComponentList)
        composition.addSection(sectionComponent);
    Identifier identifier = new Identifier();
    identifier.setSystem("https://ABDM_WRAPPER/document");
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    return composition;
  }

  private List<Composition.SectionComponent> makeCompositionSection(
      Patient patient,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Observation> vitalSignsList,
      List<Observation> bodyMeasurementList,
      List<Observation> physicalActivityList,
      List<Observation> generalAssessmentList,
      List<Observation> womanHealthList,
      List<Observation> lifeStyleList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList) {
    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();
    if (Objects.nonNull(vitalSignsList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle("Vital Signs");
      for (Observation observation : vitalSignsList) {
        sectionComponent.addEntry(
            new Reference().setReference("VitalSigns/" + observation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(bodyMeasurementList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle("Body Measurement");
      for (Observation observation : bodyMeasurementList) {
        sectionComponent.addEntry(
            new Reference().setReference("BodyMeasurement/" + observation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(physicalActivityList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle("Physical Activity");
      for (Observation observation : physicalActivityList) {
        sectionComponent.addEntry(
            new Reference().setReference("PhysicalActivity/" + observation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(generalAssessmentList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle("General Assessment");
      for (Observation observation : generalAssessmentList) {
        sectionComponent.addEntry(
            new Reference().setReference("GeneralAssessment/" + observation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(womanHealthList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle("Women Health");
      for (Observation observation : womanHealthList) {
        sectionComponent.addEntry(
            new Reference().setReference("WomanHealth/" + observation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(lifeStyleList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle("Lifestyle");
      for (Observation observation : lifeStyleList) {
        sectionComponent.addEntry(new Reference().setReference("Lifestyle/" + observation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(otherObservationList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle("Other Observations");
      for (Observation observation : otherObservationList) {
        sectionComponent.addEntry(
            new Reference().setReference("OtherObservations/" + observation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(documentReferenceList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setTitle("Document Reference");
      for (DocumentReference documentReference : documentReferenceList) {
        sectionComponent.addEntry(
            new Reference().setReference("DocumentReference/" + documentReference.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    return sectionComponentList;
  }
}
