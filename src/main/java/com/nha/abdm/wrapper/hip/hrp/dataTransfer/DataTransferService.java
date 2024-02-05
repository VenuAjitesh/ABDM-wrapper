/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.dataTransfer;

import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.callback.BundleResponseHIP;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.callback.HIPConsentNotification;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.callback.HIPHealthInformationRequest;
import org.springframework.stereotype.Service;

@Service
public class DataTransferService implements DataTransferInterface {
  /**
   * The callback from ABDM gateway after consentGrant for dataTransfer, POST method for /on-notify
   *
   * @param HIPConsentNotification careContext and demographics details are provided, and implement
   *     a logic to check the existence of the careContexts.
   */
  @Override
  public void notifyOnReceived(HIPConsentNotification HIPConsentNotification) {
    // TODO Implement onNotify workflow logic.
  }

  /**
   * POST /on-request as an acknowledgement for agreeing to make dataTransfer to ABDM gateway.
   *
   * @param HIPHealthInformationRequest HIU public keys and dataPush URL is provided
   */
  @Override
  public void requestOnReceived(HIPHealthInformationRequest HIPHealthInformationRequest) {
    // TODO Implement onRequest workflow logic.
    initiateBundleRequest(HIPHealthInformationRequest);
  }

  /**
   * Requesting HIP for FHIR bundle
   *
   * @param HIPHealthInformationRequest use the requestId to fetch the careContexts from dump to
   *     request HIP.
   */
  @Override
  public void initiateBundleRequest(HIPHealthInformationRequest HIPHealthInformationRequest) {
    // TODO Implement bundle request to HIP workflow logic.
  }

  /**
   * Encrypt the bundle and POST to /dataPushUrl of HIU
   *
   * @param bundleResponseHIP FHIR bundle received from HIP for the particular patients
   */
  @Override
  public void initiateDataTransfer(BundleResponseHIP bundleResponseHIP) {
    // TODO Implement dataPush workflow logic.
  }
}
