/* (C) 2024 */
package com.nha.abdm.fhir.mapper.database.mongo.repositiories;

import com.nha.abdm.fhir.mapper.database.mongo.tables.SnomedVaccine;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedVaccineRepo extends MongoRepository<SnomedVaccine, String> {
  SnomedVaccine findByDisplay(String display);
}
