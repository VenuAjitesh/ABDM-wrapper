/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import com.nha.abdm.fhir.mapper.common.helpers.DateRange;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChiefComplaintResource {
  @NotNull(message = "complaint is mandatory") private String complaint;

  @NotNull(message = "recordedDate is mandatory") private String recordedDate;

  private DateRange dateRange;
}
