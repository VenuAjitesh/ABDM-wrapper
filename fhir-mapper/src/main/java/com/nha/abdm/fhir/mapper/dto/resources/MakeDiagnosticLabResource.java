/* (C) 2024 */
package com.nha.abdm.fhir.mapper.dto.resources;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.constants.BundleResourceIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.BundleUrlIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.ResourceProfileIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.database.mongo.services.SnomedService;
import com.nha.abdm.fhir.mapper.requests.helpers.DiagnosticResource;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MakeDiagnosticLabResource {
  @Autowired SnomedService snomedService;

  public DiagnosticReport getDiagnosticReport(
      Patient patient,
      List<Practitioner> practitionerList,
      List<Observation> observationList,
      Encounter encounter,
      DiagnosticResource diagnosticResource)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    DiagnosticReport diagnosticReport = new DiagnosticReport();
    diagnosticReport.setId(UUID.randomUUID().toString());
    diagnosticReport.setMeta(
        new Meta()
            .setLastUpdated(Utils.getCurrentTimeStamp())
            .addProfile(ResourceProfileIdentifier.PROFILE_DIAGNOSTIC_REPORT_LAB));
    diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
    diagnosticReport.setCode(
        new CodeableConcept()
            .setText(diagnosticResource.getServiceName())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.LOINC_URL)
                    .setCode(
                        snomedService.getSnomedDiagnosticCode(diagnosticResource.getServiceName()))
                    .setDisplay(diagnosticResource.getServiceName())));
    diagnosticReport.setSubject(
        new Reference()
            .setReference(BundleResourceIdentifier.PATIENT + "/" + patient.getId())
            .setDisplay(patientName.getText()));
    if (Objects.nonNull(encounter))
      diagnosticReport.setEncounter(new Reference().setReference("/" + encounter.getId()));
    for (Practitioner practitioner : practitionerList) {
      diagnosticReport.addPerformer(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId()));
      diagnosticReport.addResultsInterpreter(
          new Reference()
              .setReference(BundleResourceIdentifier.PRACTITIONER + "/" + practitioner.getId()));
    }
    diagnosticReport.addCategory(
        new CodeableConcept()
            .setText(diagnosticResource.getServiceCategory())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(
                        snomedService.getSnomedDiagnosticCode(
                            diagnosticResource.getServiceCategory()))
                    .setDisplay(diagnosticResource.getServiceCategory())));
    for (Observation observation : observationList) {
      diagnosticReport.addResult(
          new Reference()
              .setReference(BundleResourceIdentifier.OBSERVATION + "/" + observation.getId()));
    }
    diagnosticReport.setIssued(encounter.getPeriod().getStart());
    diagnosticReport.setConclusion(diagnosticResource.getConclusion());
    diagnosticReport.addConclusionCode(
        new CodeableConcept()
            .setText(diagnosticResource.getConclusion())
            .addCoding(
                new Coding()
                    .setSystem(BundleUrlIdentifier.SNOMED_URL)
                    .setCode(SnomedCodeIdentifier.SNOMED_UNKNOWN)
                    .setDisplay(diagnosticResource.getConclusion())));
    if (Objects.nonNull(diagnosticResource.getPresentedForm())) {
      Attachment attachment = new Attachment();
      attachment.setContentType(diagnosticResource.getPresentedForm().getContentType());
      attachment.setData(
          diagnosticResource.getPresentedForm().getData().getBytes(StandardCharsets.UTF_8));
      diagnosticReport.addPresentedForm(attachment);
    }
    return diagnosticReport;
  }
}
