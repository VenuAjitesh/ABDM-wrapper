/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.VerifyOTP;
import in.nha.abdm.wrapper.v1.common.responses.FacadeResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.LinkRecordsRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses.LinkOnConfirmResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses.LinkOnInitResponse;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import org.springframework.context.annotation.Profile;

@Profile(WrapperConstants.V1)
public interface HipLinkInterface {
  FacadeResponse hipAuthInit(LinkRecordsRequest linkRecordsRequest);

  void confirmAuthDemographics(LinkOnInitResponse data) throws IllegalDataStateException;

  FacadeResponse confirmAuthOtp(VerifyOTP data) throws IllegalDataStateException;

  void hipAddCareContext(LinkOnConfirmResponse data) throws IllegalDataStateException;
}
