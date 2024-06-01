/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPresentedForm {
  @NotNull(message = "presentedForm contentType is mandatory") private String contentType;

  @NotNull(message = "presentedForm data is mandatory") private String data;
}
