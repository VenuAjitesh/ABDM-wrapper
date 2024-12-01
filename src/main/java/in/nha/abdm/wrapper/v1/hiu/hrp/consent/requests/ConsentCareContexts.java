/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentCareContexts {
  private String patientReference;
  private String careContextReference;
}
