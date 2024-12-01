/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthInformationRequestStatus {
  public String transactionId;
  public String sessionStatus;
}
