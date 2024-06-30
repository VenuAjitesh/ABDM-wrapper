/* (C) 2024 */
package com.nha.abdm.fhir.mapper.database.mongo.services;

import com.nha.abdm.fhir.mapper.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.common.helpers.SnomedResponse;
import com.nha.abdm.fhir.mapper.database.mongo.repositiories.*;
import com.nha.abdm.fhir.mapper.database.mongo.tables.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class SnomedService {
  @Autowired private final SnomedMedicineRepo snomedMedicineRepo;
  @Autowired private final SnomedConditionProcedureRepo snomedConditionProcedureRepo;
  @Autowired private final SnomedEncounterRepo snomedEncounterRepo;
  @Autowired private final SnomedSpecimenRepo snomedSpecimenRepo;
  @Autowired private final SnomedObservationRepo snomedObservationRepo;
  @Autowired private final SnomedVaccineRepo snomedVaccineRepo;
  @Autowired private final SnomedDiagnosticRepo snomedDiagnosticRepo;
  @Autowired private final SnomedMedicineRouteRepo snomedMedicineRouteRepo;

  @Autowired MongoTemplate mongoTemplate;

  public SnomedService(
      SnomedMedicineRepo snomedMedicineRepo,
      SnomedConditionProcedureRepo snomedConditionProcedureRepo,
      SnomedEncounterRepo snomedEncounterRepo,
      SnomedSpecimenRepo snomedSpecimenRepo,
      SnomedObservationRepo snomedObservationRepo,
      SnomedVaccineRepo snomedVaccineRepo,
      SnomedDiagnosticRepo snomedDiagnosticRepo,
      SnomedMedicineRouteRepo snomedMedicineRouteRepo) {
    this.snomedMedicineRepo = snomedMedicineRepo;
    this.snomedConditionProcedureRepo = snomedConditionProcedureRepo;
    this.snomedEncounterRepo = snomedEncounterRepo;
    this.snomedSpecimenRepo = snomedSpecimenRepo;
    this.snomedObservationRepo = snomedObservationRepo;
    this.snomedVaccineRepo = snomedVaccineRepo;
    this.snomedDiagnosticRepo = snomedDiagnosticRepo;
    this.snomedMedicineRouteRepo = snomedMedicineRouteRepo;
  }

  public String getConditionProcedureCode(String display) {
    SnomedConditionProcedure snomedCode = snomedConditionProcedureRepo.findByDisplay(display);
    return snomedCode != null ? snomedCode.getCode() : SnomedCodeIdentifier.SNOMED_UNKNOWN;
  }

  public List<SnomedConditionProcedure> getAllConditionProcedureCode() {
    return snomedConditionProcedureRepo.findAll();
  }

  public String getSnomedDiagnosticCode(String display) {
    SnomedDiagnostic snomedCode = snomedDiagnosticRepo.findByDisplay(display);
    return snomedCode != null ? snomedCode.getCode() : SnomedCodeIdentifier.SNOMED_UNKNOWN;
  }

  public List<SnomedDiagnostic> getAllSnomedDiagnosticCode() {
    return snomedDiagnosticRepo.findAll();
  }

  public String getSnomedEncounterCode(String display) {
    SnomedEncounter snomedCode = snomedEncounterRepo.findByDisplay(display);
    return snomedCode != null
        ? snomedCode.getCode()
        : SnomedCodeIdentifier.SNOMED_ENCOUNTER_AMBULATORY;
  }

  public List<SnomedEncounter> getAllSnomedEncounterCode() {
    return snomedEncounterRepo.findAll();
  }

  public String getSnomedMedicineCode(String display) {
    SnomedMedicine snomedCode = snomedMedicineRepo.findByDisplay(display);
    return snomedCode != null ? snomedCode.getCode() : SnomedCodeIdentifier.SNOMED_UNKNOWN;
  }

  public List<SnomedMedicine> getAllSnomedMedicineCode() {
    return snomedMedicineRepo.findAll();
  }

  public String getSnomedObservationCode(String display) {
    SnomedObservation snomedCode = snomedObservationRepo.findByDisplay(display);
    return snomedCode != null ? snomedCode.getCode() : SnomedCodeIdentifier.SNOMED_UNKNOWN;
  }

  public List<SnomedObservation> getAllSnomedObservationCode() {
    return snomedObservationRepo.findAll();
  }

  public String getSnomedSpecimenCode(String display) {
    SnomedSpecimen snomedCode = snomedSpecimenRepo.findByDisplay(display);
    return snomedCode != null ? snomedCode.getCode() : SnomedCodeIdentifier.SNOMED_UNKNOWN;
  }

  public List<SnomedSpecimen> getAllSnomedSpecimenCode() {
    return snomedSpecimenRepo.findAll();
  }

  public String getSnomedVaccineCode(String display) {
    SnomedVaccine snomedCode = snomedVaccineRepo.findByDisplay(display);
    return snomedCode != null ? snomedCode.getCode() : SnomedCodeIdentifier.SNOMED_UNKNOWN;
  }

  public List<SnomedVaccine> getAllSnomedVaccineCode() {
    return snomedVaccineRepo.findAll();
  }

  public String getSnomedMedicineRouteCode(String display) {
    SnomedMedicineRoute snomedCode = snomedMedicineRouteRepo.findByDisplay(display);
    return snomedCode != null ? snomedCode.getCode() : SnomedCodeIdentifier.SNOMED_UNKNOWN;
  }

  public List<SnomedMedicineRoute> getAllSnomedMedicineRouteCode() {
    return snomedMedicineRouteRepo.findAll();
  }

  public SnomedResponse getSnomedCodes(String resource) {
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_CONDITION)
        || resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_PROCEDURE)) {
      return SnomedResponse.builder()
          .snomedConditionProcedureCodes(getAllConditionProcedureCode())
          .build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_DIAGNOSTICS)) {
      return SnomedResponse.builder().snomedDiagnosticCodes(getAllSnomedDiagnosticCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_ENCOUNTER)) {
      return SnomedResponse.builder().snomedEncounterCodes(getAllSnomedEncounterCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_MEDICATION_ROUTE)) {
      return SnomedResponse.builder()
          .snomedMedicineRouteCodes(getAllSnomedMedicineRouteCode())
          .build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_MEDICATIONS)) {
      return SnomedResponse.builder().snomedMedicineCodes(getAllSnomedMedicineCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_OBSERVATIONS)) {
      return SnomedResponse.builder().snomedObservationCodes(getAllSnomedObservationCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_SPECIMEN)) {
      return SnomedResponse.builder().snomedSpecimenCodes(getAllSnomedSpecimenCode()).build();
    }
    if (resource.equalsIgnoreCase(SnomedCodeIdentifier.SNOMED_VACCINES)) {
      return SnomedResponse.builder().snomedVaccineCodes(getAllSnomedVaccineCode()).build();
    }
    return null;
  }
}
