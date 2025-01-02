/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.responses.FacadeResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.FetchConsentRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.InitConsentRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.OnNotifyRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.responses.ConsentResponse;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.responses.ConsentStatusResponse;

public interface HIUConsentInterface {
  FacadeResponse initiateConsentRequest(InitConsentRequest initConsentRequest);

  ConsentStatusResponse consentRequestStatus(String clientRequestId)
      throws IllegalDataStateException;

  void hiuOnNotify(OnNotifyRequest onNotifyRequest);

  ConsentResponse fetchConsent(FetchConsentRequest fetchConsentRequest, RequestLog requestLog);
}
