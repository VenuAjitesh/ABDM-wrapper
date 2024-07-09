/* (C) 2024 */
package com.nha.abdm.fhir.mapper.database.mongo.repositiories;

import com.nha.abdm.fhir.mapper.database.mongo.tables.SnomedMedicineRoute;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedMedicineRouteRepo extends MongoRepository<SnomedMedicineRoute, String> {
  SnomedMedicineRoute findByDisplay(String display);
}
