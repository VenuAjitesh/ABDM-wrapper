/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.HIUConsentOnStatusRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.NotifyHIURequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.OnFetchRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback.OnInitRequest;
import org.springframework.http.HttpStatus;

public interface ConsentGatewayCallbackInterface {
  HttpStatus onInitConsent(OnInitRequest onInitRequest) throws IllegalDataStateException;

  HttpStatus consentOnStatus(HIUConsentOnStatusRequest HIUConsentOnStatusRequest)
      throws IllegalDataStateException;

  HttpStatus hiuNotify(NotifyHIURequest notifyHIURequest) throws IllegalDataStateException;

  HttpStatus consentOnFetch(OnFetchRequest onFetchRequest) throws IllegalDataStateException;
}
