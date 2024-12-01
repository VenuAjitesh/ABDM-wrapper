/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitConsentRequest {
  private String requestId;
  private String timestamp;

  @NotNull(message = "consent is mandatory") @Valid
  private ConsentRequest consent;
}
