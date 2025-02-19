/* (C) 2024 */
package in.nha.abdm.wrapper.v3.database.mongo.services;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.models.Consent;
import in.nha.abdm.wrapper.v1.hip.HIPPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentCareContexts;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.hip.HIPV3Client;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.LinkRecordsV3Request;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class PatientV3Service {
  public PatientV3Service(PatientRepo patientRepo, PatientRepo patientRepo1) {
    this.patientRepo = patientRepo1;
  }

  private static final Logger log = LogManager.getLogger(PatientV3Service.class);
  @Autowired private final PatientRepo patientRepo;
  @Autowired MongoTemplate mongoTemplate;
  @Autowired HIPV3Client hipv3Client;

  /**
   * Fetch of patientReference using abhaAddress
   *
   * @param abhaAddress abhaAddress of patient.
   * @return patientReference.
   */
  public String getPatientReference(String abhaAddress, String hipId) {
    Patient existingRecord = this.patientRepo.findByAbhaAddress(abhaAddress, hipId);
    return existingRecord != null ? existingRecord.getPatientReference() : "";
  }

  /**
   * Fetch of patientDisplay using abhaAddress
   *
   * @param abhaAddress abhaAddress of patient.
   * @return patientDisplay.
   */
  public String getPatientDisplay(String abhaAddress, String hipId) {
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress, hipId);
    return patient != null ? patient.getPatientDisplay() : "";
  }

  /**
   * Fetch of abhaAddress using abhaAddress
   *
   * @param patientReference patientReference of patient.
   * @return abhaAddress.
   */
  public String getAbhaAddress(String patientReference, String hipId) {
    Patient existingRecord = this.patientRepo.findByPatientReference(patientReference, hipId);
    return existingRecord != null ? existingRecord.getAbhaAddress() : "";
  }

  /**
   * After successful linking of careContext updating the status i.e. isLinked to true or false.
   *
   * @param patientReference patientReference of patient.
   * @param careContexts List of careContext to update the status.
   */
  public void updateCareContext(
      String patientReference, List<CareContext> careContexts, String hipId)
      throws IllegalDataStateException {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.PATIENT_REFERENCE)
                .is(patientReference)
                .and(FieldIdentifiers.HIP_ID)
                .is(hipId));
    Patient patient = mongoTemplate.findOne(query, Patient.class);
    if (Objects.isNull(patient)) {
      throw new IllegalDataStateException("Patient not found in database: " + patientReference);
    }
    if (patient.getCareContexts() == null) {
      Update update = new Update().set(FieldIdentifiers.CARE_CONTEXTS, careContexts);
      this.mongoTemplate.updateFirst(query, update, Patient.class);
      return;
    }
    Update update = new Update().addToSet(FieldIdentifiers.CARE_CONTEXTS).each(careContexts);
    log.info(
        "updateCareContextStatus: patientReference: "
            + patientReference
            + " careContexts: "
            + careContexts);
    this.mongoTemplate.updateFirst(query, update, Patient.class);
  }

  public void updateCareContextStatus(
      String patientReference, List<CareContext> careContexts, String hipId) {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.PATIENT_REFERENCE)
                .is(patientReference)
                .and(FieldIdentifiers.HIP_ID)
                .is(hipId));

    Patient patient = this.mongoTemplate.findOne(query, Patient.class);

    if (patient == null) {
      log.error("Patient not found with reference: {} and facility: {}", patientReference, hipId);
      return;
    }

    List<CareContext> existingCareContexts = patient.getCareContexts();

    if (existingCareContexts == null) {
      log.info("No care contexts found for patient reference: {}", patientReference);
      return;
    }

    for (CareContext existingContext : existingCareContexts) {
      for (CareContext careContext : careContexts) {
        if (existingContext.getReferenceNumber().equals(careContext.getReferenceNumber())) {
          existingContext.setLinked(true);
        }
      }
    }

    Update update = new Update().set(FieldIdentifiers.CARE_CONTEXTS, existingCareContexts);
    this.mongoTemplate.updateFirst(query, update, Patient.class);

    log.info(
        "updateCareContextStatus: patientReference: "
            + patientReference
            + " careContexts: "
            + careContexts);
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>After successful link of careContexts with abhaAddress storing them into patient.
   *
   * @param linkRecordsRequest Response to facade as /link-records for hipInitiatedLinking.
   */
  public void addPatientCareContexts(LinkRecordsV3Request linkRecordsRequest) {
    String abhaAddress = linkRecordsRequest.getAbhaAddress();
    try {
      Patient existingRecord =
          this.patientRepo.findByAbhaAddress(abhaAddress, linkRecordsRequest.getRequesterId());
      if (existingRecord == null) {
        log.error("Adding patient failed -> Patient not found");
      } else {
        List<CareContext> modifiedCareContexts =
            linkRecordsRequest.getCareContexts().stream()
                .map(
                    careContextRequest -> {
                      CareContext modifiedContext = new CareContext();
                      modifiedContext.setReferenceNumber(careContextRequest.getReferenceNumber());
                      modifiedContext.setDisplay(careContextRequest.getDisplay());
                      modifiedContext.setHiType(careContextRequest.getHiType());
                      modifiedContext.setLinked(true);
                      return modifiedContext;
                    })
                .collect(Collectors.toList());
        Query query =
            new Query(
                Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                    .is(linkRecordsRequest.getAbhaAddress())
                    .and(FieldIdentifiers.HIP_ID)
                    .is(linkRecordsRequest.getRequesterId()));
        Update update =
            new Update().addToSet(FieldIdentifiers.CARE_CONTEXTS).each(modifiedCareContexts);
        this.mongoTemplate.updateFirst(query, update, Patient.class);
      }
    } catch (Exception e) {
      log.info("addPatient :" + e);
    }
    log.info("Successfully Added Patient careContexts");
  }

  public void addConsent(String abhaAddress, Consent consent, String hipId)
      throws IllegalDataStateException {
    log.info("{} Consent : {}", abhaAddress, consent.toString());
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress, hipId);
    if (patient == null) {
      throw new IllegalDataStateException("Patient not found in database: " + abhaAddress);
    }
    List<Consent> consents = patient.getConsents();
    if (!CollectionUtils.isEmpty(consents)) {
      for (Consent storedConsent : consents) {
        if (storedConsent
            .getConsentDetail()
            .getConsentId()
            .equals(consent.getConsentDetail().getConsentId())) {
          String message =
              String.format("Consent %s already exists for patient %s: ", consent, abhaAddress);
          log.warn(message);
          return;
        }
      }
    }
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                .is(abhaAddress)
                .and(FieldIdentifiers.HIP_ID)
                .is(hipId));
    Update update = new Update().addToSet(FieldIdentifiers.CONSENTS, consent);
    mongoTemplate.updateFirst(query, update, Patient.class);
  }

  /**
   * Fetching the Consent date range for health information request
   *
   * @param abhaAddress
   * @param consentId
   * @return patient consent
   */
  public Consent getConsentDetails(String abhaAddress, String consentId, String hipId)
      throws IllegalDataStateException {
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress, hipId);
    if (patient == null) {
      throw new IllegalDataStateException("Patient not found in database: " + abhaAddress);
    }
    return patient.getConsents().stream()
        .filter(consent -> consent.getConsentDetail().getConsentId().equals(consentId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Adds or Updates patient demographic data.
   *
   * @param patients List of patients with reference and demographic details.
   * @return status of adding or modifying patients in database.
   */
  @Transactional
  public FacadeV3Response upsertPatients(List<Patient> patients) {
    MongoCollection<Document> collection =
        mongoTemplate.getCollection(FieldIdentifiers.TABLE_PATIENT);
    List<WriteModel<Document>> updates = new ArrayList<>();
    for (Patient patient : patients) {
      Document filter =
          new Document()
              .append(FieldIdentifiers.ABHA_ADDRESS, patient.getAbhaAddress())
              .append(FieldIdentifiers.HIP_ID, patient.getHipId());
      Document document =
          new Document()
              .append(FieldIdentifiers.ABHA_ADDRESS, patient.getAbhaAddress())
              .append(FieldIdentifiers.NAME, patient.getName())
              .append(FieldIdentifiers.GENDER, patient.getGender())
              .append(FieldIdentifiers.DATE_OF_BIRTH, patient.getDateOfBirth())
              .append(FieldIdentifiers.PATIENT_REFERENCE, patient.getPatientReference())
              .append(FieldIdentifiers.PATIENT_DISPLAY, patient.getPatientDisplay())
              .append(FieldIdentifiers.HIP_ID, patient.getHipId())
              .append(FieldIdentifiers.PATIENT_MOBILE, patient.getPatientMobile());

      Document update = new Document("$set", document);

      updates.add(new UpdateOneModel<>(filter, update, new UpdateOptions().upsert(true)));
    }

    BulkWriteResult bulkWriteResult = collection.bulkWrite(updates);
    int updatedPatientCount =
        bulkWriteResult.getUpserts().size() > 0
            ? bulkWriteResult.getUpserts().size()
            : bulkWriteResult.getModifiedCount();

    return FacadeV3Response.builder()
        .message(String.format("Successfully upserted %d patients", updatedPatientCount))
        .build();
  }

  /**
   * <B>Data Transfer</B> For a given list of careContextsReference and patientReference check
   * whether careContexts match with patient.
   *
   * @param careContextsWithPatientReference Has CareContextReference and patientReference.
   * @return if all the Contexts match with respective patient return true;
   */
  public boolean isCareContextPresent(
      List<ConsentCareContexts> careContextsWithPatientReference, String hipId) {
    if (careContextsWithPatientReference == null) return false;
    for (ConsentCareContexts careContexts : careContextsWithPatientReference) {
      Patient patient =
          patientRepo.findByPatientReference(careContexts.getPatientReference(), hipId);
      if (patient == null) {
        return false;
      }
      List<CareContext> existingCareContexts = patient.getCareContexts();
      if (existingCareContexts == null) return false;
      if (!existingCareContexts.stream()
          .anyMatch(
              existingContext ->
                  careContextsWithPatientReference.stream()
                      .anyMatch(
                          context ->
                              context
                                      .getCareContextReference()
                                      .equals(existingContext.getReferenceNumber())
                                  && context
                                      .getPatientReference()
                                      .equals(patient.getPatientReference())))) {
        return false;
      }
    }
    return true;
  }

  public Patient getPatientDetails(String abhaAddress, String hipId) {
    return patientRepo.findByAbhaAddress(abhaAddress, hipId);
  }

  /**
   * Updating the consent details if the consent has been revoked or expired.
   *
   * @param abhaAddress
   * @param consentId
   * @param consentStatus
   * @param lastUpdated
   * @param hipId
   */
  public void updatePatientConsent(
      String abhaAddress,
      String consentId,
      String consentStatus,
      String lastUpdated,
      String hipId) {
    MongoCollection<Document> collection =
        mongoTemplate.getCollection(FieldIdentifiers.TABLE_PATIENT);
    Bson filter =
        Filters.and(
            Filters.eq(FieldIdentifiers.ABHA_ADDRESS, abhaAddress),
            Filters.eq("consents.consentDetail.consentId", consentId),
            Filters.eq(FieldIdentifiers.HIP_ID, hipId));
    Bson update =
        Updates.combine(
            Updates.set("consents.$.status", consentStatus),
            Updates.set("consents.$.lastUpdatedOn", lastUpdated));
    UpdateResult result = collection.updateOne(filter, update);
    log.debug("consent update result: ", result);
  }

  /**
   * Checking the requested careContexts are present in the patient collection.
   *
   * @param abhaAddress
   * @param hipId
   * @param careContexts
   * @return
   */
  public boolean checkCareContexts(
      String abhaAddress, String hipId, List<CareContext> careContexts) {
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress, hipId);
    if (patient == null) {
      return false;
    }
    List<CareContext> existingCareContexts = patient.getCareContexts();
    if (existingCareContexts == null) return false;
    if (!existingCareContexts.stream()
        .anyMatch(
            existingContext ->
                careContexts.stream()
                    .anyMatch(
                        context ->
                            context
                                    .getReferenceNumber()
                                    .equals(existingContext.getReferenceNumber())
                                && context.getHiType().equals(existingContext.getHiType())))) {
      return false;
    }
    return true;
  }

  /**
   * While initiating careContext linking, some of the careContext can be updated and HIP can send
   * the same careContextReference and if it is available returns list<CareContext> or null
   *
   * @param abhaAddress
   * @param hipId
   * @param careContexts
   * @return
   */
  public List<CareContext> getSameCareContexts(
      String abhaAddress, String hipId, List<CareContext> careContexts) {

    Patient patient = patientRepo.findByAbhaAddress(abhaAddress, hipId);
    if (patient == null) {
      return Collections.emptyList();
    }

    List<CareContext> existingCareContexts = patient.getCareContexts();
    if (existingCareContexts == null) return Collections.emptyList();

    boolean allNew =
        careContexts.stream()
            .allMatch(
                context ->
                    existingCareContexts.stream()
                        .noneMatch(
                            existingContext ->
                                context
                                        .getReferenceNumber()
                                        .equals(existingContext.getReferenceNumber())
                                    && context.getHiType().equals(existingContext.getHiType())
                                    && existingContext.isLinked()));

    if (allNew) {
      return Collections.emptyList();
    }

    return existingCareContexts.stream()
        .filter(
            existingContext ->
                careContexts.stream()
                    .anyMatch(
                        context ->
                            context
                                    .getReferenceNumber()
                                    .equals(existingContext.getReferenceNumber())
                                && context.getHiType().equals(existingContext.getHiType())
                                && existingContext.isLinked()))
        .collect(Collectors.toList());
  }

  /**
   * For HIP or HIU wrapper needs to verify the consent while sending or requesting of FHIR bundles
   *
   * @param abhaAddress
   * @param consentId
   * @param hipId
   * @return
   * @throws IllegalDataStateException
   */
  public boolean isConsentValid(String abhaAddress, String consentId, String hipId)
      throws IllegalDataStateException {
    Consent consent = getConsentDetails(abhaAddress, consentId, hipId);
    if (Objects.isNull(consent)) return false;
    // Checking the expiry of the consent.
    return !Utils.checkExpiry(consent.getConsentDetail().getPermission().getDataEraseAt());
  }

  public Patient getPatient(String abhaAddress, String hipId) {
    log.debug("Patient not found in database, sending request to HIP.");
    HIPPatient hipPatient = hipv3Client.getPatient(abhaAddress, hipId);
    if (Objects.nonNull(hipPatient)) {
      if (Objects.isNull(hipPatient.getError())) {
        Patient patient = new Patient();
        patient.setAbhaAddress(hipPatient.getAbhaAddress());
        patient.setGender(hipPatient.getGender());
        patient.setName(hipPatient.getName());
        patient.setDateOfBirth(hipPatient.getDateOfBirth());
        patient.setPatientDisplay(hipPatient.getPatientDisplay());
        patient.setPatientReference(hipPatient.getPatientReference());
        patient.setPatientMobile(hipPatient.getPatientMobile());
        patient.setHipId(hipId);
        patientRepo.save(patient);
        return patient;
      }
    }
    return null;
  }
}
