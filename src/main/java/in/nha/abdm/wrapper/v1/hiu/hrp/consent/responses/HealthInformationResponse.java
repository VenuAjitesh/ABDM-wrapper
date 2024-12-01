/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.responses;

import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.helpers.HealthInformationBundle;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthInformationResponse {
  private RequestStatus status;
  private String error;
  private HttpStatusCode httpStatusCode;
  private List<HealthInformationBundle> decryptedHealthInformationEntries;
}
