/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.services;

import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.ConsentCareContextMapping;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentCareContexts;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ConsentCareContextsService {
  private final MongoTemplate mongoTemplate;

  @Autowired
  public ConsentCareContextsService(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public void saveConsentContextsMapping(String consentId, List<ConsentCareContexts> careContexts) {
    Query query = Query.query(Criteria.where(FieldIdentifiers.CONSENT_ID).is(consentId));
    Update update = new Update().set(FieldIdentifiers.CARE_CONTEXTS, careContexts);
    mongoTemplate.upsert(query, update, ConsentCareContextMapping.class);
  }

  public ConsentCareContextMapping findMappingByConsentId(String consentId) {
    Query query = new Query(Criteria.where(FieldIdentifiers.CONSENT_ID).is(consentId));
    return mongoTemplate.findOne(query, ConsentCareContextMapping.class);
  }
}
