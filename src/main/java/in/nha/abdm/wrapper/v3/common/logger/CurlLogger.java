/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class CurlLogger implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(CurlLogger.class);

  /**
   * Generating the cURL from the requesting payload and headers By serializing the POJO and mapping
   * them into valid cUrl format
   *
   * @param uri url of the request
   * @param body request payload
   * @param customHeaders which has hipId and requestId
   * @param gatewayHeaders which has session token and timeStamp
   */
  public static <T> void logCurl(
      String uri, T body, HttpHeaders customHeaders, HttpHeaders gatewayHeaders) {
    String requestBody = serializeBody(body);
    StringBuilder curlCommand = new StringBuilder("curl --location '");
    curlCommand.append(uri).append("' \\");

    gatewayHeaders.forEach(
        (name, values) ->
            values.forEach(
                value ->
                    curlCommand
                        .append("--header '")
                        .append(name)
                        .append(": ")
                        .append(value)
                        .append("' \\")));
    customHeaders.forEach(
        (name, values) ->
            values.forEach(
                value ->
                    curlCommand
                        .append("--header '")
                        .append(name)
                        .append(": ")
                        .append(value)
                        .append("' \\")));

    if (!requestBody.isEmpty()) {
      curlCommand.append("--data-raw '").append(requestBody).append("' \\");
    }

    log.info("Generated cURL: {}", curlCommand);
  }

  private static <T> String serializeBody(T body) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.writeValueAsString(body);
    } catch (JsonProcessingException e) {
      log.error("Error serializing request body", e);
      return "{}";
    }
  }
}
