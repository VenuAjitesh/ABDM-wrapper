/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated;

import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.ConfirmResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.InitResponse;

public interface LinkInterface {
  void onInit(InitResponse initResponse);

  void onConfirm(ConfirmResponse confirmResponse);
}
