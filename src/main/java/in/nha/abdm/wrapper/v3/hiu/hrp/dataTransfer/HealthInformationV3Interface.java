/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.requests.HealthInformationPushRequest;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;

public interface HealthInformationV3Interface {
  GenericResponse processEncryptedHealthInformation(
      HealthInformationPushRequest healthInformationPushRequest) throws IllegalDataStateException;
}
