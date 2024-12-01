/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.cipher;

import java.security.*;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.springframework.stereotype.Service;

@Service
public class CipherKeyManager {
  private String senderPublicKey;
  private String senderPrivateKey;
  private String senderNonce;
  public static final String ALGORITHM = "ECDH";
  public static final String CURVE = "curve25519";
  public static final String PARAMETERS = "Curve25519/32byte random key";
  public static final String PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

  public Key fetchKeys()
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    if (senderPrivateKey == null && senderPublicKey == null && senderNonce == null) {
      KeyPair keyPair = generateKeyPair();
      senderPrivateKey = getBase64String(getEncodedPrivateKey(keyPair.getPrivate()));
      senderPublicKey = getBase64String(getEncodedPublicKey(keyPair.getPublic()));
      senderNonce = generateRandomKey();
    }
    return new Key(senderPrivateKey, senderPublicKey, senderNonce);
  }

  private KeyPair generateKeyPair()
      throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
    Security.addProvider(new BouncyCastleProvider());
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER);
    X9ECParameters ecParameters = CustomNamedCurves.getByName(CURVE);
    ECParameterSpec ecSpec =
        new ECParameterSpec(
            ecParameters.getCurve(),
            ecParameters.getG(),
            ecParameters.getN(),
            ecParameters.getH(),
            ecParameters.getSeed());

    keyPairGenerator.initialize(ecSpec, new SecureRandom());
    return keyPairGenerator.generateKeyPair();
  }

  private String getBase64String(byte[] value) {
    return new String(org.bouncycastle.util.encoders.Base64.encode(value));
  }

  private byte[] getEncodedPrivateKey(PrivateKey key) {
    ECPrivateKey ecKey = (ECPrivateKey) key;
    return ecKey.getD().toByteArray();
  }

  private byte[] getEncodedPublicKey(PublicKey key) {
    ECPublicKey ecKey = (ECPublicKey) key;
    return ecKey.getQ().getEncoded(false);
  }

  private String generateRandomKey() {
    byte[] salt = new byte[32];
    SecureRandom random = new SecureRandom();
    random.nextBytes(salt);
    return getBase64String(salt);
  }
}
