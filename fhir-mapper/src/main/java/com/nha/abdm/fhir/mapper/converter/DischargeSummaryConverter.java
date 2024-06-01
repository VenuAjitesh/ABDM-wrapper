/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import ca.uhn.fhir.context.FhirContext;
import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.functions.*;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.requests.DischargeSummaryRequest;
import com.nha.abdm.fhir.mapper.requests.helpers.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class DischargeSummaryConverter {
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;

  private final MakePatientResource makePatientResource;

  private final MakePractitionerResource makePractitionerResource;
  private final MakeConditionResource makeConditionResource;
  private final MakeObservationResource makeObservationResource;
  private final MakeServiceRequestResource makeServiceRequestResource;
  private final MakeAllergyToleranceResource makeAllergyToleranceResource;
  private final MakeFamilyMemberResource makeFamilyMemberResource;
  private final MakeDocumentResource makeDocumentResource;
  private final MakeEncounterResource makeEncounterResource;
  private final MakeMedicationRequestResource makeMedicationRequestResource;
  private final MakeDiagnosticLabResource makeDiagnosticLabResource;
  private String docName = "Discharge summary";
  private String docCode = "373942005";

  FhirContext ctx = FhirContext.forR4();

  public DischargeSummaryConverter(
      MakeOrganisationResource makeOrganisationResource,
      MakeBundleMetaResource makeBundleMetaResource,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeConditionResource makeConditionResource,
      MakeObservationResource makeObservationResource,
      MakeServiceRequestResource makeServiceRequestResource,
      MakeAllergyToleranceResource makeAllergyToleranceResource,
      MakeFamilyMemberResource makeFamilyMemberResource,
      MakeDocumentResource makeDocumentResource,
      MakeEncounterResource makeEncounterResource,
      MakeMedicationRequestResource makeMedicationRequestResource,
      MakeDiagnosticLabResource makeDiagnosticLabResource) {
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeConditionResource = makeConditionResource;
    this.makeObservationResource = makeObservationResource;
    this.makeServiceRequestResource = makeServiceRequestResource;
    this.makeAllergyToleranceResource = makeAllergyToleranceResource;
    this.makeFamilyMemberResource = makeFamilyMemberResource;
    this.makeDocumentResource = makeDocumentResource;
    this.makeEncounterResource = makeEncounterResource;
    this.makeMedicationRequestResource = makeMedicationRequestResource;
    this.makeDiagnosticLabResource = makeDiagnosticLabResource;
  }

  public BundleResponse convertToDischargeSummary(DischargeSummaryRequest dischargeSummaryRequest)
      throws ParseException {
    try {
      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
      Organization organization =
          makeOrganisationResource.getOrganization(dischargeSummaryRequest.getOrganisation());
      Patient patient = makePatientResource.getPatient(dischargeSummaryRequest.getPatient());
      List<Practitioner> practitionerList =
          Optional.ofNullable(dischargeSummaryRequest.getPractitioners())
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
      List<Condition> chiefComplaintList =
          dischargeSummaryRequest.getChiefComplaints() != null
              ? makeCheifComplaintsList(dischargeSummaryRequest, patient)
              : new ArrayList<>();
      List<Observation> physicalObservationList =
          dischargeSummaryRequest.getPhysicalExaminations() != null
              ? makePhysicalObservations(dischargeSummaryRequest, patient, practitionerList)
              : new ArrayList<>();
      List<AllergyIntolerance> allergieList =
          dischargeSummaryRequest.getAllergies() != null
              ? makeAllergiesList(patient, practitionerList, dischargeSummaryRequest)
              : new ArrayList<>();
      List<Condition> medicalHistoryList =
          dischargeSummaryRequest.getMedicalHistories() != null
              ? makeMedicalHistoryList(dischargeSummaryRequest, patient)
              : new ArrayList<>();
      List<FamilyMemberHistory> familyMemberHistoryList =
          dischargeSummaryRequest.getFamilyHistories() != null
              ? makeFamilyMemberHistory(patient, dischargeSummaryRequest)
              : new ArrayList<>();
      List<MedicationRequest> medicationList = new ArrayList<>();
      for (PrescriptionResource prescriptionResource : dischargeSummaryRequest.getMedications()) {
        medicationList.add(
            makeMedicationRequestResource.getMedicationResource(
                Utils.getFormattedDate(dischargeSummaryRequest.getAuthoredOn()),
                prescriptionResource,
                organization,
                practitionerList,
                patient));
      }
      List<DiagnosticReport> diagnosticReportList = new ArrayList<>();
      for (DiagnosticResource diagnosticResource : dischargeSummaryRequest.getDiagnostics()) {
        List<Observation> observationList = new ArrayList<>();
        for (ObservationResource observationResource : diagnosticResource.getResult()) {
          Observation observation =
              makeObservationResource.getObservation(
                  patient, practitionerList, observationResource);
          entries.add(
              new Bundle.BundleEntryComponent()
                  .setFullUrl("Observation/" + observation.getId())
                  .setResource(observation));
        }
        diagnosticReportList.add(
            makeDiagnosticLabResource.getDiagnosticReport(
                patient, practitionerList, observationList, encounter, diagnosticResource));
      }

      List<Procedure> procedureList =
          dischargeSummaryRequest.getProcedures() != null
              ? makeProcedureList(dischargeSummaryRequest)
              : new ArrayList<>();
      List<DocumentReference> documentReferenceList = new ArrayList<>();
      if (Objects.nonNull(dischargeSummaryRequest.getDocuments())) {
        for (DocumentResource documentResource : dischargeSummaryRequest.getDocuments()) {
          documentReferenceList.add(makeDocumentReference(patient, organization, documentResource));
        }
      }

      Composition composition =
          makeOPCompositionResource(
              patient,
              encounter,
              practitionerList,
              organization,
              chiefComplaintList,
              physicalObservationList,
              allergieList,
              medicationList,
              diagnosticReportList,
              medicalHistoryList,
              familyMemberHistoryList,
              procedureList,
              documentReferenceList);

      Bundle bundle = new Bundle();
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestamp(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      bundle.setIdentifier(
          new Identifier()
              .setSystem("https://ABDM_WRAPPER/bundle")
              .setValue(dischargeSummaryRequest.getCareContextReference()));
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

      for (Condition complaint : chiefComplaintList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("ChiefComplaints/" + complaint.getId())
                .setResource(complaint));
      }
      for (Observation physicalObservation : physicalObservationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("PhysicalExamination/" + physicalObservation.getId())
                .setResource(physicalObservation));
      }
      for (AllergyIntolerance allergyIntolerance : allergieList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Allergies/" + allergyIntolerance.getId())
                .setResource(allergyIntolerance));
      }
      for (Condition medicalHistory : medicalHistoryList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("MedicalHistory/" + medicalHistory.getId())
                .setResource(medicalHistory));
      }
      for (FamilyMemberHistory familyMemberHistory : familyMemberHistoryList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("FamilyHistory/" + familyMemberHistory.getId())
                .setResource(familyMemberHistory));
      }
      for (MedicationRequest medicationRequest : medicationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("MedicationRequest/" + medicationRequest.getId())
                .setResource(medicationRequest));
      }
      for (DiagnosticReport diagnosticReport : diagnosticReportList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("MedicationRequest/" + diagnosticReport.getId())
                .setResource(diagnosticReport));
      }

      for (Procedure procedure : procedureList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Procedure/" + procedure.getId())
                .setResource(procedure));
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

  private Composition makeOPCompositionResource(
      Patient patient,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationRequestList,
      List<DiagnosticReport> diagnosticReportList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<Procedure> procedureList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    HumanName practitionerName = null;
    Composition composition = new Composition();
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem("https://projecteka.in/sct");
    typeCoding.setCode("373942005");
    typeCoding.setDisplay("Discharge summary");
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle("Discharge summary");
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
    composition.setDateElement(new DateTimeType(Utils.getCurrentTimeStamp()));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    List<Composition.SectionComponent> sectionComponentList =
        makeCompositionSection(
            patient,
            practitionerList,
            organization,
            chiefComplaintList,
            physicalObservationList,
            allergieList,
            medicationRequestList,
            diagnosticReportList,
            medicalHistoryList,
            familyMemberHistoryList,
            procedureList,
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

  private DocumentReference makeDocumentReference(
      Patient patient, Organization organization, DocumentResource documentResource)
      throws ParseException {
    return makeDocumentResource.getDocument(
        patient, organization, documentResource, docCode, docName);
  }

  private List<Procedure> makeProcedureList(DischargeSummaryRequest dischargeSummaryRequest) {
    List<Procedure> procedureList = new ArrayList<>();
    for (ProcedureResource item : dischargeSummaryRequest.getProcedures()) {
      Procedure procedure = new Procedure();
      procedure.setId(UUID.randomUUID().toString());
      procedure.setStatus(Procedure.ProcedureStatus.INPROGRESS);
      procedure.addReasonCode(new CodeableConcept().setText(item.getDetails()));
      procedure.setOutcome(new CodeableConcept().setText(item.getOutcome()));
      procedure.addComplication(new CodeableConcept().setText(item.getCondition()));
      procedureList.add(procedure);
    }
    return procedureList;
  }

  private List<FamilyMemberHistory> makeFamilyMemberHistory(
      Patient patient, DischargeSummaryRequest dischargeSummaryRequest) throws ParseException {
    List<FamilyMemberHistory> familyMemberHistoryList = new ArrayList<>();
    for (FamilyObservationResource item : dischargeSummaryRequest.getFamilyHistories()) {
      familyMemberHistoryList.add(makeFamilyMemberResource.getFamilyHistory(patient, item));
    }
    return familyMemberHistoryList;
  }

  private List<Condition> makeMedicalHistoryList(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) throws ParseException {
    List<Condition> medicalHistoryList = new ArrayList<>();
    for (ChiefComplaintResource item : dischargeSummaryRequest.getMedicalHistories()) {
      medicalHistoryList.add(
          makeConditionResource.getCondition(
              item.getComplaint(), patient, item.getRecordedDate(), item.getDateRange()));
    }
    return medicalHistoryList;
  }

  private List<AllergyIntolerance> makeAllergiesList(
      Patient patient,
      List<Practitioner> practitionerList,
      DischargeSummaryRequest dischargeSummaryRequest)
      throws ParseException {
    List<AllergyIntolerance> allergyIntoleranceList = new ArrayList<>();
    for (String item : dischargeSummaryRequest.getAllergies()) {
      allergyIntoleranceList.add(
          makeAllergyToleranceResource.getAllergy(
              patient, practitionerList, item, dischargeSummaryRequest.getAuthoredOn()));
    }
    return allergyIntoleranceList;
  }

  private List<Observation> makePhysicalObservations(
      DischargeSummaryRequest dischargeSummaryRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    List<Observation> observationList = new ArrayList<>();
    for (ObservationResource item : dischargeSummaryRequest.getPhysicalExaminations()) {
      observationList.add(makeObservationResource.getObservation(patient, practitionerList, item));
    }
    return observationList;
  }

  private List<Condition> makeCheifComplaintsList(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) throws ParseException {
    List<Condition> conditionList = new ArrayList<>();
    for (ChiefComplaintResource item : dischargeSummaryRequest.getChiefComplaints()) {
      conditionList.add(
          makeConditionResource.getCondition(
              item.getComplaint(), patient, item.getRecordedDate(), item.getDateRange()));
    }
    return conditionList;
  }

  private List<Composition.SectionComponent> makeCompositionSection(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationRequestList,
      List<DiagnosticReport> diagnosticReportList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<Procedure> procedureList,
      List<DocumentReference> documentReferenceList) {
    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();
    if (Objects.nonNull(chiefComplaintList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Chief Complaints")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("422843007")
                      .setDisplay("Chief complaint section")));
      for (Condition chiefComplaint : chiefComplaintList) {
        sectionComponent.addEntry(
            new Reference().setReference("ChiefComplaints/" + chiefComplaint.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(physicalObservationList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Physical Examination")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("425044008")
                      .setDisplay("Physical exam section")));
      for (Observation physicalObservation : physicalObservationList) {
        sectionComponent.addEntry(
            new Reference().setReference("PhysicalExamination/" + physicalObservation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(allergieList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Allergy Section")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("722446000")
                      .setDisplay("Allergy record")));
      for (AllergyIntolerance allergyIntolerance : allergieList) {
        sectionComponent.addEntry(
            new Reference().setReference("AllergyIntolerance/" + allergyIntolerance.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(medicalHistoryList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Past medical history section")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("1003642006")
                      .setDisplay("Past medical history section")));
      for (Condition medicalHistory : medicalHistoryList) {
        sectionComponent.addEntry(
            new Reference().setReference("MedicalHistory/" + medicalHistory.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(familyMemberHistoryList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Family History")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("422432008")
                      .setDisplay("Family history section")));
      for (FamilyMemberHistory familyMemberHistory : familyMemberHistoryList) {
        sectionComponent.addEntry(
            new Reference().setReference("FamilyHistory/" + familyMemberHistory.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(medicationRequestList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Medication history section")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("1003606003")
                      .setDisplay("Medication history section")));
      for (MedicationRequest medicationRequest : medicationRequestList) {
        sectionComponent.addEntry(
            new Reference().setReference("MedicationRequest/" + medicationRequest.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(diagnosticReportList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Diagnostic studies report")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("721981007")
                      .setDisplay("Diagnostic studies report")));
      for (DiagnosticReport diagnosticReport : diagnosticReportList) {
        sectionComponent.addEntry(
            new Reference().setReference("DiagnosticReport/" + diagnosticReport.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(procedureList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("History of past procedure section")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("1003640003")
                      .setDisplay("History of past procedure section")));
      for (Procedure procedure : procedureList) {
        sectionComponent.addEntry(new Reference().setReference("Procedure/" + procedure.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(documentReferenceList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Document Reference")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode(docCode)
                      .setDisplay(docName)));
      for (DocumentReference documentReferenceItem : documentReferenceList) {
        sectionComponent.addEntry(
            new Reference().setReference("DocumentReference/" + documentReferenceItem.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }

    return sectionComponentList;
  }
}
