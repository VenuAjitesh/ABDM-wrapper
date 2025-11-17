/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.discover;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.HIPPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.*;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hip.HIPV3Client;
import in.nha.abdm.wrapper.v3.hip.hrp.discover.requests.OnDiscoverV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.helpers.PatientCareContextHIType;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class DiscoveryV3Service implements DiscoveryV3Interface {

  private final PatientRepo patientRepo;
  private final RequestV3Manager requestV3Manager;
  private final HIPV3Client hipClient;
  private final RequestLogV3Service requestLogV3Service;
  private final PatientV3Service patientV3Service;

  @Value("${onDiscoverPath}")
  public String onDiscoverPath;

  @Autowired
  public DiscoveryV3Service(
      HIPV3Client hipClient,
      PatientRepo patientRepo,
      RequestV3Manager requestV3Manager,
      RequestLogV3Service requestLogV3Service,
      PatientV3Service patientV3Service) {
    this.hipClient = hipClient;
    this.patientRepo = patientRepo;
    this.requestV3Manager = requestV3Manager;
    this.requestLogV3Service = requestLogV3Service;
    this.patientV3Service = patientV3Service;
  }

  private static final Logger log = LogManager.getLogger(DiscoveryV3Service.class);

  /**
   * <B>discovery</B>
   *
   * <p>Using the demographic details and abhaAddress fetching careContexts from db.<br>
   * Logic ->step 1: Check for AbhaAddress, if present build discoverRequest and make POST
   * /discover.<br>
   * step 2: fetch list of users with mobileNumber, then check patientIdentifier if present, then
   * return careContexts.<br>
   * if patientIdentifier present and not matched return null/not found.<br>
   * if patientIdentifier not present check for gender, then +-5 years in Year of birth, then name
   * with fuzzy logic, if any of the above demographics fail to match return null/ not matched.<br>
   * build discoverRequest and make POST /on-discover.
   *
   * @param discoverRequest Response from ABDM gateway with patient demographic details and
   *     abhaAddress.
   */
  @Override
  public ResponseEntity<GenericV3Response> discover(
      DiscoverRequest discoverRequest, HttpHeaders headers) {
    if (Objects.isNull(discoverRequest) || Objects.isNull(discoverRequest.getPatient())) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    String abhaAddress = discoverRequest.getPatient().getId();

    // First find patient using their abha address.
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress, discoverRequest.getHipId());

    // If there is no match by abha address, then the lookup should be done by mobile number
    // and patient reference number.
    if (Objects.nonNull(patient)) {

      log.info("Patient found {}::{}", patient.getName(), patient.getAbhaAddress());
      CompletableFuture.runAsync(
          () -> {
            try {
              processCareContexts(patient, abhaAddress, discoverRequest, headers, "ABHA_ADDRESS");
            } catch (Exception ex) {
              log.error("Error processing care contexts for patient: {}", abhaAddress, ex);
              ErrorResponse errorResponse = new ErrorResponse();
              errorResponse.setCode(GatewayConstants.ERROR_CODE);
              errorResponse.setMessage("Error processing care contexts");
              onDiscoverNoPatientRequest(discoverRequest, errorResponse, headers);
            }
          });
      return new ResponseEntity<>(HttpStatus.ACCEPTED);

    } else {

      log.warn("Patient not found in DB. Requesting from HIP");
      // Patient not found in database. Request Patient details from HIP.
      try {
        ResponseEntity<HIPPatient> responseEntity = hipClient.patientDiscover(discoverRequest);

        // If patient was not found at HIP as well.
        if (Objects.isNull(responseEntity)
            || responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)
            || Objects.isNull(responseEntity.getBody())
            || Objects.isNull(responseEntity.getBody().getCareContexts())) {
          ErrorResponse errorResponse = new ErrorResponse();
          errorResponse.setCode(GatewayConstants.NO_PATIENT_FOUND_CODE);
          errorResponse.setMessage(GatewayConstants.NO_PATIENT_FOUND);
          CompletableFuture.runAsync(
              () -> onDiscoverNoPatientRequest(discoverRequest, errorResponse, headers));
          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HIPPatient hipPatient = responseEntity.getBody();
        addPatientToDatabase(hipPatient);

        CompletableFuture.runAsync(
            () -> {
              try {
                onDiscoverRequest(
                    discoverRequest,
                    hipPatient.getAbhaAddress(),
                    hipPatient.getPatientReference(),
                    hipPatient.getPatientDisplay(),
                    hipPatient.getCareContexts(),
                    headers,
                    "MR");
              } catch (Exception ex) {
                log.error("Error in onDiscoverRequest for patient: {}", abhaAddress, ex);
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setCode(GatewayConstants.ERROR_CODE);
                errorResponse.setMessage("Error sending discover response: " + ex.getMessage());
                onDiscoverNoPatientRequest(discoverRequest, errorResponse, headers);
              }
            });
        return new ResponseEntity<>(HttpStatus.ACCEPTED);

      } catch (Exception ex) {
        log.error("Error calling HIP for patient discovery: {}", abhaAddress, ex);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(GatewayConstants.ERROR_CODE);
        errorResponse.setMessage("Error connecting to HIP: " + ex.getMessage());
        CompletableFuture.runAsync(
            () -> onDiscoverNoPatientRequest(discoverRequest, errorResponse, headers));
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
  }

  private void addPatientToDatabase(HIPPatient hipPatient) {
    Patient patient = new Patient();
    patient.setName(hipPatient.getName());
    patient.setPatientDisplay(hipPatient.getPatientDisplay());
    patient.setPatientMobile(hipPatient.getPatientMobile());
    patient.setDateOfBirth(hipPatient.getDateOfBirth());
    patient.setGender(hipPatient.getGender());
    patient.setAbhaAddress(hipPatient.getAbhaAddress());
    patient.setPatientReference(hipPatient.getPatientReference());
    patient.setHipId(hipPatient.getHipId());
    if (Objects.nonNull(hipPatient.getCareContexts())) {
      patient.setCareContexts(hipPatient.getCareContexts());
    }
    log.info("Updating patient into DB : {}", patient.getAbhaAddress());
    patientV3Service.upsertPatients(List.of(patient));
  }

  private void processCareContexts(
      Patient patient,
      String abhaAddress,
      DiscoverRequest discoverRequest,
      HttpHeaders headers,
      String matchedBy) {

    List<CareContext> linkedCareContexts = patient.getCareContexts();

    // Handle null or empty care contexts - fetch from HIP
    if (CollectionUtils.isEmpty(linkedCareContexts)) {
      log.info("No care contexts found in DB for patient: {}. Fetching from HIP", abhaAddress);
      fetchAndProcessHipCareContexts(patient, discoverRequest, headers, matchedBy, null);
      return;
    }

    // Check for unlinked contexts
    List<CareContext> unlinkedCareContexts =
        linkedCareContexts.stream()
            .filter(careContext -> !careContext.isLinked())
            .collect(Collectors.toList());

    if (!unlinkedCareContexts.isEmpty()) {
      log.info(
          "Found {} unlinked care contexts for patient: {}",
          unlinkedCareContexts.size(),
          abhaAddress);
      onDiscoverRequest(
          discoverRequest,
          patient.getAbhaAddress(),
          patient.getPatientReference(),
          patient.getPatientDisplay(),
          unlinkedCareContexts,
          headers,
          matchedBy);
      return;
    }

    // All care contexts are linked - fetch new ones from HIP
    log.info(
        "All care contexts linked for patient: {}. Fetching new contexts from HIP", abhaAddress);
    fetchAndProcessHipCareContexts(
        patient, discoverRequest, headers, matchedBy, linkedCareContexts);
  }

  private void fetchAndProcessHipCareContexts(
      Patient patient,
      DiscoverRequest discoverRequest,
      HttpHeaders headers,
      String matchedBy,
      List<CareContext> linkedCareContexts) {

    CareContextRequest careContextRequest =
        CareContextRequest.builder()
            .abhaAddress(discoverRequest.getPatient().getId())
            .hipId(discoverRequest.getHipId())
            .build();

    HIPPatient hipPatient = hipClient.getPatientCareContexts(careContextRequest);

    if (Objects.isNull(hipPatient) || CollectionUtils.isEmpty(hipPatient.getCareContexts())) {
      log.warn("No care contexts found at HIP for patient: {}", patient.getAbhaAddress());
      ErrorResponse errorResponse = new ErrorResponse();
      errorResponse.setCode(GatewayConstants.NO_PATIENT_FOUND_CODE);
      errorResponse.setMessage("No care contexts found for patient");
      onDiscoverNoPatientRequest(discoverRequest, errorResponse, headers);
      return;
    }

    List<CareContext> hipCareContexts = hipPatient.getCareContexts();
    List<CareContext> unlinkedCareContexts;

    if (CollectionUtils.isEmpty(linkedCareContexts)) {
      unlinkedCareContexts = hipCareContexts;
    } else {
      Set<String> linkedCareContextsSet =
          linkedCareContexts.stream()
              .map(CareContext::getReferenceNumber)
              .collect(Collectors.toSet());
      unlinkedCareContexts =
          hipCareContexts.stream()
              .filter(x -> !linkedCareContextsSet.contains(x.getReferenceNumber()))
              .collect(Collectors.toList());
    }

    if (unlinkedCareContexts.isEmpty()) {
      log.warn("No new unlinked care contexts found for patient: {}", patient.getAbhaAddress());
      ErrorResponse errorResponse = new ErrorResponse();
      errorResponse.setCode(GatewayConstants.NO_PATIENT_FOUND_CODE);
      errorResponse.setMessage("No new care contexts available for linking");
      onDiscoverNoPatientRequest(discoverRequest, errorResponse, headers);
      return;
    }

    log.info(
        "Found {} new care contexts for patient: {}",
        unlinkedCareContexts.size(),
        patient.getAbhaAddress());
    onDiscoverRequest(
        discoverRequest,
        patient.getAbhaAddress(),
        patient.getPatientReference(),
        patient.getPatientDisplay(),
        unlinkedCareContexts,
        headers,
        matchedBy);
  }

  /**
   * <B>Discovery</B>
   *
   * <p>Build the body with the respective careContexts into onDiscoverRequest.
   *
   * @param discoverRequest Response from ABDM gateway.
   * @param patientReference Patient reference number.
   * @param display Patient display name.
   * @param discoveredCareContexts list of non-linked careContexts.
   */
  private void onDiscoverRequest(
      DiscoverRequest discoverRequest,
      String abhaAddress,
      String patientReference,
      String display,
      List<CareContext> discoveredCareContexts,
      HttpHeaders headers,
      String matchedBy) {

    Map<String, List<CareContext>> groupedByHiType =
        discoveredCareContexts.stream().collect(Collectors.groupingBy(CareContext::getHiType));
    List<PatientCareContextHIType> patients =
        groupedByHiType.entrySet().stream()
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
                      .referenceNumber(patientReference)
                      .display(display)
                      .hiType(hiType == null ? "HealthDocumentRecord" : hiType)
                      .count(careContexts.size())
                      .careContexts(careContexts)
                      .build();
                })
            .collect(Collectors.toList());
    OnDiscoverV3Request onDiscoverV3Request =
        OnDiscoverV3Request.builder()
            .transactionId(discoverRequest.getTransactionId())
            .response(
                RespRequest.builder()
                    .requestId(headers.getFirst(GatewayConstants.REQUEST_ID))
                    .build())
            .patient(patients)
            .matchedBy(Collections.singletonList(matchedBy))
            .build();
    log.info("onDiscover : " + onDiscoverV3Request.toString());
    try {
      ResponseEntity<GenericV3Response> responseEntity =
          requestV3Manager.fetchResponseFromGateway(
              onDiscoverPath,
              onDiscoverV3Request,
              Utils.getCustomHeaders(
                  GatewayConstants.X_HIP_ID,
                  discoverRequest.getHipId(),
                  UUID.randomUUID().toString()));
      log.info(onDiscoverPath + " : onDiscoverCall: " + responseEntity.getStatusCode());
      requestLogV3Service.setDiscoverResponse(discoverRequest, onDiscoverV3Request, abhaAddress);
      log.info("Updating the careContexts into patient");
      patientV3Service.updateCareContext(
          abhaAddress, patientReference, discoveredCareContexts, discoverRequest.getHipId());
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception e) {
      log.info("Error: " + e);
    }
  }

  /**
   * <B>discovery</B>
   *
   * <p>build of onDiscoverRequest with error when patient not found.
   *
   * @param discoverRequest Response from ABDM gateway.
   * @param errorResponse The respective error message while matching patient data.
   */
  private void onDiscoverNoPatientRequest(
      DiscoverRequest discoverRequest, ErrorResponse errorResponse, HttpHeaders headers) {

    OnDiscoverErrorRequest onDiscoverErrorRequest =
        OnDiscoverErrorRequest.builder()
            .transactionId(discoverRequest.getTransactionId())
            .response(
                RespRequest.builder()
                    .requestId(headers.getFirst(GatewayConstants.REQUEST_ID))
                    .build())
            .error(errorResponse)
            .build();
    log.info("onDiscover : " + onDiscoverErrorRequest.toString());
    try {
      requestV3Manager.fetchResponseFromGateway(
          onDiscoverPath,
          onDiscoverErrorRequest,
          Utils.getCustomHeaders(
              GatewayConstants.X_HIP_ID, discoverRequest.getHipId(), UUID.randomUUID().toString()));
      log.info(
          onDiscoverPath
              + " Discover: requestId : "
              + discoverRequest.getRequestId()
              + ": Patient not found");
    } catch (WebClientResponseException.BadRequest ex) {
      Object error = BadRequestHandler.getError(ex);
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception e) {
      log.error(e);
    }
  }
}
