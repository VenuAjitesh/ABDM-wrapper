/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated;

import com.auth0.jwt.JWT;
import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.ErrorV3Response;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.LinkContextNotify;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.helpers.PatientId;
import in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests.helpers.PatientNotification;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentCareContexts;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentHIP;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.config.ErrorHandler;
import in.nha.abdm.wrapper.v3.database.mongo.services.LinkTokenService;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hip.HIPV3Client;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.AddCareContexts;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.GenerateTokenRequest;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.LinkRecordsV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.helpers.PatientCareContextHIType;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.responses.OnGenerateTokenResponse;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;

@Profile(WrapperConstants.V3)
@Service
public class HIPLinkV3Service implements HIPLinkV3Interface {
  @Autowired PatientRepo patientRepo;
  @Autowired RequestV3Manager requestV3Manager;
  @Autowired LinkTokenService linkTokenService;
  @Autowired RequestLogV3Service requestLogV3Service;
  @Autowired HIPV3Client hipClient;
  @Autowired PatientV3Service patientV3Service;

  @Value("${generateLinkTokenPath}")
  public String generateLinkTokenPath;

  @Value("${addCareContextsPath}")
  public String addCareContextsPath;

  @Value("${linkContextNotifyPath}")
  public String linkContextNotifyPath;

