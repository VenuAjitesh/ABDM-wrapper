/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.discover.requests;

import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.helpers.PatientDemographicDetails;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DiscoverRequest implements Serializable {

  private static final long serialVersionUID = 165269402517398406L;

  public String requestId;

  public String transactionId;

  public String timestamp;

  public ErrorResponse error;

  public PatientDemographicDetails patient;
  public String hipId;
}
