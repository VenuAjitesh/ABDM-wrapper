/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.discover.requests;

import in.nha.abdm.wrapper.v1.common.models.RespRequest;
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
public class OnDiscoverV3Request {
  private String transactionId;
  private List<PatientCareContextHIType> patient;
  private List<String> matchedBy;
  private RespRequest response;
}
