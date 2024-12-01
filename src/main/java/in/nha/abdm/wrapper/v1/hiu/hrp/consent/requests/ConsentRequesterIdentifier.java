/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentRequesterIdentifier {
  @NotNull(message = "type is mandatory") private String type;

  @NotNull(message = "value is mandatory") private String value;

  @NotNull(message = "system is mandatory") private String system;
}
