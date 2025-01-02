/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.share.reponses;

import in.nha.abdm.wrapper.v1.hip.hrp.share.reponses.helpers.PatientProfile;
import in.nha.abdm.wrapper.v1.hip.hrp.share.reponses.helpers.ProfileIntent;
import in.nha.abdm.wrapper.v1.hip.hrp.share.reponses.helpers.ProfileLocation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProfileShare {
  private String requestId;
  private String timestamp;
  private ProfileIntent intent;
  private ProfileLocation location;
  private PatientProfile profile;
}
