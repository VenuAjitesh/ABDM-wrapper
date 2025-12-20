/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables;

import in.nha.abdm.wrapper.v1.common.models.CareContext;
import in.nha.abdm.wrapper.v1.common.models.Consent;
import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.FieldIdentifiers;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "patients")
@Builder
@AllArgsConstructor
public class Patient {

  @Field(FieldIdentifiers.ABHA_ADDRESS)
  @Indexed(unique = true)
  @NotNull(message = "AbhaAddress is mandatory") public String abhaAddress;

  @Field(FieldIdentifiers.NAME)
  @NotNull(message = "Name of the patient is mandatory") public String name;

  @Field(FieldIdentifiers.GENDER)
  @NotNull(message = "Gender is mandatory M,F,T.") @Pattern(regexp = "[FMT]")
  public String gender;

  @Field(FieldIdentifiers.DATE_OF_BIRTH)
  @NotNull(message = "Date of birth is mandatory") @Pattern(
      regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
      message = "dateOfBirth must be in the format yyyy-MM-dd")
  public String dateOfBirth;

  @Field(FieldIdentifiers.PATIENT_REFERENCE)
  @Indexed(unique = true)
  @NotNull(message = "patientReference is mandatory") public String patientReference;

  @Field(FieldIdentifiers.PATIENT_DISPLAY)
  @NotNull(message = "patientDisplay is mandatory") public String patientDisplay;

  @Field(FieldIdentifiers.PATIENT_MOBILE)
  @NotNull(message = "patientMobile is mandatory") public String patientMobile;

  @Field(FieldIdentifiers.CARE_CONTEXTS)
  public List<CareContext> careContexts;

  @Field(FieldIdentifiers.CONSENTS)
  public List<Consent> consents;

  @Field(FieldIdentifiers.HIP_ID)
  public String hipId;

  @Field(FieldIdentifiers.IS_DEFAULT)
  private Boolean isDefault = false;

  @Field(FieldIdentifiers.CREATED_AT)
  private Date createdAt;

  public Patient() {}
}
