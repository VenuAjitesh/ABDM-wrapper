/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.facade.consent;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.InitConsentRequest;
import in.nha.abdm.wrapper.v3.common.constants.FacadeURL;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.HIUConsentV3Interface;
import in.nha.abdm.wrapper.v3.hiu.hrp.consent.responses.ConsentStatusV3Response;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/v3")
@Validated
public class HIUFacadeConsentV3Controller {
  private final HIUConsentV3Interface hiuV3ConsentV3Interface;

  public HIUFacadeConsentV3Controller(
      HIUConsentV3Interface hiuConsentInterface, HIUConsentV3Interface hiuV3ConsentV3Interface) {
    this.hiuV3ConsentV3Interface = hiuV3ConsentV3Interface;
  }

  /**
   * Initiating the consent request to ABDM.
   *
   * @param initConsentRequest has abhaAddress and consent dateRange and basic requirement for
   *     consent.
   * @return status of request from ABDM.
   */
  @PostMapping(FacadeURL.HIU_CONSENT_INIT_PATH)
  public ResponseEntity<FacadeV3Response> initiateConsentRequest(
      @RequestBody @Valid InitConsentRequest initConsentRequest) throws IllegalDataStateException {
    FacadeV3Response facadeResponse =
        hiuV3ConsentV3Interface.initiateConsentRequest(initConsentRequest);
    return new ResponseEntity<>(facadeResponse, facadeResponse.getHttpStatusCode());
  }

  /**
   * Getting the status of consent from hiu/status and consent/status api
   *
   * @param clientRequestId
   * @return list of consent artifacts with dateRange,expiry and hip details.
   */
  @GetMapping(FacadeURL.HIU_CONSENT_STATUS_PATH)
  public ResponseEntity<ConsentStatusV3Response> consentRequestStatus(
      @PathVariable("requestId") String clientRequestId) throws IllegalDataStateException {
    ConsentStatusV3Response consentStatusV3Response =
        hiuV3ConsentV3Interface.consentRequestStatus(clientRequestId);
    return new ResponseEntity<>(
        consentStatusV3Response, consentStatusV3Response.getHttpStatusCode());
  }
}
