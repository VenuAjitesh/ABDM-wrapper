/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses;

import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.helpers.Confirmation;
import java.io.Serializable;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ConfirmResponse implements Serializable {

  private static final long serialVersionUID = 165269402517398406L;

  public String requestId;

  public ErrorResponse error;

  public Confirmation confirmation;
}
