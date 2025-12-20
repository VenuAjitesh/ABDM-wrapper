/* (C) 2024 */
package in.nha.abdm.wrapper.v3.database.mongo.services;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.models.Consent;
import in.nha.abdm.wrapper.v1.common.models.ConsentDetail;
import in.nha.abdm.wrapper.v1.hip.HIPPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories.PatientRepo;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentCareContexts;
import in.nha.abdm.wrapper.v3.common.models.FacadeV3Response;
import in.nha.abdm.wrapper.v3.hip.HIPV3Client;
import in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests.LinkRecordsV3Request;
import java.util.*;
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
      String abhaAddress, String patientReference, List<CareContext> careContexts, String hipId)
      throws IllegalDataStateException {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                .is(abhaAddress)
                .and(FieldIdentifiers.HIP_ID)
                .is(hipId));
    Patient patient = mongoTemplate.findOne(query, Patient.class);
    if (Objects.isNull(patient)) {
      throw new IllegalDataStateException("Patient not found in database: " + abhaAddress);
    }
    if (patient.getCareContexts() == null) {
      Update update = new Update().set(FieldIdentifiers.CARE_CONTEXTS, careContexts);
      this.mongoTemplate.updateFirst(query, update, Patient.class);
      return;
    }
    Update update = new Update().addToSet(FieldIdentifiers.CARE_CONTEXTS).each(careContexts);
    log.info("updateCareContext: abhaAddress: " + abhaAddress + " careContexts: " + careContexts);
    this.mongoTemplate.updateFirst(query, update, Patient.class);
  }

  public void updateCareContextStatus(
      String abhaAddress, List<CareContext> careContexts, String hipId) {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                .is(abhaAddress)
                .and(FieldIdentifiers.HIP_ID)
                .is(hipId));

    Patient patient = this.mongoTemplate.findOne(query, Patient.class);

    if (patient == null) {
      log.error("Patient not found with abhaAddress: {} and facility: {}", abhaAddress, hipId);
      return;
    }

    List<CareContext> existingCareContexts = patient.getCareContexts();

    if (existingCareContexts == null) {
      log.info("No care contexts found for abhaAddress: {}", abhaAddress);
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
        "updateCareContextStatus: abhaAddress: " + abhaAddress + " careContexts: " + careContexts);
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

  public void addConsent(String abhaAddress, Consent consent, String hipId) {
    log.info("{} Consent : {}", abhaAddress, consent.toString());
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress, hipId);
    if (Objects.isNull(patient)) {
      log.warn("Patient not found in database: " + abhaAddress);
      patient = getPatient(abhaAddress, hipId);
    }

    List<Consent> consents = patient.getConsents();
    if (!CollectionUtils.isEmpty(consents)) {
      for (Consent storedConsent : consents) {
        if (storedConsent
            .getConsentDetail()
            .getConsentId()
            .equals(consent.getConsentDetail().getConsentId())) {
          log.warn("Consent {} already exists for patient {}.", consent, abhaAddress);
          return;
        }
      }
    }

    Set<String> existingCareContexts =
        Optional.ofNullable(patient.getCareContexts()).orElse(Collections.emptyList()).stream()
            .map(CareContext::getReferenceNumber)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    List<CareContext> newCareContexts =
        Optional.ofNullable(consent.getConsentDetail())
            .map(ConsentDetail::getCareContexts)
            .orElse(Collections.emptyList())
            .stream()
            .filter(Objects::nonNull)
            .filter(
                careContext ->
                    careContext.getCareContextReference() != null
                        && !existingCareContexts.contains(careContext.getCareContextReference()))
            .map(
                careContext ->
                    CareContext.builder()
                        .referenceNumber(careContext.getCareContextReference())
                        .display(
                            "Added careContext from consent: "
                                + consent.getConsentDetail().getConsentId())
                        .isLinked(true)
                        .hiType("HealthDocumentRecord")
                        .build())
            .collect(Collectors.toList());

    Query updateQuery =
        new Query(
            Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                .is(abhaAddress)
                .and(FieldIdentifiers.HIP_ID)
                .is(hipId)
                .and(FieldIdentifiers.CARE_CONTEXTS + ".referenceNumber")
                .in(
                    consent.getConsentDetail().getCareContexts().stream()
                        .map(ConsentCareContexts::getCareContextReference)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .and(FieldIdentifiers.CARE_CONTEXTS + ".isLinked")
                .is(false));
    Update updateExistingCareContexts =
        new Update()
            .filterArray(
                "elem.referenceNumber",
                new Document(
                    "$in",
                    consent.getConsentDetail().getCareContexts().stream()
                        .map(ConsentCareContexts::getCareContextReference)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())))
            .set(FieldIdentifiers.CARE_CONTEXTS + ".$[elem].isLinked", true);
    mongoTemplate.updateMulti(updateQuery, updateExistingCareContexts, Patient.class);

    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                .is(abhaAddress)
                .and(FieldIdentifiers.HIP_ID)
                .is(hipId));

    Update update = new Update().addToSet(FieldIdentifiers.CONSENTS, consent);

    if (!newCareContexts.isEmpty()) {
      log.info("Adding new careContexts from consent: {}", newCareContexts);
      update.addToSet(FieldIdentifiers.CARE_CONTEXTS).each(newCareContexts);
    }

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
    if (patients == null || patients.isEmpty()) {
      return FacadeV3Response.builder().message("No patients provided for upsertion").build();
    }

    try {
      MongoCollection<Document> collection =
          mongoTemplate.getCollection(FieldIdentifiers.TABLE_PATIENT);
      List<WriteModel<Document>> updates = new ArrayList<>();

      for (Patient patient : patients) {
        try {
          if (patient.getAbhaAddress() == null
              || patient.getPatientReference() == null
              || patient.getHipId() == null) {
            log.warn("Skipping patient with missing required fields: {}", patient);
            continue;
          }

          Document filter =
              new Document()
                  .append(FieldIdentifiers.ABHA_ADDRESS, patient.getAbhaAddress())
                  .append(FieldIdentifiers.PATIENT_REFERENCE, patient.getPatientReference())
                  .append(FieldIdentifiers.HIP_ID, patient.getHipId());

          Document document = new Document();
          if (patient.getName() != null) {
            document.append(FieldIdentifiers.NAME, patient.getName());
          }
          if (patient.getGender() != null) {
            document.append(FieldIdentifiers.GENDER, patient.getGender());
          }
          if (patient.getDateOfBirth() != null) {
            document.append(FieldIdentifiers.DATE_OF_BIRTH, patient.getDateOfBirth());
          }
          if (patient.getPatientReference() != null) {
            document.append(FieldIdentifiers.PATIENT_REFERENCE, patient.getPatientReference());
          }
          if (patient.getPatientDisplay() != null) {
            document.append(FieldIdentifiers.PATIENT_DISPLAY, patient.getPatientDisplay());
          }
          if (patient.getPatientMobile() != null) {
            document.append(FieldIdentifiers.PATIENT_MOBILE, patient.getPatientMobile());
          }

          document.append(FieldIdentifiers.ABHA_ADDRESS, patient.getAbhaAddress());
          document.append(FieldIdentifiers.HIP_ID, patient.getHipId());
          document.append("updatedAt", new Date());
          document.append("isDefault", false);

          Document update = new Document();
          if (!document.isEmpty()) {
            update.append("$set", document);
          }

          update.append("$setOnInsert", new Document("createdAt", new Date()));

          if (patient.getCareContexts() != null && !patient.getCareContexts().isEmpty()) {
            List<Document> careContextDocs =
                patient.getCareContexts().stream()
                    .map(
                        careContext ->
                            new Document()
                                .append("referenceNumber", careContext.getReferenceNumber())
                                .append("display", careContext.getDisplay())
                                .append("isLinked", Boolean.FALSE)
                                .append("hiType", careContext.getHiType()))
                    .collect(Collectors.toList());

            update.append(
                "$addToSet",
                new Document(
                    FieldIdentifiers.CARE_CONTEXTS, new Document("$each", careContextDocs)));
          }

          if (!update.isEmpty()) {
            updates.add(new UpdateOneModel<>(filter, update, new UpdateOptions().upsert(true)));
          }
        } catch (Exception e) {
          log.error("Error processing patient: {}", patient, e);
        }
      }

      if (!updates.isEmpty()) {
        try {
          BulkWriteResult bulkWriteResult = collection.bulkWrite(updates);
          int updatedPatientCount =
              bulkWriteResult.getUpserts().size() > 0
                  ? bulkWriteResult.getUpserts().size()
                  : bulkWriteResult.getModifiedCount();

          setLatestAsDefaultForBatch(patients);

          return FacadeV3Response.builder()
              .message(String.format("Successfully upserted %d patients", updatedPatientCount))
              .build();
        } catch (MongoBulkWriteException e) {
          log.error("Bulk write error occurred", e);
          int successCount =
              e.getWriteResult().getInsertedCount() + e.getWriteResult().getModifiedCount();
          return FacadeV3Response.builder()
              .message(
                  String.format(
                      "Partially completed: %d patients upserted, %d failed",
                      successCount, e.getWriteErrors().size()))
              .build();
        }
      }

      return FacadeV3Response.builder().message("No updates were performed").build();

    } catch (Exception e) {
      log.error("Unexpected error during patient upsert operation", e);
      throw new RuntimeException("Failed to upsert patients: " + e.getMessage(), e);
    }
  }

  /** Sets the latest record as default for each unique patientReference + hipId combination */
  private void setLatestAsDefaultForBatch(List<Patient> patients) {
    try {
      Set<String> processedCombinations = new HashSet<>();

      for (Patient patient : patients) {
        if (patient.getPatientReference() == null || patient.getHipId() == null) {
          continue;
        }

        String key = patient.getPatientReference() + "|" + patient.getHipId();

        if (!processedCombinations.contains(key)) {
          processedCombinations.add(key);
          setLatestAsDefault(patient.getPatientReference(), patient.getHipId());
        }
      }
    } catch (Exception e) {
      log.error("Error setting latest records as default", e);
    }
  }

  /** Sets the most recently updated record as default for a given patientReference and hipId */
  private void setLatestAsDefault(String patientReference, String hipId) {
    try {
      MongoCollection<Document> collection =
          mongoTemplate.getCollection(FieldIdentifiers.TABLE_PATIENT);

      Document latestDoc =
          collection
              .find(
                  new Document()
                      .append(FieldIdentifiers.PATIENT_REFERENCE, patientReference)
                      .append(FieldIdentifiers.HIP_ID, hipId))
              .sort(new Document("updatedAt", -1))
              .limit(1)
              .first();

      if (latestDoc != null) {
        String latestAbhaAddress = latestDoc.getString(FieldIdentifiers.ABHA_ADDRESS);

        List<WriteModel<Document>> updates = new ArrayList<>();

        Document disableFilter =
            new Document()
                .append(FieldIdentifiers.PATIENT_REFERENCE, patientReference)
                .append(FieldIdentifiers.HIP_ID, hipId);

        Document disableUpdate = new Document("$set", new Document("isDefault", false));

        updates.add(new UpdateManyModel<>(disableFilter, disableUpdate));

        Document enableFilter =
            new Document()
                .append(FieldIdentifiers.ABHA_ADDRESS, latestAbhaAddress)
                .append(FieldIdentifiers.PATIENT_REFERENCE, patientReference)
                .append(FieldIdentifiers.HIP_ID, hipId);

        Document enableUpdate = new Document("$set", new Document("isDefault", true));

        updates.add(new UpdateOneModel<>(enableFilter, enableUpdate));

        collection.bulkWrite(updates, new BulkWriteOptions().ordered(true));

        log.debug(
            "Set default for patientReference: {}, hipId: {}, abhaAddress: {}",
            patientReference,
            hipId,
            latestAbhaAddress);
      }
    } catch (Exception e) {
      log.error(
          "Error setting latest as default for patientReference: {}, hipId: {}",
          patientReference,
          hipId,
          e);
    }
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
        patient.setCareContexts(hipPatient.getCareContexts());
        patientRepo.save(patient);
        return patient;
      }
    }
    return null;
  }
}
