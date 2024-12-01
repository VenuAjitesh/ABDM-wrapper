/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.requests;

import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.helpers.PatientWithCareContexts;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Data
@Component
@NonNull public class LinkRecordsRequest implements Serializable {
  private static final long serialVersionUID = 165269402517398406L;

  public String requestId;
  private String requesterId;
  private String abhaAddress;
  private String authMode;
  private List<String> hiTypes;
  private PatientWithCareContexts patient;
}
