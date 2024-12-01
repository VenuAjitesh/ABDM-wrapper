/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.link.userInitiated.responses.helpers;

import java.util.List;
import lombok.Data;

@Data
public class PatientDemographicDetails {
  public String name;
  public String gender;
  public String id;

  public String yearOfBirth;
  public List<PatientVerifiedIdentifiers> verifiedIdentifiers;
  public List<PatientUnverifiedIdentifiers> unverifiedIdentifiers;
}
