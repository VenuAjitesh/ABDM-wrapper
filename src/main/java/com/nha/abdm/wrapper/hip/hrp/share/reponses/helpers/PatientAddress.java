/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.share.reponses.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PatientAddress {
  private String line;
  private String district;
  private String state;
  private String pincode;
}
