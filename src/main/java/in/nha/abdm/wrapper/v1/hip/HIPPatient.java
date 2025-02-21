/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip;

import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class HIPPatient {
  @NotNull(message = "abhaAddress is mandatory") private String abhaAddress;

  @NotNull(message = "name is mandatory") private String name;

  @NotNull(message = "gender is mandatory") private String gender;

  @NotNull(message = "dateOfBirth is mandatory") private String dateOfBirth;

  @NotNull(message = "patientReference is mandatory") private String patientReference;

  @NotNull(message = "patientDisplay is mandatory") private String patientDisplay;

  private String patientMobile;
  private List<CareContext> careContexts;
  private ErrorResponse error;
  private String hipId;
}
