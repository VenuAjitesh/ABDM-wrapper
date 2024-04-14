/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.link.deepLinking;

import com.nha.abdm.wrapper.common.RequestManager;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.ErrorResponseWrapper;
import com.nha.abdm.wrapper.common.responses.FacadeResponse;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import com.nha.abdm.wrapper.hip.hrp.link.deepLinking.requests.DeepLinkingRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

@Service
public class DeepLinkingService implements DeepLinkingInterface {
  private static final Logger log = LogManager.getLogger(DeepLinkingService.class);
  private final RequestManager requestManager;

  public DeepLinkingService(RequestManager requestManager) {
    this.requestManager = requestManager;
  }

  @Value("${deepLinkingSMSNotifyPath}")
  public String deepLinkingSMSNotifyPath;

  /**
   * Making a post request for sending a sms to patient for deepLinking.
   *
   * @param deepLinkingRequest has hipId and patient mobile number.
   * @return the status of request from ABDM.
   */
  @Override
  public FacadeResponse sendDeepLinkingSms(DeepLinkingRequest deepLinkingRequest) {
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(deepLinkingSMSNotifyPath, deepLinkingRequest);
      log.debug(deepLinkingSMSNotifyPath + " : deepLinkingRequest: " + response.getStatusCode());
      return FacadeResponse.builder()
          .clientRequestId(deepLinkingRequest.getRequestId())
          .httpStatusCode(response.getStatusCode())
          .message(RequestStatus.DEEP_LINKING_SMS_INITIATED.getValue())
          .build();
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      return FacadeResponse.builder()
          .clientRequestId(deepLinkingRequest.getRequestId())
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .message(RequestStatus.DEEP_LINKING_SMS_ERROR.getValue())
          .error(error)
          .build();
    } catch (Exception e) {
      String error =
          deepLinkingSMSNotifyPath
              + " : hipAddCareContext: Error while performing add care contexts: "
              + e.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(e);
      log.error(error);
      return FacadeResponse.builder()
          .clientRequestId(deepLinkingRequest.getRequestId())
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .message(RequestStatus.DEEP_LINKING_SMS_ERROR.getValue())
          .error(ErrorResponse.builder().message(error).code(1000).build())
          .build();
    }
  }
}
