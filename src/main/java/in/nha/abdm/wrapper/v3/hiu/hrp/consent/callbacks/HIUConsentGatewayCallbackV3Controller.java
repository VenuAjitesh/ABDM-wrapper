/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.consent.callbacks;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.responses.GatewayCallbackResponse;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.NotifyHIURequest;
import in.nha.abdm.wrapper.v3.common.constants.GatewayURL;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests.ConsentOnInitV3Request;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests.HIUConsentOnStatusV3Request;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests.OnFetchV3Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HIUConsentGatewayCallbackV3Controller {

  @Autowired private HIUConsentGatewayCallbackV3Interface gatewayCallback;
  private static final Logger log =
      LogManager.getLogger(HIUConsentGatewayCallbackV3Controller.class);

  @PostMapping(GatewayURL.CONSENT_ON_INIT_PATH)
  public ResponseEntity<GatewayCallbackResponse> onInitConsent(
      @RequestHeader HttpHeaders httpHeaders,
      @RequestBody ConsentOnInitV3Request consentOnInitV3Request)
      throws IllegalDataStateException {
    log.info(httpHeaders.toString());
    HttpStatus httpStatus = gatewayCallback.onInitConsent(consentOnInitV3Request, httpHeaders);
    return new ResponseEntity<>(GatewayCallbackResponse.builder().build(), httpStatus);
  }

  @PostMapping(GatewayURL.CONSENT_ON_STATUS_PATH)
  public ResponseEntity<GatewayCallbackResponse> consentOnStatus(
      @RequestHeader HttpHeaders httpHeaders,
      @RequestBody HIUConsentOnStatusV3Request hiuConsentOnStatusV3Request)
      throws IllegalDataStateException, IllegalDataStateException {
    log.info("hiuConsentOnStatusV3Request: " + hiuConsentOnStatusV3Request);
    HttpStatus httpStatus =
        gatewayCallback.consentOnStatus(hiuConsentOnStatusV3Request, httpHeaders);
    return new ResponseEntity<>(httpStatus);
  }

  @PostMapping(GatewayURL.CONSENT_HIU_NOTIFY_PATH)
  public ResponseEntity<GatewayCallbackResponse> consentNotify(
      @RequestHeader HttpHeaders httpHeaders, @RequestBody NotifyHIURequest notifyHIURequest)
      throws IllegalDataStateException, IllegalDataStateException {
    log.info(httpHeaders);
    gatewayCallback.hiuNotify(notifyHIURequest, httpHeaders);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }

  @PostMapping(GatewayURL.CONSENT_ON_FETCH_PATH)
  public ResponseEntity<GatewayCallbackResponse> onFetchConsent(
      @RequestHeader HttpHeaders httpHeaders, @RequestBody OnFetchV3Request onFetchRequest)
      throws IllegalDataStateException {
    gatewayCallback.consentOnFetch(onFetchRequest, httpHeaders);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
  }
}
