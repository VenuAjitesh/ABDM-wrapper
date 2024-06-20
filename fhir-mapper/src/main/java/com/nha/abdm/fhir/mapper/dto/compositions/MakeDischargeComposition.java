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
public class MakeDischargeComposition {
  public Composition makeDischargeCompositionResource(
      Patient patient,
      String authoredOn,
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
      List<DocumentReference> documentReferenceList,
      String docCode,
      String docName)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    HumanName practitionerName = null;
    Composition composition = new Composition();
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem(BundleUrlIdentifier.SNOMED_URL);
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
    composition.setDateElement(Utils.getFormattedDateTime(authoredOn));
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
            documentReferenceList,
            docCode,
            docName);
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
      List<MedicationRequest> medicationRequestList,
      List<DiagnosticReport> diagnosticReportList,
      List<Condition> medicalHistoryList,
      List<FamilyMemberHistory> familyMemberHistoryList,
      List<Procedure> procedureList,
      List<DocumentReference> documentReferenceList,
      String docCode,
      String docName) {
    List<Composition.SectionComponent> sectionComponentList = new ArrayList<>();
    if (!(chiefComplaintList.isEmpty())) {
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
    if (!(physicalObservationList.isEmpty())) {
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
    if (!(allergieList.isEmpty())) {
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
    if (!medicalHistoryList.isEmpty()) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Past medical history section")
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("1003642006")
                      .setDisplay("Past medical history section")));
      for (Condition medicalHistory : medicalHistoryList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.MEDICAL_HISTORY + "/" + medicalHistory.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (!(familyMemberHistoryList.isEmpty())) {
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
    if (!(medicationRequestList.isEmpty())) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Medication history section")
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("1003606003")
                      .setDisplay("Medication history section")));
      for (MedicationRequest medicationRequest : medicationRequestList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.MEDICATION_REQUEST
                        + BundleResourceIdentifier.FAMILY_HISTORY
                        + "/"
                        + medicationRequest.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (!(diagnosticReportList.isEmpty())) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Diagnostic studies report")
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("721981007")
                      .setDisplay("Diagnostic studies report")));
      for (DiagnosticReport diagnosticReport : diagnosticReportList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(
                    BundleResourceIdentifier.DIAGNOSTIC_REPORT + "/" + diagnosticReport.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (!(procedureList.isEmpty())) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("History of past procedure section")
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode("1003640003")
                      .setDisplay("History of past procedure section")));
      for (Procedure procedure : procedureList) {
        sectionComponent.addEntry(
            new Reference()
                .setReference(BundleResourceIdentifier.PROCEDURE + "/" + procedure.getId()));
      }
      sectionComponentList.add(sectionComponent);
    }
    if (!(documentReferenceList.isEmpty())) {
      Composition.SectionComponent sectionComponent = new Composition.SectionComponent();
      sectionComponent.setCode(
          new CodeableConcept()
              .setText("Document Reference")
              .addCoding(
                  new Coding()
                      .setSystem(BundleUrlIdentifier.SNOMED_URL)
                      .setCode(docCode)
                      .setDisplay(docName)));
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
