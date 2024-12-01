/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.share;

import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.ProfileShareV3Request;
import org.springframework.http.HttpHeaders;

public interface ProfileShareV3Interface {
  void shareProfile(ProfileShareV3Request profileShare, HttpHeaders headers);
}
