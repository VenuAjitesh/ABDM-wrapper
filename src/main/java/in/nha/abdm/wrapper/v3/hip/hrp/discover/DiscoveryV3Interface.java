/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.discover;

import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.DiscoverRequest;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public interface DiscoveryV3Interface {
  ResponseEntity<GenericV3Response> discover(DiscoverRequest discoverRequest, HttpHeaders headers);
}
