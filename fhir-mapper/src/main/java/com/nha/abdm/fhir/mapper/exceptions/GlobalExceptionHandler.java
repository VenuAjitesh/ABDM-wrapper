/* (C) 2024 */
package com.nha.abdm.fhir.mapper.exceptions;

import com.nha.abdm.fhir.mapper.common.helpers.FacadeError;
import com.nha.abdm.fhir.mapper.common.helpers.FieldErrorsResponse;
import com.nha.abdm.fhir.mapper.common.helpers.ValidationErrorResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<FacadeError> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    List<FieldErrorsResponse> fieldErrorResponses = new ArrayList<>();
    for (ObjectError error : ex.getBindingResult().getAllErrors()) {
      if (error instanceof FieldError) {
        FieldError fieldError = (FieldError) error;
        fieldErrorResponses.add(
            new FieldErrorsResponse(fieldError.getField(), fieldError.getDefaultMessage()));
      }
    }

    ValidationErrorResponse errorResponse = new ValidationErrorResponse(1000, fieldErrorResponses);
    return new ResponseEntity<>(
        FacadeError.builder().validationErrors(errorResponse).build(), HttpStatus.BAD_REQUEST);
  }
}
