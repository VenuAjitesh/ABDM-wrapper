/* (C) 2024 */
package com.nha.abdm.wrapper.hip;

import com.nha.abdm.wrapper.common.requests.HealthInformationPushRequest;
import com.nha.abdm.wrapper.common.responses.GenericResponse;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class HIUClient {
  private static final Logger log = LogManager.getLogger(HIUClient.class);

  @Retryable(
          value = {WebClientRequestException.class, ReadTimeoutException.class, TimeoutException.class},
          maxAttempts = 5,
          backoff = @Backoff(delay = 1000, multiplier = 2))
  public ResponseEntity<GenericResponse> pushHealthInformation(
      String datPushURl, HealthInformationPushRequest healthInformationPushRequest) {
    WebClient webClient =
        WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    ResponseEntity<GenericResponse> response =
        webClient
            .post()
            .uri(datPushURl)
            .body(BodyInserters.fromValue(healthInformationPushRequest))
            .retrieve()
            .toEntity(GenericResponse.class)
                .retryWhen(
                        Retry.backoff(5, Duration.ofSeconds(2))
                                .filter(
                                        throwable ->
                                                throwable instanceof HttpServerErrorException
                                                        || throwable instanceof WebClientRequestException
                                                        || throwable instanceof ReadTimeoutException
                                                        || throwable instanceof TimeoutException))
            .block();
    log.debug("correlation id: " + response.getHeaders());
    return response;
  }
}
