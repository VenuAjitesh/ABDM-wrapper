/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.discover.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnDiscoverErrorRequest {

  private String requestId;
  private String timestamp;
  private String transactionId;
  private ErrorResponse error;
  private RespRequest resp;
  private RespRequest response;
}
