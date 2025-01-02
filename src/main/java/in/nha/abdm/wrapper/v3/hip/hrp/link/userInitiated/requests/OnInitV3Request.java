/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.requests;

import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.requests.OnInitLink;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnInitV3Request {
  private String transactionId;
  private OnInitLink link;
  private RespRequest response;
  private ErrorResponse error;
}
