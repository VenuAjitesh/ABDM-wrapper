/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.callbacks;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.requests.OnHealthInformationV3Request;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public interface HealthInformationV3GatewayCallbackInterface {
  HttpStatus onHealthInformationRequest(
      OnHealthInformationV3Request onHealthInformationRequest, HttpHeaders httpHeaders)
      throws IllegalDataStateException;
}
