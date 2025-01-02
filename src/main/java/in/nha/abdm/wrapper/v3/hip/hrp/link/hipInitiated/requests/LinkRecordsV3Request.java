/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.link.hipInitiated.requests;

import in.nha.abdm.wrapper.v1.common.models.CareContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class LinkRecordsV3Request {
  @NotNull(message = "requestId is mandatory") public String requestId;

  @NotNull(message = "requesterId is mandatory i.e HipID") private String requesterId;

  @NotNull(message = "abhaAddress is mandatory") private String abhaAddress;

  @NotEmpty(message = "careContexts are mandatory")
  @Valid
  private List<CareContext> careContexts;
}
