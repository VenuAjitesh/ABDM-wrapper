/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.dataTransfer.encryption;

import com.nha.abdm.wrapper.common.cipher.CipherKeyManager;
import com.nha.abdm.wrapper.common.cipher.Key;
import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.HealthInformationBundleResponse;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.helpers.HealthInformationBundle;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.KeyAgreement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EncryptionService {
  private static final Logger log = LogManager.getLogger(EncryptionService.class);
  @Autowired CipherKeyManager cipherKeyManager;

  /**
   * The encryption algorithm is replicated from "https://github.com/sukreet/fidelius"
   *
   * @param hipHealthInformationRequest has receiver keys used to encrypt the FHIR
   * @param bundleResponse is the response from HIP which has FHIR bundle.
   */
  public EncryptionResponse encrypt(
      HIPHealthInformationRequest hipHealthInformationRequest,
      HealthInformationBundleResponse bundleResponse)
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException,
          InvalidKeySpecException, InvalidKeyException, IllegalDataStateException {
    if (CollectionUtils.isEmpty(bundleResponse.getHealthInformationBundle())) {
      throw new IllegalDataStateException("Bundle response is null");
    }
    Key senderKeys = cipherKeyManager.fetchKeys();
    Key receiverKeys =
        Key.builder()
            .publicKey(
                hipHealthInformationRequest
                    .getHiRequest()
                    .getKeyMaterial()
                    .getDhPublicKey()
                    .getKeyValue())
            .nonce(hipHealthInformationRequest.getHiRequest().getKeyMaterial().getNonce())
            .build();
    byte[] xorOfRandom = xorOfRandom(senderKeys.getNonce(), receiverKeys.getNonce());
    List<HealthInformationBundle> encryptedCareContextsList = new ArrayList<>();
    for (HealthInformationBundle healthInformationBundle :
        bundleResponse.getHealthInformationBundle()) {
      try {
        String encryptedData =
            encrypt(
                xorOfRandom,
                senderKeys.getPrivateKey(),
                receiverKeys.getPublicKey(),
                healthInformationBundle.getBundleContent());
        encryptedCareContextsList.add(
            HealthInformationBundle.builder()
                .careContextReference(healthInformationBundle.getCareContextReference())
                .bundleContent(encryptedData)
                .build());
      } catch (Exception e) {
        log.error(
            "Error encrypting data for care context: "
                + healthInformationBundle.getCareContextReference());
      }
    }
    String keyToShare = getBase64String(getEncodedHIPPublicKey(getKey(senderKeys.getPublicKey())));
    return new EncryptionResponse(encryptedCareContextsList, keyToShare, senderKeys.getNonce());
  }

  private byte[] xorOfRandom(String senderNonce, String receiverNonce) {
    byte[] randomSender = getBytesForBase64String(senderNonce);
    byte[] randomReceiver = getBytesForBase64String(receiverNonce);

    byte[] combinedRandom = new byte[randomSender.length];
    for (int i = 0; i < randomSender.length; i++) {
      combinedRandom[i] = (byte) (randomSender[i] ^ randomReceiver[i % randomReceiver.length]);
    }
    return combinedRandom;
  }

  public byte[] getBytesForBase64String(String value) {
    return org.bouncycastle.util.encoders.Base64.decode(value);
  }

  private String encrypt(
      byte[] xorOfRandom, String senderPrivateKey, String receiverPublicKey, String stringToEncrypt)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException,
          InvalidKeyException {
    // Generating shared secret
    String sharedKey =
        doECDH(
            getBytesForBase64String(senderPrivateKey), getBytesForBase64String(receiverPublicKey));

    // Generating iv and HKDF-AES key
    byte[] iv = Arrays.copyOfRange(xorOfRandom, xorOfRandom.length - 12, xorOfRandom.length);

    byte[] aesKey = generateAesKey(xorOfRandom, sharedKey);
    // Perform Encryption
    String encryptedData = "";
    try {
      byte[] stringBytes = stringToEncrypt.getBytes();

      GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
      AEADParameters parameters = new AEADParameters(new KeyParameter(aesKey), 128, iv, null);

      cipher.init(true, parameters);
      byte[] plainBytes = new byte[cipher.getOutputSize(stringBytes.length)];
      int retLen = cipher.processBytes(stringBytes, 0, stringBytes.length, plainBytes, 0);
      cipher.doFinal(plainBytes, retLen);

      encryptedData = getBase64String(plainBytes);
    } catch (Exception e) {
      log.error(e.getLocalizedMessage());
    }

    log.debug("EncryptedData: " + encryptedData);
    return encryptedData;
  }

  private String doECDH(byte[] dataPrv, byte[] dataPub)
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException,
          InvalidKeyException {
    KeyAgreement ka =
        KeyAgreement.getInstance(CipherKeyManager.ALGORITHM, CipherKeyManager.PROVIDER);
    ka.init(loadPrivateKey(dataPrv));
    ka.doPhase(loadPublicKey(dataPub), true);
    byte[] secret = ka.generateSecret();
    return getBase64String(secret);
  }

  private PrivateKey loadPrivateKey(byte[] data)
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
    X9ECParameters ecP = CustomNamedCurves.getByName(CipherKeyManager.CURVE);
    ECParameterSpec params =
        new ECParameterSpec(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(), ecP.getSeed());
    ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(data), params);
    KeyFactory kf = KeyFactory.getInstance(CipherKeyManager.ALGORITHM, CipherKeyManager.PROVIDER);
    return kf.generatePrivate(privateKeySpec);
  }

  private PublicKey loadPublicKey(byte[] data)
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
    Security.addProvider(new BouncyCastleProvider());
    X9ECParameters ecP = CustomNamedCurves.getByName(CipherKeyManager.CURVE);
    ECParameterSpec ecNamedCurveParameterSpec =
        new ECParameterSpec(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(), ecP.getSeed());

    return KeyFactory.getInstance(CipherKeyManager.ALGORITHM, CipherKeyManager.PROVIDER)
        .generatePublic(
            new ECPublicKeySpec(
                ecNamedCurveParameterSpec.getCurve().decodePoint(data), ecNamedCurveParameterSpec));
  }

  private byte[] generateAesKey(byte[] xorOfRandoms, String sharedKey) {
    byte[] salt = Arrays.copyOfRange(xorOfRandoms, 0, 20);
    HKDFBytesGenerator hkdfBytesGenerator = new HKDFBytesGenerator(new SHA256Digest());
    HKDFParameters hkdfParameters =
        new HKDFParameters(getBytesForBase64String(sharedKey), salt, null);
    hkdfBytesGenerator.init(hkdfParameters);
    byte[] aesKey = new byte[32];
    hkdfBytesGenerator.generateBytes(aesKey, 0, 32);
    return aesKey;
  }

  private String getBase64String(byte[] value) {
    return new String(org.bouncycastle.util.encoders.Base64.encode(value));
  }

  private byte[] getEncodedHIPPublicKey(PublicKey key) {
    ECPublicKey ecKey = (ECPublicKey) key;
    return ecKey.getEncoded();
  }

  private PublicKey getKey(String key)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    byte[] bytesForBase64String = getBytesForBase64String(key);
    return loadPublicKey(bytesForBase64String);
  }
}
