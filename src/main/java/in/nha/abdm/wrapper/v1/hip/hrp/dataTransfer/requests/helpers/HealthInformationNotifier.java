/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HealthInformationNotifier {
  public String type;
  public String id;
}
