/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.share.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileV3Acknowledgement {
  private String status;
  private String abhaAddress;
  private TokenProfile profile;
}
