/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip;

import in.nha.abdm.wrapper.v1.common.responses.RequestStatusResponse;
import in.nha.abdm.wrapper.v1.common.responses.ResponseOtp;
import in.nha.abdm.wrapper.v1.hip.HIPPatient;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.HealthInformationBundleRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.dataTransfer.requests.HealthInformationBundleResponse;
import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.CareContextRequest;
import in.nha.abdm.wrapper.v1.hip.hrp.discover.requests.DiscoverRequest;
import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.ProfileV3Acknowledgement;
import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.ShareProfileV3Request;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.util.retry.Retry;

@Validated
@Component
public class HIPV3Client {
  @Value("${hipBaseUrl}")
  private String hipBaseUrl;

  @Value("${getPatientPath}")
  private String patientPath;

  @Value("${patientDiscoverPath}")
  private String patientDiscoverPath;

  @Value("${getPatientCareContextsPath}")
  private String getPatientCareContextsPath;

  @Value("${getHealthInformationPath}")
  private String getHealthInformationPath;

  @Value("${shareProfilePath}")
  private String shareProfilePath;

  private final WebClient webClient;

  @Retryable(
      value = {WebClientRequestException.class, ReadTimeoutException.class, TimeoutException.class},
      maxAttempts = 2,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public HIPPatient getPatient(String patientId, String hipId) {
    ResponseEntity<HIPPatient> responseEntity =
        webClient
            .get()
            .uri(
                uriBuilder ->
                    uriBuilder
                        .path(patientPath + "/" + patientId)
                        .queryParam("requesterId", hipId)
                        .build())
            .retrieve()
            .toEntity(HIPPatient.class)
            .retryWhen(
                Retry.backoff(2, Duration.ofSeconds(2))
                    .filter(
                        throwable ->
                            throwable instanceof HttpServerErrorException
                                || throwable instanceof WebClientRequestException
                                || throwable instanceof ReadTimeoutException
                                || throwable instanceof TimeoutException))
            .block();

    return responseEntity.getBody();
  }

  public HIPV3Client(@Value("${hipBaseUrl}") final String baseUrl) {
    final int size = 50 * 1024 * 1024;
    final ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(size))
            .build();
    webClient =
        WebClient.builder()
            .baseUrl(baseUrl)
            .exchangeStrategies(strategies)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
  }

  @Retryable(
      value = {WebClientRequestException.class, ReadTimeoutException.class, TimeoutException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 200, multiplier = 2))
  public ResponseEntity<HIPPatient> patientDiscover(DiscoverRequest discoverRequest) {
    return webClient
        .post()
        .uri(patientDiscoverPath)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(discoverRequest))
        .retrieve()
        .toEntity(HIPPatient.class)
        .block();
  }

  @Retryable(
      value = {WebClientRequestException.class, ReadTimeoutException.class, TimeoutException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 200, multiplier = 3))
  public HIPPatient getPatientCareContexts(CareContextRequest careContextRequest) {
    ResponseEntity<HIPPatient> responseEntity =
        webClient
            .post()
            .uri(getPatientCareContextsPath)
            .accept(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(careContextRequest))
            .retrieve()
            .toEntity(HIPPatient.class)
            .block();

    return responseEntity.getBody();
  }

  @Retryable(
      value = {WebClientRequestException.class, ReadTimeoutException.class, TimeoutException.class},
      maxAttempts = 2,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public ResponseEntity<HealthInformationBundleResponse> healthInformationBundleRequest(
      HealthInformationBundleRequest healthInformationBundleRequest) {
    return webClient
        .post()
        .uri(getHealthInformationPath)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(healthInformationBundleRequest))
        .retrieve()
        .toEntity(HealthInformationBundleResponse.class)
        .block();
  }

  @Retryable(
      value = {WebClientRequestException.class, ReadTimeoutException.class, TimeoutException.class},
      maxAttempts = 2,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public ResponseEntity<ProfileV3Acknowledgement> shareProfile(
      ShareProfileV3Request shareProfileRequest) {
    return webClient
        .post()
        .uri(shareProfilePath)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(shareProfileRequest))
        .retrieve()
        .toEntity(ProfileV3Acknowledgement.class)
        .block();
  }

  @Retryable(
      value = {WebClientRequestException.class, ReadTimeoutException.class, TimeoutException.class},
      maxAttempts = 2,
      backoff = @Backoff(delay = 200, multiplier = 2))
  public <T> ResponseEntity<ResponseOtp> requestOtp(String uri, T request) {
    return webClient
        .post()
        .uri(uri)
        .body(BodyInserters.fromValue(request))
        .retrieve()
        .toEntity(ResponseOtp.class)
        .block();
  }

  @Retryable(
      value = {WebClientRequestException.class, ReadTimeoutException.class, TimeoutException.class},
      maxAttempts = 2,
      backoff = @Backoff(delay = 5000, multiplier = 2))
  public <T> ResponseEntity<RequestStatusResponse> fetchResponseFromHIP(String uri, T request) {
    return webClient
        .post()
        .uri(uri)
        .body(BodyInserters.fromValue(request))
        .retrieve()
        .toEntity(RequestStatusResponse.class)
        .block();
  }
}
