/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "consent-patient")
public class ConsentPatient {
  @Field("consentId")
  @Indexed(unique = true)
  public String consentId;

  @Field("abhaAddress")
  public String abhaAddress;

  @Field("entityType")
  public String entityType;

  @Field("hipId")
  public String hipId;
}
