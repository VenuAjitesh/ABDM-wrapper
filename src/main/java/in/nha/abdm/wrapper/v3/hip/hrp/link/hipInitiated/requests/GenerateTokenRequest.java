/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTokenRequest {
  public String abhaAddress;
  public String name;
  public String gender;
  public int yearOfBirth;
}
