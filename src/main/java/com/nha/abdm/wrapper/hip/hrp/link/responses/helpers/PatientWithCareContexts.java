/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.link.responses.helpers;

import java.util.List;
import lombok.Data;

@Data
public class PatientWithCareContexts {

  private String id;
  private String referenceNumber;
  private List<InitCareContextList> careContexts;
}
