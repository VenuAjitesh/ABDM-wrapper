/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.discover.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response {
  private String requestId;
}
