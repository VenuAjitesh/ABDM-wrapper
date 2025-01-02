/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.requests;

import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers.HealthInformationRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthInformationV3OnRequest {
  private HealthInformationRequestStatus hiRequest;
  private ErrorResponse error;
  private RespRequest response;
}
