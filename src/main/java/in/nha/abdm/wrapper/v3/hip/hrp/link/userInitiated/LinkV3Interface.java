/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated;

import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.ConfirmResponse;
import in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.responses.InitV3Response;
import org.springframework.http.HttpHeaders;

public interface LinkV3Interface {
  void onInit(InitV3Response initResponse, HttpHeaders headers);

  void onConfirm(ConfirmResponse confirmResponse, HttpHeaders headers);
}
