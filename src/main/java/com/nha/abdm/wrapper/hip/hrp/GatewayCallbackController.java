/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nha.abdm.wrapper.hip.hrp.discover.responses.DiscoverResponse;
import com.nha.abdm.wrapper.hip.hrp.link.responses.ConfirmResponse;
import com.nha.abdm.wrapper.hip.hrp.link.responses.InitResponse;
import java.net.URISyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Component
@RestController
public class GatewayCallbackController {

  @Autowired WorkflowManager workflowManager;

  private static final Logger log = LogManager.getLogger(GatewayCallbackController.class);

  @PostMapping("/v0.5/care-contexts/discover")
  public void discoverCall(@RequestBody DiscoverResponse data)
      throws URISyntaxException, JsonProcessingException {
    if (data != null && data.getError() == null) {
      log.info("/v0.5/care-contexts/discover :" + data.toString());
      workflowManager.initiateOnDiscover(data);
    } else {
      log.error("/v0.5/care-contexts/discover :" + data.getError().getMessage());
    }
  }

  @PostMapping("/v0.5/links/link/init")
  public void initCall(@RequestBody InitResponse data)
      throws URISyntaxException, JsonProcessingException {
    if (data != null && data.getError() == null) {
      log.info("/v0.5/links/link/init :" + data.toString());
      workflowManager.initiateOnInit(data);
    } else {
      log.error("/v0.5/links/link/init :" + data.getError().getMessage());
    }
  }

  @PostMapping("/v0.5/links/link/confirm")
  public void confirmCall(@RequestBody ConfirmResponse data)
      throws URISyntaxException, JsonProcessingException {
    if (data != null && data.getError() == null) {
      log.info("/v0.5/links/link/confirm : " + data.toString());
      workflowManager.startOnConfirmCall(data);
    } else {
      log.error("/v0.5/links/link/confirm : " + data.getError().getMessage());
    }
  }
}
