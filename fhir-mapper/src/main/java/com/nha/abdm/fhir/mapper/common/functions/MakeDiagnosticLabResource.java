/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.requests.helpers.DiagnosticResource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeDiagnosticLabResource {
  public DiagnosticReport getDiagnosticReport(
      Patient patient,
      List<Practitioner> practitionerList,
      List<Observation> observationList,
      Encounter encounter,
      DiagnosticResource diagnosticResource) {
    DiagnosticReport diagnosticReport = new DiagnosticReport();
    diagnosticReport.setId(UUID.randomUUID().toString());
    diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.FINAL);
    diagnosticReport.setCode(
        new CodeableConcept()
            .setText(diagnosticResource.getServiceName())
            .addCoding(
                new Coding()
                    .setSystem("http://loinc.org")
                    .setCode("261665006")
                    .setDisplay(diagnosticResource.getServiceName())));
    diagnosticReport.setSubject(new Reference().setReference("Patient/" + patient.getId()));
    if (Objects.nonNull(encounter))
      diagnosticReport.setEncounter(new Reference().setReference("Encounter/" + encounter.getId()));
    for (Practitioner practitioner : practitionerList) {
      diagnosticReport.addPerformer(
          new Reference().setReference("Practitioner/" + practitioner.getId()));
    }
    diagnosticReport.addCategory(
        new CodeableConcept()
            .setText(diagnosticResource.getServiceCategory())
            .addCoding(
                new Coding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("261665006")
                    .setDisplay(diagnosticResource.getServiceCategory())));
    for (Observation observation : observationList)
      diagnosticReport.addResult(
          new Reference().setReference("Observation/" + observation.getId()));
    diagnosticReport.addConclusionCode(
        new CodeableConcept()
            .setText(diagnosticResource.getConclusion())
            .addCoding(
                new Coding()
                    .setSystem("http://snomed.info/sct")
                    .setCode("261665006")
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
