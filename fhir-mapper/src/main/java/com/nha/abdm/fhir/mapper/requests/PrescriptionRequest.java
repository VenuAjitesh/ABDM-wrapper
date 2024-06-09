/* (C) 2024 */
package com.nha.abdm.fhir.mapper.requests;

import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import com.nha.abdm.fhir.mapper.common.helpers.OrganisationResource;
import com.nha.abdm.fhir.mapper.common.helpers.PatientResource;
import com.nha.abdm.fhir.mapper.common.helpers.PractitionerResource;
import com.nha.abdm.fhir.mapper.requests.helpers.PrescriptionResource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class PrescriptionRequest {
  @Pattern(regexp = "PrescriptionRecord")
  @NotBlank(message = "BundleType is mandatory and must not be empty : 'PrescriptionRecord'")
  private String bundleType;

  @NotBlank(message = "careContextReference is mandatory and must not be empty")
  private String careContextReference;

  @Valid
  @NotNull(message = "Patient demographic details are mandatory and must not be empty") private PatientResource patient;

  @Pattern(
      regexp = "((\\d{4}-\\d{2}-\\d{2})|(\\d{4}-\\d{2}-\\d{2}'T'\\d{2}:\\d{2}:\\d{2}.\\d{3}X))",
      message = "Value must match either yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss.SSSX")
  @NotBlank(message = "authoredOn is mandatory timestamp")
  private String authoredOn;

  private String encounter;

  @Valid
  @NotNull(message = "practitioners are mandatory and must not be empty")private List<PractitionerResource> practitioners;

  private OrganisationResource organisation;

  @Valid
  @NotNull(message = "prescription is mandatory and must not be empty") private List<PrescriptionResource> prescriptions;

  @Valid private List<DocumentResource> documents;
}
