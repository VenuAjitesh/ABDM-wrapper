/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common.requests;

import com.fasterxml.jackson.databind.node.ObjectNode;
import in.nha.abdm.wrapper.ApplicationConfig;
import in.nha.abdm.wrapper.v1.common.Utils;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.TimeoutException;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.Exceptions;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;
import reactor.util.retry.Retry;

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

  @Value("${connectionTimeout}")
  private int connectionTimeout;

  @Value("${responseTimeout}")
  private int responseTimeout;

  private static final Logger log = LogManager.getLogger(SessionManager.class);

  private static final String CONSENT_MANAGER_ENVIRONMENT = "X-CM-ID";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String TIMESTAMP = "TIMESTAMP";
  private static final String REQUEST_ID = "REQUEST-ID";

  public HttpHeaders setGatewayRequestHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(CONSENT_MANAGER_ENVIRONMENT, applicationConfig.environment);
    headers.add(AUTHORIZATION_HEADER, this.fetchAccessToken());
    return headers;
  }

  public HttpHeaders setDefaultSessionHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add(CONSENT_MANAGER_ENVIRONMENT, applicationConfig.environment);
    headers.add(TIMESTAMP, Utils.getCurrentTimeStamp());
    headers.add(REQUEST_ID, UUID.randomUUID().toString());
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
   * accessToken every 15 minutes to avoid Unauthorized error.
   */
  @Scheduled(initialDelay = 15 * 60 * 1000, fixedRate = 15 * 60 * 1000)
  @Retryable(
      value = {WebClientRequestException.class, ReadTimeoutException.class, TimeoutException.class},
      maxAttempts = 5,
      backoff = @Backoff(delay = 1000, multiplier = 2))
  public void startSession() throws Throwable {

    CreateSessionRequest createSessionRequest =
        CreateSessionRequest.builder()
            .clientId(applicationConfig.clientId)
            .clientSecret(applicationConfig.clientSecret)
            .grantType("client_credentials")
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
    WebClient webClient =
        WebClient.builder()
            .baseUrl(gatewayBaseUrl)
            .defaultHeaders(httpHeaders -> httpHeaders.addAll(setDefaultSessionHeaders()))
            .clientConnector(new ReactorClientHttpConnector(getHttpClient(useProxySettings)))
            .build();

    return webClient
        .post()
        .uri(createSessionPath)
        .body(BodyInserters.fromValue(createSessionRequest))
        .retrieve()
        .toEntity(ObjectNode.class)
        .retryWhen(
            Retry.backoff(5, Duration.ofSeconds(2))
                .filter(
                    throwable ->
                        throwable instanceof HttpServerErrorException
                            || throwable instanceof WebClientRequestException
                            || throwable instanceof ReadTimeoutException
                            || throwable instanceof java.util.concurrent.TimeoutException))
        .block();
  }

  public HttpClient getHttpClient(boolean useProxySettings) {
    HttpClient httpClient;
    if (useProxySettings) {
      httpClient =
          HttpClient.create()
              .responseTimeout(Duration.ofSeconds(responseTimeout))
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
              .tcpConfiguration(
                  tcpClient ->
                      tcpClient.proxy(
                          proxy ->
                              proxy
                                  .type(ProxyProvider.Proxy.HTTP)
                                  .host(proxyHost)
                                  .port(proxyPort)));
    } else {
      httpClient =
          HttpClient.create()
              .responseTimeout(Duration.ofSeconds(responseTimeout))
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout);
    }
    return httpClient;
  }
}
