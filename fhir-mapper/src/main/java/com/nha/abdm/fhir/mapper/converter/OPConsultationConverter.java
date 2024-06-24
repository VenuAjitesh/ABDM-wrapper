/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.dto.compositions.MakeOpComposition;
import com.nha.abdm.fhir.mapper.dto.resources.*;
import com.nha.abdm.fhir.mapper.requests.OPConsultationRequest;
import com.nha.abdm.fhir.mapper.requests.helpers.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class OPConsultationConverter {
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
  private final MakeProcedureResource makeProcedureResource;
  private final MakeOpComposition makeOpComposition;
  private String docName = "Clinical consultation report";
  private String docCode = "371530004";

  public OPConsultationConverter(
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
      MakeProcedureResource makeProcedureResource,
      MakeOpComposition makeOpComposition) {
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
    this.makeProcedureResource = makeProcedureResource;
    this.makeOpComposition = makeOpComposition;
  }

  public BundleResponse convertToOPConsultationBundle(OPConsultationRequest opConsultationRequest)
      throws ParseException {
    try {
      Organization organization =
          makeOrganisationResource.getOrganization(opConsultationRequest.getOrganisation());
      Patient patient = makePatientResource.getPatient(opConsultationRequest.getPatient());
      List<Practitioner> practitionerList =
          Optional.ofNullable(opConsultationRequest.getPractitioners())
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
              opConsultationRequest.getEncounter() != null
                  ? opConsultationRequest.getEncounter()
                  : null,
              opConsultationRequest.getVisitDate());
      List<Condition> chiefComplaintList =
          opConsultationRequest.getChiefComplaints() != null
              ? makeCheifComplaintsList(opConsultationRequest, patient)
              : new ArrayList<>();
      List<Observation> physicalObservationList =
          opConsultationRequest.getPhysicalExaminations() != null
              ? makePhysicalObservations(opConsultationRequest, patient, practitionerList)
              : new ArrayList<>();
      List<AllergyIntolerance> allergieList =
          opConsultationRequest.getAllergies() != null
              ? makeAllergiesList(patient, practitionerList, opConsultationRequest)
              : new ArrayList<>();
      List<Condition> medicalHistoryList =
          opConsultationRequest.getMedicalHistories() != null
              ? makeMedicalHistoryList(opConsultationRequest, patient)
              : new ArrayList<>();
      List<FamilyMemberHistory> familyMemberHistoryList =
          opConsultationRequest.getFamilyHistories() != null
              ? makeFamilyMemberHistory(patient, opConsultationRequest)
              : new ArrayList<>();
      List<ServiceRequest> investigationAdviceList =
          opConsultationRequest.getServiceRequests() != null
              ? makeInvestigationAdviceList(opConsultationRequest, patient, practitionerList)
              : new ArrayList<>();
      HashMap<Medication, MedicationRequest> medicationRequestMap = new HashMap<>();
      List<MedicationRequest> medicationList = new ArrayList<>();
      List<Condition> medicationConditionList = new ArrayList<>();
      for (PrescriptionResource prescriptionResource : opConsultationRequest.getMedications()) {
        Condition medicationCondition =
            prescriptionResource.getReason() != null
                ? makeConditionResource.getCondition(
                    prescriptionResource.getReason(),
                    patient,
                    opConsultationRequest.getVisitDate(),
                    null)
                : null;
        medicationList.add(
            makeMedicationRequestResource.getMedicationResource(
                opConsultationRequest.getVisitDate(),
                prescriptionResource,
                medicationCondition,
                organization,
                practitionerList,
                patient));
        if (medicationCondition != null) {
          medicationConditionList.add(medicationCondition);
        }
      }
      List<Appointment> followupList =
          opConsultationRequest.getFollowups() != null
              ? makeFollowupList(patient, opConsultationRequest)
              : new ArrayList<>();
      List<Procedure> procedureList =
          opConsultationRequest.getProcedures() != null
              ? makeProcedureList(opConsultationRequest, patient)
              : new ArrayList<>();
      List<ServiceRequest> referralList =
          opConsultationRequest.getReferrals() != null
              ? makeReferralList(opConsultationRequest, patient, practitionerList)
              : new ArrayList<>();
      List<Observation> otherObservationList =
          opConsultationRequest.getOtherObservations() != null
              ? makeOtherObservations(patient, practitionerList, opConsultationRequest)
              : new ArrayList<>();
      List<DocumentReference> documentReferenceList = new ArrayList<>();
      if (Objects.nonNull(opConsultationRequest.getDocuments())) {
        for (DocumentResource documentResource : opConsultationRequest.getDocuments()) {
          documentReferenceList.add(makeDocumentReference(patient, organization, documentResource));
        }
      }

      Composition composition =
          makeOpComposition.makeOPCompositionResource(
              patient,
              opConsultationRequest.getVisitDate(),
              encounter,
              practitionerList,
              organization,
              chiefComplaintList,
              physicalObservationList,
              allergieList,
              medicationList,
              medicalHistoryList,
              familyMemberHistoryList,
              investigationAdviceList,
              followupList,
              procedureList,
              referralList,
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
              .setValue(opConsultationRequest.getCareContextReference()));
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
                .setFullUrl(
                    BundleResourceIdentifier.ALLERGY_INTOLERANCE + "/" + allergyIntolerance.getId())
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
      for (ServiceRequest investigation : investigationAdviceList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.INVESTIGATION_ADVICE + "/" + investigation.getId())
                .setResource(investigation));
      }
      for (MedicationRequest medicationRequest : medicationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(
                    BundleResourceIdentifier.MEDICATION_REQUEST + "/" + medicationRequest.getId())
                .setResource(medicationRequest));
      }
      for (Condition medicationCondition : medicationConditionList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.CONDITION + "/" + medicationCondition.getId())
                .setResource(medicationCondition));
      }
      for (Appointment followUp : followupList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.FOLLOW_UP + "/" + followUp.getId())
                .setResource(followUp));
      }
      for (Procedure procedure : procedureList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.PROCEDURE + "/" + procedure.getId())
                .setResource(procedure));
      }
      for (ServiceRequest referral : referralList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl(BundleResourceIdentifier.REFERRAL + "/" + referral.getId())
                .setResource(referral));
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

  private DocumentReference makeDocumentReference(
      Patient patient, Organization organization, DocumentResource documentResource)
      throws ParseException {
    return makeDocumentResource.getDocument(
        patient, organization, documentResource, docCode, docName);
  }

  private List<Observation> makeOtherObservations(
      Patient patient,
      List<Practitioner> practitionerList,
      OPConsultationRequest opConsultationRequest)
      throws ParseException {
    List<Observation> observationList = new ArrayList<>();
    for (ObservationResource item : opConsultationRequest.getOtherObservations()) {
      observationList.add(makeObservationResource.getObservation(patient, practitionerList, item));
    }
    return observationList;
  }

  private List<ServiceRequest> makeReferralList(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    List<ServiceRequest> refferalList = new ArrayList<>();
    for (ServiceRequestResource item : opConsultationRequest.getReferrals()) {
      refferalList.add(
          makeServiceRequestResource.getServiceRequest(
              patient, practitionerList, item, opConsultationRequest.getVisitDate()));
    }
    return refferalList;
  }

  private List<Procedure> makeProcedureList(
      OPConsultationRequest opConsultationRequest, Patient patient) throws ParseException {
    List<Procedure> procedureList = new ArrayList<>();
    for (ProcedureResource item : opConsultationRequest.getProcedures()) {
      procedureList.add(makeProcedureResource.getProcedure(patient, item));
    }
    return procedureList;
  }

  private List<Appointment> makeFollowupList(
      Patient patient, OPConsultationRequest opConsultationRequest) throws ParseException {
    List<Appointment> followupList = new ArrayList<>();
    for (FollowupResource item : opConsultationRequest.getFollowups()) {
      Appointment appointment = new Appointment();
      appointment.setId(UUID.randomUUID().toString());
      appointment.setStatus(Appointment.AppointmentStatus.PROPOSED);
      appointment.setParticipant(
          Collections.singletonList(
              new Appointment.AppointmentParticipantComponent()
                  .setActor(
                      new Reference()
                          .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId()))
                  .setStatus(Appointment.ParticipationStatus.ACCEPTED)));
      appointment.setStart(Utils.getFormattedDateTime(item.getAppointmentTime()).getValue());
      appointment.addReasonCode(new CodeableConcept().setText(item.getReason()));
      appointment.setServiceType(
          Collections.singletonList(new CodeableConcept().setText(item.getServiceType())));
      followupList.add(appointment);
    }
    return followupList;
  }

  private List<ServiceRequest> makeInvestigationAdviceList(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    List<ServiceRequest> investigationList = new ArrayList<>();
    for (ServiceRequestResource item : opConsultationRequest.getServiceRequests()) {
      investigationList.add(
          makeServiceRequestResource.getServiceRequest(
              patient, practitionerList, item, opConsultationRequest.getVisitDate()));
    }
    return investigationList;
  }

  private List<FamilyMemberHistory> makeFamilyMemberHistory(
      Patient patient, OPConsultationRequest opConsultationRequest) throws ParseException {
    List<FamilyMemberHistory> familyMemberHistoryList = new ArrayList<>();
    for (FamilyObservationResource item : opConsultationRequest.getFamilyHistories()) {
      familyMemberHistoryList.add(makeFamilyMemberResource.getFamilyHistory(patient, item));
    }
    return familyMemberHistoryList;
  }

  private List<Condition> makeMedicalHistoryList(
      OPConsultationRequest opConsultationRequest, Patient patient) throws ParseException {
    List<Condition> medicalHistoryList = new ArrayList<>();
    for (ChiefComplaintResource item : opConsultationRequest.getMedicalHistories()) {
      medicalHistoryList.add(
          makeConditionResource.getCondition(
              item.getComplaint(), patient, item.getRecordedDate(), item.getDateRange()));
    }
    return medicalHistoryList;
  }

  private List<AllergyIntolerance> makeAllergiesList(
      Patient patient,
      List<Practitioner> practitionerList,
      OPConsultationRequest opConsultationRequest)
      throws ParseException {
    List<AllergyIntolerance> allergyIntoleranceList = new ArrayList<>();
    for (String item : opConsultationRequest.getAllergies()) {
      allergyIntoleranceList.add(
          makeAllergyToleranceResource.getAllergy(
              patient, practitionerList, item, opConsultationRequest.getVisitDate()));
    }
    return allergyIntoleranceList;
  }

  private List<Observation> makePhysicalObservations(
      OPConsultationRequest opConsultationRequest,
      Patient patient,
      List<Practitioner> practitionerList)
      throws ParseException {
    List<Observation> observationList = new ArrayList<>();
    for (ObservationResource item : opConsultationRequest.getPhysicalExaminations()) {
      observationList.add(makeObservationResource.getObservation(patient, practitionerList, item));
    }
    return observationList;
  }

  private List<Condition> makeCheifComplaintsList(
      OPConsultationRequest opConsultationRequest, Patient patient) throws ParseException {
    List<Condition> conditionList = new ArrayList<>();
    for (ChiefComplaintResource item : opConsultationRequest.getChiefComplaints()) {
      conditionList.add(
          makeConditionResource.getCondition(
              item.getComplaint(), patient, item.getRecordedDate(), item.getDateRange()));
    }
    return conditionList;
  }
}
