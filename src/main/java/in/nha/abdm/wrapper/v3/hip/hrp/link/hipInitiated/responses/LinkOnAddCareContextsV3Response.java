/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.responses;

import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkOnAddCareContextsV3Response {
  private String abhaAddress;
  private String status;
  private RespRequest response;
  private ErrorResponse error;
}
