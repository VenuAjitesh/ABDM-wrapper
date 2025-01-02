/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.discover;

import in.nha.abdm.wrapper.v1.common.responses.GatewayCallbackResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.DiscoverRequest;
import org.springframework.http.ResponseEntity;

public interface DiscoveryInterface {
  ResponseEntity<GatewayCallbackResponse> discover(DiscoverRequest discoverRequest);
}
