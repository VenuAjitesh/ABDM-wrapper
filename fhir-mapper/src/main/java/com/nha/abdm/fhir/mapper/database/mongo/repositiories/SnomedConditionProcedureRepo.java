/* (C) 2024 */
package com.nha.abdm.fhir.mapper.database.mongo.repositiories;

import com.nha.abdm.fhir.mapper.database.mongo.tables.SnomedConditionProcedure;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnomedConditionProcedureRepo
    extends MongoRepository<SnomedConditionProcedure, String> {
  SnomedConditionProcedure findByDisplay(String display);
}
