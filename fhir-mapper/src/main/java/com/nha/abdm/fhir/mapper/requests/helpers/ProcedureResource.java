/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProcedureResource {
  @NotNull(message = "date of the procedure is mandatory") private String date;

  @Pattern(regexp = "COMPLETED|INPROGRESS")
  private String status;

  @NotNull(message = "procedureReason is mandatory") private String procedureReason;

  private String outcome;

  @NotNull(message = "procedureName is mandatory") private String procedureName;
}
