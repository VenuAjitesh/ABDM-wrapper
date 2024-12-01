/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.GatewayCallbackResponse;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.HIUConsentOnStatusRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.NotifyHIURequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.OnFetchRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.OnInitRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HIUConsentGatewayCallbackController {

  @Autowired private ConsentGatewayCallbackInterface gatewayCallback;
  private static final Logger log = LogManager.getLogger(HIUConsentGatewayCallbackController.class);

  @PostMapping({"/v0.5/consent-requests/on-init"})
  public ResponseEntity<GatewayCallbackResponse> onInitConsent(
      @RequestBody OnInitRequest onInitRequest) throws IllegalDataStateException {
    HttpStatus httpStatus = gatewayCallback.onInitConsent(onInitRequest);
    return new ResponseEntity<>(GatewayCallbackResponse.builder().build(), httpStatus);
  }

  @PostMapping({"/v0.5/consent-requests/on-status"})
  public ResponseEntity<GatewayCallbackResponse> consentOnStatus(
      @RequestBody HIUConsentOnStatusRequest hiuConsentOnStatusRequest)
      throws IllegalDataStateException {
    log.info("hiuConsentOnStatusRequest: " + hiuConsentOnStatusRequest);
    HttpStatus httpStatus = gatewayCallback.consentOnStatus(hiuConsentOnStatusRequest);
    return new ResponseEntity<>(httpStatus);
  }

  @PostMapping({"/v0.5/consents/hiu/notify"})
  public ResponseEntity<GatewayCallbackResponse> onInitConsent(
      @RequestBody NotifyHIURequest notifyHIURequest) throws IllegalDataStateException {
    gatewayCallback.hiuNotify(notifyHIURequest);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @PostMapping({"/v0.5/consents/on-fetch"})
  public ResponseEntity<GatewayCallbackResponse> onFetchConsent(
      @RequestBody OnFetchRequest onFetchRequest) throws IllegalDataStateException {
    gatewayCallback.consentOnFetch(onFetchRequest);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @ExceptionHandler(IllegalDataStateException.class)
  private ResponseEntity<GatewayCallbackResponse> handleIllegalDataStateException(
      IllegalDataStateException ex) {
    return new ResponseEntity<>(
        GatewayCallbackResponse.builder()
            .error(ErrorResponse.builder().message(ex.getMessage()).build())
            .build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
