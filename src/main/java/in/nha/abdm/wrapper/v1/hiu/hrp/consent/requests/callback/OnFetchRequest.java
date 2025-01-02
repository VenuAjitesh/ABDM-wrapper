/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback;

import in.nha.abdm.wrapper.v1.common.models.Consent;
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
public class OnFetchRequest {
  private String requestId;
  private String timestamp;
  private Consent consent;
  private ErrorResponse error;
  private RespRequest resp;
}
