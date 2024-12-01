/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import org.springframework.http.HttpHeaders;

public interface HIPHealthInformationV3Interface {

  void healthInformation(HIPHealthInformationRequest data, HttpHeaders headers)
      throws IllegalDataStateException,
          InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException;
}
