/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests;

import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.requests.helpers.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class OPConsultationRequest {
  @Pattern(regexp = "OPConsultRecord")
  @NotNull(message = "BundleType is mandatory and must not be empty : 'OPConsultRecord'") private String bundleType;

  @NotNull(message = "careContextReference is mandatory and must not be empty") private String careContextReference;

  @Valid
  @NotNull(message = "Patient demographic details are mandatory and must not be empty") private PatientResource patient;

  private String encounter;

  @Valid private List<PractitionerResource> practitioners;

  @Valid
  @NotNull(message = "organisation is mandatory") private OrganisationResource organisation;

  @Valid private List<ChiefComplaintResource> chiefComplaints;
  @Valid private List<ObservationResource> physicalExaminations;
  private List<String> allergies;
  @Valid private List<ChiefComplaintResource> medicalHistories;
  @Valid private List<FamilyObservationResource> familyHistories;
  @Valid private List<ServiceRequestResource> serviceRequests;

  @Pattern(
      regexp = "((\\d{4}-\\d{2}-\\d{2})|(\\d{4}-\\d{2}-\\d{2}'T'\\d{2}:\\d{2}:\\d{2}.\\d{3}X))",
      message = "Value must match either yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss.SSSX")
  @NotNull(message = "authoredOn is mandatory timestamp") @NotNull private String authoredOn;

  @Valid private List<PrescriptionResource> medications;
  @Valid private List<FollowupResource> followups;
  @Valid private List<ProcedureResource> procedures;
  @Valid private List<ServiceRequestResource> referrals;
  @Valid private List<ObservationResource> otherObservations;
  @Valid private List<DocumentResource> documents;
}
