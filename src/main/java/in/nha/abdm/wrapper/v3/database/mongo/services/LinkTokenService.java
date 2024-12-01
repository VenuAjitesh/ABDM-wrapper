/* (C) 2024 */
package in.nha.abdm.wrapper.v3.database.mongo.services;

import in.nha.abdm.wrapper.v1.common.Utils;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import in.nha.abdm.wrapper.v3.database.mongo.repositories.LinkTokenRepo;
import in.nha.abdm.wrapper.v3.database.mongo.tables.LinkToken;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class LinkTokenService {
  private static final Logger log = LoggerFactory.getLogger(LinkTokenService.class);
  @Autowired LinkTokenRepo linkTokenRepo;
  @Autowired MongoTemplate mongoTemplate;

  /**
   * Checking the expiry of the LinkToken, if expired or not available returns null
   *
   * @param abhaAddress
   * @param entity
   * @return
   */
  public String getLinkToken(String abhaAddress, String entity) {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                .is(abhaAddress)
                .and(FieldIdentifiers.HIP_ID)
                .is(entity));
    LinkToken linkToken = mongoTemplate.findOne(query, LinkToken.class);
    if (linkToken != null
        && linkToken.getLinkToken() != null
        && !Utils.checkExpiry(linkToken.getExpiry())) {
      return linkToken.getLinkToken();
    }
    return null;
  }

  /**
   * Saving of LinkToken with respective of ABHA Address
   *
   * @param abhaAddress
   * @param linkToken
   * @param entity
   */
  public void saveLinkToken(String abhaAddress, String linkToken, String entity) {
    log.info("Saving linkToken of " + abhaAddress + " " + entity);
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                .is(abhaAddress)
                .and(FieldIdentifiers.HIP_ID)
                .is(entity));
    LinkToken existingToken = mongoTemplate.findOne(query, LinkToken.class);
    if (Objects.nonNull(existingToken)) {
      Update update = new Update();
      update.set(FieldIdentifiers.LINK_TOKEN, linkToken);
      update.set(FieldIdentifiers.EXPIRY, Utils.setLinkTokenExpiry());
      mongoTemplate.upsert(query, update, LinkToken.class);
    } else {
      LinkToken newToken = new LinkToken();
      newToken.setAbhaAddress(abhaAddress);
      newToken.setLinkToken(linkToken);
      newToken.setExpiry(Utils.setLinkTokenExpiry());
      newToken.setHipId(entity);
      mongoTemplate.insert(newToken);
    }
  }

  /**
   * Saving the LinkToken while initiation of careContext linking.
   *
   * @param abhaAddress
   * @param entity
   * @param linkTokenRequestId
   */
  public void saveLinkTokenRequestId(String abhaAddress, String entity, String linkTokenRequestId) {
    Query query =
        new Query(
            Criteria.where(FieldIdentifiers.ABHA_ADDRESS)
                .is(abhaAddress)
                .and(FieldIdentifiers.HIP_ID)
                .is(entity));
    LinkToken existingToken = mongoTemplate.findOne(query, LinkToken.class);

    if (Objects.nonNull(existingToken)) {
      Update update = new Update();
      update.set(FieldIdentifiers.LINK_TOKEN_REQUEST_ID, linkTokenRequestId);
      mongoTemplate.upsert(query, update, LinkToken.class);
    } else {
      LinkToken newToken = new LinkToken();
      newToken.setAbhaAddress(abhaAddress);
      newToken.setHipId(entity);
      newToken.setLinkTokenRequestId(linkTokenRequestId);
      mongoTemplate.save(newToken);
    }
  }
}
