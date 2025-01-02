/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.stereotype.Component;

@Data
@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CareContext {
  @NotNull(message = "careContext 'referenceNumber' is mandatory") public String referenceNumber;

  @NotNull(message = "careContext 'display' is mandatory") public String display;

  public String hiType;
  @JsonIgnore public boolean isLinked;
}
