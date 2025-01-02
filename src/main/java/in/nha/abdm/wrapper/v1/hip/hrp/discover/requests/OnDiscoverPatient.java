/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.discover.requests;

import in.nha.abdm.wrapper.v1.common.models.CareContext;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OnDiscoverPatient {

  private String referenceNumber;
  private String display;
  private List<CareContext> careContexts;
  private List<String> matchedBy;
}
