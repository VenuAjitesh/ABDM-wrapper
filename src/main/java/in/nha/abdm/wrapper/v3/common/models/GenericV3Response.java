/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GenericV3Response {
  private HttpStatus httpStatus;
  private Object error;
  private String status;
  private String message;
}
