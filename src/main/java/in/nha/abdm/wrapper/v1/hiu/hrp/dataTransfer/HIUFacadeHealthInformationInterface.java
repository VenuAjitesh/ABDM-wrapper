/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.responses.FacadeResponse;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.responses.HealthInformationResponse;
import in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer.requests.HIUClientHealthInformationRequest;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import org.bouncycastle.crypto.InvalidCipherTextException;

public interface HIUFacadeHealthInformationInterface {
  FacadeResponse healthInformation(
      HIUClientHealthInformationRequest hiuClientHealthInformationRequest)
      throws InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          NoSuchProviderException,
          IllegalDataStateException,
          ParseException;

  HealthInformationResponse getHealthInformation(String requestId)
      throws IllegalDataStateException,
          InvalidCipherTextException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException;
}
