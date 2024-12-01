/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestOtp {
  private String abhaAddress;
  private String patientReference;
  private String hipId;
}
