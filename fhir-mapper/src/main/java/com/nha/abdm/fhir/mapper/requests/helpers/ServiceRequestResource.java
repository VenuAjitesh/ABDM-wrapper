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
public class ServiceRequestResource {
  @NotNull(message = "status is mandatory") private String status;

  @NotNull(message = "details of service is mandatory") private String details;

  private String specimen;
}
