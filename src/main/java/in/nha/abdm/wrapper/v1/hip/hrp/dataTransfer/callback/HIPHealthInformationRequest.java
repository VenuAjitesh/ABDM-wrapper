/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.callback;

import in.nha.abdm.wrapper.v1.common.requests.HealthInformationRequest;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HIPHealthInformationRequest implements Serializable {

  private static final long serialVersionUID = 165269402517398406L;

  private String requestId;
  private String timestamp;
  private String transactionId;
  private HealthInformationRequest hiRequest;
}
