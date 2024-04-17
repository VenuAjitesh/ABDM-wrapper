/* (C) 2024 */
package com.nha.abdm.wrapper.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOTP {
  private String loginHint;
  private String requestId;
  private String authCode;
  private String linkRefNumber;

}
