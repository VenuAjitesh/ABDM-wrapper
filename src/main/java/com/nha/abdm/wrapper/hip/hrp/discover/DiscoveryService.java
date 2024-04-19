/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.discover;

import com.nha.abdm.wrapper.common.RequestManager;
import com.nha.abdm.wrapper.common.Utils;
import com.nha.abdm.wrapper.common.models.CareContext;
import com.nha.abdm.wrapper.common.models.RespRequest;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.ErrorResponseWrapper;
import com.nha.abdm.wrapper.common.responses.GatewayCallbackResponse;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import com.nha.abdm.wrapper.hip.HIPClient;
import com.nha.abdm.wrapper.hip.HIPPatient;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.PatientRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.PatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.RequestLogService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.Patient;
import com.nha.abdm.wrapper.hip.hrp.discover.requests.*;
import info.debatty.java.stringsimilarity.JaroWinkler;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class DiscoveryService implements DiscoveryInterface {

  private final PatientRepo patientRepo;
  private final RequestManager requestManager;
  private final HIPClient hipClient;
  private final RequestLogService requestLogService;
  private final PatientService patientService;

  JaroWinkler jaroWinkler = new JaroWinkler();

  @Value("${onDiscoverPath}")
  public String onDiscoverPath;

  @Autowired
  public DiscoveryService(
      RequestManager requestManager,
      HIPClient hipClient,
      PatientRepo patientRepo,
      RequestLogService requestLogService,
      PatientService patientService) {
    this.requestManager = requestManager;
    this.hipClient = hipClient;
    this.patientRepo = patientRepo;
    this.requestLogService = requestLogService;
    this.patientService = patientService;
  }

  private static final Logger log = LogManager.getLogger(DiscoveryService.class);

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
  public ResponseEntity<GatewayCallbackResponse> discover(DiscoverRequest discoverRequest) {
    log.info("DiscoveryService discoverRequest: " + discoverRequest);
    if (Objects.isNull(discoverRequest) || Objects.isNull(discoverRequest.getPatient())) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    String abhaAddress = discoverRequest.getPatient().getId();
    String yearOfBirth = discoverRequest.getPatient().getYearOfBirth();
    String gender = discoverRequest.getPatient().getGender();
    String name = discoverRequest.getPatient().getName();

    // First find patient using their abha address.
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress);
    log.info("DiscoveryService patient:" + patient);
    // If there is no match by abha address, then the lookup should be done by mobile number
    // and patient reference number.
    if (Objects.isNull(patient)) {
      boolean patientFound = false;
      if (!CollectionUtils.isEmpty(discoverRequest.getPatient().getVerifiedIdentifiers())) {
        String patientMobileNumber =
            discoverRequest.getPatient().getVerifiedIdentifiers().get(0).getValue();
        // If mobile number was provided in request as verified identifier.
        if (StringUtils.hasLength(patientMobileNumber)) {
          Optional<Patient> patientMatch =
              findPatientUsingMobile(
                  discoverRequest, patientMobileNumber, yearOfBirth, gender, name);
          if (!patientMatch.isEmpty()) {
            patient = patientMatch.get();
            patientFound = true;
          }
        } else if (!CollectionUtils.isEmpty(
            discoverRequest.getPatient().getUnverifiedIdentifiers())) {
          // Now search using patient reference number.
          String patientReferenceNumber =
              discoverRequest.getPatient().getUnverifiedIdentifiers().get(0).getValue();
          // If patient reference number was provided in request as unverified identifier.
          if (StringUtils.hasLength(patientReferenceNumber)) {
            Patient patientByReference = patientRepo.findByPatientReference(patientReferenceNumber);
            // If patient is not found in database using their reference number
            // or if found but not matched by demographics, then send error.
            if (Objects.nonNull(patientByReference)
                && (isGenderMatch(patientByReference, gender)
                    && (isYearOfBirthInRange(patientByReference, yearOfBirth)
                        && (isFuzzyNameMatch(patientByReference, name))))) {
              patient = patientByReference;
              patientFound = true;
            }
          }
        }

        if (!patientFound) {
          // Patient not found in database. Request Patient details from HIP.
          ResponseEntity<HIPPatient> responseEntity = hipClient.patientDiscover(discoverRequest);
          // If patient was not found at HIP as well.
          if (Objects.isNull(responseEntity)
              || responseEntity.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setCode(1000);
            errorResponse.setMessage("Patient details could not be found in the system.");
            onDiscoverNoPatientRequest(discoverRequest, errorResponse);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
          }
          HIPPatient hipPatient = responseEntity.getBody();
          onDiscoverRequest(
              discoverRequest,
              hipPatient.getPatientReference(),
              hipPatient.getPatientDisplay(),
              hipPatient.getCareContexts());
          addPatientToDatabase(hipPatient);
          return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
      }
    }
    processCareContexts(patient, abhaAddress, discoverRequest);
    return new ResponseEntity<>(HttpStatus.ACCEPTED);
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

    patientService.upsertPatients(Arrays.asList(patient));
  }

  private Optional<Patient> findPatientUsingMobile(
      DiscoverRequest discoverRequest,
      String patientMobileNumber,
      String yearOfBirth,
      String gender,
      String name) {
    List<Patient> patientsByMobileNumber = patientRepo.findByPatientMobile(patientMobileNumber);
    if (!CollectionUtils.isEmpty(discoverRequest.getPatient().getUnverifiedIdentifiers())) {
      String patientReferenceNumber =
          discoverRequest.getPatient().getUnverifiedIdentifiers().get(0).getValue();
      // If patient reference number was provided in request as unverified identifier.
      if (StringUtils.hasLength(patientReferenceNumber)) {
        return patientsByMobileNumber.stream()
            .filter(p -> patientReferenceNumber.equals(p.getPatientReference()))
            .findFirst();
      } else {
        return matchPatientByDemographics(patientsByMobileNumber, yearOfBirth, gender, name);
      }
    } else {
      return matchPatientByDemographics(patientsByMobileNumber, yearOfBirth, gender, name);
    }
  }

  private Optional<Patient> matchPatientByDemographics(
      List<Patient> patientsByMobileNumber, String yearOfBirth, String gender, String name) {
    return patientsByMobileNumber.stream()
        .filter(patient -> isGenderMatch(patient, gender))
        .filter(patient -> isYearOfBirthInRange(patient, yearOfBirth))
        .filter(patient -> isFuzzyNameMatch(patient, name))
        .findFirst();
  }

  private void processCareContexts(
      Patient patient, String abhaAddress, DiscoverRequest discoverRequest) {
    // Get Linked Care Contexts which were fetched from database.
    List<CareContext> linkedCareContexts = patient.getCareContexts();
    // Get All Care Contexts of the given patient from HIP.
    CareContextRequest careContextRequest =
        CareContextRequest.builder()
            .abhaAddress(discoverRequest.getPatient().getId())
            .hipId(discoverRequest.getHipId())
            .build();
    HIPPatient hipPatient = hipClient.getPatientCareContexts(careContextRequest);
    if (Objects.isNull(hipPatient)) {
      ErrorResponse errorResponse = new ErrorResponse();
      errorResponse.setCode(1000);
      errorResponse.setMessage("Patient not found in HIP");
      onDiscoverNoPatientRequest(discoverRequest, errorResponse);
    } else if (CollectionUtils.isEmpty(hipPatient.getCareContexts())) {
      ErrorResponse errorResponse = new ErrorResponse();
      errorResponse.setCode(1000);
      errorResponse.setMessage("Care Contexts not found for patient");
      onDiscoverNoPatientRequest(discoverRequest, errorResponse);
    } else {
      List<CareContext> careContexts = hipPatient.getCareContexts();
      List<CareContext> unlinkedCareContexts;
      if (CollectionUtils.isEmpty(linkedCareContexts)) {
        unlinkedCareContexts = careContexts;
      } else {
        Set<String> linkedCareContextsSet =
            linkedCareContexts.stream()
                .map(x -> x.getReferenceNumber())
                .collect(Collectors.toSet());
        unlinkedCareContexts =
            careContexts.stream()
                .filter(x -> !linkedCareContextsSet.contains(x.getReferenceNumber()))
                .collect(Collectors.toList());
      }
      if (unlinkedCareContexts.isEmpty()) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(1000);
        errorResponse.setMessage("Care Contexts not found for patient");
        onDiscoverNoPatientRequest(discoverRequest, errorResponse);
        return;
      }
      onDiscoverRequest(
          discoverRequest,
          hipPatient.getPatientReference(),
          hipPatient.getPatientDisplay(),
          unlinkedCareContexts);
    }
  }

  /**
   * <B>Discovery</B>
   *
   * <p>Build the body with the respective careContexts into onDiscoverRequest.
   *
   * @param discoverRequest Response from ABDM gateway.
   * @param patientReference Patient reference number.
   * @param display Patient display name.
   * @param careContexts list of non-linked careContexts.
   */
  private void onDiscoverRequest(
      DiscoverRequest discoverRequest,
      String patientReference,
      String display,
      List<CareContext> careContexts) {

    OnDiscoverPatient onDiscoverPatient =
        OnDiscoverPatient.builder()
            .referenceNumber(patientReference)
            .display(display)
            .careContexts(careContexts)
            .matchedBy(Arrays.asList("MOBILE"))
            .build();
    OnDiscoverRequest onDiscoverRequest =
        OnDiscoverRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp().toString())
            .transactionId(discoverRequest.getTransactionId())
            .patient(onDiscoverPatient)
            .resp(RespRequest.builder().requestId(discoverRequest.getRequestId()).build())
            .build();
    log.info("onDiscover : " + onDiscoverRequest.toString());
    try {
      ResponseEntity<GenericResponse> responseEntity =
          requestManager.fetchResponseFromGateway(onDiscoverPath, onDiscoverRequest);
      log.info(onDiscoverPath + " : onDiscoverCall: " + responseEntity.getStatusCode());
      requestLogService.setDiscoverResponse(discoverRequest);
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
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
      DiscoverRequest discoverRequest, ErrorResponse errorResponse) {

    OnDiscoverErrorRequest onDiscoverErrorRequest =
        OnDiscoverErrorRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .timestamp(Utils.getCurrentTimeStamp().toString())
            .transactionId(discoverRequest.getTransactionId())
            .resp(RespRequest.builder().requestId(discoverRequest.getRequestId()).build())
            .error(errorResponse)
            .build();
    log.info("onDiscover : " + onDiscoverErrorRequest.toString());
    try {
      requestManager.fetchResponseFromGateway(onDiscoverPath, onDiscoverErrorRequest);
      log.info(
          onDiscoverPath
              + " Discover: requestId : "
              + discoverRequest.getRequestId()
              + ": Patient not found");
    } catch (WebClientResponseException.BadRequest ex) {
      ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
      log.error("HTTP error {}: {}", ex.getStatusCode(), error);
    } catch (Exception e) {
      log.error(e);
    }
  }

  /**
   * <B>discovery</B> Check of Year Of Birth in the range of +-5 years
   *
   * @param patient Particular patient record.
   * @param receivedYob Response : Year Of Birth.
   * @return true if in range else false
   */
  private boolean isYearOfBirthInRange(Patient patient, String receivedYob) {
    int existingDate = Integer.parseInt(patient.getDateOfBirth().substring(0, 4));
    return Math.abs(existingDate - Integer.parseInt(receivedYob)) <= 5;
  }

  /**
   * <B>discovery</B> Check of gender match with response gender.
   *
   * @param patient particular patient record.
   * @param receivedGender Response : gender.
   * @return true if gender matches or else false.
   */
  private boolean isGenderMatch(Patient patient, String receivedGender) {
    return receivedGender.equals(patient.getGender());
  }

  /**
   * <B>discovery</B> Matching of patient name with response name by jaroWinkler algorithm making
   * 0.5 a reasonable validation.
   *
   * @param patient particular patient record.
   * @param receivedName Response : name.
   * @return true if name matches or else false.
   */
  private boolean isFuzzyNameMatch(Patient patient, String receivedName) {
    return jaroWinkler.similarity(patient.getName(), receivedName) >= 0.5;
  }
}
