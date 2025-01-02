/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.hipInitiated.responses.helpers;

import lombok.Data;

@Data
public class LinkAuthData {
  private String accessToken;
  private String transactionId;
  private String mode;
}
