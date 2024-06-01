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
public class DocumentResource {
  @NotNull(message = "contentType is mandatory") private String contentType;

  @NotNull(message = "type is mandatory") private String type;

  @NotNull(message = "data is mandatory") private String data;
}
