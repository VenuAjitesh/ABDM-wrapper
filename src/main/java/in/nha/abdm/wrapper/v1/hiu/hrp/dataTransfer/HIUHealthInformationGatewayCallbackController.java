/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.requests.OnHealthInformationRequest;
import in.nha.abdm.wrapper.v1.common.responses.GatewayCallbackResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HIUHealthInformationGatewayCallbackController {

  @Autowired
  private HealthInformationGatewayCallbackInterface healthInformationGatewayCallbackInterface;

  @PostMapping({"/v0.5/health-information/hiu/on-request"})
  public ResponseEntity<GatewayCallbackResponse> onHealthInformationRequest(
      @RequestBody OnHealthInformationRequest onHealthInformationRequest)
      throws IllegalDataStateException {
    HttpStatus httpStatus =
        healthInformationGatewayCallbackInterface.onHealthInformationRequest(
            onHealthInformationRequest);
    return new ResponseEntity<>(GatewayCallbackResponse.builder().build(), httpStatus);
  }
}
