/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import ca.uhn.fhir.context.FhirContext;
import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.functions.*;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
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
  private String docName = "Clinical consultation report";
  private String docCode = "371530004";

  FhirContext ctx = FhirContext.forR4();

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
      MakeProcedureResource makeProcedureResource) {
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
      for (PrescriptionResource prescriptionResource : opConsultationRequest.getMedications()) {
        medicationList.add(
            makeMedicationRequestResource.getMedicationResource(
                opConsultationRequest.getVisitDate(),
                prescriptionResource,
                organization,
                practitionerList,
                patient));
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
          makeOPCompositionResource(
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
      for (ServiceRequest investigation : investigationAdviceList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("InvestigationAdvice/" + investigation.getId())
                .setResource(investigation));
      }
      for (MedicationRequest medicationRequest : medicationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("MedicationRequest/" + medicationRequest.getId())
                .setResource(medicationRequest));
      }
      for (Appointment followUp : followupList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("FollowUp/" + followUp.getId())
                .setResource(followUp));
      }
      for (Procedure procedure : procedureList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Procedure/" + procedure.getId())
                .setResource(procedure));
      }
      for (ServiceRequest referral : referralList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Referral/" + referral.getId())
                .setResource(referral));
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

  private Composition makeOPCompositionResource(
      Patient patient,
      String visitDate,
      Encounter encounter,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<ServiceRequest> investigationAdviceList,
      List<Appointment> followupList,
      List<Procedure> procedureList,
      List<ServiceRequest> referralList,
      List<Observation> otherObservationList,
      List<DocumentReference> documentReferenceList)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    HumanName practitionerName = null;
    Composition composition = new Composition();
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem("http://snomed.info/sct");
    typeCoding.setCode("371530004");
    typeCoding.setDisplay("Clinical consultation report");
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle("Consultation Report");
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
    composition.setDateElement(new DateTimeType(Utils.getFormattedDateTime(visitDate)));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    List<Composition.SectionComponent> sectionComponentList =
        makeCompositionSection(
            patient,
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
                  .setActor(new Reference().setReference("Patient/" + patient.getId()))
                  .setStatus(Appointment.ParticipationStatus.ACCEPTED)));
      appointment.setStart(Utils.getFormattedDateTime(item.getAppointmentTime()));
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

  private List<Composition.SectionComponent> makeCompositionSection(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Condition> chiefComplaintList,
      List<Observation> physicalObservationList,
      List<AllergyIntolerance> allergieList,
      List<MedicationRequest> medicationList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<ServiceRequest> investigationAdviceList,
      List<Appointment> followupList,
      List<Procedure> procedureList,
      List<ServiceRequest> referralList,
      List<Observation> otherObservationList,
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
              .setText("Medical History")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("371529009")
                      .setDisplay("History and physical report")));
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
    if (Objects.nonNull(investigationAdviceList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Investigation Advice")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("721963009")
                      .setDisplay("Order document")));
      for (ServiceRequest investigation : investigationAdviceList) {
        sectionComponent.addEntry(
            new Reference().setReference("InvestigationAdvice/" + investigation.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(medicationList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Medication summary document")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("721912009")
                      .setDisplay("Medication summary document")));
      for (MedicationRequest medication : medicationList) {
        sectionComponent.addEntry(
            new Reference().setReference("MedicationRequest/" + medication.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(followupList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Follow Up")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("390906007")
                      .setDisplay("Follow-up encounter")));
      for (Appointment followUp : followupList) {
        sectionComponent.addEntry(new Reference().setReference("FollowUp/" + followUp.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(procedureList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Procedure")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("371525003")
                      .setDisplay("Clinical procedure report")));
      for (Procedure procedure : procedureList) {
        sectionComponent.addEntry(new Reference().setReference("Procedure/" + procedure.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(referralList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Referral")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("306206005")
                      .setDisplay("Referral to service")));
      for (ServiceRequest referral : referralList) {
        sectionComponent.addEntry(new Reference().setReference("Referral/" + referral.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (Objects.nonNull(otherObservationList)) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Other Observations")
              .addCoding(
                  new Coding()
                      .setSystem("http://snomed.info/sct")
                      .setCode("404684003")
                      .setDisplay("Clinical finding")));
      for (Observation otherObservation : otherObservationList) {
        sectionComponent.addEntry(
            new Reference().setReference("OtherObservations/" + otherObservation.getId()));
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
                      .setCode("371530004")
                      .setDisplay("Clinical consultation report")));
      for (DocumentReference documentReferenceItem : documentReferenceList) {
        sectionComponent.addEntry(
            new Reference().setReference("DocumentReference/" + documentReferenceItem.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }

    return sectionComponentList;
  }
}
