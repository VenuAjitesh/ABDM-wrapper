/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests;

import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.helpers.LinkTokenAndPatient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LinkAddCareContext {
  private String requestId;
  private String timestamp;
  private LinkTokenAndPatient link;
}
