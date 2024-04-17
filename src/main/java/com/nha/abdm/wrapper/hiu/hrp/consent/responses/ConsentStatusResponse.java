/* (C) 2024 */
package com.nha.abdm.wrapper.hiu.hrp.consent.responses;

import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.InitConsentRequest;
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
  private ErrorResponse error;
  private HttpStatusCode httpStatusCode;
  private InitConsentRequest initConsentRequest;
  private FacadeConsentDetails consentDetails;
}
