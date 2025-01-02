/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.facade.dataTransfer;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v3.common.constants.FacadeURL;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.HIUV3FacadeHealthInformationInterface;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.requests.HIUClientHealthInformationV3Request;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.responses.HealthInformationV3Response;
import jakarta.validation.Valid;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.Objects;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = FacadeURL.HIU_V3_HEALTH_INFORMATION_PATH)
public class HIUFacadeHealthInformationV3Controller {

  @Autowired private HIUV3FacadeHealthInformationInterface hiuV3FacadeHealthInformationInterface;

  /**
   * Initiating the fetch records from HIP
   *
   * @param hiuClientHealthInformationRequest has consentId
   * @return status of request from ABDM
   */
  @PostMapping(FacadeURL.HIU_FETCH_RECORDS_PATH)
  public ResponseEntity<FacadeV3Response> healthInformation(
      @RequestBody @Valid HIUClientHealthInformationV3Request hiuClientHealthInformationRequest)
      throws InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          NoSuchProviderException,
          IllegalDataStateException,
          ParseException {
    FacadeV3Response facadeV3Response =
        hiuV3FacadeHealthInformationInterface.healthInformation(hiuClientHealthInformationRequest);
    return new ResponseEntity<>(facadeV3Response, facadeV3Response.getHttpStatusCode());
  }

  /**
   * Displaying the decrypted FHIR bundles from HIP
   *
   * @param requestId
   * @return HealthInformationResponse has careContexts and their FHIR bundles
   */
  @GetMapping(FacadeURL.HIU_FETCH_RECORDS_STATUS_PATH)
  public ResponseEntity<HealthInformationV3Response> getHealthInformationRequestStatus(
      @PathVariable("requestId") String requestId)
      throws IllegalDataStateException,
          InvalidCipherTextException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException {
    HealthInformationV3Response healthInformationV3Response =
        hiuV3FacadeHealthInformationInterface.getHealthInformation(requestId);
    HttpStatusCode httpStatusCode = healthInformationV3Response.getHttpStatusCode();
    if (Objects.isNull(httpStatusCode)) {
      return new ResponseEntity<>(healthInformationV3Response, HttpStatus.OK);
    }
    return new ResponseEntity<>(
        healthInformationV3Response, healthInformationV3Response.getHttpStatusCode());
  }
}
