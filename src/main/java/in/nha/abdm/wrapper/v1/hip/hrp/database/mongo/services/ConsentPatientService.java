/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services;

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
public class ConsentPatientService {

  private final MongoTemplate mongoTemplate;

  @Autowired
  public ConsentPatientService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public void saveConsentPatientMapping(
      String consentId, String patientAbhaAddress, String entityType) {
    MongoCollection<Document> collection = mongoTemplate.getCollection("consent-patient");
    UpdateOptions updateOptions = new UpdateOptions().upsert(true);
    collection.updateOne(
        Filters.and(
            Filters.eq(FieldIdentifiers.CONSENT_ID, consentId),
            Filters.eq(FieldIdentifiers.ENTITY_TYPE, entityType)),
        Updates.combine(
            Updates.set(FieldIdentifiers.ABHA_ADDRESS, patientAbhaAddress),
            Updates.set(FieldIdentifiers.ENTITY_TYPE, entityType)),
        updateOptions);
  }

  public ConsentPatient findMappingByConsentId(String consentId, String entityType) {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.CONSENT_ID)
                .is(consentId)
                .and(FieldIdentifiers.ENTITY_TYPE)
                .is(entityType));
    return mongoTemplate.findOne(query, ConsentPatient.class);
  }
}
