/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.discover.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CareContextRequest {
  private String abhaAddress;
  private String hipId;
}
