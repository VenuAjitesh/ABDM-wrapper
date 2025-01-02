/* (C) 2024 */
package com.nha.abdm.fhir.mapper.rest.database.h2.tables;

import com.nha.abdm.fhir.mapper.rest.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.rest.database.h2.services.Displayable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "snomed_condition_procedure")
public class SnomedConditionProcedure implements Displayable {
  @Id public String code;

  public String display;

  public final String type =
      SnomedCodeIdentifier.SNOMED_CONDITION + "/" + SnomedCodeIdentifier.SNOMED_PROCEDURE;
}
