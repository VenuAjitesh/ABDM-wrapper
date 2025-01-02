/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentCipherMapping;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ConsentCipherMappingService {
  private final MongoTemplate mongoTemplate;

  @Autowired
  public ConsentCipherMappingService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public void saveConsentPrivateKeyMapping(String consentId, String privateKey, String nonce) {
    MongoCollection<Document> collection =
        mongoTemplate.getCollection("consent-cipher-key-mappings");
    UpdateOptions updateOptions = new UpdateOptions().upsert(true);
    collection.updateOne(
        Filters.eq(FieldIdentifiers.CONSENT_ID, consentId),
        Updates.combine(
            Updates.set(FieldIdentifiers.PRIVATE_KEY, privateKey),
            Updates.set(FieldIdentifiers.NONCE, nonce)),
        updateOptions);
  }

  public void updateTransactionId(String consentId, String transactionId) {
    MongoCollection<Document> collection =
        mongoTemplate.getCollection("consent-cipher-key-mappings");
    UpdateOptions updateOptions = new UpdateOptions().upsert(true);
    collection.updateOne(
        Filters.eq(FieldIdentifiers.CONSENT_ID, consentId),
        Updates.combine(Updates.set(FieldIdentifiers.TRANSACTION_ID, transactionId)),
        updateOptions);
  }

  public ConsentCipherMapping getConsentCipherMapping(String transactionId) {
    Query query = new Query(Criteria.where(FieldIdentifiers.TRANSACTION_ID).is(transactionId));
    return mongoTemplate.findOne(query, ConsentCipherMapping.class);
  }
}
