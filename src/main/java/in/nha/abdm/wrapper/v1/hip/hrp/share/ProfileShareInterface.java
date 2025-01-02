/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.share;

import in.nha.abdm.wrapper.v1.hip.hrp.share.reponses.ProfileShare;

public interface ProfileShareInterface {
  void shareProfile(ProfileShare profileShare, String hipId);
}
