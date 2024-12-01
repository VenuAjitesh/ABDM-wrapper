/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.consent.callbacks;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.NotifyHIURequest;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests.ConsentOnInitV3Request;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests.HIUConsentOnStatusV3Request;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests.OnFetchV3Request;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public interface HIUConsentGatewayCallbackV3Interface {
  HttpStatus onInitConsent(ConsentOnInitV3Request consentOnInitV3Request, HttpHeaders httpHeaders)
      throws IllegalDataStateException;

  HttpStatus consentOnStatus(
      HIUConsentOnStatusV3Request hiuConsentOnStatusV3Request, HttpHeaders httpHeaders)
      throws IllegalDataStateException, IllegalDataStateException;

  HttpStatus hiuNotify(NotifyHIURequest notifyHIURequest, HttpHeaders httpHeaders)
      throws IllegalDataStateException, IllegalDataStateException;

  HttpStatus consentOnFetch(OnFetchV3Request onFetchRequest, HttpHeaders httpHeaders)
      throws IllegalDataStateException;
}
