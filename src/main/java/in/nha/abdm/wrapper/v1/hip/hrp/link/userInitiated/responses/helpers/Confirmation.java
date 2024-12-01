/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.helpers;

import lombok.Data;

@Data
public class Confirmation {

  public String linkRefNumber;

  public String token;
}
