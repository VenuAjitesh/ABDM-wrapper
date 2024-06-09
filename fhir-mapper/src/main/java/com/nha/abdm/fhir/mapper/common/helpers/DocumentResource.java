/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.helpers;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DocumentResource {
  @NotBlank(message = "contentType is mandatory")
  private String contentType;

  @NotBlank(message = "type is mandatory")
  private String type;

  @NotBlank(message = "data is mandatory")
  private String data;
}
