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
public class Frequency {
  @NotNull(message = "unit is mandatory") private String unit;

  @NotNull(message = "value is mandatory") private int value;

  @NotNull(message = "repeats is mandatory") private int repeats;
}
