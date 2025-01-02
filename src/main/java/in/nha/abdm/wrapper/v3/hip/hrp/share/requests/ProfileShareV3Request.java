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
public class ProfileShareV3Request {
  private String intent;
  private ProfileShareMetaData metaData;
  private ProfileShareProfile profile;
}
