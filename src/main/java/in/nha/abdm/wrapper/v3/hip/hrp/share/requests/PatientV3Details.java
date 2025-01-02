/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.share.requests;

import in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.helpers.PatientVerifiedIdentifiers;
import in.nha.abdm.wrapper.v1.hip.hrp.share.reponses.helpers.PatientAddress;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientV3Details {
  private String abhaAddress;
  private String abhaNumber;
  private String name;
  private String gender;
  private PatientAddress address;
  private String yearOfBirth;
  private String dayOfBirth;
  private String monthOfBirth;
  private String phoneNumber;
  private List<PatientVerifiedIdentifiers> identifiers;
}
