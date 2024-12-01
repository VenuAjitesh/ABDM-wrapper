/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GenericResponse {
  private HttpStatus httpStatus;
  private ErrorResponse errorResponse;
}
