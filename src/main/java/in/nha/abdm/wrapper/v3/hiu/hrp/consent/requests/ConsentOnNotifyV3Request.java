/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests;

import in.nha.abdm.wrapper.v1.common.models.ConsentAcknowledgement;
import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentOnNotifyV3Request {
  private List<ConsentAcknowledgement> acknowledgement;
  private ErrorResponse error;
  private RespRequest response;
}
