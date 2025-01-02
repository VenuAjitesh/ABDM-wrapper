/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.deepLinking;

import in.nha.abdm.wrapper.v1.common.responses.FacadeResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.deepLinking.requests.DeepLinkingRequest;

public interface DeepLinkingInterface {
  FacadeResponse sendDeepLinkingSms(DeepLinkingRequest deepLinkingRequest);
}
