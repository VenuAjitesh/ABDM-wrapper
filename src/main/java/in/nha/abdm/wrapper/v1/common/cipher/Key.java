/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.cipher;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Key {
  private String privateKey;
  private String publicKey;
  private String nonce;
}
