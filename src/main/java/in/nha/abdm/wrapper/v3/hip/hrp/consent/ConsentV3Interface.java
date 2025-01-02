/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.consent;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPNotifyRequest;
import org.springframework.http.HttpHeaders;

public interface ConsentV3Interface {
  void hipNotify(HIPNotifyRequest hipNotifyRequest, HttpHeaders headers)
      throws IllegalDataStateException;
}
