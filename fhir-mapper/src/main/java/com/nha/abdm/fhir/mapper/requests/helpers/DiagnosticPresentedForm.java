/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPresentedForm {
  @NotBlank(message = "presentedForm contentType is mandatory")
  private String contentType;

  @NotBlank(message = "presentedForm data is mandatory")
  private String data;
}
