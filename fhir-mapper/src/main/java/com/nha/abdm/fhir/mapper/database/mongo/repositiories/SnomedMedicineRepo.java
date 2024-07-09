/* (C) 2024 */
package com.nha.abdm.fhir.mapper.database.mongo.repositiories;

import com.nha.abdm.fhir.mapper.database.mongo.tables.SnomedMedicine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedMedicineRepo extends MongoRepository<SnomedMedicine, String> {
  SnomedMedicine findByDisplay(String display);
}
