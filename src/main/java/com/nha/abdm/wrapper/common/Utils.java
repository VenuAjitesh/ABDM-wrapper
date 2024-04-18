/* (C) 2024 */
package com.nha.abdm.wrapper.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class Utils {
  public static String getCurrentTimeStamp() {
    return DateTimeFormatter.ISO_INSTANT.format(Instant.now());
  }

  public static Boolean checkExpiry(String inputDate) {
    Instant instant = Instant.parse(inputDate);
    LocalDateTime expiryTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    return LocalDateTime.now().isAfter(expiryTime);
  }

  public static String getSmsExpiry() {
    return LocalDateTime.now().plusMinutes(15).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
  }
}
