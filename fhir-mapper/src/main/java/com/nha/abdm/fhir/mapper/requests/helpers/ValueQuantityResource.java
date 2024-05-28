/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ValueQuantityResource {
  private String unit;
  private int value;
}
