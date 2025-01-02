/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatusCode;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FacadeV3Response {
  private String clientRequestId;
  private HttpStatusCode httpStatusCode;
  private String message;
  private List<ErrorV3Response> errors;
}
