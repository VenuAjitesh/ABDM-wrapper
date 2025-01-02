/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.exceptions;

import com.mongodb.MongoException;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.FacadeResponse;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Profile(WrapperConstants.V1)
public class MongoExceptions {
  @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleIncorrectResultSizeDataAccessException(
      IncorrectResultSizeDataAccessException ex) {

    ErrorResponse errorResponse = new ErrorResponse("1000", ex.getMessage());
    FacadeResponse facadeResponse =
        FacadeResponse.builder()
            .code(1000)
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .error(errorResponse)
            .message("Incorrect number of results returned")
            .build();

    return new ResponseEntity<>(facadeResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MongoException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<Object> handleMongoException(MongoException ex) {

    ErrorResponse errorResponse = new ErrorResponse("1000", ex.getMessage());
    FacadeResponse facadeResponse =
        FacadeResponse.builder()
            .code(1000)
            .httpStatusCode(HttpStatus.INTERNAL_SERVER_ERROR)
            .error(errorResponse)
            .message("MongoDB Exception")
            .build();

    return new ResponseEntity<>(facadeResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(DataAccessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleDataAccessException(DataAccessException ex) {

    ErrorResponse errorResponse = new ErrorResponse("1000", ex.getMessage());
    FacadeResponse facadeResponse =
        FacadeResponse.builder()
            .code(1000)
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .error(errorResponse)
            .message("Data Access Exception")
            .build();

    return new ResponseEntity<>(facadeResponse, HttpStatus.BAD_REQUEST);
  }
}
