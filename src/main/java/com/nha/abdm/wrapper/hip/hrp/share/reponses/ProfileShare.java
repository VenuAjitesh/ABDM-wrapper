/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.share.reponses;

import com.nha.abdm.wrapper.hip.hrp.share.reponses.helpers.PatientProfile;
import com.nha.abdm.wrapper.hip.hrp.share.reponses.helpers.ProfileIntent;
import com.nha.abdm.wrapper.hip.hrp.share.reponses.helpers.ProfileLocation;
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
