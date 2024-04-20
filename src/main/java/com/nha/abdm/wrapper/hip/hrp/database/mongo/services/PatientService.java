/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.database.mongo.services;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import com.nha.abdm.wrapper.common.exceptions.IllegalDataStateException;
import com.nha.abdm.wrapper.common.models.CareContext;
import com.nha.abdm.wrapper.common.models.Consent;
import com.nha.abdm.wrapper.common.responses.FacadeResponse;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.repositories.PatientRepo;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.Patient;
import com.nha.abdm.wrapper.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import com.nha.abdm.wrapper.hip.hrp.link.hipInitiated.requests.LinkRecordsRequest;
import com.nha.abdm.wrapper.hiu.hrp.consent.requests.ConsentCareContexts;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.util.CollectionUtils;

@Service
public class PatientService {
  private static final Logger log = LogManager.getLogger(PatientService.class);
  @Autowired private final PatientRepo patientRepo;

  @Autowired MongoTemplate mongoTemplate;

  @Autowired
  public PatientService(PatientRepo patientRepo) {
    this.patientRepo = patientRepo;
  }

  /**
   * Fetch of patientReference using abhaAddress
   *
   * @param abhaAddress abhaAddress of patient.
   * @return patientReference.
   */
  public String getPatientReference(String abhaAddress) {
    Patient existingRecord = this.patientRepo.findByAbhaAddress(abhaAddress);
    return existingRecord != null ? existingRecord.getPatientReference() : "";
  }
  /**
   * Fetch of patientDisplay using abhaAddress
   *
   * @param abhaAddress abhaAddress of patient.
   * @return patientDisplay.
   */
  public String getPatientDisplay(String abhaAddress) {
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress);
    return patient != null ? patient.getPatientDisplay() : "";
  }
  /**
   * Fetch of abhaAddress using abhaAddress
   *
   * @param patientReference patientReference of patient.
   * @return abhaAddress.
   */
  public String getAbhaAddress(String patientReference) {
    Patient existingRecord = this.patientRepo.findByPatientReference(patientReference);
    return existingRecord != null ? existingRecord.getAbhaAddress() : "";
  }

  /**
   * After successful linking of careContext updating the status i.e. isLinked to true or false.
   *
   * @param abhaAddress abhaAddress of patient.
   * @param careContexts List of careContext to update the status.
   */
  public void updateCareContextStatus(String abhaAddress, List<CareContext> careContexts) {
    Query query = new Query(Criteria.where(FieldIdentifiers.ABHA_ADDRESS).is(abhaAddress));
    Update update = new Update().addToSet(FieldIdentifiers.CARE_CONTEXTS).each(careContexts);
    log.info(
        "updateCareContextStatus: abhaAddress: " + abhaAddress + " careContexts: " + careContexts);
    this.mongoTemplate.updateFirst(query, update, Patient.class);
  }

  /**
   * <B>hipInitiatedLinking</B>
   *
   * <p>After successful link of careContexts with abhaAddress storing them into patient.
   *
   * @param linkRecordsRequest Response to facade as /link-records for hipInitiatedLinking.
   */
  public void addPatientCareContexts(LinkRecordsRequest linkRecordsRequest) {
    String abhaAddress = linkRecordsRequest.getAbhaAddress();
    try {
      Patient existingRecord = this.patientRepo.findByAbhaAddress(abhaAddress);
      if (existingRecord == null) {
        log.error("Adding patient failed -> Patient not found");
      } else {
        List<CareContext> modifiedCareContexts =
            linkRecordsRequest.getPatient().getCareContexts().stream()
                .map(
                    careContextRequest -> {
                      CareContext modifiedContext = new CareContext();
                      modifiedContext.setReferenceNumber(careContextRequest.getReferenceNumber());
                      modifiedContext.setDisplay(careContextRequest.getDisplay());
                      return modifiedContext;
                    })
                .collect(Collectors.toList());
        Query query =
            new Query(
                Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                    .is(linkRecordsRequest.getAbhaAddress()));
        Update update =
            new Update().addToSet(FieldIdentifiers.CARE_CONTEXTS).each(modifiedCareContexts);
        this.mongoTemplate.updateFirst(query, update, Patient.class);
      }
    } catch (Exception e) {
      log.info("addPatient :" + e);
    }
    log.info("Successfully Added Patient careContexts");
  }

  public void addConsent(String abhaAddress, Consent consent) throws IllegalDataStateException {
    log.info("Consent : " + consent.toString());
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress);
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
    Query query = new Query(Criteria.where(FieldIdentifiers.ABHA_ADDRESS).is(abhaAddress));
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
  public Consent getConsentDetails(String abhaAddress, String consentId)
      throws IllegalDataStateException {
    Patient patient = patientRepo.findByAbhaAddress(abhaAddress);
    if (patient == null) {
      throw new IllegalDataStateException("Patient not found in database: " + abhaAddress);
    }
    List<Consent> consents = patient.getConsents();
    if (!CollectionUtils.isEmpty(consents)) {
      for (Consent storedConsent : consents) {
        if (storedConsent.getConsentDetail().getConsentId().equals(consentId)) {
          return storedConsent;
        }
      }
    }
    return null;
  }

  /**
   * Adds or Updates patient demographic data.
   *
   * @param patients List of patients with reference and demographic details.
   * @return status of adding or modifying patients in database.
   */
  public FacadeResponse upsertPatients(List<Patient> patients) {
    MongoCollection<Document> collection = mongoTemplate.getCollection("patients");
    List<WriteModel<Document>> updates = new ArrayList<>();
    for (Patient patient : patients) {
      Document document =
          new Document()
              .append(FieldIdentifiers.ABHA_ADDRESS, patient.getAbhaAddress())
              .append(FieldIdentifiers.NAME, patient.getName())
              .append(FieldIdentifiers.GENDER, patient.getGender())
              .append(FieldIdentifiers.DATE_OF_BIRTH, patient.getDateOfBirth())
              .append(FieldIdentifiers.PATIENT_REFERENCE, patient.getPatientReference())
              .append(FieldIdentifiers.PATIENT_DISPLAY, patient.getPatientDisplay())
              .append(FieldIdentifiers.PATIENT_MOBILE, patient.getPatientMobile());
      updates.add(
          new UpdateOneModel<>(
              new Document(FieldIdentifiers.ABHA_ADDRESS, patient.getAbhaAddress()),
              new Document("$set", document),
              new UpdateOptions().upsert(true)));
    }

    BulkWriteResult bulkWriteResult = collection.bulkWrite(updates);
    int updatedPatientCount =
        bulkWriteResult.getUpserts().size() > 0
            ? bulkWriteResult.getUpserts().size()
            : bulkWriteResult.getModifiedCount();

    return FacadeResponse.builder()
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
  public boolean isCareContextPresent(List<ConsentCareContexts> careContextsWithPatientReference) {
    if (careContextsWithPatientReference == null) return false;
    for (ConsentCareContexts careContexts : careContextsWithPatientReference) {
      Patient patient = patientRepo.findByPatientReference(careContexts.getPatientReference());
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

  public Patient getPatientDetails(String abhaAddress) {
    return patientRepo.findByAbhaAddress(abhaAddress);
  }

  public void updatePatientConsent(
      String abhaAddress, String consentId, String consentStatus, String lastUpdated) {
    MongoCollection<Document> collection = mongoTemplate.getCollection("patients");
    Bson filter =
        Filters.and(
            Filters.eq(FieldIdentifiers.ABHA_ADDRESS, abhaAddress),
            Filters.eq("consents.consentDetail.consentId", consentId));
    Bson update =
        Updates.combine(
            Updates.set("consents.$.status", consentStatus),
            Updates.set("consents.$.lastUpdatedOn", lastUpdated));
    UpdateResult result = collection.updateOne(filter, update);
    log.debug("consent update result: ", result);
  }
}
