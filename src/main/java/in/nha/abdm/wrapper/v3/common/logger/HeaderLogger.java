/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.logger;

import in.nha.abdm.wrapper.v1.common.GatewayConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HeaderLogger implements HandlerInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(HeaderLogger.class);

  /**
   * The V3 version of APIs from ABDM contains X-HIP-ID, REQUEST-ID etc, this preHandle method logs
   * the particular details
   *
   * @param request current HTTP request
   * @param response current HTTP response
   * @param handler chosen handler to execute, for type and/or instance evaluation
   * @return Logging the headers
   */
  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {

    String hipId = request.getHeader(GatewayConstants.X_HIP_ID);
    String hiuId = request.getHeader(GatewayConstants.X_HIU_ID);
    String requestId = request.getHeader(GatewayConstants.REQUEST_ID);
    logger.info("[X-HIP-ID: {}, X-HIU-ID: {}, REQUEST-ID: {}]", hipId, hiuId, requestId);
    return true;
  }
}
