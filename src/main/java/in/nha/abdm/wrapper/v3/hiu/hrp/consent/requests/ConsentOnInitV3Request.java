/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests;

import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.OnInitConsentRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentOnInitV3Request {
  private OnInitConsentRequest consentRequest;
  private ErrorResponse error;
  private RespRequest response;
}
