/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.helpers;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PractitionerResource {
  @NotNull(message = "Name of the practitioner is mandatory") private String name;

  private String practitionerId;
}
