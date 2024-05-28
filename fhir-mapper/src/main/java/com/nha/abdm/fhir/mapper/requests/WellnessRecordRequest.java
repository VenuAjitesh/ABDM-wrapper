/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests;

import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.requests.helpers.ObservationResource;
import com.nha.abdm.fhir.mapper.requests.helpers.WellnessObservationResource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class WellnessRecordRequest {
  @NotNull(message = "BundleType is mandatory and must not be empty, ex: prescription") private String bundleType;

  @NotNull(message = "careContextReference is mandatory and must not be empty") private String careContextReference;

  @Valid
  @NotNull(message = "Patient demographic details are mandatory and must not be empty") private PatientResource patient;

  private String encounter;

  @NotNull(message = "No Practitioner found") private List<PractitionerResource> practitioner;

  @Valid
  @NotNull(message = "organisation is mandatory") private OrganisationResource organisation;

  @Valid private List<WellnessObservationResource> vitalSigns;
  @Valid private List<WellnessObservationResource> bodyMeasurement;
  @Valid private List<WellnessObservationResource> physicalActivity;
  @Valid private List<WellnessObservationResource> generalAssessment;
  @Valid private List<WellnessObservationResource> womanHealth;
  @Valid private List<WellnessObservationResource> lifeStyle;
  @Valid private List<ObservationResource> otherObservations;
  private List<DocumentResource> documentList;
}
