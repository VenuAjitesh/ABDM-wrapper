/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables;

import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "consent-requests")
public class ConsentRequest {
  @Field(FieldIdentifiers.CONSENT_REQUEST_ID)
  @Indexed(unique = true)
  public String consentRequestId;

  @Field(FieldIdentifiers.GATEWAY_REQUEST_ID)
  public String gatewayRequestId;
}
