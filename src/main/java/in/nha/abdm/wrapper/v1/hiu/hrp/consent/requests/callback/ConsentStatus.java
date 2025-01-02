/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.callback;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentStatus {
  /**
   * Added JsonIgnore because while fetching the consentStatus there are two id 1) consentRequestId
   * 2) consentId in consentArtifacts both are represented as ID so the consentRequestId is not
   * required for user, so ignoring the consentRequestId
   */
  @JsonIgnore private String id;

  private String status;
  private List<ConsentArtefact> consentArtefacts;
}
