/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.callbacks;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.responses.GatewayCallbackResponse;
import in.nha.abdm.wrapper.v3.common.constants.GatewayURL;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.requests.OnHealthInformationV3Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HIUV3HealthInformationGatewayCallbackController {

  @Autowired
  private HealthInformationV3GatewayCallbackInterface healthInformationV3GatewayCallbackInterface;

  @PostMapping(GatewayURL.HIU_HEALTH_INFORMATION_ON_REQUEST_PATH)
  public ResponseEntity<GatewayCallbackResponse> onHealthInformationRequest(
      @RequestHeader HttpHeaders httpHeaders,
      @RequestBody OnHealthInformationV3Request onHealthInformationRequest)
      throws IllegalDataStateException {
    HttpStatus httpStatus =
        healthInformationV3GatewayCallbackInterface.onHealthInformationRequest(
            onHealthInformationRequest, httpHeaders);
    return new ResponseEntity<>(GatewayCallbackResponse.builder().build(), httpStatus);
  }
}
