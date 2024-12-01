/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDemographic {
  private String name;
  private String gender;
  private String dateOfBirth;
}
