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
public class Permission {
  @NotNull(message = "accessMode in permission is mandatory") private String accessMode;

  @NotNull(message = "dateRange in permission is mandatory") @Valid
  private DateRange dateRange;

  @NotNull(message = "dataEraseAt in permission is mandatory") private String dataEraseAt;

  @NotNull(message = "frequency in permission is mandatory") @Valid
  private Frequency frequency;
}
