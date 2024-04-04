/* (C) 2024 */
package com.nha.abdm.wrapper.hip;

import com.nha.abdm.wrapper.common.requests.HealthInformationPushRequest;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class HIUClient {
  private static final Logger log = LogManager.getLogger(HIUClient.class);
  // The WebClient is throwing 404 not found where as RestTemplate is working //TODO
  // url: https://dev.abdm.gov.in/api-hiu/data/notification
  // url: https://dev.abdm.gov.in/patient-hiu/data/notification
  // These are the known url where WebClient is throwing 404 error.
  public ResponseEntity<GenericResponse> pushHealthInformation(
      String datPushURl, HealthInformationPushRequest healthInformationPushRequest) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<GenericResponse> response =
        restTemplate.postForEntity(datPushURl, healthInformationPushRequest, GenericResponse.class);
    //    WebClient webClient =
    //        WebClient.builder()
    //            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    //            .build();
    //
    //    ResponseEntity<GenericResponse> response =
    //        webClient
    //            .post()
    //            .uri(datPushURl)
    //            .body(BodyInserters.fromValue(healthInformationPushRequest))
    //            .retrieve()
    //            .toEntity(GenericResponse.class)
    //            .block();
    //    log.debug("correlation id: " + response.getHeaders());
    return response;
  }
}
