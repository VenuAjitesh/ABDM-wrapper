/* (C) 2024 */
package in.nha.abdm.wrapper.v3.database.mongo.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.requests.HealthInformationPushRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPNotifyRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.consent.requests.HIPOnNotifyRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.callback.HIPHealthInformationRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.LogsRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.DiscoverRequest;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.InitConsentRequest;
import in.nha.abdm.wrapper.v3.common.models.RequestStatusV3Response;
import in.nha.abdm.wrapper.v3.config.ErrorHandler;
import in.nha.abdm.wrapper.v3.database.mongo.repositories.LinkTokenRepo;
import in.nha.abdm.wrapper.v3.hip.hrp.discover.requests.OnDiscoverV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.LinkRecordsV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.helpers.PatientCareContextHIType;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.responses.LinkOnAddCareContextsV3Response;
import in.nha.abdm.wrapper.v3.hip.hrp.link.userInitiated.responses.InitV3Response;
import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.OnShareV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.ProfileShareV3Request;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RequestLogV3Service {
  @Autowired public LogsRepo logsRepo;
  @Autowired public LinkTokenRepo linkTokenRepo;
  @Autowired MongoTemplate mongoTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Autowired PatientV3Service patientService;
  private static final Logger log = LogManager.getLogger(RequestLogV3Service.class);

  /** Saving the initial consentRequest into requestLog */
  public void saveConsentRequest(InitConsentRequest initConsentRequest) {
    RequestLog requestLog = new RequestLog();
    requestLog.setAbhaAddress(initConsentRequest.getConsent().getPatient().getId());
    requestLog.setModule(FieldIdentifiers.HIU_CONSENT);
    requestLog.setCreatedOn(Utils.getCurrentDateTime());
    requestLog.setLastUpdated(Utils.getCurrentDateTime());
    requestLog.setEntityType(GatewayConstants.HIU);
    requestLog.setHipId(initConsentRequest.getConsent().getHiu().getId());
    requestLog.setClientRequestId(initConsentRequest.getRequestId());
    requestLog.setGatewayRequestId(initConsentRequest.getRequestId());
    requestLog.setStatus(RequestStatus.INITIATING);
    HashMap<String, Object> requestDetails = new HashMap<>();
    requestDetails.put(FieldIdentifiers.CONSENT_INIT_REQUEST, initConsentRequest);
    requestLog.setRequestDetails(requestDetails);
    mongoTemplate.save(requestLog);
  }

  /**
   * Updating the consentStatus in requestLogs
   *
   * @param requestId
   * @param status
   * @throws IllegalDataStateException
   */
  public void updateConsentStatus(String requestId, RequestStatus status)
      throws IllegalDataStateException {
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    RequestLog requestLog = mongoTemplate.findOne(query, RequestLog.class);
    if (requestLog == null) {
      throw new IllegalDataStateException("Request not found for request id: " + requestId);
    }
    Update update = new Update();
    update.set(FieldIdentifiers.STATUS, status);
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  /**
   * If any error encountered in the flow or consequent APIs this method is used to update in
   * requestLogs
   *
   * @param requestId
   * @param errors
   * @param requestStatus
   */
  public void updateError(String requestId, Object errors, RequestStatus requestStatus) {
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    Update update = new Update();
    update.set(FieldIdentifiers.ERROR, errors);
    update.set(FieldIdentifiers.STATUS, requestStatus);
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  /**
   * If any error encountered in the generation of LinkToken API this method is used to update in
   * requestLogs
   *
   * @param linkTokenRequestId
   * @param errors
   * @param requestStatus
   */
  public void updateErrorLinkError(
      String linkTokenRequestId, Object errors, RequestStatus requestStatus) {
    Query query =
        new Query(Criteria.where(FieldIdentifiers.LINK_TOKEN_REQUEST_ID).is(linkTokenRequestId));
    Update update = new Update();
    update.set(FieldIdentifiers.ERROR, errors);
    update.set(FieldIdentifiers.STATUS, requestStatus);
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public void linkTokenUpdateError(String requestId, Object errors, RequestStatus requestStatus) {
    log.info(requestId);
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    Update update = new Update();
    update.set(FieldIdentifiers.ERROR, errors);
    update.set(FieldIdentifiers.STATUS, requestStatus);
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  /**
   * Updating the current status of the request in requestLogs
   *
   * @param requestId
   * @param requestStatus
   */
  public void updateStatus(String requestId, RequestStatus requestStatus) {
    log.info("GatewayRequestId: " + requestId + "requestStatus: " + requestStatus);
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    Update update = new Update();
    update.set(FieldIdentifiers.STATUS, requestStatus);
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  /**
   * Saving the consent in the requestLogs when we receive the hip/notify or hiu/notify
   *
   * @param requestId
   * @param identifier
   * @param requestStatus
   * @param consentDetails
   * @param <T>
   * @throws IllegalDataStateException
   */
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
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  /**
   * As a HIU when HIP sends the encrypted data, storing them in the requestLogs
   *
   * @param healthInformationPushRequest
   * @param requestStatus
   * @return
   */
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
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
    return GenericResponse.builder().httpStatus(HttpStatus.OK).build();
  }

  public RequestLog findRequestLogByTransactionId(String transactionId) {
    Query query = new Query(Criteria.where(FieldIdentifiers.TRANSACTION_ID).is(transactionId));
    return mongoTemplate.findOne(query, RequestLog.class);
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Adds linkRecordsRequest into request-logs collection.
   *
   * @param linkRecordsV3Request Request received to facade for hipLinking.
   */
  public void persistHipLinkRequest(
      LinkRecordsV3Request linkRecordsV3Request, RequestStatus status, Object errors) {
    if (Objects.isNull(linkRecordsV3Request)) {
      return;
    }
    RequestLog existingLog = logsRepo.findByClientRequestId(linkRecordsV3Request.getRequestId());
    if (Objects.isNull(existingLog)) {
      RequestLog requestLog = new RequestLog();
      requestLog.setAbhaAddress(linkRecordsV3Request.getAbhaAddress());
      requestLog.setModule(FieldIdentifiers.HIP_INITIATED_LINKING);
      requestLog.setCreatedOn(Utils.getCurrentDateTime());
      requestLog.setLastUpdated(Utils.getCurrentDateTime());
      requestLog.setClientRequestId(linkRecordsV3Request.getRequestId());
      requestLog.setGatewayRequestId(linkRecordsV3Request.getRequestId());
      requestLog.setHipId(linkRecordsV3Request.getRequesterId());
      requestLog.setStatus(status);
      HashMap<String, Object> map = new HashMap<>();
      map.put(FieldIdentifiers.LINK_RECORDS_REQUEST, linkRecordsV3Request);
      requestLog.setRequestDetails(map);
      if (Objects.nonNull(errors)) {
        requestLog.setError(errors);
      }
      mongoTemplate.save(requestLog);
      return;
    }
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.CLIENT_REQUEST_ID)
                .is(linkRecordsV3Request.getRequestId()));
    Map<String, Object> map = existingLog.getRequestDetails();
    if (Objects.isNull(map)) {
      map = new HashMap<>();
    }
    map.replace(FieldIdentifiers.LINK_RECORDS_REQUEST, linkRecordsV3Request);
    Update update = new Update();
    update.set(FieldIdentifiers.REQUEST_DETAILS, map);
    update.set(FieldIdentifiers.STATUS, status);
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Checking the status of hipLinking
   *
   * @param requestId Response from ABDM gateway for discovery.
   * @return status of linking after /on-add-contexts acknowledgment.
   */
  public RequestStatusV3Response getStatus(String requestId) throws IllegalDataStateException {
    RequestLog RequestLog = logsRepo.findByClientRequestId(requestId);
    if (RequestLog != null) {
      if (Objects.nonNull(RequestLog.getError())) {
        return RequestStatusV3Response.builder()
            .requestId(requestId)
            .status(RequestLog.getStatus().getValue())
            .errors(ErrorHandler.getErrors(RequestLog.getError()))
            .build();
      }
      if (Objects.nonNull(RequestLog.getStatus())
          && StringUtils.isNotBlank(RequestLog.getStatus().getValue())) {
        return RequestStatusV3Response.builder()
            .requestId(requestId)
            .status(RequestLog.getStatus().getValue())
            .build();
      }
    }
    throw new IllegalDataStateException("Request not found in database for: " + requestId);
  }

  public void saveHIUHealthInformationRequest(
      String abhaAddress,
      String hipId,
      String requestId,
      String consentId,
      RequestStatus requestStatus,
      String error)
      throws IllegalDataStateException {
    RequestLog requestLog = new RequestLog();
    requestLog.setAbhaAddress(abhaAddress);
    requestLog.setModule(FieldIdentifiers.HIU_DATA_REQUEST);
    requestLog.setClientRequestId(requestId);
    requestLog.setGatewayRequestId(requestId);
    requestLog.setStatus(requestStatus);
    requestLog.setConsentId(consentId);
    requestLog.setEntityType(GatewayConstants.HIU);
    requestLog.setHipId(hipId); // TODO
    if (StringUtils.isNotBlank(error)) {
      requestLog.setError(
          Collections.singletonList(
              ErrorV3Response.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(GatewayConstants.ERROR_CODE)
                          .message(error)
                          .build())
                  .build()));
    }
    requestLog.setCreatedOn(Utils.getCurrentDateTime());
    requestLog.setLastUpdated(Utils.getCurrentDateTime());
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
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  /**
   * Since we have common database schema for HIU and HIP, we need a way to distinguish the logs for
   * them. We are doing that by setting entity type.
   *
   * @param consentId
   * @param entityType
   * @return
   */
  public RequestLog findByConsentId(String consentId, String entityType) {
    Criteria criteria =
        Criteria.where(FieldIdentifiers.CONSENT_ID)
            .is(consentId)
            .and(FieldIdentifiers.ENTITY_TYPE)
            .is(entityType);
    Query query = Query.query(criteria);
    return mongoTemplate.findOne(query, RequestLog.class);
  }

  /**
   * Since we have common database schema for HIU and HIP, we need a way to distinguish the logs for
   * them. We are doing that by setting entity type.
   *
   * @param consentId
   * @param entityType
   * @return
   */
  public RequestLog findByConsentId(String consentId, String entityType, String hipId) {
    Criteria criteria =
        Criteria.where(FieldIdentifiers.CONSENT_ID)
            .is(consentId)
            .and(FieldIdentifiers.ENTITY_TYPE)
            .is(entityType)
            .and(FieldIdentifiers.HIP_ID)
            .is(hipId);
    Query query = Query.query(criteria);
    return mongoTemplate.findOne(query, RequestLog.class);
  }

  public void dataTransferNotify(
      HIPNotifyRequest hipNotifyRequest,
      RequestStatus requestStatus,
      HIPOnNotifyRequest hipOnNotifyRequest,
      HttpHeaders headers) {
    RequestLog RequestLog = new RequestLog();
    RequestLog.setModule(FieldIdentifiers.HIP_CONSENT);
    RequestLog.setGatewayRequestId(hipOnNotifyRequest.getRequestId());
    RequestLog.setStatus(requestStatus);
    RequestLog.setConsentId(hipNotifyRequest.getNotification().getConsentId());
    RequestLog.setEntityType(GatewayConstants.HIP);
    RequestLog.setHipId(headers.getFirst(GatewayConstants.X_HIP_ID));
    HashMap<String, Object> map = new HashMap<>();
    map.put(FieldIdentifiers.HIP_NOTIFY_REQUEST, hipNotifyRequest);
    RequestLog.setRequestDetails(map);
    if (hipOnNotifyRequest.getError() != null) {
      RequestLog.setError(hipOnNotifyRequest.getError());
    }
    RequestLog.setLastUpdated(Utils.getCurrentDateTime());
    RequestLog.setCreatedOn(Utils.getCurrentDateTime());

    mongoTemplate.save(RequestLog);
  }

  /**
   * Fetch of patient abhaAddress from requestLogs.
   *
   * @param linkRefNumber identifier for list of careContexts for linking.
   * @return abhaAddress
   */
  public String getPatientId(String linkRefNumber) {
    RequestLog existingRecord = logsRepo.findByLinkRefNumber(linkRefNumber);
    InitV3Response data =
        (InitV3Response) existingRecord.getRequestDetails().get(FieldIdentifiers.INIT_RESPONSE);
    return data.getAbhaAddress();
  }

  /**
   * Fetch of patientReferenceNumber from requestLogs.
   *
   * @param linkRefNumber identifier for list of careContexts for linking.
   * @return patientReference
   */
  public String getPatientReference(String linkRefNumber) {
    RequestLog existingRecord = logsRepo.findByLinkRefNumber(linkRefNumber);
    InitV3Response data =
        (InitV3Response) existingRecord.getRequestDetails().get(FieldIdentifiers.INIT_RESPONSE);
    return patientService.getPatientReference(data.getAbhaAddress(), existingRecord.getHipId());
  }

  /**
   * <B>discovery</B>
   *
   * <p>Adding discoverResponseDump into db.
   *
   * @param discoverRequest Response from ABDM gateway for discovery
   */
  public void setDiscoverResponse(
      DiscoverRequest discoverRequest,
      OnDiscoverV3Request onDiscoverV3Request,
      String patientReference) {
    if (Objects.isNull(discoverRequest)) {
      return;
    }
    RequestLog newRecord = new RequestLog();
    newRecord.setClientRequestId(discoverRequest.getRequestId());
    newRecord.setModule(FieldIdentifiers.HIP_DISCOVERY);
    newRecord.setTransactionId(discoverRequest.getTransactionId());
    HashMap<String, Object> map = new HashMap<>();
    map.put(FieldIdentifiers.DISCOVER_REQUEST, discoverRequest);
    map.put(FieldIdentifiers.ON_DISCOVER_RESPONSE, onDiscoverV3Request);
    newRecord.setRequestDetails(map);
    newRecord.setHipId(discoverRequest.getHipId());
    newRecord.setCreatedOn(Utils.getCurrentDateTime());
    newRecord.setLastUpdated(Utils.getCurrentDateTime());
    newRecord.setAbhaAddress(
        patientService.getAbhaAddress(patientReference, discoverRequest.getHipId()));
    mongoTemplate.save(newRecord);
  }

  /**
   * <B>discovery</B>
   *
   * <p>Adding initResponse dump into db.
   *
   * @param initResponse Response from ABDM gateway for linking particular careContexts.
   */
  public void setLinkResponse(
      InitV3Response initResponse, String requestId, String referenceNumber, HttpHeaders headers) {
    if (Objects.isNull(initResponse)) {
      return;
    }

    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.TRANSACTION_ID).is(initResponse.getTransactionId()));
    RequestLog existingRecord = mongoTemplate.findOne(query, RequestLog.class);
    if (existingRecord == null) {
      RequestLog newRecord =
          new RequestLog(
              headers.getFirst(GatewayConstants.X_HIP_ID),
              headers.getFirst(GatewayConstants.REQUEST_ID),
              requestId,
              initResponse.getAbhaAddress(),
              initResponse.getTransactionId(),
              RequestStatus.USER_INIT_REQUEST_RECEIVED_BY_WRAPPER);
      mongoTemplate.insert(newRecord);
    } else {
      Map<String, Object> map = existingRecord.getRequestDetails();
      map.put(FieldIdentifiers.INIT_RESPONSE, initResponse);
      Update update =
          (new Update())
              .set(
                  FieldIdentifiers.CLIENT_REQUEST_ID, headers.getFirst(GatewayConstants.REQUEST_ID))
              .set(FieldIdentifiers.GATEWAY_REQUEST_ID, requestId)
              .set(FieldIdentifiers.LINK_REFERENCE_NUMBER, referenceNumber)
              .set(FieldIdentifiers.REQUEST_DETAILS, map)
              .set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
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
    RequestLog RequestLog = logsRepo.findByLinkRefNumber(linkRefNumber);
    log.info("linkRefNum in getSelectedContexts : " + linkRefNumber);
    if (Objects.nonNull(RequestLog)) {
      Map<String, Object> requestDetails = RequestLog.getRequestDetails();
      if (Objects.nonNull(requestDetails)) {
        InitV3Response initResponse =
            (InitV3Response) requestDetails.get(FieldIdentifiers.INIT_RESPONSE);
        List<CareContext> selectedCareContexts = new ArrayList<>();
        for (PatientCareContextHIType patient : initResponse.getPatient()) {
          selectedCareContexts.addAll(patient.getCareContexts());
        }
        List<CareContext> existingCareContexts =
            patientService.getPatientDetails(abhaAddress, RequestLog.getHipId()).getCareContexts();
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

  public void updateTransactionId(String requestId, String transactionId) {
    Query query = new Query(Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID).is(requestId));
    Update update = new Update();
    update.set(FieldIdentifiers.TRANSACTION_ID, transactionId);
    update.set(FieldIdentifiers.LAST_UPDATED, Utils.getCurrentDateTime());
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public void saveLinkTokenRequest(
      LinkRecordsV3Request linkRecordsV3Request,
      String linkTokenRequestId,
      RequestStatus status,
      List<ErrorV3Response> errors) {
    if (Objects.isNull(linkRecordsV3Request)) {
      return;
    }
    RequestLog requestLog = new RequestLog();
    requestLog.setModule(FieldIdentifiers.HIP_INITIATED_LINKING);
    requestLog.setAbhaAddress(linkRecordsV3Request.getAbhaAddress());
    requestLog.setCreatedOn(Utils.getCurrentDateTime());
    requestLog.setLastUpdated(Utils.getCurrentDateTime());
    requestLog.setClientRequestId(linkRecordsV3Request.getRequestId());
    requestLog.setGatewayRequestId(linkRecordsV3Request.getRequestId());
    requestLog.setLinkTokenRequestId(linkTokenRequestId);
    requestLog.setHipId(linkRecordsV3Request.getRequesterId());
    requestLog.setStatus(status);
    HashMap<String, Object> map = new HashMap<>();
    map.put(FieldIdentifiers.LINK_RECORDS_REQUEST, linkRecordsV3Request);
    requestLog.setRequestDetails(map);
    if (Objects.nonNull(errors)) {
      requestLog.setError(errors);
    }
    mongoTemplate.save(requestLog);
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>Adding linkOnAddCareContextsResponse dump into db.
   *
   * @param linkOnAddCareContextsV3Response Acknowledgement from ABDM gateway for HipLinking.
   */
  public void setHipOnAddCareContextResponse(
      LinkOnAddCareContextsV3Response linkOnAddCareContextsV3Response)
      throws IllegalDataStateException {
    RequestLog RequestLog =
        logsRepo.findByGatewayRequestId(
            linkOnAddCareContextsV3Response.getResponse().getRequestId());

    if (RequestLog == null) {
      throw new IllegalDataStateException(
          "Request not found in database for: "
              + linkOnAddCareContextsV3Response.getResponse().getRequestId());
    }
    HashMap<String, Object> map = RequestLog.getRequestDetails();
    map.put(FieldIdentifiers.HIP_ON_ADD_CARE_CONTEXT_RESPONSE, linkOnAddCareContextsV3Response);
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.GATEWAY_REQUEST_ID)
                .is(linkOnAddCareContextsV3Response.getResponse().getRequestId()));
    Update update = new Update();
    if ((Objects.nonNull(linkOnAddCareContextsV3Response.getError()))) {
      update.set(FieldIdentifiers.ERROR, linkOnAddCareContextsV3Response.getError());
    } else {
      update.set(FieldIdentifiers.STATUS, RequestStatus.CARE_CONTEXT_LINKED);
      LinkRecordsV3Request linkRecordsV3Request =
          (LinkRecordsV3Request)
              RequestLog.getRequestDetails().get(FieldIdentifiers.LINK_RECORDS_REQUEST);
      patientService.addPatientCareContexts(linkRecordsV3Request);
    }
    update.set(FieldIdentifiers.REQUEST_DETAILS, map);
    mongoTemplate.updateFirst(query, update, RequestLog.class);
  }

  public RequestLog getLogsByAbhaAddress(String abhaAddress, String hipId) {
    String linkTokenRequestId =
        linkTokenRepo.findByAbhaAddress(abhaAddress, hipId).getLinkTokenRequestId();
    return logsRepo.findByLinkTokenRequestId(linkTokenRequestId);
  }

  /**
   * <B>Scan & Share</B> Storing the details of the token and patient
   *
   * @param profileShareV3Request
   * @param onShareV3Request
   */
  public void saveScanAndShareDetails(
      ProfileShareV3Request profileShareV3Request, OnShareV3Request onShareV3Request) {
    RequestLog requestLog = new RequestLog();
    requestLog.setModule(FieldIdentifiers.HIP_SCAN_AND_SHARE);
    requestLog.setAbhaAddress(profileShareV3Request.getProfile().getPatient().getAbhaAddress());
    requestLog.setHipId(profileShareV3Request.getMetaData().getHipId());
    requestLog.setCreatedOn(Utils.getCurrentDateTime());
    requestLog.setLastUpdated(Utils.getCurrentDateTime());
    HashMap<String, Object> map = new HashMap<>();
    map.put(FieldIdentifiers.SHARE_PROFILE_REQUEST, profileShareV3Request);
    map.put(FieldIdentifiers.SHARE_PROFILE_RESPONSE, onShareV3Request);
    requestLog.setRequestDetails(map);
    mongoTemplate.save(requestLog);
  }
}
