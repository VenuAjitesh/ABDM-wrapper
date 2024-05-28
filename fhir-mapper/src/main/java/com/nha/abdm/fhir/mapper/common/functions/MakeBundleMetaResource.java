/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import java.text.ParseException;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Meta;
import org.springframework.stereotype.Component;

@Component
public class MakeBundleMetaResource {
  public Meta getMeta() throws ParseException {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdated(Utils.getCurrentTimeStamp());
    meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DocumentBundle");
    meta.addSecurity(
        new Coding()
            .setSystem("http://terminology.hl7.org/CodeSystem/v3-Confidentiality")
            .setCode("V")
            .setDisplay("very restricted"));
    return meta;
  }
}
