/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ImmunizationResource {
  //  @Pattern(
  //      regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
  //      message = "Date must be in the format yyyy-MM-dd")
  @NotEmpty(message = "date of vaccine is mandatory")
  private String date;

  @NotEmpty(message = "vaccineName is mandatory")
  private String vaccineName;

  private String lotNumber;
  private String manufacturer;
  private int doseNumber;
}
