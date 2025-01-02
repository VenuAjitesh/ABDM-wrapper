/* (C) 2024 */
package in.nha.abdm.wrapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:application.properties"})
public class ApplicationConfig {

  @Value("${clientId}")
  public String clientId;

  @Value("${clientSecret}")
  public String clientSecret;

  @Value("${environment}")
  public String environment;
}
