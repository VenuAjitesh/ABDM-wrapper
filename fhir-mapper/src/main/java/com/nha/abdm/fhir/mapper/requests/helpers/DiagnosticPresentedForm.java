/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticPresentedForm {
  private String contentType;
  private String data;
}
