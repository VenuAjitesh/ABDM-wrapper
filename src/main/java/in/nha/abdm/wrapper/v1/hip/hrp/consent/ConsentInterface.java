/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.consent;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPNotifyRequest;

public interface ConsentInterface {
  void hipNotify(HIPNotifyRequest hipNotifyRequest) throws IllegalDataStateException;
}
