/* (C) 2024 */
package com.nha.abdm.fhir.mapper.database.mongo.repositiories;

import com.nha.abdm.fhir.mapper.database.mongo.tables.SnomedSpecimen;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedSpecimenRepo extends MongoRepository<SnomedSpecimen, String> {
  SnomedSpecimen findByDisplay(String display);
}
