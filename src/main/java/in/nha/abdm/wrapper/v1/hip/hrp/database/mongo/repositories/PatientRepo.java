/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.repositories;

import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.Patient;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepo extends MongoRepository<Patient, String> {
  Patient findByAbhaAddress(String abhaAddress);

  Patient findByPatientReference(String patientReference);

  List<Patient> findByPatientMobile(String patientMobile);

  @Query("{ 'abhaAddress': ?0, 'hipId': ?1 }")
  Patient findByAbhaAddress(String abhaAddress, String hipId);

  @Query("{ 'patientReference': ?0, 'hipId': ?1 }")
  Patient findByPatientReference(String patientReference, String hipId);

  @Query("{ 'patientMobile': ?0, 'hipId': ?1 }")
  List<Patient> findByPatientMobile(String patientMobile, String hipId);
}
