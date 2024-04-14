/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.link.deepLinking;

import com.nha.abdm.wrapper.common.responses.FacadeResponse;
import com.nha.abdm.wrapper.hip.hrp.link.deepLinking.requests.DeepLinkingRequest;

public interface DeepLinkingInterface {
  FacadeResponse sendDeepLinkingSms(DeepLinkingRequest deepLinkingRequest);
}
