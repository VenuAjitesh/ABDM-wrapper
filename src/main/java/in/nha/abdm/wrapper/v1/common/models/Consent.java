/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consent {
  private String status;
  private String lastUpdatedOn;
  private String grantedOn;
  private String revokedOn;
  private ConsentDetail consentDetail;
  private String signature;
}
