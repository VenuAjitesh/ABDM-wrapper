/* (C) 2024 */
package com.nha.abdm.fhir.mapper.database.mongo.tables;

import com.nha.abdm.fhir.mapper.common.constants.BundleFieldIdentifier;
import com.nha.abdm.fhir.mapper.common.constants.SnomedCodeIdentifier;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "snomed-observations")
public class SnomedObservation {
  @Field(BundleFieldIdentifier.CODE)
  @Indexed(unique = true)
  public String code;

  @Field(BundleFieldIdentifier.DISPLAY)
  @Indexed(unique = true)
  public String display;

  @Field(SnomedCodeIdentifier.CATEGORY)
  public String type = SnomedCodeIdentifier.SNOMED_OBSERVATIONS;
}
