/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.helpers;

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
public class PatientCareContextHIType {
  public String referenceNumber;
  public String display;
  public List<CareContext> careContexts;
  public String hiType;
  public int count;
}
