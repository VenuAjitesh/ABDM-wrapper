/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests;

import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.requests.helpers.DiagnosticResource;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DiagnosticReportRequest {
  private String bundleType;
  private String careContextReference;
  private PatientResource patient;
  private List<PractitionerResource> practitioner;
  private OrganisationResource organisation;
  private String encounter;
  private List<DiagnosticResource> diagnostics;
  private List<DocumentResource> document;
}
