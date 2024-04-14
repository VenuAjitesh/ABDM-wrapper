/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.requests;

import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.requests.helpers.PatientNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkContextNotify {
  private String requestId;
  private String timestamp;
  private PatientNotification notification;
}
