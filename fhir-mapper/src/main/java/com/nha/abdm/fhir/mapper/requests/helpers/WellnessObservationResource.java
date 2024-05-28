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
public class WellnessObservationResource {
  @NotNull(message = "observation is mandatory") private String observation;

  //  @NotNull(message = "result is mandatory")
  private String result;

  private ValueQuantityResource valueQuantity;
}
