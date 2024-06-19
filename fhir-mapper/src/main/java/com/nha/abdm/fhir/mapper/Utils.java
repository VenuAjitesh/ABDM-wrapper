/* (C) 2024 */
package com.nha.abdm.fhir.mapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import org.hl7.fhir.r4.model.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Utils {
  private static final SimpleDateFormat ISO_DATE_TIME_FORMAT =
      new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
  private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final Logger log = LoggerFactory.getLogger(Utils.class);

  public static Date getCurrentTimeStamp() throws ParseException {
    return Date.from(Instant.now().atOffset(ZoneOffset.UTC).toInstant());
  }

  public static DateTimeType getFormattedDateTime(String dateTimeString) throws ParseException {
    dateTimeString = dateTimeString.trim();
    if (dateTimeString.length() <= 10) {
      return new DateTimeType(dateTimeString);
    } else {
      return (DateTimeType) new DateTimeType(ISO_DATE_TIME_FORMAT.parse(dateTimeString));
    }
  }

  public static Date getFormattedDate(String dateTimeString) throws ParseException {
    dateTimeString = dateTimeString.trim();
    if (dateTimeString.length() <= 10) {
      return DATE_ONLY_FORMAT.parse(dateTimeString);
    } else return null;
  }
}
// public static DateTimeType getFormattedDateTime(String dateTimeString) throws ParseException {
//  dateTimeString = dateTimeString.trim();
//  if (dateTimeString.length() <= 10) {
//    return new DateTimeType(dateTimeString);
//  } else {
//    log.info(String.valueOf((DateTimeType) new DateTimeType(dateTimeString)));
//    return (DateTimeType) new DateTimeType(dateTimeString).setTimeZoneZulu(true);
//  }
// }
