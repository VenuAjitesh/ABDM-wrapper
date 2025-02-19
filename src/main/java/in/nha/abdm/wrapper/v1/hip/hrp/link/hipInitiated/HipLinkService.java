/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated;

import in.nha.abdm.wrapper.v1.common.RequestManager;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.models.VerifyOTP;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v1.common.responses.FacadeResponse;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;
import in.nha.abdm.wrapper.v1.hip.HIPClient;
import in.nha.abdm.wrapper.v1.hip.HIPPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.LogsRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services.RequestLogService;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.OnDiscoverPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.*;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.helpers.*;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses.LinkOnConfirmResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses.LinkOnInitResponse;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentCareContexts;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentHIP;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

@Service
@Profile(WrapperConstants.V1)
public class HipLinkService implements HipLinkInterface {
  @Autowired PatientRepo patientRepo;
  @Autowired LogsRepo logsRepo;
  private final RequestManager requestManager;
  @Autowired RequestLogService requestLogService;
  private final HIPClient hipClient;
  private static final String REQUESTER_TYPE = "HIP";
  private static final String LINK_PURPOSE = "KYC_AND_LINK";

  @Value("${linkAuthInitPath}")
  public String linkAuthInitPath;

  @Value("${linkConfirmAuthPath}")
  public String linkConfirmAuthPath;

  @Value("${linkAddContextsPath}")
  public String linkAddContextsPath;

  @Value("${linkContextNotifyPath}")
  public String linkContextNotifyPath;

  @Autowired
  public HipLinkService(HIPClient hipClient, RequestManager requestManager) {
    this.hipClient = hipClient;
    this.requestManager = requestManager;
  }

