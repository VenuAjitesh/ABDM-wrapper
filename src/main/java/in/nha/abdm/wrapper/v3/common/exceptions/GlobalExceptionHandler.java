/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.config.ErrorHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@ControllerAdvice
@Profile(WrapperConstants.V3)
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * If there is any attribute is missing this method handles the MethodArgumentNotValidException
   * and returns the appropriate response
   *
   * @param ex
   * @return FacadeV3Response
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<FacadeV3Response> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    List<ErrorV3Response> fieldErrorResponses = new ArrayList<>();

    // Check for validation errors and collect them
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              log.error(error.toString());
              if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                fieldErrorResponses.add(
                    new ErrorV3Response(
                        ErrorResponse.builder()
                            .code(fieldError.getField())
                            .message(fieldError.getDefaultMessage())
                            .build()));
              } else {

                fieldErrorResponses.add(
                    new ErrorV3Response(
                        ErrorResponse.builder()
                            .code(error.getObjectName()) // Class-level validation errors
                            .message(error.getDefaultMessage())
                            .build()));
              }
            });

    FacadeV3Response facadeResponse =
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .errors(fieldErrorResponses)
            .message("Validation errors")
            .build();

    return new ResponseEntity<>(facadeResponse, HttpStatus.BAD_REQUEST);
  }

  /**
   * If there is any attribute is missing this method handles the MethodArgumentNotValidException
   * and returns the appropriate response
   *
   * @param ex
   * @return FacadeV3Response
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<FacadeV3Response> handleValidationExceptionsList(
      ConstraintViolationException ex) {
    List<ErrorV3Response> fieldErrorResponses = new ArrayList<>();
    for (ConstraintViolation error : ex.getConstraintViolations()) {
      fieldErrorResponses.add(
          new ErrorV3Response(
              ErrorResponse.builder()
                  .code(error.getPropertyPath().toString())
                  .message(error.getMessage())
                  .build()));
    }
    return new ResponseEntity<>(
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .errors(fieldErrorResponses)
            .message("Validation errors")
            .build(),
        HttpStatus.BAD_REQUEST);
  }

  /**
   * While communicating with external resources, if there are unavailable responding with HTTP 503
   * error
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(ConnectException.class)
  public ResponseEntity<FacadeV3Response> handleConnectException(ConnectException ex) {
    List<ErrorV3Response> errorV3Responses = new ArrayList<>();
    errorV3Responses.add(
        ErrorV3Response.builder()
            .error(
                ErrorResponse.builder()
                    .code("Wrapper-1001")
                    .message(
                        "HIP is currently unreachable for sharing patient details. Please try again later.")
                    .build())
            .build());

    return new ResponseEntity<>(
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.SERVICE_UNAVAILABLE)
            .message("Patient details not found")
            .errors(errorV3Responses)
            .build(),
        HttpStatus.SERVICE_UNAVAILABLE);
  }

  /**
   * If the network is down then the dns wont be resolved, so responding with appropriate response.
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(UnknownHostException.class)
  public ResponseEntity<FacadeV3Response> handleConnectException(UnknownHostException ex) {
    List<ErrorV3Response> errorV3Responses = new ArrayList<>();
    errorV3Responses.add(
        ErrorV3Response.builder()
            .error(
                ErrorResponse.builder()
                    .code("Wrapper-1001")
                    .message(ex.getLocalizedMessage())
                    .build())
            .build());

    return new ResponseEntity<>(
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.SERVICE_UNAVAILABLE)
            .message("Unable to resolve the host. Please check your network connection.")
            .errors(errorV3Responses)
            .build(),
        HttpStatus.SERVICE_UNAVAILABLE);
  }

  /**
   * Handling the Request TimedOut Exception
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(SocketException.class)
  public ResponseEntity<FacadeV3Response> handleConnectException(SocketException ex) {
    List<ErrorV3Response> errorV3Responses = new ArrayList<>();
    errorV3Responses.add(
        ErrorV3Response.builder()
            .error(
                ErrorResponse.builder()
                    .code("Wrapper-1001")
                    .message(ex.getLocalizedMessage())
                    .build())
            .build());

    return new ResponseEntity<>(
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.REQUEST_TIMEOUT)
            .message("The request timed out. Please try again later.")
            .errors(errorV3Responses)
            .build(),
        HttpStatus.REQUEST_TIMEOUT);
  }

  /**
   * If in the request url, path parameter or query parameter is missing then this method handles
   * that exception
   *
   * @param ex
   * @return
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<FacadeV3Response> handleMissingParams(
      MissingServletRequestParameterException ex) {
    String name = ex.getParameterName();
    String message = String.format("The required parameter '%s' is missing", name);
    return new ResponseEntity<>(
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .errors(
                ErrorHandler.getErrors(
                    ErrorV3Response.builder()
                        .error(
                            ErrorResponse.builder().code("Wrapper-1001").message(message).build())
                        .build()))
            .build(),
        HttpStatus.BAD_REQUEST);
  }

  /**
   * Handles invalid argument exception
   *
   * @param ex
   * @return
   */
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(IllegalDataStateException.class)
  public ResponseEntity<FacadeV3Response> handleIllegalArgumentException(
      IllegalDataStateException ex) {

    ErrorV3Response errorResponse =
        ErrorV3Response.builder()
            .error(ErrorResponse.builder().code("Wrapper-1001").message(ex.getMessage()).build())
            .build();

    FacadeV3Response response =
        FacadeV3Response.builder()
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .errors(Collections.singletonList(errorResponse))
            .message("Invalid arguments provided.")
            .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  /** Convert JsonProcessingException exceptions thrown by Facade controller to API error */
  @ExceptionHandler(JsonProcessingException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  private FacadeV3Response handleJsonProcessingException(JsonProcessingException ex) {
    return FacadeV3Response.builder()
        .errors(ErrorHandler.getErrors(ex.getMessage()))
        .message("JSON Processing exception")
        .httpStatusCode(HttpStatus.BAD_REQUEST)
        .build();
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ResponseBody
  private FacadeV3Response handleHandlerMethodValidationException(
      HandlerMethodValidationException ex) {
    List<ErrorV3Response> errorResponses = new ArrayList<>();

    ex.getAllValidationResults()
        .forEach(
            validationError -> {
              String fieldName =
                  validationError.getMethodParameter().getParameterName()
                      + "["
                      + validationError.getMethodParameter().getParameterIndex()
                      + "]";

              validationError
                  .getResolvableErrors()
                  .forEach(
                      resolvableError -> {
                        String errorMessage = resolvableError.getDefaultMessage();
                        errorResponses.add(
                            new ErrorV3Response(new ErrorResponse(fieldName, errorMessage)));
                      });
            });

    if (errorResponses.isEmpty()) {
      errorResponses.add(
          new ErrorV3Response(new ErrorResponse("1000", "Unknown validation error")));
    }

    return FacadeV3Response.builder()
        .httpStatusCode(HttpStatus.BAD_REQUEST)
        .errors(errorResponses)
        .message("Validation errors")
        .build();
  }
}
