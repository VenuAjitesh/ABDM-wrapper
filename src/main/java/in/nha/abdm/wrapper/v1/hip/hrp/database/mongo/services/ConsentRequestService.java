/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ConsentRequestService {

  private final MongoTemplate mongoTemplate;

  @Autowired
  public ConsentRequestService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public void saveConsentRequest(String consentRequestId, String gatewayRequestId) {
    MongoCollection<Document> collection = mongoTemplate.getCollection("consent-requests");
    UpdateOptions updateOptions = new UpdateOptions().upsert(true);
    collection.updateOne(
        Filters.eq(FieldIdentifiers.CONSENT_REQUEST_ID, consentRequestId),
        Updates.combine(Updates.set(FieldIdentifiers.GATEWAY_REQUEST_ID, gatewayRequestId)),
        updateOptions);
  }

  public String getGatewayRequestId(String consentRequestId) throws IllegalDataStateException {
    Query query =
        new Query(Criteria.where(FieldIdentifiers.CONSENT_REQUEST_ID).is(consentRequestId));
    ConsentRequest consentRequest = mongoTemplate.findOne(query, ConsentRequest.class);
    if (consentRequest == null) {
      throw new IllegalDataStateException(
          "No request found for consent request id: " + consentRequestId);
    }
    return consentRequest.getGatewayRequestId();
  }
}
