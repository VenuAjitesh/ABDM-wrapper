/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
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
public class RequestStatusV3Response {
  private String requestId;
  private String status;
  private List<ErrorV3Response> errors;
}
