/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  @NotBlank(message = "serviceName is mandatory")
  private String serviceName;

  @NotBlank(message = "serviceCategory is mandatory")
  private String serviceCategory;

  @Valid
  @NotNull(message = "results of the report is mandatory") private List<ObservationResource> result;

  @NotBlank(message = "conclusion is mandatory")
  private String conclusion;

  @Valid private DiagnosticPresentedForm presentedForm;
}
