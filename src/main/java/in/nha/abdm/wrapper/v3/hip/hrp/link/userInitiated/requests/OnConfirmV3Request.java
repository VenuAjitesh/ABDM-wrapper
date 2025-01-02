/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.helpers.PatientCareContextHIType;
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
public class OnConfirmV3Request {
  private List<PatientCareContextHIType> patient;
  private RespRequest response;
  private ErrorResponse error;
}
