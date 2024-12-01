/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.share.requests;

import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnShareV3Request {
  private ProfileV3Acknowledgement acknowledgement;
  private RespRequest response;
  private ErrorResponse error;
}
