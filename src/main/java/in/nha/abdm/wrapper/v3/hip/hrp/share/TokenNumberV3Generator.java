/* (C) 2024 */
package in.nha.abdm.wrapper.v3.hip.hrp.share;

import in.nha.abdm.wrapper.v1.hip.hrp.share.requests.helpers.TokenDetails;
import in.nha.abdm.wrapper.v1.hip.hrp.share.requests.helpers.TokenTimeStamp;
import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.ProfileShareV3Request;
import in.nha.abdm.wrapper.v3.hip.hrp.share.requests.ProfileV3Acknowledgement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenNumberV3Generator {
  private AtomicInteger counter = new AtomicInteger(0);
  private LocalDate currentDate = LocalDate.now();
  private static final Map<TokenDetails, TokenTimeStamp> tokenCache = new ConcurrentHashMap<>();

  @Scheduled(cron = "0 0 0 * * *")
  public void resetTokenCount() {
    counter.set(0);
    tokenCache.clear();
    currentDate = LocalDate.now();
  }

  public TokenTimeStamp generateTokenNumber(ProfileShareV3Request profileShare, String hipId) {
    if (currentDate.isBefore(LocalDate.now())) {
      resetTokenCount();
    }
    int tokenNumber = counter.incrementAndGet();
    TokenDetails tokenDetails =
        TokenDetails.builder()
            .hipId(hipId)
            .abhaAddress(profileShare.getProfile().getPatient().getAbhaAddress())
            .hipCounterCode(profileShare.getMetaData().getContext())
            .build();
    TokenTimeStamp tokenTimeStamp =
        TokenTimeStamp.builder()
            .timeStamp(LocalDateTime.now())
            .token(String.format("%04d", tokenNumber))
            .build();
    tokenCache.put(tokenDetails, tokenTimeStamp);
    return tokenTimeStamp;
  }

  /**
   * Using the ConcurrentHashMap we are storing the hipId, hipCounterCode, and abhaAddress as key
   * and token number and timestamp as value When ever we find the exact key checking the timestamp
   * with a token validity of 30 mins if the token is not expired returning the same token or else
   * generating a new token.
   *
   * @param profileShare basic patient demographic details.
   * @param hipId facilityId
   */
  public TokenTimeStamp checkTokenStatus(ProfileShareV3Request profileShare, String hipId) {
    String abhaAddress = profileShare.getProfile().getPatient().getAbhaAddress();
    String hipCounterCode = profileShare.getMetaData().getContext();
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
      return token;
    }
    return null;
  }

  public void updateExpiry(ProfileV3Acknowledgement profileV3Acknowledgement, String hipId) {
    String abhaAddress = profileV3Acknowledgement.getAbhaAddress();
    String hipCounterCode = profileV3Acknowledgement.getProfile().getContext();
    TokenDetails tokenDetails =
        TokenDetails.builder()
            .abhaAddress(abhaAddress)
            .hipCounterCode(hipCounterCode)
            .hipId(hipId)
            .build();
    TokenTimeStamp token = tokenCache.get(tokenDetails);
    token.setToken(profileV3Acknowledgement.getProfile().getTokenNumber());
    token.setExpiry(
        LocalDateTime.now().plusMinutes(60)); // Hardcoding 60 Min since there is no use case of it.
    tokenCache.replace(tokenDetails, token);
  }

  private static boolean isTokenNotExpired(TokenTimeStamp token) {
    LocalDateTime currentTime = LocalDateTime.now();
    LocalDateTime tokenTimestamp = token.getTimeStamp();
    return currentTime.isBefore(tokenTimestamp.plusMinutes(60));
  }
}
