/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer.requests;

import in.nha.abdm.wrapper.v1.common.requests.HealthInformationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HIUGatewayHealthInformationRequest {
  private String requestId;
  private String timestamp;
  private HealthInformationRequest hiRequest;
}
