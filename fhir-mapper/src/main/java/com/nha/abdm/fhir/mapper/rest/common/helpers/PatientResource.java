/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.common.helpers;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PatientResource {
  @NotBlank(message = "name of the patient is mandatory")
  private String name;

  @NotBlank(message = "patientReference of the patient is mandatory")
  private String patientReference;

  private String gender;
  private String birthDate;
}
