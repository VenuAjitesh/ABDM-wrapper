/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.consent.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers.ConsentAcknowledgement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HIPOnNotifyRequest {
  private String requestId;
  private String timestamp;
  private ConsentAcknowledgement acknowledgement;
  private ErrorResponse error;
  private RespRequest resp;
  private RespRequest response;
}
