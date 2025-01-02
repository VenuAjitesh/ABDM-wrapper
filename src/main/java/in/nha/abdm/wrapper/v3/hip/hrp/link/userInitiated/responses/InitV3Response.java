/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.responses;

import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.helpers.PatientCareContextHIType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitV3Response {
  private String transactionId;
  private String abhaAddress;
  private List<PatientCareContextHIType> patient;
}
