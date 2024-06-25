/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.dataTransfer;

import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

public interface HealthInformationInterface {

  void healthInformation(HIPHealthInformationRequest data)
      throws IllegalDataStateException,
          InvalidAlgorithmParameterException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException;
}
