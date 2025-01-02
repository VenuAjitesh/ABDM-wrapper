/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.requests.OnHealthInformationRequest;
import org.springframework.http.HttpStatus;

public interface HealthInformationGatewayCallbackInterface {
  HttpStatus onHealthInformationRequest(OnHealthInformationRequest onHealthInformationRequest)
      throws IllegalDataStateException;
}
