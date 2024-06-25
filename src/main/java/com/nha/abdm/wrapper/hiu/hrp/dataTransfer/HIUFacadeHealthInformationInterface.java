/* (C) 2024 */
package com.nha.abdm.wrapper.hiu.hrp.dataTransfer;

import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.responses.FacadeResponse;
import com.nha.abdm.wrapper.hiu.hrp.consent.responses.HealthInformationResponse;
import com.nha.abdm.wrapper.hiu.hrp.dataTransfer.requests.HIUClientHealthInformationRequest;
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
