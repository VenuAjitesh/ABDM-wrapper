/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProcedureResource {
  @NotNull(message = "status is mandatory") private String status;

  @NotNull(message = "condition is mandatory") private String condition;

  private String outcome;

  @NotNull(message = "details is mandatory") private String details;
}
