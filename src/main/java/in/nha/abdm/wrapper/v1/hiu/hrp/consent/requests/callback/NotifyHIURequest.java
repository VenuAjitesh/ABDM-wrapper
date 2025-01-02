/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback;

import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyHIURequest {
  private String requestId;
  private String timestamp;
  private Notification notification;
  private ErrorResponse error;
}
