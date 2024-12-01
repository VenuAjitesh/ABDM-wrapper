/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests;

import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.helpers.LinkCredential;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkConfirmRequest {
  private String requestId;
  private String timestamp;
  private String transactionId;
  private LinkCredential credential;
}
