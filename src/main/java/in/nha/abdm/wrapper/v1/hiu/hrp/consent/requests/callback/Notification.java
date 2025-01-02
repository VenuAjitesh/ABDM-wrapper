/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
  private String consentRequestId;
  private String status;
  private List<ConsentArtefact> consentArtefacts;
}
