/* (C) 2024 */
package com.nha.abdm.fhir.mapper.controller;

import ca.uhn.fhir.context.FhirContext;
import com.nha.abdm.fhir.mapper.common.helpers.BundleResponse;
import com.nha.abdm.fhir.mapper.common.helpers.ErrorResponse;
import com.nha.abdm.fhir.mapper.common.helpers.FacadeError;
import com.nha.abdm.fhir.mapper.converter.*;
import com.nha.abdm.fhir.mapper.requests.*;
import jakarta.validation.Valid;
import java.text.ParseException;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v1/bundle")
@Validated
public class BundleController {
  @Autowired ImmunizationConverter immunizationConverter;
  @Autowired PrescriptionConverter prescriptionConverter;
  @Autowired HealthDocumentConverter healthDocumentConverter;
  @Autowired OPConsultationConverter opConsultationConverter;
  @Autowired DiagnosticReportConverter diagnosticReportConverter;
  @Autowired DischargeSummaryConverter dischargeSummaryConverter;
  @Autowired WellnessRecordConverter wellnessRecordConverter;
  FhirContext ctx = FhirContext.forR4();

  @PostMapping("/immunization")
  public Object createImmunizationBundle(
      @Validated @RequestBody ImmunizationRequest immunizationRequest) throws ParseException {
    if (!immunizationRequest.getBundleType().equalsIgnoreCase("Immunization")) {
      return ResponseEntity.badRequest()
          .body(
              FacadeError.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(1000)
                          .message(
                              "Incorrect bundleType: "
                                  + "'"
                                  + immunizationRequest.getBundleType()
                                  + "'"
                                  + " required: Immunization")
                          .build())
                  .build());
    }
    BundleResponse bundleResponse =
        immunizationConverter.makeImmunizationBundle(immunizationRequest);
    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.ok()
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  @PostMapping("/prescription")
  public ResponseEntity<Object> createPrescriptionBundle(
      @Valid @RequestBody PrescriptionRequest prescriptionRequest) throws ParseException {
    if (!prescriptionRequest.getBundleType().equalsIgnoreCase("prescription")) {
      return ResponseEntity.badRequest()
          .body(
              FacadeError.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(1000)
                          .message(
                              "Incorrect bundleType: "
                                  + "'"
                                  + prescriptionRequest.getBundleType()
                                  + "'"
                                  + " required: prescription")
                          .build())
                  .build());
    }
    BundleResponse bundleResponse =
        prescriptionConverter.convertToPrescriptionBundle(prescriptionRequest);
    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.ok()
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  @PostMapping("/op-consultation")
  public ResponseEntity<Object> createOPConsultationBundle(
      @Valid @RequestBody OPConsultationRequest opConsultationRequest) throws ParseException {
    if (Objects.isNull(opConsultationRequest)
        || !opConsultationRequest.getBundleType().equalsIgnoreCase("OPConsultation")) {
      return ResponseEntity.badRequest()
          .body(
              FacadeError.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(1000)
                          .message(
                              "Incorrect bundleType: "
                                  + "'"
                                  + opConsultationRequest.getBundleType()
                                  + "'"
                                  + " required: OPConsultation")
                          .build())
                  .build());
    }
    BundleResponse bundleResponse =
        opConsultationConverter.convertToOPConsultationBundle(opConsultationRequest);
    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.ok()
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  @PostMapping("/health-document")
  public ResponseEntity<Object> createHealthDocumentBundle(
      @Valid @RequestBody HealthDocumentRecord healthDocumentRecord) throws ParseException {
    if (Objects.isNull(healthDocumentRecord)
        || !healthDocumentRecord.getBundleType().equalsIgnoreCase("HealthDocumentRecord")) {
      return ResponseEntity.badRequest()
          .body(
              FacadeError.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(1000)
                          .message(
                              "Incorrect bundleType: "
                                  + "'"
                                  + healthDocumentRecord.getBundleType()
                                  + "'"
                                  + " required: HealthDocumentRecord")
                          .build())
                  .build());
    }
    BundleResponse bundleResponse =
        healthDocumentConverter.convertToHealthDocumentBundle(healthDocumentRecord);
    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.ok()
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  @PostMapping("/diagnostic-report")
  public ResponseEntity<Object> createDiagnosticReportBundle(
      @RequestBody DiagnosticReportRequest diagnosticReportRequest) throws ParseException {
    if (Objects.isNull(diagnosticReportRequest)
        || !diagnosticReportRequest.getBundleType().equalsIgnoreCase("DiagnosticReport")) {
      return ResponseEntity.badRequest()
          .body(
              FacadeError.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(1000)
                          .message(
                              "Incorrect bundleType: "
                                  + "'"
                                  + diagnosticReportRequest.getBundleType()
                                  + "'"
                                  + " required: DiagnosticReport")
                          .build())
                  .build());
    }
    BundleResponse bundleResponse =
        diagnosticReportConverter.convertToDiagnosticBundle(diagnosticReportRequest);
    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .body(FacadeError.builder().error(bundleResponse.getError()));
    return ResponseEntity.ok()
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  @PostMapping("/discharge-summary")
  public ResponseEntity<Object> createDischargeSummaryBundle(
      @Valid @RequestBody DischargeSummaryRequest dischargeSummaryRequest) throws ParseException {
    if (Objects.isNull(dischargeSummaryRequest)
        || !dischargeSummaryRequest.getBundleType().equalsIgnoreCase("discharge-summary")) {
      return ResponseEntity.badRequest()
          .body(
              FacadeError.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(1000)
                          .message(
                              "Incorrect bundleType: "
                                  + "'"
                                  + dischargeSummaryRequest.getBundleType()
                                  + "'"
                                  + " required: discharge-summary")
                          .build())
                  .build());
    }
    BundleResponse bundleResponse =
        dischargeSummaryConverter.convertToDischargeSummary(dischargeSummaryRequest);
    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.ok()
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }

  @PostMapping("/wellness-record")
  public ResponseEntity<Object> createWellnessBundle(
      @Valid @RequestBody WellnessRecordRequest wellnessRecordRequest) throws ParseException {
    if (Objects.isNull(wellnessRecordRequest)
        || !wellnessRecordRequest.getBundleType().equalsIgnoreCase("wellness-record")) {
      return ResponseEntity.badRequest()
          .body(
              FacadeError.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(1000)
                          .message(
                              "Incorrect bundleType: "
                                  + "'"
                                  + wellnessRecordRequest.getBundleType()
                                  + "'"
                                  + " required: wellness-record")
                          .build())
                  .build());
    }
    BundleResponse bundleResponse =
        wellnessRecordConverter.getWellnessBundle(wellnessRecordRequest);
    if (Objects.nonNull(bundleResponse.getError()))
      return ResponseEntity.badRequest()
          .body(FacadeError.builder().error(bundleResponse.getError()).build());
    return ResponseEntity.ok()
        .body(ctx.newJsonParser().encodeResourceToString(bundleResponse.getBundle()));
  }
}
