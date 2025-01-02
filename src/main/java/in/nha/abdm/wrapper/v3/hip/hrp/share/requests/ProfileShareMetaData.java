/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.share.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileShareMetaData {
  private String hipId;
  private String context;
  private String hprId;
  private String latitude;
  private String longitude;
}
