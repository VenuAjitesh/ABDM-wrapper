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
public class PatientResource {
  @NotNull(message = "name of the patient is mandatory") private String name;

  @NotNull(message = "patientReference of the patient is mandatory") private String patientReference;

  private String gender;
  private String birthDate;
}
