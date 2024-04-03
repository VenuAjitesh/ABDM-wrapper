/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.share;

import com.nha.abdm.wrapper.common.RequestManager;
import com.nha.abdm.wrapper.common.Utils;
import com.nha.abdm.wrapper.common.models.RespRequest;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.common.responses.ErrorResponseWrapper;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import com.nha.abdm.wrapper.hip.HIPClient;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.PatientRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.PatientService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.services.RequestLogService;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.Patient;
import com.nha.abdm.wrapper.hip.hrp.share.reponses.ProfileShare;
import com.nha.abdm.wrapper.hip.hrp.share.requests.ProfileOnShare;
import com.nha.abdm.wrapper.hip.hrp.share.requests.ShareProfileRequest;
import com.nha.abdm.wrapper.hip.hrp.share.requests.helpers.ProfileAcknowledgement;
import java.util.Collections;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class ProfileShareService implements ProfileShareInterface {
  private final PatientRepo patientRepo;
  private final RequestManager requestManager;
  private final HIPClient hipClient;
  private final RequestLogService requestLogService;
  private final PatientService patientService;
  @Autowired TokenNumberGenerator tokenNumberGenerator;

  @Value("${profileOnSharePath}")
  public String profileOnSharePath;

  private static final Logger log = LogManager.getLogger(ProfileShareService.class);

  public ProfileShareService(
      PatientRepo patientRepo,
      RequestManager requestManager,
      HIPClient hipClient,
      RequestLogService requestLogService,
      PatientService patientService) {
    this.patientRepo = patientRepo;
    this.requestManager = requestManager;
    this.hipClient = hipClient;
    this.requestLogService = requestLogService;
    this.patientService = patientService;
  }

  @Override
  public void shareProfile(ProfileShare profileShare, String hipId) {
    String token = tokenNumberGenerator.generateTokenNumber();
    log.info("Making post request to HIP-profile/share with token : " + token);
    ResponseEntity<ProfileAcknowledgement> profileAcknowledgement =
        hipClient.shareProfile(
            ShareProfileRequest.builder().token(token).hipId(hipId).profile(profileShare).build());
    ProfileAcknowledgement acknowledgement = profileAcknowledgement.getBody();
    if (acknowledgement != null && acknowledgement.getStatus().equals("SUCCESS")) {
      ProfileOnShare profileOnShare =
          ProfileOnShare.builder()
              .requestId(UUID.randomUUID().toString())
              .resp(new RespRequest(profileShare.getRequestId()))
              .timestamp(Utils.getCurrentTimeStamp())
              .acknowledgement(acknowledgement)
              .build();
      log.info("onShare : " + profileOnShare.toString());
      if (patientRepo.findByAbhaAddress(profileShare.getProfile().getPatient().getHealthId())
          == null) {
        Patient patient = new Patient();
        patient.setAbhaAddress(profileShare.getProfile().getPatient().getHealthId());
        patient.setGender(profileShare.getProfile().getPatient().getGender());
        patient.setName(profileShare.getProfile().getPatient().getName());
        patient.setDateOfBirth(
            profileShare.getProfile().getPatient().getYearOfBirth()
                + "-"
                + profileShare.getProfile().getPatient().getMonthOfBirth()
                + "-"
                + profileShare.getProfile().getPatient().getDayOfBirth());
        patient.setPatientDisplay(profileShare.getProfile().getPatient().getName());
        patient.setPatientMobile(
            profileShare.getProfile().getPatient().getIdentifiers().get(0).getValue());
        patient.setEntity(hipId);
        patientService.upsertPatients(Collections.singletonList(patient));
        log.info("Saved patient details into wrapper db");
      }
      try {
        ResponseEntity<GenericResponse> responseEntity =
            requestManager.fetchResponseFromGateway(profileOnSharePath, profileOnShare);
        log.info(profileOnSharePath + " : onShare: " + responseEntity.getStatusCode());
      } catch (WebClientResponseException.BadRequest ex) {
        ErrorResponse error = ex.getResponseBodyAs(ErrorResponseWrapper.class).getError();
        log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      } catch (Exception e) {
        log.info("Error: " + e);
      }
    }
  }
}
