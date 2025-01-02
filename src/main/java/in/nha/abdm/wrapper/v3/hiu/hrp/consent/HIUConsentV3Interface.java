/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.consent;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.FetchConsentRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.InitConsentRequest;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.requests.ConsentOnNotifyV3Request;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.responses.ConsentStatusV3Response;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.responses.ConsentV3Response;
import org.springframework.http.HttpHeaders;

public interface HIUConsentV3Interface {
  FacadeV3Response initiateConsentRequest(InitConsentRequest initConsentRequest)
      throws IllegalDataStateException;

  ConsentStatusV3Response consentRequestStatus(String clientRequestId)
      throws IllegalDataStateException;

  void hiuOnNotify(ConsentOnNotifyV3Request consentOnNotifyV3Request, HttpHeaders headers);

  ConsentV3Response fetchConsent(
      FetchConsentRequest fetchConsentRequest, RequestLog requestLog, HttpHeaders headers);
}
