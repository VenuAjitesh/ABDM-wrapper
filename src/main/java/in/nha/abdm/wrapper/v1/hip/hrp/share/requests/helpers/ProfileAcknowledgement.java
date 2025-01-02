/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.share.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProfileAcknowledgement {
  private String status;
  private String healthId;
  private String tokenNumber;
}
