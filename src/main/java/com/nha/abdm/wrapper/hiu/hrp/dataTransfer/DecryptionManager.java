/* (C) 2024 */
package com.nha.abdm.wrapper.hiu.hrp.dataTransfer;

import com.nha.abdm.wrapper.common.cipher.CipherKeyManager;
import com.nimbusds.jose.shaded.gson.JsonParser;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import javax.crypto.KeyAgreement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.springframework.stereotype.Component;

@Component
public class DecryptionManager {
  private static final Logger log = LogManager.getLogger(DecryptionManager.class);

  /**
   * When the service restarts the Security and its provider becomes null. It throws exception
   * NoSuchProviderException while decrypting the bundle. Using this constructor we can fetch
   * decrypted data anytime.
   */
  public DecryptionManager() {
    Security.addProvider(new BouncyCastleProvider());
  }

  public String decryptedHealthInformation(
      String hipNonce,
      String hiuNonce,
      String hiuPrivateKey,
      String hipPublicKey,
      String encryptedData)
      throws InvalidCipherTextException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException {
    byte[] xorOfRandom = xorOfRandom(hipNonce, hiuNonce);
    return decrypt(xorOfRandom, hiuPrivateKey, hipPublicKey, encryptedData);
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

  public String decrypt(
      byte[] xorOfRandom, String receiverPrivateKey, String senderPublicKey, String stringToDecrypt)
      throws InvalidCipherTextException,
          NoSuchAlgorithmException,
          InvalidKeySpecException,
          NoSuchProviderException,
          InvalidKeyException {
    String sharedKey =
        doECDH(
            getBytesForBase64String(receiverPrivateKey), getBytesForBase64String(senderPublicKey));

    // Generating iv and HKDF-AES key
    byte[] iv = Arrays.copyOfRange(xorOfRandom, xorOfRandom.length - 12, xorOfRandom.length);
    byte[] aesKey = generateAesKey(xorOfRandom, sharedKey);

    // Perform Decryption
    String decryptedData = "";
    byte[] encryptedBytes = getBytesForBase64String(stringToDecrypt);

    GCMBlockCipher cipher = new GCMBlockCipher(new AESEngine());
    AEADParameters parameters = new AEADParameters(new KeyParameter(aesKey), 128, iv, null);

    cipher.init(false, parameters);
    byte[] plainBytes = new byte[cipher.getOutputSize(encryptedBytes.length)];
    int retLen = cipher.processBytes(encryptedBytes, 0, encryptedBytes.length, plainBytes, 0);
    cipher.doFinal(plainBytes, retLen);

    // removing the spaces and new lines during conversion of decrypted data into String.
    JsonParser jsonParser = new JsonParser();
    decryptedData =
        String.valueOf(jsonParser.parse(new String(plainBytes, StandardCharsets.UTF_8)));
    log.debug("decryptedData :" + decryptedData);
    return decryptedData;
  }

  private String doECDH(byte[] dataPrv, byte[] dataPub)
      throws NoSuchAlgorithmException,
          NoSuchProviderException,
          InvalidKeySpecException,
          InvalidKeyException {
    KeyAgreement ka =
        KeyAgreement.getInstance(CipherKeyManager.ALGORITHM, CipherKeyManager.PROVIDER);
    ka.init(loadPrivateKey(dataPrv));
    ka.doPhase(loadPublicKeyForProjectEKAHIU(dataPub), true);
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

  private PublicKey loadPublicKeyForProjectEKAHIU(byte[] data)
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
    KeyFactory ecKeyFac =
        KeyFactory.getInstance(CipherKeyManager.ALGORITHM, CipherKeyManager.PROVIDER);
    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(data);
    PublicKey publicKey = ecKeyFac.generatePublic(x509EncodedKeySpec);
    return publicKey;
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

  public String getBase64String(byte[] value) {
    return new String(org.bouncycastle.util.encoders.Base64.encode(value));
  }
}
