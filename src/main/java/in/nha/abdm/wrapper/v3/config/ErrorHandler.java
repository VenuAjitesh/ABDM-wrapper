/* (C) 2024 */
package in.nha.abdm.wrapper.v3.config;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import java.util.ArrayList;
import java.util.List;

public class ErrorHandler {
  /**
   * Whatever the errors be, getErrors method checks the instance of Object and returns an
   * List<ErrorV3Response>
   *
   * @return list of errors
   */
  public static List<ErrorV3Response> getErrors(Object error) {
    List<ErrorV3Response> errorList = new ArrayList<>();

    if (error instanceof ErrorV3Response) {
      errorList.add((ErrorV3Response) error);
    } else if (error instanceof ErrorResponse) {
      ErrorV3Response errorV3Response =
          ErrorV3Response.builder().error((ErrorResponse) error).build();
      errorList.add(errorV3Response);
    } else if (error instanceof List) {
      errorList.addAll((List<ErrorV3Response>) error);
    } else if (error instanceof String) {
      ErrorResponse errorResponse =
          ErrorResponse.builder()
              .code(GatewayConstants.ERROR_CODE)
              .message(error.toString())
              .build();
      errorList.add(ErrorV3Response.builder().error(errorResponse).build());
    }
    return errorList;
  }
}
