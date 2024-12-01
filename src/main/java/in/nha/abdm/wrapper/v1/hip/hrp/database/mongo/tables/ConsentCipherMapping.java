/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables;

import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "consent-cipher-key-mappings")
public class ConsentCipherMapping {

  @Field(FieldIdentifiers.CONSENT_ID)
  @Indexed(unique = true)
  public String consentId;

  @Field(FieldIdentifiers.PRIVATE_KEY)
  public String privateKey;

  @Field(FieldIdentifiers.NONCE)
  public String nonce;

  @Field(FieldIdentifiers.TRANSACTION_ID)
  public String transactionId;
}
