/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HealthInformationKeyMaterial {
  private String cryptoAlg;
  private String curve;
  private HealthInformationDhPublicKey dhPublicKey;
  private String nonce;
}
