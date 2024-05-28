/* (C) 2024 */
package com.nha.abdm.fhir.mapper.converter;

import ca.uhn.fhir.context.FhirContext;
import com.nha.abdm.fhir.mapper.Utils;
import com.nha.abdm.fhir.mapper.common.functions.*;
import com.nha.abdm.fhir.mapper.common.helpers.*;
import com.nha.abdm.fhir.mapper.requests.ImmunizationRequest;
import com.nha.abdm.fhir.mapper.requests.helpers.ImmunizationResource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Service;

@Service
public class ImmunizationConverter {
  private final MakeDocumentResource makeDocumentReference;
  private final MakePatientResource makePatientResource;
  private final MakePractitionerResource makePractitionerResource;
  private final MakeOrganisationResource makeOrganisationResource;
  private final MakeImmunizationResource makeImmunizationResource;
  private final MakeBundleMetaResource makeBundleMetaResource;
  private String docName = "Immunization record";
  private String docCode = "41000179103";

  public ImmunizationConverter(
      MakeDocumentResource makeDocumentReference,
      MakePatientResource makePatientResource,
      MakePractitionerResource makePractitionerResource,
      MakeOrganisationResource makeOrganisationResource,
      MakeImmunizationResource makeImmunizationResource,
      MakeBundleMetaResource makeBundleMetaResource) {
    this.makeDocumentReference = makeDocumentReference;
    this.makePatientResource = makePatientResource;
    this.makePractitionerResource = makePractitionerResource;
    this.makeOrganisationResource = makeOrganisationResource;
    this.makeImmunizationResource = makeImmunizationResource;
    this.makeBundleMetaResource = makeBundleMetaResource;
  }

