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
public class ProfileShareProfile {
  private PatientV3Details patient;
}
