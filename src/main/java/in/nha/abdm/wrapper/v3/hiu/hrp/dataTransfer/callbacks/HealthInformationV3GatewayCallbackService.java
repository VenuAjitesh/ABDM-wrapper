/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.callbacks;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.LogsRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.ConsentCipherMappingService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.requests.OnHealthInformationV3Request;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class HealthInformationV3GatewayCallbackService
    implements HealthInformationV3GatewayCallbackInterface {

  private final RequestLogV3Service requestLogService;
  private final LogsRepo logsRepo;
  private final ConsentCipherMappingService consentCipherMappingService;

  @Autowired
  public HealthInformationV3GatewayCallbackService(
      RequestLogV3Service requestLogService,
      LogsRepo logsRepo,
      ConsentCipherMappingService consentCipherMappingService) {
    this.requestLogService = requestLogService;
    this.logsRepo = logsRepo;
    this.consentCipherMappingService = consentCipherMappingService;
  }

  @Override
  public HttpStatus onHealthInformationRequest(
      OnHealthInformationV3Request onHealthInformationRequest, HttpHeaders httpHeaders)
      throws IllegalDataStateException {
    if (Objects.isNull(onHealthInformationRequest)
        || Objects.isNull(onHealthInformationRequest.getHiRequest())) {
      return HttpStatus.BAD_REQUEST;
    }
    requestLogService.updateTransactionId(
        onHealthInformationRequest.getResponse().getRequestId(),
        onHealthInformationRequest.getHiRequest().getTransactionId());
    RequestLog requestLog =
        logsRepo.findByGatewayRequestId(onHealthInformationRequest.getResponse().getRequestId());
    if (requestLog == null) {
      throw new IllegalDataStateException(
          "Request not found in database for: "
              + onHealthInformationRequest.getResponse().getRequestId());
    }
    String consentId = requestLog.getConsentId();
    if (StringUtils.isEmpty(consentId)) {
      throw new IllegalDataStateException(
          "Consent id not found for transaction id: "
              + onHealthInformationRequest.getHiRequest().getTransactionId());
    }
    consentCipherMappingService.updateTransactionId(
        consentId, onHealthInformationRequest.getHiRequest().getTransactionId());
    return HttpStatus.ACCEPTED;
  }
}
