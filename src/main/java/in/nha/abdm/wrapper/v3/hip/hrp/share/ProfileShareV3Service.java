/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.share;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.models.RespRequest;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.share.requests.helpers.TokenTimeStamp;
import in.nha.abdm.wrapper.v3.common.RequestV3Manager;
import in.nha.abdm.wrapper.v3.common.exceptions.BadRequestHandler;
import in.nha.abdm.wrapper.v3.common.models.GenericV3Response;
import in.nha.abdm.wrapper.v3.database.mongo.services.PatientV3Service;
import in.nha.abdm.wrapper.v3.database.mongo.services.RequestLogV3Service;
import in.nha.abdm.wrapper.v3.hip.HIPV3Client;
import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.*;
import java.util.Collections;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class ProfileShareV3Service implements ProfileShareV3Interface {
  private final PatientRepo patientRepo;
  private final RequestV3Manager requestV3Manager;
  private final HIPV3Client hipClient;
  private final RequestLogV3Service requestLogV3Service;
  private final PatientV3Service patientV3Service;
  @Autowired TokenNumberV3Generator tokenNumberGenerator;

  @Value("${profileOnSharePath}")
  public String profileOnSharePath;

  private static final Logger log = LogManager.getLogger(ProfileShareV3Service.class);

  public ProfileShareV3Service(
      PatientRepo patientRepo,
      RequestV3Manager requestV3Manager,
      HIPV3Client hipClient,
      RequestLogV3Service requestLogV3Service,
      PatientV3Service patientV3Service) {
    this.patientRepo = patientRepo;
    this.requestV3Manager = requestV3Manager;
    this.hipClient = hipClient;
    this.requestLogV3Service = requestLogV3Service;
    this.patientV3Service = patientV3Service;
  }

  /**
   * With the use of hashMap checking the cache token has already been generated If there already is
   * an entry passing the same token number again instead of generating a new token
   *
   * @param profileShare has the basic demographic details for registering the patient in facility.
   */
  @Override
  public void shareProfile(ProfileShareV3Request profileShare, HttpHeaders headers) {
    TokenTimeStamp existingToken =
        tokenNumberGenerator.checkTokenStatus(
            profileShare, headers.getFirst(GatewayConstants.X_HIP_ID));
    TokenTimeStamp token = null;
    ProfileV3Acknowledgement acknowledgement = null;
    if (existingToken != null) {
      token = existingToken;
      TokenProfile tokenProfile =
          TokenProfile.builder()
              .context(profileShare.getMetaData().getContext())
              .tokenNumber(token.getToken())
              .expiry(String.valueOf(token.getExpiry()))
              .build();
      acknowledgement =
          ProfileV3Acknowledgement.builder()
              .abhaAddress(profileShare.getProfile().getPatient().getAbhaAddress())
              .status("SUCCESS")
              .profile(tokenProfile)
              .build();
    } else {
      token =
          tokenNumberGenerator.generateTokenNumber(
              profileShare, headers.getFirst(GatewayConstants.X_HIP_ID));
      log.info("Making post request to HIP-profile/share with token : " + token);
      ResponseEntity<ProfileV3Acknowledgement> profileAcknowledgement =
          hipClient.shareProfile(
              ShareProfileV3Request.builder()
                  .token(token.getToken())
                  .hipId(headers.getFirst(GatewayConstants.X_HIP_ID))
                  .patient(profileShare.getProfile().getPatient())
                  .context(profileShare.getMetaData().getContext())
                  .build());
      acknowledgement = profileAcknowledgement.getBody();

      if (patientRepo.findByAbhaAddress(
              profileShare.getProfile().getPatient().getAbhaAddress(),
              headers.getFirst(GatewayConstants.X_HIP_ID))
          == null) {

        patientV3Service.upsertPatients(
            Collections.singletonList(
                savePatient(profileShare, headers.getFirst(GatewayConstants.X_HIP_ID))));
        log.info("Saved patient details into wrapper db");
      }
    }
    OnShareV3Request profileOnShare = null;
    if (acknowledgement != null && acknowledgement.getStatus().equals("SUCCESS")) {
      profileOnShare =
          OnShareV3Request.builder()
              .response(new RespRequest(headers.getFirst(GatewayConstants.REQUEST_ID)))
              .acknowledgement(acknowledgement)
              .build();
      log.info("onShare : " + profileOnShare.toString());
      try {
        ResponseEntity<GenericV3Response> responseEntity =
            requestV3Manager.fetchResponseFromGateway(
                profileOnSharePath,
                profileOnShare,
                Utils.getCustomHeaders(
                    GatewayConstants.X_HIP_ID,
                    headers.getFirst(GatewayConstants.X_HIP_ID),
                    UUID.randomUUID().toString()));
        log.info(profileOnSharePath + " : onShare: " + responseEntity.getStatusCode());
        tokenNumberGenerator.updateExpiry(
            acknowledgement, headers.getFirst(GatewayConstants.X_HIP_ID));
      } catch (WebClientResponseException.BadRequest ex) {
        Object error = BadRequestHandler.getError(ex);
        log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      } catch (Exception e) {
        log.info("Error: " + e);
      }
    } else {
      profileOnShare =
          OnShareV3Request.builder()
              .response(new RespRequest(headers.getFirst(GatewayConstants.REQUEST_ID)))
              .error(
                  ErrorResponse.builder()
                      .code(GatewayConstants.ERROR_CODE)
                      .message("FAILURE at HIP")
                      .build())
              .build();
      log.info("onShareError : " + profileOnShare.toString());
      try {
        ResponseEntity<GenericV3Response> responseEntity =
            requestV3Manager.fetchResponseFromGateway(
                profileOnSharePath,
                profileOnShare,
                Utils.getCustomHeaders(
                    GatewayConstants.X_HIP_ID,
                    headers.getFirst(GatewayConstants.X_HIP_ID),
                    UUID.randomUUID().toString()));
        log.info(profileOnSharePath + " : onShareError: " + responseEntity.getStatusCode());
      } catch (WebClientResponseException.BadRequest ex) {
        Object error = BadRequestHandler.getError(ex);
        log.error("HTTP error {}: {}", ex.getStatusCode(), error);
      } catch (Exception e) {
        log.info("Error: " + e);
      }
    }
    try {
      requestLogV3Service.saveScanAndShareDetails(profileShare, profileOnShare);
    } catch (Exception e) {
      log.error("Unable to store scan and share token details");
    }
  }

  private Patient savePatient(ProfileShareV3Request profileShare, String hipId) {
    Patient patient = new Patient();
    patient.setAbhaAddress(profileShare.getProfile().getPatient().getAbhaAddress());
    patient.setGender(profileShare.getProfile().getPatient().getGender());
    patient.setName(profileShare.getProfile().getPatient().getName());
    patient.setDateOfBirth(
        profileShare.getProfile().getPatient().getYearOfBirth()
            + "-"
            + profileShare.getProfile().getPatient().getMonthOfBirth()
            + "-"
            + profileShare.getProfile().getPatient().getDayOfBirth());
    patient.setPatientDisplay(profileShare.getProfile().getPatient().getName());
    patient.setPatientMobile(profileShare.getProfile().getPatient().getPhoneNumber());
    patient.setHipId(hipId);
    patient.setPatientReference(profileShare.getProfile().getPatient().getAbhaAddress());
    return patient;
  }
}
