/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated;

import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.LinkRecordsV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.responses.OnGenerateTokenResponse;
import org.springframework.http.HttpHeaders;

public interface HIPLinkV3Interface {
  FacadeV3Response addCareContexts(LinkRecordsV3Request linkRecordsV3Request);

  void handleAddCareContexts(OnGenerateTokenResponse onGenerateTokenResponse, HttpHeaders headers);
}
