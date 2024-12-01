/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip;

import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import java.util.List;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class HIPPatient {
  private String abhaAddress;
  private String name;
  private String gender;
  private String dateOfBirth;
  private String patientReference;
  private String patientDisplay;
  private String patientMobile;
  private List<CareContext> careContexts;
  private ErrorResponse error;
  private String hipId;
}
