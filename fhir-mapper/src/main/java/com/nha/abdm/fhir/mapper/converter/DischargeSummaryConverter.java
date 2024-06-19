/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.dto.compositions.MakeDischargeComposition;
import com.nha.abdm.fhir.mapper.dto.resources.*;
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
  private final MakeProcedureResource makeProcedureResource;
  private final MakeDischargeComposition makeDischargeComposition;
  private String docName = "Discharge summary";
  private String docCode = "373942005";

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
      MakeDiagnosticLabResource makeDiagnosticLabResource,
      MakeProcedureResource makeProcedureResource,
      MakeDischargeComposition makeDischargeComposition) {
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
    this.makeProcedureResource = makeProcedureResource;
    this.makeDischargeComposition = makeDischargeComposition;
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
      Encounter encounter =
          makeEncounterResource.getEncounter(
              patient,
              dischargeSummaryRequest.getEncounter() != null
                  ? dischargeSummaryRequest.getEncounter()
                  : null,
              dischargeSummaryRequest.getAuthoredOn());
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
                dischargeSummaryRequest.getAuthoredOn(),
                prescriptionResource,
                organization,
                practitionerList,
                patient));
      }
      List<DiagnosticReport> diagnosticReportList = new ArrayList<>();
      List<Observation> diagnosticObservationList = new ArrayList<>();
      for (DiagnosticResource diagnosticResource : dischargeSummaryRequest.getDiagnostics()) {
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

      List<Procedure> procedureList =
          dischargeSummaryRequest.getProcedures() != null
              ? makeProcedureList(dischargeSummaryRequest, patient)
              : new ArrayList<>();
      List<DocumentReference> documentReferenceList = new ArrayList<>();
      if (Objects.nonNull(dischargeSummaryRequest.getDocuments())) {
        for (DocumentResource documentResource : dischargeSummaryRequest.getDocuments()) {
          documentReferenceList.add(makeDocumentReference(patient, organization, documentResource));
        }
      }

      Composition composition =
          makeDischargeComposition.makeDischargeCompositionResource(
              patient,
              dischargeSummaryRequest.getAuthoredOn(),
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
              documentReferenceList,
              docCode,
              docName);

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

      for (Condition complaint : chiefComplaintList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.CHIEF_COMPLAINTS + "/" + complaint.getId())
                .setResource(complaint));
      }
      for (Observation physicalObservation : physicalObservationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.PHYSICAL_EXAMINATION
                        + "/"
                        + physicalObservation.getId())
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
                .setFullUrl(BundleResourceIdentifier.MEDICAL_HISTORY + "/" + medicalHistory.getId())
                .setResource(medicalHistory));
      }
      for (FamilyMemberHistory familyMemberHistory : familyMemberHistoryList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.FAMILY_HISTORY + "/" + familyMemberHistory.getId())
                .setResource(familyMemberHistory));
      }
      for (MedicationRequest medicationRequest : medicationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.FAMILY_HISTORY + "/" + medicationRequest.getId())
                .setResource(medicationRequest));
      }

      for (DiagnosticReport diagnosticReport : diagnosticReportList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.DIAGNOSTIC_REPORT + "/" + diagnosticReport.getId())
                .setResource(diagnosticReport));
      }

      for (Procedure procedure : procedureList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.PROCEDURE + "/" + procedure.getId())
                .setResource(procedure));
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

  private DocumentReference makeDocumentReference(
      Patient patient, Organization organization, DocumentResource documentResource)
      throws ParseException {
    return makeDocumentResource.getDocument(
        patient, organization, documentResource, docCode, docName);
  }

  private List<Procedure> makeProcedureList(
      DischargeSummaryRequest dischargeSummaryRequest, Patient patient) throws ParseException {
    List<Procedure> procedureList = new ArrayList<>();
    for (ProcedureResource item : dischargeSummaryRequest.getProcedures()) {
      procedureList.add(makeProcedureResource.getProcedure(patient, item));
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
}
