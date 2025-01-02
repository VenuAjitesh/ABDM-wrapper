/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests;

import in.nha.abdm.wrapper.v1.common.requests.HealthInformationNotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HealthInformationPushNotification {
  public String requestId;
  public String timestamp;
  public HealthInformationNotificationStatus notification;
}
