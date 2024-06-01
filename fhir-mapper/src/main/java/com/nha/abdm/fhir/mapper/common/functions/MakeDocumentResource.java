/* (C) 2024 */
package com.nha.abdm.fhir.mapper.common.functions;

import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.helpers.DocumentResource;
import java.text.ParseException;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class MakeDocumentResource {
  public DocumentReference getDocument(
      Patient patient,
      Organization organization,
      DocumentResource documentResource,
      String docCode,
      String docName)
      throws ParseException {
    HumanName patientName = patient.getName().get(0);
    Coding coding = new Coding();
    coding.setCode(docCode);
    coding.setSystem("http://snomed.info/sct");
    coding.setDisplay(docName);
    CodeableConcept codeableConcept = new CodeableConcept();
    codeableConcept.addCoding(coding);
    codeableConcept.setText(documentResource.getType());
    Identifier identifier = new Identifier();
    identifier.setType(codeableConcept);
    identifier.setSystem("https://facility.abdm.gov.in");
    identifier.setValue(
        organization.getId() == null ? UUID.randomUUID().toString() : organization.getId());

    Attachment attachment = new Attachment();
    attachment.setContentType(documentResource.getContentType());
    attachment.setData(documentResource.getData().getBytes());
    attachment.setTitle(documentResource.getType());
    attachment.setCreation(Utils.getCurrentTimeStamp());
    DocumentReference.DocumentReferenceContentComponent documentReferenceContentComponent =
        new DocumentReference.DocumentReferenceContentComponent().setAttachment(attachment);
    DocumentReference documentReference = new DocumentReference();
    documentReference.setId(UUID.randomUUID().toString());
    documentReference.setMeta(
        new Meta()
            .setLastUpdated(Utils.getCurrentTimeStamp())
            .addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/DocumentReference"));
    documentReference.addIdentifier(identifier);
    documentReference.addContent(documentReferenceContentComponent);
    documentReference.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
    documentReference.setDocStatus(DocumentReference.ReferredDocumentStatus.FINAL);
    Reference documentSubject = new Reference();
    documentReference.setSubject(
        documentSubject
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    return documentReference;
  }
}
