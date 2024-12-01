/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.exceptions;

import com.mongodb.MongoException;
import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.config.ErrorHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Profile(WrapperConstants.V3)
public class MongoV3Exceptions {
  /**
   * If the return size is more than expected like return of list of patients but expected one
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleIncorrectResultSizeDataAccessException(
      IncorrectResultSizeDataAccessException ex) {

    ErrorResponse errorResponse = new ErrorResponse(GatewayConstants.ERROR_CODE, ex.getMessage());
    FacadeV3Response facadeResponse =
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .errors(ErrorHandler.getErrors(errorResponse))
            .message("Incorrect number of results returned")
            .build();

    return new ResponseEntity<>(facadeResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * Any Error related to Mongo queries will be handled by this method.
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(MongoException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Object> handleMongoException(MongoException ex) {

    ErrorResponse errorResponse = new ErrorResponse(GatewayConstants.ERROR_CODE, ex.getMessage());
    FacadeV3Response facadeResponse =
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
            .errors(ErrorHandler.getErrors(errorResponse))
            .message("MongoDB Exception")
            .build();

    return new ResponseEntity<>(facadeResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /**
   * @param ex
   * @return
   */
  @ExceptionHandler(DataAccessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleDataAccessException(DataAccessException ex) {

    ErrorResponse errorResponse = new ErrorResponse(GatewayConstants.ERROR_CODE, ex.getMessage());
    FacadeV3Response facadeResponse =
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .errors(ErrorHandler.getErrors(errorResponse))
            .message("Data Access Exception")
            .build();

    return new ResponseEntity<>(facadeResponse, HttpStatus.BAD_REQUEST);
  }
}
