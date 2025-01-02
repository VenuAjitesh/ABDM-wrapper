/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.callbacks;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.requests.HealthInformationPushRequest;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.HealthInformationV3Interface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v3/transfer")
public class HIUV3HealthInformationController {
  @Autowired HealthInformationV3Interface healthInformationV3Interface;

  @Autowired
  private in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.HIUV3FacadeHealthInformationInterface
      HIUV3FacadeHealthInformationInterface;

  @PostMapping({"/"})
  public ResponseEntity<GenericResponse> healthInformation(
      @RequestBody HealthInformationPushRequest healthInformationPushRequest)
      throws IllegalDataStateException {
    GenericResponse response =
        healthInformationV3Interface.processEncryptedHealthInformation(
            healthInformationPushRequest);
    return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getHttpStatus().value()));
  }
}
