/* (C) 2024 */
package com.nha.abdm.fhir.mapper.controller;

import com.nha.abdm.fhir.mapper.common.constants.SnomedCodeIdentifier;
import com.nha.abdm.fhir.mapper.common.helpers.SnomedResponse;
import com.nha.abdm.fhir.mapper.database.mongo.services.SnomedService;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1/snomed")
public class SnomedController {
  @Autowired SnomedService snomedService;

  @GetMapping({"/{resource}"})
  public ResponseEntity<SnomedResponse> getSnomedCodes(@PathVariable("resource") String resource) {
    if (SnomedCodeIdentifier.availableSnomed.contains(resource)) {
      SnomedResponse snomedResponse = snomedService.getSnomedCodes(resource);
      if (Objects.nonNull(snomedResponse)) {
        snomedResponse.setMessage("Retrieved");
        return ResponseEntity.ok().body(snomedResponse);
      } else {
        snomedResponse.setMessage("Empty Codes");
        return ResponseEntity.badRequest().body(snomedResponse);
      }
    } else
      return ResponseEntity.badRequest()
          .body(
              SnomedResponse.builder()
                  .message("Invalid Resource")
                  .availableSnomed(SnomedCodeIdentifier.availableSnomed)
                  .build());
  }
}
