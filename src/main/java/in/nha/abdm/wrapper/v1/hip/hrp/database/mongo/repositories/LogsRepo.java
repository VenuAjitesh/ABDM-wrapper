/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories;

import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.RequestLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LogsRepo extends MongoRepository<RequestLog, String> {
  RequestLog findByClientRequestId(String clientRequestId);

  @Query("{linkRefNumber :?0}")
  RequestLog findByLinkRefNumber(String linkRefNumber);

  RequestLog findByGatewayRequestId(String clientRequestId);

  RequestLog findByConsentId(String consentId);

  RequestLog findByLinkTokenRequestId(String linkTokenRequestId);
}
