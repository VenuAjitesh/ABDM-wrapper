/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.requests.helpers;

import com.nha.abdm.wrapper.hiu.hrp.consent.requests.ConsentCareContexts;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.ConsentHIP;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PatientNotification {
  private ConsentCareContexts careContexts;
  private List<String> hiTypes;
  private PatientId patient;
  private String date;
  private ConsentHIP hip;
}