  private static final Logger log = LogManager.getLogger(HipLinkService.class);

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>1)Build the required body for /auth/init including abhaAddress.<br>
   * 2)Stores the request of linkRecordsRequest into requestLog.<br>
   * 3)makes a POST request to /auth/init API
   *
   * @param linkRecordsRequest Response which has authMode, patient details and careContexts.
   * @return it returns the requestId and status of initiation to the Facility for future tracking
   */
  public FacadeResponse hipAuthInit(LinkRecordsRequest linkRecordsRequest) {

    LinkRequester linkRequester =
        LinkRequester.builder()
            .id(linkRecordsRequest.getRequesterId())
            .type(REQUESTER_TYPE)
            .build();

    LinkQuery linkQuery =
        LinkQuery.builder()
            .id(linkRecordsRequest.getAbhaAddress())
            .purpose(LINK_PURPOSE)
            .authMode(linkRecordsRequest.getAuthMode())
            .requester(linkRequester)
            .build();

    LinkAuthInit linkAuthInit =
        LinkAuthInit.builder()
            .requestId(linkRecordsRequest.getRequestId())
            .timestamp(Utils.getCurrentTimeStamp())
            .query(linkQuery)
            .build();

    log.debug("LinkAuthInit : " + linkAuthInit.toString());
    requestLogService.persistHipLinkRequest(linkRecordsRequest, RequestStatus.INITIATING, null);
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(linkAuthInitPath, linkAuthInit);
      log.debug(linkAuthInitPath + " : linkAuthInit: " + response.getStatusCode());
      if (response.getStatusCode() == HttpStatus.ACCEPTED) {
        requestLogService.updateStatus(
            linkAuthInit.getRequestId(), RequestStatus.AUTH_INIT_ACCEPTED);
      } else if (Objects.nonNull(response.getBody())
          && Objects.nonNull(response.getBody().getErrorResponse())) {
        requestLogService.updateError(
            linkAuthInit.getRequestId(),
            response.getBody().getErrorResponse().getMessage(),
            RequestStatus.AUTH_CONFIRM_ERROR);
        return FacadeResponse.builder()
            .error(response.getBody().getErrorResponse())
            .clientRequestId(linkRecordsRequest.getRequestId())
            .code(response.getStatusCode().value())
            .build();
      }
      return FacadeResponse.builder()
          .clientRequestId(linkRecordsRequest.getRequestId())
          .code(response.getStatusCode().value())
          .build();
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorV3Response.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      return FacadeResponse.builder()
          .clientRequestId(linkRecordsRequest.getRequestId())
          .error(error)
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .build();
    } catch (Exception ex) {
      String error =
          "Exception while Initiating HIP auth: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
      return FacadeResponse.builder()
          .message(error)
          .clientRequestId(linkRecordsRequest.getRequestId())
          .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
          .build();
    }
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>1) Build the required body for /auth/confirm.<br>
   * 2)transactionId from LinkOnInitResponse and the credentials of the authMethod i.e. DEMOGRAPHICS
   * details of patient to make request.<br>
   * 3)Stores the request of LinkOnInitResponse into requestLog.<br>
   * 4)Makes a POST request to "/v0.5/users/auth/confirm"
   *
   * @param linkOnInitResponse Response from ABDM gateway with transactionId after successful
   *     auth/init.
   */
  public void confirmAuthDemographics(LinkOnInitResponse linkOnInitResponse)
      throws IllegalDataStateException {
    RequestLog requestLog =
        logsRepo.findByGatewayRequestId(linkOnInitResponse.getResp().getRequestId());
    if (requestLog == null) {
      String error = "confirmAuthDemographics: Illegal State - Request Id not found in database.";
      log.error(error);
      throw new IllegalDataStateException(error);
    }
    log.debug(
        "In confirmAuthDemographics found existing record log for client request: "
            + requestLog.getClientRequestId());
    LinkRecordsRequest linkRecordsRequest =
        (LinkRecordsRequest)
            requestLog.getRequestDetails().get(FieldIdentifiers.LINK_RECORDS_REQUEST);
    Patient patient =
        Optional.ofNullable(patientRepo.findByAbhaAddress(linkRecordsRequest.getAbhaAddress()))
            .orElseGet(() -> getPatient(linkRecordsRequest.getAbhaAddress()));
    UserDemographic userDemographic =
        UserDemographic.builder()
            .name(patient.getName())
            .gender(patient.getGender())
            .dateOfBirth(patient.getDateOfBirth())
            .build();
    LinkCredential linkCredential = LinkCredential.builder().demographic(userDemographic).build();
    LinkConfirmRequest linkConfirmRequest =
        LinkConfirmRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp())
            .transactionId(linkOnInitResponse.getAuth().getTransactionId())
            .credential(linkCredential)
            .build();
    log.debug("confirmAuthDemographics linkConfirmRequest: " + linkConfirmRequest.toString());
    requestLogService.updateHipOnInitResponse(linkOnInitResponse, linkConfirmRequest);
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(linkConfirmAuthPath, linkConfirmRequest);
      log.info(linkConfirmAuthPath + " : confirmAuthDemographics: " + response.getStatusCode());
      if (response.getStatusCode() == HttpStatus.ACCEPTED) {
        requestLogService.updateStatus(
            linkConfirmRequest.getRequestId(), RequestStatus.AUTH_CONFIRM_ACCEPTED);
      } else if (Objects.nonNull(response.getBody())) {
        requestLogService.updateError(
            linkConfirmRequest.getRequestId(),
            response.getBody().getErrorResponse().getMessage(),
            RequestStatus.AUTH_CONFIRM_ERROR);
      }
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorV3Response.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogService.updateError(
          linkConfirmRequest.getRequestId(), error.getMessage(), RequestStatus.AUTH_CONFIRM_ERROR);
    } catch (Exception e) {
      String error =
          linkConfirmAuthPath
              + " : confirmAuthDemographics: Error while performing confirm auth: "
              + e.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(e);
      log.error(error);
      requestLog.setError(error);
      requestLogService.updateError(
          linkConfirmRequest.getRequestId(), error, RequestStatus.AUTH_CONFIRM_ERROR);
    }
  }

  private Patient getPatient(String abhaAddress) {
    log.debug("Patient not found in database, sending request to HIP.");
    HIPPatient hipPatient = hipClient.getPatient(abhaAddress);
    Patient patient = new Patient();
    patient.setAbhaAddress(hipPatient.getAbhaAddress());
    patient.setGender(hipPatient.getGender());
    patient.setName(hipPatient.getName());
    patient.setDateOfBirth(hipPatient.getDateOfBirth());
    patient.setPatientDisplay(hipPatient.getPatientDisplay());
    patient.setPatientReference(hipPatient.getPatientReference());
    patient.setPatientMobile(hipPatient.getPatientMobile());

    // Save the patient into the database.
    patientRepo.save(patient);

    return patient;
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>1)Build the required body for /auth/on-confirm.<br>
   * 2)If the authMode is "MOBILE_OTP", using the request of verifyOTP which has OTP.<br>
   * 3)And fetch the rawResponse dump of "HIPOnConfirm" (on-confirm request API) from db.<br>
   * 4)Updating the gatewayRequestId in db via "updateOnInitResponseOTP"<br>
   * 5)Makes a POST request to /v0.5/users/auth/confirm.
   *
   * @param verifyOTP Response to facade with OTP for authentication.
   */
  public FacadeResponse confirmAuthOtp(VerifyOTP verifyOTP) throws IllegalDataStateException {
    RequestLog requestLog = logsRepo.findByClientRequestId(verifyOTP.getRequestId());
    if (requestLog == null) {
      throw new IllegalDataStateException(
          "Illegal State: Request Not found in database: " + verifyOTP.getRequestId());
    }
    log.debug("In confirmAuthOtp found existing record");

    LinkCredential linkCredential =
        LinkCredential.builder().authCode(verifyOTP.getAuthCode()).build();

    LinkOnInitResponse linkOnInitResponse =
        (LinkOnInitResponse)
            requestLog.getRequestDetails().get(FieldIdentifiers.HIP_ON_INIT_RESPONSE);
    LinkConfirmRequest linkConfirmRequest =
        LinkConfirmRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp())
            .transactionId(linkOnInitResponse.getAuth().getTransactionId())
            .credential(linkCredential)
            .build();
    log.debug("confirmAuthOtp" + linkConfirmRequest.toString());
    requestLogService.updateOnInitResponseOTP(
        verifyOTP.getRequestId(), linkConfirmRequest.getRequestId());
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(linkConfirmAuthPath, linkConfirmRequest);
      log.debug(linkConfirmAuthPath + " : confirmAuthOtp: " + response.getStatusCode());
      if (response.getStatusCode() == HttpStatus.ACCEPTED) {
        requestLogService.updateStatus(
            linkConfirmRequest.getRequestId(), RequestStatus.AUTH_CONFIRM_ACCEPTED);
      } else if (Objects.nonNull(response.getBody())
          && Objects.nonNull(response.getBody().getErrorResponse())) {
        requestLogService.updateError(
            linkConfirmRequest.getRequestId(),
            response.getBody().getErrorResponse().getMessage(),
            RequestStatus.AUTH_CONFIRM_ERROR);
      }
      return FacadeResponse.builder()
          .message(linkConfirmAuthPath + " : confirmAuthOtp: " + response.getStatusCode())
          .clientRequestId(verifyOTP.getRequestId())
          .error(Objects.nonNull(response.getBody()) ? response.getBody().getErrorResponse() : null)
          .build();
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorV3Response.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogService.updateError(
          linkConfirmRequest.getRequestId(), error.getMessage(), RequestStatus.AUTH_CONFIRM_ERROR);
      return FacadeResponse.builder()
          .clientRequestId(verifyOTP.getRequestId())
          .error(error)
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .build();
    } catch (Exception e) {
      String error =
          linkConfirmAuthPath
              + ": Error while executing link Confirm Auth: "
              + e.getMessage()
              + " exception: "
              + Exceptions.unwrap(e);
      log.error(error);
      requestLogService.updateError(
          linkConfirmRequest.getRequestId(), error, RequestStatus.AUTH_CONFIRM_ERROR);
      return FacadeResponse.builder()
          .clientRequestId(verifyOTP.getRequestId())
          .error(ErrorResponse.builder().message(error).build())
          .build();
    }
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>1)linkOnConfirmResponse has the linkToken for linking the careContext.<br>
   * 2)Using the gatewayRequestId fetching the careContext which is present in linkRecordsResponse.
   * <br>
   * 3)Build the body of linkAddCareContext using the linkToken and careContexts.<br>
   * 4)Makes a POST request to "/v0.5/links/link/add-contexts".
   *
   * @param linkOnConfirmResponse Response from ABDM gateway with linkToken for linking careContext.
   */
  public void hipAddCareContext(LinkOnConfirmResponse linkOnConfirmResponse)
      throws IllegalDataStateException {
    RequestLog requestLog =
        logsRepo.findByGatewayRequestId(linkOnConfirmResponse.getResp().getRequestId());
    if (requestLog == null) {
      String error = "hipAddCareContext: Illegal state - Gateway request Id not found in database";
      log.error(error);
      throw new IllegalDataStateException(error);
    }
    LinkRecordsRequest linkRecordsRequest =
        (LinkRecordsRequest)
            requestLog.getRequestDetails().get(FieldIdentifiers.LINK_RECORDS_REQUEST);
    Patient patient = patientRepo.findByAbhaAddress(linkRecordsRequest.getAbhaAddress());
    if (patient == null) {
      String error = "hipAddCareContext: Illegal state - Patient not found in database";
      log.error(error);
      throw new IllegalDataStateException(error);
    }

    OnDiscoverPatient patientNode =
        OnDiscoverPatient.builder()
            .referenceNumber(patient.getPatientReference())
            .display(patient.getPatientDisplay())
            .careContexts(linkRecordsRequest.getPatient().getCareContexts())
            .build();
    LinkTokenAndPatient linkNode =
        LinkTokenAndPatient.builder()
            .accessToken(linkOnConfirmResponse.getAuth().getAccessToken())
            .patient(patientNode)
            .build();
    LinkAddCareContext linkAddCareContext =
        LinkAddCareContext.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp())
            .link(linkNode)
            .build();
    log.debug("Link AddCareContext : " + linkAddCareContext.toString());
    requestLogService.setHipOnConfirmResponse(linkOnConfirmResponse, linkAddCareContext);
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(linkAddContextsPath, linkAddCareContext);
      log.debug(linkAddContextsPath + " : linkAddContexts: " + response.getStatusCode());
      if (response.getStatusCode() == HttpStatus.ACCEPTED) {
        requestLogService.updateStatus(
            linkAddCareContext.getRequestId(), RequestStatus.ADD_CARE_CONTEXT_ACCEPTED);
      } else if (Objects.nonNull(response.getBody())
          && Objects.nonNull(response.getBody().getErrorResponse())) {
        requestLogService.updateError(
            linkAddCareContext.getRequestId(),
            response.getBody().getErrorResponse().getMessage(),
            RequestStatus.ADD_CARE_CONTEXT_ERROR);
      }
      // When the linkAddContexts is done we have to notify the ABDM gateway that these careContexts
      // are linked
      // With particular abhaAddress and what are the HiTypes present in that careContext.
      for (CareContext careContext : linkRecordsRequest.getPatient().getCareContexts()) {
        hipContextNotify(
            linkRecordsRequest, careContext.getReferenceNumber(), patient.getPatientReference());
      }
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorV3Response.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      log.info(requestLog.toString());
      requestLogService.updateError(
          linkAddCareContext.getRequestId(),
          error.getMessage(),
          RequestStatus.ADD_CARE_CONTEXT_ERROR);
    } catch (Exception e) {
      String error =
          linkConfirmAuthPath
              + " : hipAddCareContext: Error while performing add care contexts: "
              + e.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(e);
      log.error(error);
      requestLogService.updateError(
          linkAddCareContext.getRequestId(), error, RequestStatus.ADD_CARE_CONTEXT_ERROR);
    }
  }

  /**
   * Notifying ABDM gateway that these careContexts with HiTypes were linked with abhaAddress.
   *
   * @param linkRecordsRequest
   * @param careContextReference
   * @param patientReference
   */
  public void hipContextNotify(
      LinkRecordsRequest linkRecordsRequest, String careContextReference, String patientReference) {
    if (Objects.isNull(linkRecordsRequest)
        || careContextReference == null
        || patientReference == null) {
      log.error("hipContextNotify failed because careContexts are null");
      return;
    }
    PatientNotification patientNotification =
        PatientNotification.builder()
            .patient(PatientId.builder().id(linkRecordsRequest.getAbhaAddress()).build())
            .hip(ConsentHIP.builder().id(linkRecordsRequest.getRequesterId()).build())
            .hiTypes(linkRecordsRequest.getHiTypes())
            .date(Utils.getCurrentTimeStamp())
            .careContext(
                ConsentCareContexts.builder()
                    .careContextReference(careContextReference)
                    .patientReference(patientReference)
                    .build())
            .build();
    LinkContextNotify linkContextNotify =
        LinkContextNotify.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp())
            .notification(patientNotification)
            .build();
    log.debug(linkContextNotifyPath + " : " + linkContextNotify.toString());
    try {
      ResponseEntity<GenericResponse> response =
          requestManager.fetchResponseFromGateway(linkContextNotifyPath, linkContextNotify);
      log.debug(linkContextNotifyPath + " : linkContextNotify: " + response.getStatusCode());
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorV3Response.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception e) {
      String error =
          linkContextNotifyPath
              + " : hipAddCareContext: Error while performing add care contexts: "
              + e.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(e);
      log.error(error);
    }
  }
}
