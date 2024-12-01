/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.share.requests.helpers;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TokenTimeStamp {
  private LocalDateTime timeStamp;
  private String token;
  private LocalDateTime expiry;
}
