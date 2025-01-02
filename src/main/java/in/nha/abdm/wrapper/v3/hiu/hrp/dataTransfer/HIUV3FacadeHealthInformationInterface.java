/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.requests.HIUClientHealthInformationV3Request;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.responses.HealthInformationV3Response;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import org.bouncycastle.crypto.InvalidCipherTextException;

public interface HIUV3FacadeHealthInformationInterface {
  FacadeV3Response healthInformation(
      HIUClientHealthInformationV3Request hiuClientHealthInformationRequest)
      throws InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          NoSuchProviderException,
          IllegalDataStateException,
          ParseException;

  HealthInformationV3Response getHealthInformation(String requestId)
      throws IllegalDataStateException,
          InvalidCipherTextException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException;
}
