/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.helpers;

import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.OnDiscoverPatient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LinkTokenAndPatient {
  private String accessToken;
  private OnDiscoverPatient patient;
}
