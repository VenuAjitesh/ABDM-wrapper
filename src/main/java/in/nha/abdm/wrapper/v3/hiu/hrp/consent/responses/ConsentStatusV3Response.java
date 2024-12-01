/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.consent.responses;

import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.InitConsentRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.responses.FacadeConsentDetails;
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
public class ConsentStatusV3Response {
  private RequestStatus status;
  private List<ErrorV3Response> errors;
  private HttpStatusCode httpStatusCode;
  private InitConsentRequest initConsentRequest;
  private FacadeConsentDetails consentDetails;
}
