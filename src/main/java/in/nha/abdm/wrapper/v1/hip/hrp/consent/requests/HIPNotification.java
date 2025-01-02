/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.consent.requests;

import in.nha.abdm.wrapper.v1.common.models.ConsentDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HIPNotification {
  private String status;
  private String consentId;
  private ConsentDetail consentDetail;
  private String signature;
}
