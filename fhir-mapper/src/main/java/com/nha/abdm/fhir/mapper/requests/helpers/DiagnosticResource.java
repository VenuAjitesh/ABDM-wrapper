/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticResource {
  private String serviceName;
  private String serviceCategory;
  private List<ObservationResource> result;
  private String conclusion;
  private DiagnosticPresentedForm presentedForm;
}
