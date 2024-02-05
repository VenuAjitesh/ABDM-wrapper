/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.dataTransfer;

import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.callback.BundleResponseHIP;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.callback.HIPConsentNotification;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.callback.HIPHealthInformationRequest;

public interface DataTransferInterface {
  void notifyOnReceived(HIPConsentNotification data);

  void requestOnReceived(HIPHealthInformationRequest data);

  void initiateBundleRequest(HIPHealthInformationRequest HIPHealthInformationRequest);

  void initiateDataTransfer(BundleResponseHIP bundleResponseHIP);
}
