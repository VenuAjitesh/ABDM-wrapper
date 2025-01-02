/* (C) 2024 */
package in.nha.abdm.wrapper.v3.config;

import in.nha.abdm.wrapper.v3.common.constants.WrapperConstants;
import in.nha.abdm.wrapper.v3.common.logger.CurlLogger;
import in.nha.abdm.wrapper.v3.common.logger.HeaderLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Profile(WrapperConstants.V3)
@PropertySource({"classpath:application-v3.properties"})
public class ApplicationV3Config implements WebMvcConfigurer {

  // Only this API is added into V3 so adding into config
  @Value("${generateLinkTokenPath}")
  public String generateLinkTokenPath;

  @Autowired HeaderLogger headerLogger;
  @Autowired VersionHandler versionHandler;
  @Autowired CurlLogger curlLogger;

  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(
        versionHandler); // Blocking the v1 version request if v3 profile is set.

    registry.addInterceptor(headerLogger); // Logging custom headers

    registry.addInterceptor(curlLogger); // Logging custom headers
  }
}
