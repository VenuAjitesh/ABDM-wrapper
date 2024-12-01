/* (C) 2024 */
package in.nha.abdm.wrapper.v1.common;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class Utils {
  public static String getCurrentTimeStamp() {
    return DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.MILLIS));
  }

  public static Boolean checkExpiry(String inputDate) {
    // In Production because of inconsistency of timestamps, the parsing of inputDate without 'Z' is
    // failing
    // Simply checking the end of the string instead of using REGEX
    if (!inputDate.endsWith("Z")) {
      inputDate += "Z";
    }
    Instant instant = Instant.parse(inputDate);
    LocalDateTime expiryTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    return LocalDateTime.now().isAfter(expiryTime);
  }

  public static String getSmsExpiry() {
    // Adding 10 Mins for sms expiry in user-initiatedLinking
    return DateTimeFormatter.ISO_INSTANT.format(
        Instant.now().plus(10, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MILLIS));
  }

  public static String setLinkTokenExpiry() {
    // The LinkToken is valid for 6 months
    return LocalDateTime.now().plusMonths(6).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }

  public static HttpHeaders getCustomHeaders(String entityType, String entity, String requestID) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(entityType, entity);
    httpHeaders.add(GatewayConstants.REQUEST_ID, requestID);
    httpHeaders.add(GatewayConstants.TIMESTAMP, getCurrentTimeStamp());
    return httpHeaders;
  }

  public static LocalDateTime getCurrentDateTime() {
    // Tried all the ways of generating the current timestamp not the utc.
    // Using only at the RequestLogs.
    return LocalDateTime.now().plusHours(5).plusMinutes(30);
  }

  public static HttpHeaders getLinkTokenHeaders(String entity, String requestID, String linkToken) {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(GatewayConstants.X_HIP_ID, entity);
    httpHeaders.add(GatewayConstants.REQUEST_ID, requestID);
    httpHeaders.add(GatewayConstants.TIMESTAMP, getCurrentTimeStamp());
    httpHeaders.add(GatewayConstants.X_LINK_TOKEN, linkToken);
    return httpHeaders;
  }
}
