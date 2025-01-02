/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.requests.HealthInformationPushRequest;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/transfer")
public class HIUHealthInformationController {
  @Autowired HealthInformationInterface healthInformationInterface;

  @Autowired private HIUFacadeHealthInformationInterface hiuFacadeHealthInformationInterface;

  @PostMapping({"/"})
  public ResponseEntity<GenericResponse> healthInformation(
      @RequestBody HealthInformationPushRequest healthInformationPushRequest)
      throws IllegalDataStateException {
    GenericResponse response =
        healthInformationInterface.processEncryptedHealthInformation(healthInformationPushRequest);
    return new ResponseEntity<>(response, HttpStatusCode.valueOf(response.getHttpStatus().value()));
  }
}
