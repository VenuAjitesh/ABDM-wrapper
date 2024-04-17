/* (C) 2024 */
package com.nha.abdm.wrapper.common.requests;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nha.abdm.wrapper.ApplicationConfig;
import java.text.MessageFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Component
public class SessionManager {

  @Autowired ApplicationConfig applicationConfig;
  private String accessToken;

  @Value("${gatewayBaseUrl}")
  private String gatewayBaseUrl;

  @Value("${createSessionPath}")
  private String createSessionPath;

  @Value("${useProxySettings}")
  private boolean useProxySettings;

  @Value("${proxyHost}")
  private String proxyHost;

  @Value("${proxyPort}")
  private int proxyPort;

  private static final Logger log = LogManager.getLogger(SessionManager.class);

  private static final String CONSENT_MANAGER_ENVIRONMENT = "X-CM-ID";
  private static final String AUTHORIZATION_HEADER = "Authorization";

  public HttpHeaders setGatewayRequestHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(CONSENT_MANAGER_ENVIRONMENT, applicationConfig.environment);
    headers.add(AUTHORIZATION_HEADER, this.fetchAccessToken());
    return headers;
  }

  private String fetchAccessToken() {
    if (accessToken == null) {
      try {
        startSession();
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }
    return accessToken;
  }

  /**
   * The accessToken expires at 20 minutes post creating, using this scheduler refreshing the
   * accessToken every 18 minutes to avoid Unauthorized error.
   */
  @Scheduled(initialDelay = 15 * 60 * 1000, fixedRate = 15 * 60 * 1000)
  private void startSession() throws Throwable {

    CreateSessionRequest createSessionRequest =
        CreateSessionRequest.builder()
            .clientId(applicationConfig.clientId)
            .clientSecret(applicationConfig.clientSecret)
            .build();
    try {

      ResponseEntity<ObjectNode> responseEntity = getSessionResponse(createSessionRequest);
      String accessTokenResponse =
          MessageFormat.format(
              "Bearer {0}", responseEntity.getBody().findValue("accessToken").asText());
      if (accessTokenResponse != null) {
        accessToken = accessTokenResponse;
      } else {
        log.error(
            "Empty access token found"
                + createSessionPath
                + " : "
                + responseEntity.getBody().toString());
      }
    } catch (Exception e) {
      log.error("Could not start gateway session: " + e);
      // This is to get actual exception wrapped under ReactiveException.
      throw Exceptions.unwrap(e);
    }
  }

  private ResponseEntity<ObjectNode> getSessionResponse(CreateSessionRequest createSessionRequest) {
    WebClient webClient;
    if (useProxySettings) {
      webClient =
          WebClient.builder()
              .baseUrl(gatewayBaseUrl)
              .clientConnector(new ReactorClientHttpConnector(getHttpClient()))
              .build();
    } else {
      webClient = WebClient.builder().baseUrl(gatewayBaseUrl).build();
    }
    return webClient
        .post()
        .uri(createSessionPath)
        .body(BodyInserters.fromValue(createSessionRequest))
        .retrieve()
        .toEntity(ObjectNode.class)
        .block();
  }

  public HttpClient getHttpClient() {
    HttpClient httpClient =
        HttpClient.create()
            .tcpConfiguration(
                tcpClient ->
                    tcpClient.proxy(
                        proxy ->
                            proxy.type(ProxyProvider.Proxy.HTTP).host(proxyHost).port(proxyPort)));
    return httpClient;
  }
}
