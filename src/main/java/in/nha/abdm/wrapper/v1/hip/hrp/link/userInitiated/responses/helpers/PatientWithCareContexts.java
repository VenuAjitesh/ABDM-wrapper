/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.helpers;

import in.nha.abdm.wrapper.v1.common.models.CareContext;
import java.util.List;
import lombok.Data;

@Data
public class PatientWithCareContexts {

  private String id;
  private String referenceNumber;
  private List<CareContext> careContexts;
}
