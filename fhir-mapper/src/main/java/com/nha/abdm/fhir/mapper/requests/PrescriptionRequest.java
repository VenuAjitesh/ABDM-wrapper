/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests;

import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.requests.helpers.PrescriptionResource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PrescriptionRequest {
  @NotNull(message = "BundleType is mandatory and must not be empty, ex: prescription") private String bundleType;

  @NotNull(message = "careContextReference is mandatory and must not be empty") private String careContextReference;

  @Valid
  @NotNull(message = "Patient demographic details are mandatory and must not be empty") private PatientResource patient;

  private Date authoredOn;
  private String encounter;

  @Valid
  @NotEmpty(message = "practitionerList is mandatory and must not be empty")
  private List<PractitionerResource> practitionerList;

  private OrganisationResource organisation;

  @Valid
  @NotNull(message = "prescription is mandatory and must not be empty") private List<PrescriptionResource> prescription;

  private List<DocumentResource> documentList;
}