  private static final Logger log = LogManager.getLogger(HIPLinkV3Service.class);

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>1)Build the required body for /auth/init including abhaAddress.<br>
   * 2)Stores the request of linkRecordsRequest into requestLog.<br>
   * 3)makes a POST request to /auth/init API
   *
   * @param linkRecordsV3Request Response which has authMode, patient details and careContexts.
   * @return it returns the requestId and status of initiation to the Facility for future tracking
   */
  public FacadeV3Response addCareContexts(LinkRecordsV3Request linkRecordsV3Request) {
    Patient patient = getPatientOrNull(linkRecordsV3Request);
    if (Objects.isNull(patient)) {
      return buildErrorResponse(
          linkRecordsV3Request,
          GatewayConstants.ERROR_CODE,
          "Patient not found to generate linkToken");
    }

    String linkToken =
        linkTokenService.getLinkToken(
            patient.getAbhaAddress(), linkRecordsV3Request.getRequesterId());
    if (linkToken == null) {
      return generateLinkToken(patient, linkRecordsV3Request);
    }

    List<CareContext> sameCareContexts =
        patientV3Service.getSameCareContexts(
            linkRecordsV3Request.getAbhaAddress(),
            linkRecordsV3Request.getRequesterId(),
            linkRecordsV3Request.getCareContexts());

    if (Objects.nonNull(sameCareContexts)) {
      return hipContextNotify(linkRecordsV3Request, patient, sameCareContexts);
    }

    return processAddCareContexts(linkRecordsV3Request, patient, linkToken);
  }

  // Checking the patient, if not found fetching from HIP
  private Patient getPatientOrNull(LinkRecordsV3Request request) {
    Patient patient =
        patientRepo.findByAbhaAddress(request.getAbhaAddress(), request.getRequesterId());
    return patient != null
        ? patient
        : patientV3Service.getPatient(request.getAbhaAddress(), request.getRequesterId());
  }

  // The request payload needs to be grouped by HI-Types
  private Map<String, List<CareContext>> groupCareContextsByHiType(LinkRecordsV3Request request) {
    return request.getCareContexts().stream()
        .collect(Collectors.groupingBy(CareContext::getHiType));
  }

  // The request builder
  private List<PatientCareContextHIType> buildPatientCareContextHIType(
      Patient patient, Map<String, List<CareContext>> groupedByHiType) {
    return groupedByHiType.entrySet().stream()
        .map(
            entry -> {
              String hiType = entry.getKey();
              List<CareContext> careContexts =
                  entry.getValue().stream()
                      .map(
                          context ->
                              CareContext.builder()
                                  .display(context.getDisplay())
                                  .referenceNumber(context.getReferenceNumber())
                                  .build())
                      .collect(Collectors.toList());

              return PatientCareContextHIType.builder()
                  .referenceNumber(patient.getPatientReference())
                  .display(patient.getPatientDisplay())
                  .hiType(hiType == null ? "HealthDocumentRecord" : hiType)
                  .count(careContexts.size())
                  .careContexts(careContexts)
                  .build();
            })
        .collect(Collectors.toList());
  }

  // Making POST request to ABDM
  private FacadeV3Response sendAddCareContextsRequest(
      LinkRecordsV3Request request, AddCareContexts addCareContexts, String linkToken) {
    try {
      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              addCareContextsPath,
              addCareContexts,
              Utils.getLinkTokenHeaders(
                  request.getRequesterId(), request.getRequestId(), linkToken));

      if (response.getStatusCode() == HttpStatus.ACCEPTED) {
        requestLogV3Service.persistHipLinkRequest(
            request, RequestStatus.ADD_CARE_CONTEXT_ACCEPTED, null);
        return buildSuccessResponse(
            request, HttpStatus.ACCEPTED, RequestStatus.ADD_CARE_CONTEXT_ACCEPTED.getValue());
      } else {
        return handleAddCareContextError(request);
      }
    } catch (WebClientResponseException.BadRequest ex) {
      return handleWebClientBadRequest(request, ex);
    } catch (Exception ex) {
      return handleGeneralException(request, ex);
    }
  }

  // Grouping and building the request payload
  private FacadeV3Response processAddCareContexts(
      LinkRecordsV3Request request, Patient patient, String linkToken) {
    Map<String, List<CareContext>> groupedByHiType = groupCareContextsByHiType(request);
    List<PatientCareContextHIType> patientCareContexts =
        buildPatientCareContextHIType(patient, groupedByHiType);

    AddCareContexts addCareContexts =
        AddCareContexts.builder()
            .abhaNumber(getAbhaNumber(linkToken))
            .abhaAddress(patient.getAbhaAddress())
            .patient(patientCareContexts)
            .build();

    return sendAddCareContextsRequest(request, addCareContexts, linkToken);
  }

  private FacadeV3Response buildSuccessResponse(
      LinkRecordsV3Request request, HttpStatus status, String message) {
    return FacadeV3Response.builder()
        .clientRequestId(request.getRequestId())
        .httpStatusCode(status)
        .message(message)
        .build();
  }

  private FacadeV3Response buildErrorResponse(
      LinkRecordsV3Request request, String code, String message) {
    return FacadeV3Response.builder()
        .clientRequestId(request.getRequestId())
        .httpStatusCode(HttpStatus.BAD_REQUEST)
        .errors(ErrorHandler.getErrors(ErrorResponse.builder().code(code).message(message).build()))
        .build();
  }

  private FacadeV3Response handleAddCareContextError(LinkRecordsV3Request request) {
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .code(GatewayConstants.ERROR_CODE)
            .message("Unable to link care contexts")
            .build();
    requestLogV3Service.persistHipLinkRequest(
        request, RequestStatus.ADD_CARE_CONTEXT_ERROR, ErrorHandler.getErrors(errorResponse));
    return FacadeV3Response.builder()
        .clientRequestId(request.getRequestId())
        .message(RequestStatus.ADD_CARE_CONTEXT_ERROR.getValue())
        .errors(ErrorHandler.getErrors(errorResponse))
        .build();
  }

  private FacadeV3Response handleWebClientBadRequest(
      LinkRecordsV3Request request, WebClientResponseException.BadRequest ex) {
    Object error = BadRequestHandler.getError(ex);
    requestLogV3Service.persistHipLinkRequest(request, RequestStatus.ADD_CARE_CONTEXT_ERROR, error);
    return buildErrorResponse(request, "Wrapper-1001", ex.getMessage());
  }

  private FacadeV3Response handleGeneralException(LinkRecordsV3Request request, Exception ex) {
    String errorMessage =
        "Exception while Initiating HIP Linking: "
            + ex.getMessage()
            + " unwrapped exception: "
            + Exceptions.unwrap(ex);
    log.debug(errorMessage);
    requestLogV3Service.persistHipLinkRequest(
        request, RequestStatus.ADD_CARE_CONTEXT_ERROR, errorMessage);
    return buildErrorResponse(request, GatewayConstants.ERROR_CODE, errorMessage);
  }

  public FacadeV3Response generateLinkToken(
      Patient patient, LinkRecordsV3Request linkRecordsV3Request) {
    GenerateTokenRequest generateTokenRequest =
        GenerateTokenRequest.builder()
            .abhaAddress(patient.getAbhaAddress())
            .gender(patient.getGender())
            .name(patient.getName())
            .yearOfBirth(Integer.parseInt(patient.getDateOfBirth().substring(0, 4)))
            .build();
    String generateTokenRequestId = UUID.randomUUID().toString();
    try {
      linkTokenService.saveLinkTokenRequestId(
          patient.getAbhaAddress(), patient.getHipId(), generateTokenRequestId);
      ResponseEntity<GenericV3Response> response =
          requestV3Manager.fetchResponseFromGateway(
              generateLinkTokenPath,
              generateTokenRequest,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIP_ID,
                  linkRecordsV3Request.getRequesterId(),
                  generateTokenRequestId));
      log.debug(generateLinkTokenPath + " : generateTokenRequest: " + response.getStatusCode());
      if (response.getStatusCode() == HttpStatus.ACCEPTED) {
        requestLogV3Service.saveLinkTokenRequest(
            linkRecordsV3Request,
            generateTokenRequestId,
            RequestStatus.LINK_TOKEN_REQUEST_ACCEPTED,
            null);
        return FacadeV3Response.builder()
            .httpStatusCode(response.getStatusCode())
            .clientRequestId(linkRecordsV3Request.getRequestId())
            .message(RequestStatus.LINK_TOKEN_REQUEST_ACCEPTED.getValue())
            .build();
      } else if (Objects.nonNull(response.getBody())) {
        ErrorResponse errorResponse =
            ErrorResponse.builder()
                .code(GatewayConstants.ERROR_CODE)
                .message("Unable to generate linkToken")
                .build();
        requestLogV3Service.saveLinkTokenRequest(
            linkRecordsV3Request,
            generateTokenRequestId,
            RequestStatus.LINK_TOKEN_REQUEST_ERROR,
            ErrorHandler.getErrors(errorResponse));
        return FacadeV3Response.builder()
            .httpStatusCode(response.getStatusCode())
            .clientRequestId(linkRecordsV3Request.getRequestId())
            .errors(ErrorHandler.getErrors(errorResponse))
            .message("LinkToken generation Failed, Kindly try again later")
            .build();
      }
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      requestLogV3Service.saveLinkTokenRequest(
          linkRecordsV3Request,
          generateTokenRequestId,
          RequestStatus.LINK_TOKEN_REQUEST_ERROR,
          Collections.singletonList(
              ErrorV3Response.builder()
                  .error(
                      ErrorResponse.builder()
                          .code(GatewayConstants.ERROR_CODE)
                          .message("Unable to generate linkToken")
                          .build())
                  .build()));
      return FacadeV3Response.builder()
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .clientRequestId(linkRecordsV3Request.getRequestId())
          .message("LinkToken generation Failed, kindly try again later.")
          .errors(ErrorHandler.getErrors(error))
          .build();
    } catch (Exception ex) {
      String error =
          "Exception while Generation of Link Token for HIP Linking: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
      return FacadeV3Response.builder()
          .httpStatusCode(HttpStatus.BAD_REQUEST)
          .clientRequestId(linkRecordsV3Request.getRequestId())
          .message("LinkToken generation Failed, kindly try again later.")
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
    return null;
  }

  public void handleAddCareContexts(
      OnGenerateTokenResponse onGenerateTokenResponse, HttpHeaders headers) {
    try {
      if (Objects.nonNull(onGenerateTokenResponse.getError())) {
        requestLogV3Service.updateErrorLinkError(
            onGenerateTokenResponse.getResponse().getRequestId(),
            onGenerateTokenResponse.getError(),
            RequestStatus.LINK_TOKEN_REQUEST_ERROR);
        return;
      }

      linkTokenService.saveLinkToken(
          onGenerateTokenResponse.getAbhaAddress(),
          onGenerateTokenResponse.getLinkToken(),
          Objects.requireNonNull(headers.getFirst(GatewayConstants.X_HIP_ID)));
      // Fetching the GenerateLinkToken request
      RequestLog RequestLog =
          requestLogV3Service.getLogsByAbhaAddress(
              onGenerateTokenResponse.getAbhaAddress(),
              Objects.requireNonNull(headers.get(GatewayConstants.X_HIP_ID)).toString());
      if (Objects.isNull(RequestLog)) {
        return;
      }

      LinkRecordsV3Request linkRecordsV3Request =
          (LinkRecordsV3Request)
              RequestLog.getRequestDetails().get(FieldIdentifiers.LINK_RECORDS_REQUEST);

      FacadeV3Response facadeV3Response = addCareContexts(linkRecordsV3Request);
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception ex) {
      String error =
          "Exception while Generation of Link Token for HIP Linking: "
              + ex.getMessage()
              + " unwrapped exception: "
              + Exceptions.unwrap(ex);
      log.debug(error);
    }
  }

  private String getAbhaNumber(String linkToken) {
    return JWT.decode(linkToken).getClaim("abhaNumber").asString();
  }

  /**
   * Notifying ABDM gateway that these careContexts with HiTypes were linked with abhaAddress.
   *
   * @param linkRecordsV3Request
   */
  public FacadeV3Response hipContextNotify(
      LinkRecordsV3Request linkRecordsV3Request, Patient patient, List<CareContext> careContexts) {
    if (Objects.isNull(linkRecordsV3Request)) {
      log.error("hipContextNotify failed because careContexts are null");
      return FacadeV3Response.builder()
          .errors(
              ErrorHandler.getErrors(
                  ErrorResponse.builder()
                      .message("hipContextNotify failed because careContexts are null")
                      .code(GatewayConstants.ERROR_CODE)))
          .build();
    }
    for (CareContext careContext : careContexts) {
      PatientNotification patientNotification =
          PatientNotification.builder()
              .patient(PatientId.builder().id(linkRecordsV3Request.getAbhaAddress()).build())
              .hip(ConsentHIP.builder().id(linkRecordsV3Request.getRequesterId()).build())
              .hiTypes(Collections.singletonList(careContext.getHiType()))
              .date(Utils.getCurrentTimeStamp())
              .careContexts(
                  ConsentCareContexts.builder()
                      .careContextReference(careContext.getReferenceNumber())
                      .patientReference(patient.getPatientReference())
                      .build())
              .build();
      LinkContextNotify linkContextNotify =
          LinkContextNotify.builder().notification(patientNotification).build();
      log.debug(linkContextNotifyPath + " : " + linkContextNotify.toString());
      try {
        ResponseEntity<GenericV3Response> response =
            requestV3Manager.fetchResponseFromGateway(
                linkContextNotifyPath,
                linkContextNotify,
                Utils.getCustomHeaders(
                    GatewayConstants.X_HIP_ID, patient.getHipId(), UUID.randomUUID().toString()));
        log.debug(linkContextNotifyPath + " : linkContextNotify: " + response.getStatusCode());
      } catch (WebClientResponseException.BadRequest ex) {
        ErrorResponse error = ex.getResponseBodyAs(ErrorV3Response.class).getError();
        log.error("HTTP error {}: {}", ex.getStatusCode(), error);
        requestLogV3Service.persistHipLinkRequest(
            linkRecordsV3Request, RequestStatus.CARECONTEXT_NOTIFY_ERROR, error);
        return FacadeV3Response.builder()
            .errors(ErrorHandler.getErrors(error))
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .clientRequestId(linkRecordsV3Request.getRequestId())
            .message(RequestStatus.CARECONTEXT_NOTIFY_ERROR.getValue())
            .build();
      } catch (Exception e) {
        String error =
            linkContextNotifyPath
                + " : context/notify: Error while performing updation of careContexts by gateway: "
                + e.getMessage()
                + " unwrapped exception: "
                + Exceptions.unwrap(e);
        log.error(error);
        requestLogV3Service.persistHipLinkRequest(
            linkRecordsV3Request,
            RequestStatus.CARECONTEXT_NOTIFY_ERROR,
            ErrorResponse.builder().code(GatewayConstants.ERROR_CODE).message(error).build());
        return FacadeV3Response.builder()
            .errors(
                ErrorHandler.getErrors(
                    ErrorResponse.builder()
                        .code(GatewayConstants.ERROR_CODE)
                        .message(error)
                        .build()))
            .httpStatusCode(HttpStatus.BAD_REQUEST)
            .clientRequestId(linkRecordsV3Request.getRequestId())
            .message(RequestStatus.CARECONTEXT_NOTIFY_ERROR.getValue())
            .build();
      }
    }
    requestLogV3Service.persistHipLinkRequest(
        linkRecordsV3Request, RequestStatus.CARECONTEXT_NOTIFY_ACCEPTED, null);
    return FacadeV3Response.builder()
        .httpStatusCode(HttpStatus.ACCEPTED)
        .clientRequestId(linkRecordsV3Request.getRequestId())
        .message(RequestStatus.CARECONTEXT_NOTIFY_ACCEPTED.getValue())
        .build();
  }
}
