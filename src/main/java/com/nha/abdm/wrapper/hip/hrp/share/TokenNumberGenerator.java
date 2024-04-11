/* (C) 2024 */
package com.nha.abdm.wrapper.hip.hrp.share;

import com.nha.abdm.wrapper.hip.hrp.share.reponses.ProfileShare;
import com.nha.abdm.wrapper.hip.hrp.share.requests.helpers.TokenDetails;
import com.nha.abdm.wrapper.hip.hrp.share.requests.helpers.TokenTimeStamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenNumberGenerator {

  private AtomicInteger counter = new AtomicInteger(0);
  private LocalDate currentDate = LocalDate.now();
  private static final Map<TokenDetails, TokenTimeStamp> tokenCache = new ConcurrentHashMap<>();

  @Scheduled(cron = "0 0 0 * * *")
  public void resetTokenCount() {
    counter.set(0);
    tokenCache.clear();
    currentDate = LocalDate.now();
  }

  public String generateTokenNumber(ProfileShare profileShare, String hipId) {
    if (currentDate.isBefore(LocalDate.now())) {
      resetTokenCount();
    }
    int tokenNumber = counter.incrementAndGet();
    TokenDetails tokenDetails =
        TokenDetails.builder()
            .hipId(hipId)
            .abhaAddress(profileShare.getProfile().getPatient().getHealthId())
            .hipCounterCode(profileShare.getProfile().getHipCode())
            .build();
    tokenCache.put(
        tokenDetails,
        TokenTimeStamp.builder()
            .timeStamp(LocalDateTime.now())
            .token(String.format("%04d", tokenNumber))
            .build());
    return String.format("%04d", tokenNumber);
  }

  // Using the ConcurrentHashMap we are storing the hipId, hipCounterCode, and abhaAddress as key
  // and token number and timestamp as value
  // When ever we find the exact key checking the timestamp with a token validity of 30 mins
  // if the token is not expired returning the same token or else generating a new token.//TODO
  public String checkTokenStatus(ProfileShare profileShare, String hipId) {
    String abhaAddress = profileShare.getProfile().getPatient().getHealthId();
    String hipCounterCode = profileShare.getProfile().getHipCode();
    TokenDetails tokenDetails =
        TokenDetails.builder()
            .abhaAddress(abhaAddress)
            .hipCounterCode(hipCounterCode)
            .hipId(hipId)
            .build();
    TokenTimeStamp token = tokenCache.get(tokenDetails);
    if (Objects.isNull(token)) {
      return null;
    }
    if (isTokenNotExpired(token)) {
      return token.getToken();
    }
    return null;
  }

  private static boolean isTokenNotExpired(TokenTimeStamp token) {
    LocalDateTime currentTime = LocalDateTime.now();
    LocalDateTime tokenTimestamp = token.getTimeStamp();
    return currentTime.isBefore(tokenTimestamp.plusMinutes(30));
  }
}
