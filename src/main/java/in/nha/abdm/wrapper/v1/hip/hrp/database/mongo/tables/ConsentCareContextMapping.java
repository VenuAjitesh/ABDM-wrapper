/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables;

import in.nha.abdm.wrapper.v1.hiu.hrp.consent.requests.ConsentCareContexts;
import java.util.List;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "consent-careContexts")
public class ConsentCareContextMapping {
  @Field("consentId")
  @Indexed(unique = true)
  public String consentId;

  @Field("careContexts")
  public List<ConsentCareContexts> careContexts;
}
