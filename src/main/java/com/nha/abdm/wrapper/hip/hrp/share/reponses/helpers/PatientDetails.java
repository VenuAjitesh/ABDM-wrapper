/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.share.reponses.helpers;

import com.nha.abdm.wrapper.hip.hrp.link.userInitiated.responses.helpers.PatientVerifiedIdentifiers;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PatientDetails {
  private String healthId;
  private String healthIdNumber;
  private String name;
  private String gender;
  private PatientAddress address;
  private String yearOfBirth;
  private String dayOfBirth;
  private String monthOfBirth;
  private List<PatientVerifiedIdentifiers> identifiers;
}
