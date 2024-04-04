/* (C) 2024 */
package com.nha.abdm.wrapper.hip;

import com.nha.abdm.wrapper.common.responses.RequestStatusResponse;
import com.nha.abdm.wrapper.common.responses.ResponseOtp;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.HealthInformationBundleRequest;
import com.nha.abdm.wrapper.hip.hrp.dataTransfer.requests.HealthInformationBundleResponse;
import com.nha.abdm.wrapper.hip.hrp.discover.requests.CareContextRequest;
import com.nha.abdm.wrapper.hip.hrp.discover.requests.DiscoverRequest;
import com.nha.abdm.wrapper.hip.hrp.share.requests.ShareProfileRequest;
import com.nha.abdm.wrapper.hip.hrp.share.requests.helpers.ProfileAcknowledgement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HIPClient {

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

  private WebClient webClient;

  public HIPClient(@Value("${hipBaseUrl}") final String baseUrl) {
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

  public HIPPatient getPatient(String patientId) {
    ResponseEntity<HIPPatient> responseEntity =
        webClient
            .get()
            .uri(patientPath + "/" + patientId)
            .retrieve()
            .toEntity(HIPPatient.class)
            .block();

    return responseEntity.getBody();
  }

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

  public ResponseEntity<ProfileAcknowledgement> shareProfile(
      ShareProfileRequest shareProfileRequest) {
    return webClient
        .post()
        .uri(shareProfilePath)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(shareProfileRequest))
        .retrieve()
        .toEntity(ProfileAcknowledgement.class)
        .block();
  }

  public <T> ResponseEntity<ResponseOtp> requestOtp(String uri, T request) {
    return webClient
        .post()
        .uri(uri)
        .body(BodyInserters.fromValue(request))
        .retrieve()
        .toEntity(ResponseOtp.class)
        .block();
  }

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
