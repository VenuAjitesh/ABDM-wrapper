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
public class HealthInformationDhPublicKey {

  private String expiry;
  private String parameters;
  private String keyValue;
}
