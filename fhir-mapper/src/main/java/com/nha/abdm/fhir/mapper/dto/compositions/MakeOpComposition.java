/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.compositions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleUrlIdentifier;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class MakeOpComposition {
  public Composition makeOPCompositionResource(
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
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
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
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId())
              .setDisplay(practitionerName != null ? practitionerName.getText() : null));
    }
    composition.setEncounter(
        new Reference().setReference(BundleResourceIdentifier.ENCOUNTER + "/" + encounter.getId()));
    composition.setCustodian(
        new Reference()
            .setReference(BundleResourceIdentifier.ORGANISATION + "/" + organization.getId())
            .setDisplay(organization.getName()));
    composition.setAuthor(authorList);
    composition.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    composition.setDateElement(Utils.getFormattedDateTime(visitDate));
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
    identifier.setSystem(BundleUrlIdentifier.WRAPPER_URL);
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    return composition;
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("422843007")
                      .setDisplay("Chief complaint section")));
      for (Condition chiefComplaint : chiefComplaintList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.CHIEF_COMPLAINTS + "/" + chiefComplaint.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("425044008")
                      .setDisplay("Physical exam section")));
      for (Observation physicalObservation : physicalObservationList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.PHYSICAL_EXAMINATION
                        + "/"
                        + physicalObservation.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("722446000")
                      .setDisplay("Allergy record")));
      for (AllergyIntolerance allergyIntolerance : allergieList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.ALLERGY_INTOLERANCE
                        + "/"
                        + allergyIntolerance.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("371529009")
                      .setDisplay("History and physical report")));
      for (Condition medicalHistory : medicalHistoryList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.MEDICAL_HISTORY + "/" + medicalHistory.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("422432008")
                      .setDisplay("Family history section")));
      for (FamilyMemberHistory familyMemberHistory : familyMemberHistoryList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.FAMILY_HISTORY + "/" + familyMemberHistory.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("721963009")
                      .setDisplay("Order document")));
      for (ServiceRequest investigation : investigationAdviceList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.INVESTIGATION_ADVICE + "/" + investigation.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("721912009")
                      .setDisplay("Medication summary document")));
      for (MedicationRequest medication : medicationList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.FAMILY_HISTORY + "/" + medication.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("390906007")
                      .setDisplay("Follow-up encounter")));
      for (Appointment followUp : followupList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.FOLLOW_UP + "/" + followUp.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("371525003")
                      .setDisplay("Clinical procedure report")));
      for (Procedure procedure : procedureList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.PROCEDURE + "/" + procedure.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("306206005")
                      .setDisplay("Referral to service")));
      for (ServiceRequest referral : referralList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.REFERRAL + "/" + referral.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("404684003")
                      .setDisplay("Clinical finding")));
      for (Observation otherObservation : otherObservationList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.OTHER_OBSERVATIONS + "/" + otherObservation.getId()));
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
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("371530004")
                      .setDisplay("Clinical consultation report")));
      for (DocumentReference documentReferenceItem : documentReferenceList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.DOCUMENT_REFERENCE
                        + "/"
                        + documentReferenceItem.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }

    return sectionComponentList;
  }
}
