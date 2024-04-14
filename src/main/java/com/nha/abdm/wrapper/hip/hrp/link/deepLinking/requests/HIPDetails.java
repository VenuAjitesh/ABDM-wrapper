/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.link.deepLinking.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HIPDetails {
  private String name;
  private String id;
}
