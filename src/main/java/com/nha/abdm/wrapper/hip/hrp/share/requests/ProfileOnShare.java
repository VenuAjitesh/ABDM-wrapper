/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.share.requests;

import com.nha.abdm.wrapper.common.models.RespRequest;
import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import com.nha.abdm.wrapper.hip.hrp.share.requests.helpers.ProfileAcknowledgement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ProfileOnShare {
  private String requestId;
  private String timestamp;
  private ProfileAcknowledgement acknowledgement;
  private ErrorResponse error;
  private RespRequest resp;
}
