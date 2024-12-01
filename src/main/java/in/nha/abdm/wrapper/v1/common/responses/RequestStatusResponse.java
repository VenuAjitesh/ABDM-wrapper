/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusResponse {
  private String requestId;
  private String status;
  private Object error;
}