  public BundleResponse makeImmunizationBundle(ImmunizationRequest immunizationRequest)
      throws ParseException {
    try {
      Bundle bundle = new Bundle();
      FhirContext context = FhirContext.forR4();
      Patient patient = makePatientResource.getPatient(immunizationRequest.getPatient());
      List<Practitioner> practitionerList = new ArrayList<>();
      for (PractitionerResource practitionerResource : immunizationRequest.getPractitionerList()) {
        practitionerList.add(makePractitionerResource.getPractitioner(practitionerResource));
      }
      Organization organization =
          makeOrganisationResource.getOrganization(immunizationRequest.getOrganisation());
      List<Organization> manufactureList = new ArrayList<>();
      List<Immunization> immunizationList = new ArrayList<>();
      for (ImmunizationResource immunizationResource : immunizationRequest.getImmunizationList()) {
        Organization manufacturer =
            makeOrganisationResource.getOrganization(
                OrganisationResource.builder()
                    .facilityId(immunizationResource.getManufacturer())
                    .facilityName(immunizationResource.getManufacturer())
                    .build());
        immunizationList.add(
            makeImmunizationResource.getImmunization(
                patient, practitionerList, manufacturer, immunizationResource));
        manufactureList.add(manufacturer);
      }
      List<DocumentReference> documentList = new ArrayList<>();
      if (immunizationRequest.getDocumentList() != null) {
        for (DocumentResource documentResource : immunizationRequest.getDocumentList()) {
          documentList.add(
              makeDocumentReference.getDocument(
                  patient, organization, documentResource, docCode, docName));
        }
      }
      Composition composition =
          makeCompositionResource(
              patient, practitionerList, organization, immunizationList, documentList);
      bundle.setId(UUID.randomUUID().toString());
      bundle.setType(Bundle.BundleType.DOCUMENT);
      bundle.setTimestamp(Utils.getCurrentTimeStamp());
      bundle.setMeta(makeBundleMetaResource.getMeta());
      List<Bundle.BundleEntryComponent> entries = new ArrayList<>();
      bundle.setIdentifier(
          new Identifier()
              .setSystem("https://ABDM_WRAPPER/bundle")
              .setValue(immunizationRequest.getCareContextReference()));
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl("Composition/" + composition.getId())
              .setResource(composition));
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl("Patient/" + patient.getId())
              .setResource(patient));
      for (Practitioner practitioner : practitionerList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Practitioner/" + practitioner.getId())
                .setResource(practitioner));
      }
      entries.add(
          new Bundle.BundleEntryComponent()
              .setFullUrl("Organisation/" + organization.getId())
              .setResource(organization));
      for (Organization manufacturer : manufactureList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Manufacturer/" + manufacturer.getId())
                .setResource(manufacturer));
      }
      for (Immunization immunization : immunizationList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("Immunization/" + immunization.getId())
                .setResource(immunization));
      }
      for (DocumentReference documentReference : documentList) {
        entries.add(
            new Bundle.BundleEntryComponent()
                .setFullUrl("DocumentReference/" + documentReference.getId())
                .setResource(documentReference));
      }
      bundle.setEntry(entries);
      return BundleResponse.builder().bundle(bundle).build();
    } catch (Exception e) {
      return BundleResponse.builder()
          .error(ErrorResponse.builder().code(1000).message(e.getMessage()).build())
          .build();
    }
  }

  private Composition makeCompositionResource(
      Patient patient,
      List<Practitioner> practitionerList,
      Organization organization,
      List<Immunization> immunizationList,
      List<DocumentReference> documentList)
      throws ParseException {
    Composition composition = new Composition();
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.setLastUpdated(Utils.getCurrentTimeStamp());
    meta.addProfile("https://nrces.in/ndhm/fhir/r4/StructureDefinition/ImmunizationRecord");
    composition.setMeta(meta);
    CodeableConcept typeCode = new CodeableConcept();
    Coding typeCoding = new Coding();
    typeCoding.setSystem("http://snomed.info/sct");
    typeCoding.setCode("41000179103");
    typeCoding.setDisplay("Immunization record");
    typeCode.addCoding(typeCoding);
    composition.setType(typeCode);
    composition.setTitle("Immunization record");
    if (Objects.nonNull(organization))
      composition.setCustodian(
          new Reference().setReference("Organisation/" + organization.getId()));
    List<Reference> authorList = new ArrayList<>();
    HumanName practitionerName = null;
    for (Practitioner author : practitionerList) {
      practitionerName = author.getName().get(0);
      authorList.add(
          new Reference()
              .setReference("Practitioner/" + author.getId())
              .setDisplay(practitionerName.getText()));
    }
    composition.setAuthor(authorList);
    HumanName patientName = patient.getName().get(0);
    composition.setSubject(
        new Reference()
            .setReference("Patient/" + patient.getId())
            .setDisplay(patientName.getText()));
    composition.setDateElement(new DateTimeType(Utils.getCurrentTimeStamp()));
    Composition.SectionComponent immunizationSection = new Composition.SectionComponent();
    immunizationSection.setTitle("Immunizations");
    immunizationSection.setCode(
        new CodeableConcept()
            .setText("Immunizations")
            .addCoding(
                new Coding()
                    .setCode("41000179103")
                    .setDisplay("Immunization record")
                    .setSystem("http://snomed.info/sct")));
    for (Immunization immunization : immunizationList) {
      Reference entryReference =
          new Reference()
              .setReference("Immunization/" + immunization.getId())
              .setType("Immunization");
      immunizationSection.addEntry(entryReference);
    }
    composition.addSection(immunizationSection);
    for (DocumentReference documentReference : documentList)
      immunizationSection.addEntry(
          new Reference()
              .setReference("DocumentReference/" + documentReference.getId())
              .setType("DocumentReference"));
    composition.setStatus(Composition.CompositionStatus.FINAL);
    Identifier identifier = new Identifier();
    identifier.setSystem("https://ABDM_WRAPPER/document");
    identifier.setValue(UUID.randomUUID().toString());
    composition.setIdentifier(identifier);
    composition.setId(UUID.randomUUID().toString());
    return composition;
  }
}
