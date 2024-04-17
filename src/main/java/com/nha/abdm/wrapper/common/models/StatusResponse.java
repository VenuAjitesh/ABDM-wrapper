/* (C) 2024 */
package com.nha.abdm.wrapper.common.models;

import com.nha.abdm.wrapper.common.responses.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponse {
  private String requestId;
  private String timestamp;
  private ErrorResponse error;
  private RespRequest resp;
  private String status;
  private Acknowledgement acknowledgement;

}
