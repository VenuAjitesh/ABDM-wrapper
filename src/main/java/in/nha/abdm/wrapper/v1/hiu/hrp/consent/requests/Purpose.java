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
public class Purpose {
  @NotNull(message = "text is mandatory") private String text;

  @NotNull(message = "code is mandatory") private String code;

  @NotNull(message = "refUri is mandatory") private String refUri;
}
