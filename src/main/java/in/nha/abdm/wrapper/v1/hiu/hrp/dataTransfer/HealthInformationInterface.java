/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hiu.hrp.dataTransfer;

import in.nha.abdm.wrapper.v1.common.exceptions.IllegalDataStateException;
import in.nha.abdm.wrapper.v1.common.requests.HealthInformationPushRequest;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;

public interface HealthInformationInterface {
  GenericResponse processEncryptedHealthInformation(
      HealthInformationPushRequest healthInformationPushRequest) throws IllegalDataStateException;
}
