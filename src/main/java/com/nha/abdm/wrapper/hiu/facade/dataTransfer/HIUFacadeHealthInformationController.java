/* (C) 2024 */
package com.nha.abdm.wrapper.hiu.facade.dataTransfer;

import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.FacadeResponse;
import com.nha.abdm.wrapper.hiu.hrp.consent.responses.HealthInformationResponse;
import com.nha.abdm.wrapper.hiu.hrp.dataTransfer.HIUFacadeHealthInformationInterface;
import com.nha.abdm.wrapper.hiu.hrp.dataTransfer.requests.HIUClientHealthInformationRequest;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Objects;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping(path = "/v1/health-information")
public class HIUFacadeHealthInformationController {

  @Autowired private HIUFacadeHealthInformationInterface hiuFacadeHealthInformationInterface;

  /**
   * Initiating the fetch records from HIP
   *
   * @param hiuClientHealthInformationRequest has consentId
   * @return status of request from ABDM
   */
  @PostMapping({"/fetch-records"})
  public ResponseEntity<FacadeResponse> healthInformation(
      @RequestBody HIUClientHealthInformationRequest hiuClientHealthInformationRequest)
      throws InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          NoSuchProviderException,
          IllegalDataStateException,
          ParseException {
    FacadeResponse facadeResponse =
        hiuFacadeHealthInformationInterface.healthInformation(hiuClientHealthInformationRequest);
    return new ResponseEntity<>(facadeResponse, facadeResponse.getHttpStatusCode());
  }

  /**
   * Displaying the decrypted FHIR bundles from HIP
   *
   * @param requestId
   * @return HealthInformationResponse has careContexts and their FHIR bundles
   */
  @GetMapping({"/status/{requestId}"})
  public ResponseEntity<HealthInformationResponse> getHealthInformationRequestStatus(
      @PathVariable("requestId") String requestId)
      throws IllegalDataStateException,
          InvalidCipherTextException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException {
    HealthInformationResponse healthInformationResponse =
        hiuFacadeHealthInformationInterface.getHealthInformation(requestId);
    HttpStatusCode httpStatusCode = healthInformationResponse.getHttpStatusCode();
    if (Objects.isNull(httpStatusCode)) {
      return new ResponseEntity<>(healthInformationResponse, HttpStatus.OK);
    }
    return new ResponseEntity<>(
        healthInformationResponse, healthInformationResponse.getHttpStatusCode());
  }

  @ExceptionHandler({
    IllegalDataStateException.class,
    InvalidAlgorithmParameterException.class,
    NoSuchAlgorithmException.class,
    NoSuchProviderException.class,
    InvalidCipherTextException.class,
    InvalidKeySpecException.class
  })
  private ResponseEntity<FacadeResponse> handleExceptions(IllegalDataStateException ex) {
    return new ResponseEntity<>(
        FacadeResponse.builder()
            .error(ErrorResponse.builder().message(ex.getMessage()).build())
            .build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(HttpClientErrorException.class)
  private ResponseEntity<FacadeResponse> handleHttpClientException(HttpClientErrorException ex) {
    return new ResponseEntity<>(
        FacadeResponse.builder()
            .error(ErrorResponse.builder().message(ex.getMessage()).build())
            .build(),
        ex.getStatusCode());
  }
}
