/* (C) 2024 */
package in.nha.abdm.wrapper.v3.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nha.abdm.wrapper.v1.common.responses.ErrorResponse;
import in.nha.abdm.wrapper.v1.common.responses.FacadeResponse;
import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class VersionHandler implements HandlerInterceptor {
  @Value("${spring.profiles.active}")
  private String activeProfile;

  private final ObjectMapper objectMapper;

  public VersionHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Stopping the V1 version api request when the profile is set to V3
   *
   * @param request current HTTP request
   * @param response current HTTP response
   * @param handler chosen handler to execute, for type and/or instance evaluation
   * @return Blocks v1 APIs
   * @throws Exception
   */
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (WrapperConstants.V3.equals(activeProfile) && request.getRequestURI().startsWith("/v1")) {
      response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      response.setContentType("application/json");

      ErrorResponse errorResponse =
          new ErrorResponse("Wrapper-1001", "v1 APIs are not allowed under v3 profile");
      FacadeResponse facadeResponse =
          FacadeResponse.builder()
              .code(1000)
              .httpStatusCode(HttpStatus.FORBIDDEN)
              .error(errorResponse)
              .message("Kindly use v3 APIs")
              .build();
      response.getWriter().write(objectMapper.writeValueAsString(facadeResponse));
      return false;
    }
    return true;
  }
}
