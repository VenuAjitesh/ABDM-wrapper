/* (C) 2024 */
package in.nha.abdm.wrapper.v3.database.mongo.tables;

import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = FieldIdentifiers.TABLE_LINK_TOKEN)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkToken {

  @Field(FieldIdentifiers.ABHA_ADDRESS)
  @Indexed(unique = true)
  public String abhaAddress;

  @Field(FieldIdentifiers.LINK_TOKEN)
  public String linkToken;

  @Field(FieldIdentifiers.EXPIRY)
  public String expiry;

  @Indexed(unique = true)
  @Field(FieldIdentifiers.HIP_ID)
  public String hipId;

  @Field(FieldIdentifiers.LINK_TOKEN_REQUEST_ID)
  public String linkTokenRequestId;
}
