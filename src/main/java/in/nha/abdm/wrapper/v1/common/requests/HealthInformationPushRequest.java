/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.requests;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HealthInformationPushRequest {
  private int pageNumber;
  private int pageCount;
  private String transactionId;
  private List<HealthInformationEntry> entries;
  private HealthInformationKeyMaterial keyMaterial;
}
