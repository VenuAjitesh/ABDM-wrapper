/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests;

import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.helpers.PatientNotification;
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
