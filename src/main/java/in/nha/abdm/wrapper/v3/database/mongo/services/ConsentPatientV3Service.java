/* (C) 2024 */
package in.nha.abdm.wrapper.v3.database.mongo.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ConsentPatientV3Service {

  private final MongoTemplate mongoTemplate;

  @Autowired
  public ConsentPatientV3Service(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  /**
   * Saving the consentDetails with respective of hipId and AbhaAddress
   *
   * @param consentId of the consentRequest crucial for data transfer
   * @param entityType HIP/HIU
   * @param hipId facilityId
   */
  public void saveConsentPatientMapping(
      String consentId, String patientAbhaAddress, String entityType, String hipId) {
    MongoCollection<Document> collection =
        mongoTemplate.getCollection(FieldIdentifiers.TABLE_CONSENT_PATIENT);
    UpdateOptions updateOptions = new UpdateOptions().upsert(true);
    collection.updateOne(
        Filters.and(
            Filters.eq(FieldIdentifiers.CONSENT_ID, consentId),
            Filters.eq(FieldIdentifiers.ENTITY_TYPE, entityType),
            Filters.eq(FieldIdentifiers.HIP_ID, hipId)),
        Updates.combine(
            Updates.set(FieldIdentifiers.ABHA_ADDRESS, patientAbhaAddress),
            Updates.set(FieldIdentifiers.ENTITY_TYPE, entityType),
            Updates.set(FieldIdentifiers.HIP_ID, hipId)),
        updateOptions);
  }

  /**
   * Retrieving of consent details.
   *
   * @param entityType HIP/HIU
   * @param hipId facilityId
   * @return Consent Details
   */
  public ConsentPatient findMappingByConsentId(String consentId, String entityType, String hipId) {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.CONSENT_ID)
                .is(consentId)
                .and(FieldIdentifiers.ENTITY_TYPE)
                .is(entityType)
                .and(FieldIdentifiers.HIP_ID)
                .is(hipId));
    return mongoTemplate.findOne(query, ConsentPatient.class);
  }
}
