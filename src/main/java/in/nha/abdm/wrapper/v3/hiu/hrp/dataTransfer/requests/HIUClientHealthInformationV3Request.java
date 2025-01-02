/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class HIUClientHealthInformationV3Request {
  @NotNull(message = "requestId is mandatory") private String requestId;

  @NotNull(message = "consentId is mandatory") private String consentId;

  @NotNull(message = "requesterId (hiuId) is mandatory")
  private String requesterId;
}
