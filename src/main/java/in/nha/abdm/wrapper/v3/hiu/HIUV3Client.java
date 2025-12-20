/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hiu;

import in.nha.abdm.wrapper.v1.common.requests.HealthInformationPushRequest;
import in.nha.abdm.wrapper.v1.common.responses.GenericResponse;
import in.nha.abdm.wrapper.v3.common.logger.CurlLogger;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Component
public class HIUV3Client {
  @Value("${logCurl}")
  private boolean logCurl;

  private static final Logger log = LogManager.getLogger(HIUV3Client.class);

  public List<ResponseEntity<GenericResponse>> pushHealthInformation(
      String dataPushURL, List<HealthInformationPushRequest> healthInformationPushRequestList) {

    WebClient webClient =
        WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024))
            .build();

    return healthInformationPushRequestList.stream()
        .map(
            request -> {
              if (logCurl) {
                CurlLogger.logCurl(dataPushURL, request, null, null);
              }
              try {
                ResponseEntity<GenericResponse> response =
                    webClient
                        .post()
                        .uri(dataPushURL)
                        .body(BodyInserters.fromValue(request))
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

                log.info("Pushed health information. Response headers: {}", response.getHeaders());
                return response;
              } catch (WebClientResponseException e) {
                log.error(
                    "WebClient error for request {}. Status: {}, headers: {}, body: {}",
                    request,
                    e.getStatusCode(),
                    e.getHeaders(),
                    e.getResponseBodyAsString());
                return null;
              } catch (Exception e) {
                log.error(
                    "Failed to push health information for request: {} {}",
                    request,
                    e.getMessage(),
                    e);
                return null;
              }
            })
        .collect(Collectors.toList());
  }
}
