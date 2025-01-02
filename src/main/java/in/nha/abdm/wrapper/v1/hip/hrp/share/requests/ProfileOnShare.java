/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.share.requests;

import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.share.requests.helpers.ProfileAcknowledgement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProfileOnShare {
  private String requestId;
  private String timestamp;
  private ProfileAcknowledgement acknowledgement;
  private ErrorResponse error;
  private RespRequest resp;
}
