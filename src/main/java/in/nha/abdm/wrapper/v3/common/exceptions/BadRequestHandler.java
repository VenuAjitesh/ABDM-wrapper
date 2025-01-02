/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.exceptions;

import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class BadRequestHandler {

  /**
   * The ABDM returns different types of error responses, the getError handles them and returns as
   * Object.
   *
   * @param expectedException
   * @return ErrorV3Response, List<ErrorV3Response> as Object
   */
  public static Object getError(WebClientResponseException expectedException) {
    try {
      List<ErrorV3Response> errorList =
          expectedException.getResponseBodyAs(
              new ParameterizedTypeReference<List<ErrorV3Response>>() {});
      return errorList;
    } catch (Exception e) {
      try {
        ErrorV3Response errorResponse = expectedException.getResponseBodyAs(ErrorV3Response.class);
        return errorResponse;
      } catch (Exception unknownException) {
        throw new RuntimeException("Failed to parse error response", unknownException);
      }
    }
  }
}
