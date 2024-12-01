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
public class ConsentRequester {
  @NotNull(message = "name in requester is mandatory") private String name;

  @NotNull(message = "identifier in requester is mandatory") @Valid
  private ConsentRequesterIdentifier identifier;
}
