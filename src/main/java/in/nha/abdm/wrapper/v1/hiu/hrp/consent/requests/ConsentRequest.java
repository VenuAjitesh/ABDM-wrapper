/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequest {
  @NotNull(message = "purpose is mandatory") @Valid
  private Purpose purpose;

  @NotNull(message = "patient is mandatory") @Valid
  private ConsentPatient patient;

  private ConsentHIP hip;
  private List<ConsentCareContexts> careContexts;

  @NotNull(message = "hiu is mandatory") @Valid
  private ConsentHIU hiu;

  @NotNull(message = "requester is mandatory") @Valid
  private ConsentRequester requester;

  @NotEmpty(message = "hiTypes is mandatory")
  private List<String> hiTypes;

  @NotNull(message = "permission is mandatory") @Valid
  private Permission permission;
}
