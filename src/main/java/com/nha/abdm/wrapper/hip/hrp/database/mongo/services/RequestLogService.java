/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.database.mongo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.models.CareContext;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.RequestStatusResponse;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.LogsRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.RequestLog;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import com.nha.abdm.wrapper.hip.hrp.discover.responses.DiscoverResponse;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.requests.LinkAddCareContext;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.requests.LinkConfirmRequest;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.requests.LinkRecordsRequest;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.responses.LinkOnAddCareContextsResponse;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.responses.LinkOnConfirmResponse;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.responses.LinkOnInitResponse;
import com.nha.abdm.wrapper.hip.hrp.link.userInitiated.responses.InitResponse;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.InitConsentRequest;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.callback.ConsentStatus;
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
   * @param discoverResponse Response from ABDM gateway for discovery
   */
  public void setDiscoverResponse(DiscoverResponse discoverResponse) {
    if (Objects.isNull(discoverResponse)) {
      return;
    }
    RequestLog newRecord = new RequestLog();
    newRecord.setClientRequestId(discoverResponse.getRequestId());
    newRecord.setTransactionId(discoverResponse.getTransactionId());
    HashMap<String, Object> map = new HashMap<>();
    map.put("DiscoverResponse", discoverResponse);
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
              .set("rawResponse", map);
      mongoTemplate.updateFirst(query, update, RequestLog.class);
    }
  }

  /**
   * Select the careContexts according to the careContexts referenceNumbers of the response
   *
   * @param linkRefNumber identifier for list of careContexts for linking.
   * @param careContextsList list of careContexts in response of /link/init.
   * @return the selected careContexts.
   */
  public List<CareContext> getSelectedCareContexts(
      String linkRefNumber, List<CareContext> careContextsList) {
    RequestLog existingRecord = logsRepo.findByLinkRefNumber(linkRefNumber);
    log.info("linkRefNum in getSelectedContexts : " + linkRefNumber);
    if (existingRecord != null) {
      ObjectNode dump =
          objectMapper.convertValue(
              existingRecord.getRequestDetails().get("InitResponse"), ObjectNode.class);
      if (dump != null && dump.has("patient") && dump.get("patient").has("careContexts")) {
        ArrayNode careContexts = (ArrayNode) dump.path("patient").path("careContexts");
        List<String> selectedList = careContexts.findValuesAsText("referenceNumber");

        List<CareContext> selectedCareContexts =
            careContextsList.stream()
                .filter(careContext -> selectedList.contains(careContext.getReferenceNumber()))
                .collect(Collectors.toList());
        log.info("Dump: {}", dump);
        log.info("Selected List: {}", selectedList);

        return selectedCareContexts;
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
      if (StringUtils.isNotBlank(requestLog.getError())) {
        return RequestStatusResponse.builder()
            .error(ErrorResponse.builder().message(requestLog.getError()).build())
            .build();
      }
      if (Objects.nonNull(requestLog.getStatus())
          && StringUtils.isNotBlank(requestLog.getStatus().getValue())) {
        return RequestStatusResponse.builder().status(requestLog.getStatus().getValue()).build();
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
    map.put(FieldIdentifiers.HIP_ON_CONFIRM, linkOnConfirmResponse);
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

  public void persistConsentInitRequest(
      InitConsentRequest initConsentRequest, RequestStatus status, String error) {
    RequestLog requestLog = new RequestLog();
    requestLog.setClientRequestId(initConsentRequest.getRequestId());
    requestLog.setGatewayRequestId(initConsentRequest.getRequestId());
    requestLog.setStatus(status);
    if (StringUtils.isNotBlank(error)) {
      requestLog.setError(error);
    }
    mongoTemplate.save(requestLog);
  }

  public void updateConsentResponse(String requestId, String consentRequestId) {
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    Map<String, Object> map = new HashMap<>();
    map.put(FieldIdentifiers.CONSENT_REQUEST_ID, consentRequestId);
    Update update = new Update();
    update.set(FieldIdentifiers.RESPONSE_DETAILS, map);
    update.set(FieldIdentifiers.STATUS, RequestStatus.CONSENT_ON_INIT_RESPONSE_RECEIVED);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public void updateConsentResponse(String requestId, ConsentStatus consentDetails)
      throws IllegalDataStateException {
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    RequestLog requestLog = mongoTemplate.findOne(query, RequestLog.class);
    if (requestLog == null) {
      throw new IllegalDataStateException("Request not found for request id: " + requestId);
    }
    Map<String, Object> map = requestLog.getResponseDetails();
    map.put(FieldIdentifiers.CONSENT_DETAILS_RESPONSE, consentDetails);
    Update update = new Update();
    update.set(FieldIdentifiers.RESPONSE_DETAILS, map);
    update.set(FieldIdentifiers.STATUS, RequestStatus.CONSENT_ON_STATUS_RESPONSE_RECEIVED);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }
}
