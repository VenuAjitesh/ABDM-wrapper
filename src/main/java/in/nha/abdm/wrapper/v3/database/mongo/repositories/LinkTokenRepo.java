/* (C) 2024 */
package in.nha.abdm.wrapper.v3.database.mongo.repositories;

import in.nha.abdm.wrapper.v3.database.mongo.tables.LinkToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkTokenRepo extends MongoRepository<LinkToken, String> {
  /**
   * Fetching the LinkToken using Abha Address and facility
   *
   * @param abhaAddress
   * @param hipId
   * @return
   */
  LinkToken findByAbhaAddress(String abhaAddress, String hipId);
}
