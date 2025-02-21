/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.helpers;

import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentCareContexts;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentHIP;
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
  private ConsentCareContexts careContext;
  private List<String> hiTypes;
  private PatientId patient;
  private String date;
  private ConsentHIP hip;
}
