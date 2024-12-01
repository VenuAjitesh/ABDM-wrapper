/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.responses;

import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.InitConsentRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentStatusResponse {
  private RequestStatus status;
  private Object error;
  private HttpStatusCode httpStatusCode;
  private InitConsentRequest initConsentRequest;
  private FacadeConsentDetails consentDetails;
}
