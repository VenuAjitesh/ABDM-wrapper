/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.requests.HealthInformationPushRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;
import in.nha.abdm.wrapper.v1.common.responses.RequestStatusResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPNotifyRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPOnNotifyRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.LogsRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.DiscoverRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.LinkAddCareContext;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.LinkConfirmRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.LinkRecordsRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses.LinkOnAddCareContextsResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses.LinkOnConfirmResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses.LinkOnInitResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.InitResponse;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RequestLogService<T> {
  @Autowired public LogsRepo logsRepo;
  @Autowired MongoTemplate mongoTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired PatientService patientService;
  private static final Logger log = LogManager.getLogger(RequestLogService.class);

  /**
   * Fetch of patient abhaAddress from requestLogs.
   *
   * @param linkRefNumber identifier for list of careContexts for linking.
   * @return abhaAddress
   */
  public String getPatientId(String linkRefNumber) {
    RequestLog existingRecord = logsRepo.findByLinkRefNumber(linkRefNumber);
    InitResponse data = (InitResponse) existingRecord.getRequestDetails().get("InitResponse");
    return data.getPatient().getId();
  }

  /**
   * Fetch of patientReferenceNumber from requestLogs.
   *
   * @param linkRefNumber identifier for list of careContexts for linking.
   * @return patientReference
   */
  public String getPatientReference(String linkRefNumber) {
    RequestLog existingRecord = logsRepo.findByLinkRefNumber(linkRefNumber);
    InitResponse data = (InitResponse) existingRecord.getRequestDetails().get("InitResponse");
    return data.getPatient().getReferenceNumber();
  }

  /**
   * <B>discovery</B>
   *
   * <p>Adding discoverResponseDump into db.
   *
   * @param discoverRequest Response from ABDM gateway for discovery
   */
  public void setDiscoverResponse(DiscoverRequest discoverRequest) {
    if (Objects.isNull(discoverRequest)) {
      return;
    }
    RequestLog newRecord = new RequestLog();
    newRecord.setClientRequestId(discoverRequest.getRequestId());
    newRecord.setTransactionId(discoverRequest.getTransactionId());
    HashMap<String, Object> map = new HashMap<>();
    map.put("DiscoverResponse", discoverRequest);
    newRecord.setRequestDetails(map);
    mongoTemplate.save(newRecord);
  }

  /**
   * <B>discovery</B>
   *
   * <p>Adding initResponse dump into db.
   *
   * @param initResponse Response from ABDM gateway for linking particular careContexts.
   */
  public void setLinkResponse(InitResponse initResponse, String requestId, String referenceNumber) {
    if (Objects.isNull(initResponse)) {
      return;
    }

    Query query = new Query(Criteria.where("transactionId").is(initResponse.getTransactionId()));
    RequestLog existingRecord = mongoTemplate.findOne(query, RequestLog.class);
    if (existingRecord == null) {
      RequestLog newRecord =
          new RequestLog(
              initResponse.getRequestId(),
              requestId,
              initResponse.getPatient().getId(),
              initResponse.getTransactionId(),
              RequestStatus.USER_INIT_REQUEST_RECEIVED_BY_WRAPPER);
      mongoTemplate.insert(newRecord);
    } else {
      Map<String, Object> map = existingRecord.getRequestDetails();
      map.put("InitResponse", initResponse);
      Update update =
          (new Update())
              .set("clientRequestId", initResponse.getRequestId())
              .set("gatewayRequestId", requestId)
              .set("linkRefNumber", referenceNumber)
              .set(FieldIdentifiers.REQUEST_DETAILS, map);
      mongoTemplate.updateFirst(query, update, RequestLog.class);
    }
  }

  /**
   * Select the careContexts according to the careContexts referenceNumbers of the response
   *
   * @param linkRefNumber identifier for list of careContexts for linking.
   * @return the selected careContexts.
   */
  public List<CareContext> getSelectedCareContexts(String abhaAddress, String linkRefNumber) {
    RequestLog requestLog = logsRepo.findByLinkRefNumber(linkRefNumber);
    log.info("linkRefNum in getSelectedContexts : " + linkRefNumber);
    if (Objects.nonNull(requestLog)) {
      Map<String, Object> requestDetails = requestLog.getRequestDetails();
      if (Objects.nonNull(requestDetails)) {
        InitResponse initResponse =
            (InitResponse) requestDetails.get(FieldIdentifiers.INIT_RESPONSE);
        List<CareContext> selectedCareContexts = initResponse.getPatient().getCareContexts();
        List<CareContext> existingCareContexts =
            patientService.getPatientDetails(abhaAddress).getCareContexts();
        if (existingCareContexts == null) {
          return null;
        }
        Set<String> selectedReferenceNumbers =
            selectedCareContexts.stream()
                .map(CareContext::getReferenceNumber)
                .collect(Collectors.toSet());
        return existingCareContexts.stream()
            .filter(selected -> selectedReferenceNumbers.contains(selected.getReferenceNumber()))
            .collect(Collectors.toList());
      }
    }
    return null;
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Checking the status of hipLinking
   *
   * @param requestId Response from ABDM gateway for discovery.
   * @return status of linking after /on-add-contexts acknowledgment.
   */
  public RequestStatusResponse getStatus(String requestId) throws IllegalDataStateException {
    RequestLog requestLog = logsRepo.findByClientRequestId(requestId);
    if (requestLog != null) {
      if (Objects.nonNull(requestLog.getError())) {
        return RequestStatusResponse.builder()
            .requestId(requestId)
            .status("Error")
            .error(requestLog.getError())
            .build();
      }
      if (Objects.nonNull(requestLog.getStatus())
          && StringUtils.isNotBlank(requestLog.getStatus().getValue())) {
        return RequestStatusResponse.builder()
            .requestId(requestId)
            .status(requestLog.getStatus().getValue())
            .build();
      }
    }
    throw new IllegalDataStateException("Request not found in database for: " + requestId);
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Adds linkRecordsRequest into request-logs collection.
   *
   * @param linkRecordsRequest Request received to facade for hipLinking.
   */
  public void persistHipLinkRequest(
      LinkRecordsRequest linkRecordsRequest, RequestStatus status, String error) {
    if (Objects.isNull(linkRecordsRequest)) {
      return;
    }
    RequestLog requestLog = new RequestLog();
    requestLog.setClientRequestId(linkRecordsRequest.getRequestId());
    requestLog.setGatewayRequestId(linkRecordsRequest.getRequestId());
    requestLog.setStatus(status);
    HashMap<String, Object> map = new HashMap<>();
    map.put(FieldIdentifiers.LINK_RECORDS_REQUEST, linkRecordsRequest);
    requestLog.setRequestDetails(map);
    if (StringUtils.isNotBlank(error)) {
      requestLog.setError(error);
    }
    mongoTemplate.save(requestLog);
  }

  public void updateRequestError() {}

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Adding linkOnInitResponse dump into db.
   *
   * @param linkOnInitResponse Response from ABDM gateway after successful auth/init.
   */
  public void updateHipOnInitResponse(
      LinkOnInitResponse linkOnInitResponse, LinkConfirmRequest linkConfirmRequest) {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID)
                .is(linkOnInitResponse.getResp().getRequestId()));
    RequestLog requestLog = mongoTemplate.findOne(query, RequestLog.class);
    HashMap<String, Object> map = requestLog.getRequestDetails();
    map.put(FieldIdentifiers.HIP_ON_INIT_RESPONSE, linkOnInitResponse);
    if (requestLog != null) {
      Update update =
          (new Update())
              .set(FieldIdentifiers.REQUEST_DETAILS, map)
              .set(FieldIdentifiers.GATEWAY_REQUEST_ID, linkConfirmRequest.getRequestId());
      mongoTemplate.updateFirst(query, update, RequestLog.class);
    }
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Adding linkOnConfirmResponse dump into db.
   *
   * @param linkOnConfirmResponse Response from ABDM gateway for successful auth/on-confirm.
   */
  public void setHipOnConfirmResponse(
      LinkOnConfirmResponse linkOnConfirmResponse, LinkAddCareContext linkAddCareContext) {
    Query query =
        new Query(
            Criteria.where("gatewayRequestId").is(linkOnConfirmResponse.getResp().getRequestId()));
    RequestLog existingRecord = mongoTemplate.findOne(query, RequestLog.class);
    HashMap<String, Object> map = existingRecord.getRequestDetails();
    map.put(FieldIdentifiers.HIP_ON_CONFIRM_RESPONSE, linkOnConfirmResponse);
    if (existingRecord != null) {
      Update update =
          (new Update())
              .set(FieldIdentifiers.REQUEST_DETAILS, map)
              .set("gatewayRequestId", linkAddCareContext.getRequestId());
      mongoTemplate.updateFirst(query, update, RequestLog.class);
    }
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Adding only linkOnInitResponse dump into db if authMode is OTP .
   *
   * @param linkOnInitResponse Response from ABDM gateway for successful auth/init.
   */
  public void setHipOnInitResponseOTP(LinkOnInitResponse linkOnInitResponse) {
    Query query =
        new Query(
            Criteria.where("gatewayRequestId").is(linkOnInitResponse.getResp().getRequestId()));
    RequestLog existingRecord = mongoTemplate.findOne(query, RequestLog.class);
    HashMap<String, Object> map = existingRecord.getRequestDetails();
    map.put(FieldIdentifiers.HIP_ON_INIT_RESPONSE, linkOnInitResponse);
    if (existingRecord != null) {
      Update update = (new Update()).set(FieldIdentifiers.REQUEST_DETAILS, map);
      mongoTemplate.updateFirst(query, update, RequestLog.class);
    }
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Updating gatewayRequestId if authMode is OTP since the dump is already stored in db.
   *
   * @param gateWayRequestId requestId in auth/confirm.
   * @param clientRequestId requestId in auth/on-init.
   */
  public void updateOnInitResponseOTP(String clientRequestId, String gateWayRequestId) {
    Query query = new Query(Criteria.where("clientRequestId").is(clientRequestId));
    RequestLog existingRecord = mongoTemplate.findOne(query, RequestLog.class);
    if (existingRecord != null) {
      Update update = (new Update()).set("gatewayRequestId", gateWayRequestId);
      mongoTemplate.updateFirst(query, update, RequestLog.class);
    }
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Adding linkOnAddCareContextsResponse dump into db.
   *
   * @param linkOnAddCareContextsResponse Acknowledgement from ABDM gateway for HipLinking.
   */
  public void setHipOnAddCareContextResponse(
      LinkOnAddCareContextsResponse linkOnAddCareContextsResponse)
      throws IllegalDataStateException {
    RequestLog requestLog =
        logsRepo.findByGatewayRequestId(linkOnAddCareContextsResponse.getResp().getRequestId());

    if (requestLog == null) {
      throw new IllegalDataStateException(
          "Request not found in database for: "
              + linkOnAddCareContextsResponse.getResp().getRequestId());
    }
    HashMap<String, Object> map = requestLog.getRequestDetails();
    map.put(FieldIdentifiers.HIP_ON_ADD_CARE_CONTEXT_RESPONSE, linkOnAddCareContextsResponse);
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID)
                .is(linkOnAddCareContextsResponse.getResp().getRequestId()));
    Update update = new Update();
    if (linkOnAddCareContextsResponse.getAcknowledgement() == null
        || (Objects.nonNull(linkOnAddCareContextsResponse.getError())
            && StringUtils.isNotBlank(linkOnAddCareContextsResponse.getError().getMessage()))) {
      update.set(FieldIdentifiers.ERROR, linkOnAddCareContextsResponse.getError().getMessage());
    } else {
      update.set(FieldIdentifiers.STATUS, RequestStatus.CARE_CONTEXT_LINKED);
      LinkRecordsRequest linkRecordsRequest =
          (LinkRecordsRequest)
              requestLog.getRequestDetails().get(FieldIdentifiers.LINK_RECORDS_REQUEST);
      patientService.addPatientCareContexts(linkRecordsRequest);
    }
    update.set(FieldIdentifiers.REQUEST_DETAILS, map);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public void updateStatus(String requestId, RequestStatus requestStatus) {
    log.info("GatewayRequestId: " + requestId + "requestStatus: " + requestStatus);
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    Update update = new Update();
    update.set(FieldIdentifiers.STATUS, requestStatus);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public void updateError(String requestId, String message, RequestStatus requestStatus) {
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    Update update = new Update();
    update.set(FieldIdentifiers.ERROR, message);
    update.set(FieldIdentifiers.STATUS, requestStatus);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public void saveRequest(String requestId, RequestStatus status, String error) {
    RequestLog requestLog = new RequestLog();
    requestLog.setClientRequestId(requestId);
    requestLog.setGatewayRequestId(requestId);
    requestLog.setStatus(status);
    if (StringUtils.isNotBlank(error)) {
      requestLog.setError(error);
    }
    mongoTemplate.save(requestLog);
  }

  public <T> void updateConsentResponse(
      String requestId, String identifier, RequestStatus requestStatus, T consentDetails)
      throws IllegalDataStateException {
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    RequestLog requestLog = mongoTemplate.findOne(query, RequestLog.class);
    if (requestLog == null) {
      throw new IllegalDataStateException("Request not found for request id: " + requestId);
    }
    Map<String, Object> map = requestLog.getResponseDetails();
    if (map == null) {
      map = new HashMap<>();
    }
    map.put(identifier, consentDetails);
    Update update = new Update();
    update.set(FieldIdentifiers.RESPONSE_DETAILS, map);
    update.set(FieldIdentifiers.STATUS, requestStatus);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public <T> void updateConsentRequest(
      String requestId, String identifier, RequestStatus requestStatus, T consentDetails)
      throws IllegalDataStateException {
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    RequestLog requestLog = mongoTemplate.findOne(query, RequestLog.class);
    if (requestLog == null) {
      throw new IllegalDataStateException("Request not found for request id: " + requestId);
    }
    Map<String, Object> map = requestLog.getRequestDetails();
    if (map == null) {
      map = new HashMap<>();
    }
    map.put(identifier, consentDetails);
    Update update = new Update();
    update.set(FieldIdentifiers.REQUEST_DETAILS, map);
    update.set(FieldIdentifiers.STATUS, requestStatus);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public void dataTransferNotify(
      HIPNotifyRequest hipNotifyRequest,
      RequestStatus requestStatus,
      HIPOnNotifyRequest hipOnNotifyRequest) {
    RequestLog requestLog = new RequestLog();
    requestLog.setGatewayRequestId(hipOnNotifyRequest.getRequestId());
    requestLog.setStatus(requestStatus);
    requestLog.setConsentId(hipNotifyRequest.getNotification().getConsentId());
    requestLog.setEntityType("HIP");
    HashMap<String, Object> map = new HashMap<>();
    map.put(FieldIdentifiers.HIP_NOTIFY_REQUEST, hipNotifyRequest);
    requestLog.setRequestDetails(map);
    if (hipOnNotifyRequest.getError() != null) {
      requestLog.setError(hipOnNotifyRequest.getError().getMessage());
    }
    mongoTemplate.save(requestLog);
  }

  public void saveHealthInformationRequest(
      HIPHealthInformationRequest hipHealthInformationRequest, RequestStatus requestStatus)
      throws IllegalDataStateException {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.CONSENT_ID)
                .is(hipHealthInformationRequest.getHiRequest().getConsent().getId()));
    RequestLog requestLog = mongoTemplate.findOne(query, RequestLog.class);
    if (requestLog == null) {
      throw new IllegalDataStateException(
          "Request not found for consentId: "
              + hipHealthInformationRequest.getHiRequest().getConsent().getId());
    }
    Map<String, Object> map = requestLog.getRequestDetails();
    if (Objects.isNull(map)) {
      map = new HashMap<>();
    }
    map.put(FieldIdentifiers.HEALTH_INFORMATION_REQUEST, hipHealthInformationRequest);
    Update update = new Update();
    update.set(FieldIdentifiers.REQUEST_DETAILS, map);
    update.set(FieldIdentifiers.STATUS, requestStatus);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public void updateTransactionId(String requestId, String transactionId) {
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    Update update = new Update();
    update.set(FieldIdentifiers.TRANSACTION_ID, transactionId);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public RequestLog findRequestLogByTransactionId(String transactionId) {
    Query query = new Query(Criteria.where(FieldIdentifiers.TRANSACTION_ID).is(transactionId));
    return mongoTemplate.findOne(query, RequestLog.class);
  }

  public GenericResponse saveEncryptedHealthInformation(
      HealthInformationPushRequest healthInformationPushRequest, RequestStatus requestStatus) {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.TRANSACTION_ID)
                .is(healthInformationPushRequest.getTransactionId()));
    RequestLog requestLog = mongoTemplate.findOne(query, RequestLog.class);
    if (requestLog == null) {
      return GenericResponse.builder()
          .httpStatus(HttpStatus.NOT_FOUND)
          .errorResponse(
              ErrorResponse.builder()
                  .message(
                      "Transaction id not found: "
                          + healthInformationPushRequest.getTransactionId())
                  .build())
          .build();
    }
    Map<String, Object> map = requestLog.getResponseDetails();
    if (map == null) {
      map = new HashMap<>();
    }
    List<HealthInformationPushRequest> healthInformationPushRequests = new ArrayList<>();
    Object existingObject = map.get(FieldIdentifiers.ENCRYPTED_HEALTH_INFORMATION);
    if (existingObject != null) {
      List<HealthInformationPushRequest> existingList =
          (List<HealthInformationPushRequest>) existingObject;
      healthInformationPushRequests.addAll(existingList);
    }
    healthInformationPushRequests.add(healthInformationPushRequest);
    map.put(FieldIdentifiers.ENCRYPTED_HEALTH_INFORMATION, healthInformationPushRequests);
    Update update = new Update();
    update.set(FieldIdentifiers.RESPONSE_DETAILS, map);
    update.set(FieldIdentifiers.STATUS, requestStatus);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
    return GenericResponse.builder().httpStatus(HttpStatus.OK).build();
  }

  public void saveHIUHealthInformationRequest(
      String requestId, String consentId, RequestStatus requestStatus, String error)
      throws IllegalDataStateException {
    RequestLog requestLog = new RequestLog();
    requestLog.setClientRequestId(requestId);
    requestLog.setGatewayRequestId(requestId);
    requestLog.setStatus(requestStatus);
    requestLog.setConsentId(consentId);
    requestLog.setEntityType("HIU");
    if (StringUtils.isNotBlank(error)) {
      requestLog.setError(error);
    }
    mongoTemplate.save(requestLog);
  }

  /**
   * Since we have common database schema for HIU and HIP, we need a way to distinguish the logs for
   * them. We are doing that by setting entity type.
   *
   * @param consentId
   * @param entity
   * @return
   */
  public RequestLog findByConsentId(String consentId, String entity) {
    Criteria criteria =
        Criteria.where(FieldIdentifiers.CONSENT_ID)
            .is(consentId)
            .and(FieldIdentifiers.HIP_ID)
            .is(entity);
    Query query = Query.query(criteria);
    return mongoTemplate.findOne(query, RequestLog.class);
  }
}
