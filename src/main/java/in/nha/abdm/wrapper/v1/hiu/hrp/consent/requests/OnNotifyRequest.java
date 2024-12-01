/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnNotifyRequest {
  private String requestId;
  private String timestamp;
  private List<ConsentAcknowledgement> acknowledgment;
  private ErrorResponse error;
  private RespRequest resp;
}
