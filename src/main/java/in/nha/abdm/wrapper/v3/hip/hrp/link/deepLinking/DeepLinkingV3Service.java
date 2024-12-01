/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.deepLinking;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hip.hrp.link.deepLinking.requests.DeepLinkingRequest;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.config.ErrorHandler;
import java.util.Collections;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

@Service
public class DeepLinkingV3Service implements DeepLinkingV3Interface {
  private static final Logger log = LogManager.getLogger(DeepLinkingV3Service.class);
  private final RequestV3Manager requestV3Manager;

  public DeepLinkingV3Service(RequestV3Manager requestV3Manager) {
    this.requestV3Manager = requestV3Manager;
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
  public FacadeV3Response sendDeepLinkingSms(DeepLinkingRequest deepLinkingRequest) {
    try {
      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              deepLinkingSMSNotifyPath,
              deepLinkingRequest,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIP_ID,
                  deepLinkingRequest.getNotification().getHip().getId(),
                  UUID.randomUUID().toString()));
      log.debug(deepLinkingSMSNotifyPath + " : deepLinkingRequest: " + response.getStatusCode());
      return FacadeV3Response.builder()
          .clientRequestId(deepLinkingRequest.getRequestId())
          .httpStatusCode(response.getStatusCode())
          .message(RequestStatus.DEEP_LINKING_SMS_INITIATED.getValue())
          .build();
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      return FacadeV3Response.builder()
          .clientRequestId(deepLinkingRequest.getRequestId())
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .message(RequestStatus.DEEP_LINKING_SMS_ERROR.getValue())
          .errors(ErrorHandler.getErrors(error))
          .build();
    } catch (Exception e) {
      String error =
          deepLinkingSMSNotifyPath
              + " : DeepLinking SMS: Error while executing deepLinking SMS: "
              + e.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(e);
      log.error(error);
      return FacadeV3Response.builder()
          .clientRequestId(deepLinkingRequest.getRequestId())
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .message(RequestStatus.DEEP_LINKING_SMS_ERROR.getValue())
          .errors(
              Collections.singletonList(
                  ErrorV3Response.builder()
                      .error(
                          ErrorResponse.builder()
                              .code(GatewayConstants.ERROR_CODE)
                              .message(error)
                              .build())
                      .build()))
          .build();
    }
  }
}
