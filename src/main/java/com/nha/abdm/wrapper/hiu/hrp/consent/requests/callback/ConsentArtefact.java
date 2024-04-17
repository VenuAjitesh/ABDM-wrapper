/* (C) 2024 */
package com.nha.abdm.wrapper.hiu.hrp.consent.requests.callback;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsentArtefact {
  private String id;
  private String lastUpdated;
  private String hipId;
  private List<String> careContextReference;
}
