/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.deepLinking;

import in.nha.abdm.wrapper.v1.hip.hrp.link.deepLinking.requests.DeepLinkingRequest;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;

public interface DeepLinkingV3Interface {
  FacadeV3Response sendDeepLinkingSms(DeepLinkingRequest deepLinkingRequest);
}
