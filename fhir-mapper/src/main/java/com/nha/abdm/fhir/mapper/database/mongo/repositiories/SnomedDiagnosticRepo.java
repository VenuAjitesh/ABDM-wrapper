/* (C) 2024 */
package com.nha.abdm.fhir.mapper.database.mongo.repositiories;

import com.nha.abdm.fhir.mapper.database.mongo.tables.SnomedDiagnostic;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedDiagnosticRepo extends MongoRepository<SnomedDiagnostic, String> {
  SnomedDiagnostic findByDisplay(String display);
}
