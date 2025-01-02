/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.share.requests;

import in.nha.abdm.wrapper.v1.hip.hrp.share.reponses.ProfileShare;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ShareProfileRequest {
  private String token;
  private String hipId;
  private ProfileShare profile;
}
