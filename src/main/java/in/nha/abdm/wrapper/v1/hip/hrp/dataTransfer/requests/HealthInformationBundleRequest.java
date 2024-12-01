/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests;

import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentCareContexts;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HealthInformationBundleRequest {
  private String hipId;
  private List<ConsentCareContexts> careContextsWithPatientReferences;
}
