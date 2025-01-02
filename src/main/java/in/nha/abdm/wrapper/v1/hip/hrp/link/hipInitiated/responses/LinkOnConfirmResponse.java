/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses;

import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses.helpers.LinkAuthData;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LinkOnConfirmResponse implements Serializable {
  private static final long serialVersionUID = 165269402517398406L;
  private String requestId;
  private String timestamp;
  private LinkAuthData auth;
  private ErrorResponse error;
  private RespRequest resp;
}
