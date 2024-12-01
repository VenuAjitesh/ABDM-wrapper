/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.discover.requests;

import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnDiscoverRequest {

  private String requestId;
  private String timestamp;
  private String transactionId;
  private OnDiscoverPatient patient;
  private RespRequest resp;
}
