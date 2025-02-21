package com.nha.abdm.hip;


import com.nha.abdm.wrapper.client.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping(path = "/v1")
public class PatientController {

    @Value("${filePath}")
    private String fhirFilePath;

    private static final Logger log = LogManager.getLogger(PatientController.class);
    private static final String requestId = "263ad643-ffb9-4c7d-b5bc-e099577e7e99";

    @GetMapping({"/patients/{patientId}"})
    public Patient fetchPatientById(@PathVariable("patientId") String abhaAddress) {

        // TODO: Logic to find patient in HIP database using abhaAddress.

        // Placeholder to send dummy patient.
        Patient patient = new Patient();
        patient.setAbhaAddress(abhaAddress);
        patient.setName("random");
        patient.setGender(Patient.GenderEnum.M);
        patient.setDateOfBirth("1986-10-13");

        return patient;
    }
    @PostMapping({"/profile/share"})
    public ProfileAcknowledgement ProfileAcknowledgement(@RequestBody ShareProfileRequest shareProfileRequest){
        ProfileAcknowledgement profileAcknowledgement=new ProfileAcknowledgement();
        profileAcknowledgement.setStatus("SUCCESS");
        profileAcknowledgement.setHealthId(shareProfileRequest.getProfile().getProfile().getPatient().getHealthId());
        profileAcknowledgement.setTokenNumber(shareProfileRequest.getToken());
        return profileAcknowledgement;
    }
    @PostMapping({"/request/otp"})
    public RequestStatusResponse requestOtp(@RequestBody RequestOtpPostRequest requestOtpPostRequest){
        RequestStatusResponse requestStatusResponse=new RequestStatusResponse();
        requestStatusResponse.setLinkRefNumber(UUID.randomUUID().toString());
        requestStatusResponse.setStatus("SUCCESS");
        return requestStatusResponse;
    }
    @PostMapping({"/verify/otp"})
    public RequestStatusResponse verifyOtp(@RequestBody VerifyOtpPostRequest verifyOTPRequest){
        RequestStatusResponse requestStatusResponse=new RequestStatusResponse();
        if(verifyOTPRequest.getAuthCode().equals("123456")){
            requestStatusResponse.setStatus("SUCCESS");
        }else{
            requestStatusResponse.setStatus("FAILURE");
            requestStatusResponse.setError(new ErrorResponse().code(1000).message("OTP Mismatch"));
        }
        return requestStatusResponse;
    }

    @PostMapping({"/patient-discover"})
    public @ResponseBody Patient discoverPatient(@RequestBody PatientDiscoveryRequest patientDiscoveryRequest) {

        // TODO: Logic to discover patient in HIP database using abhaAddress, verifiedIdentifiers or unverifiedIdentifiers.

        // Use this hipId to route your discovery request.
        String hipId = patientDiscoveryRequest.getHipId();

        // Placeholder to send dummy patient.
        Patient patient = new Patient();
        patient.setAbhaAddress(patientDiscoveryRequest.getPatient().getId());
        patient.setName(patientDiscoveryRequest.getPatient().getName());
        patient.setGender(Patient.GenderEnum.M);
        patient.setDateOfBirth("1986-10-13");
        patient.setPatientDisplay(patientDiscoveryRequest.getPatient().getName());
        patient.setPatientReference(patientDiscoveryRequest.getPatient().getId());
        CareContext careContext1 = new CareContext();
        careContext1.setReferenceNumber(UUID.randomUUID().toString());
        careContext1.setDisplay("care-context-display41");

        CareContext careContext2 = new CareContext();
        careContext2.setReferenceNumber("care-context-reference42");
        careContext2.setDisplay("care-context-display42");

        List<CareContext> careContexts = new ArrayList<>();
        careContexts.add(careContext1);
        careContexts.add(careContext2);
        patient.setCareContexts(careContexts);

        return patient;
    }

    @PostMapping({"/patient-care-contexts"})
    public Patient fetchPatientCareContexts(@RequestBody CareContextRequest careContextRequest) {

        // TODO: Logic to find patient care contexts in HIP database.

        // Use this hipId to route your discovery request.
        String hipId = careContextRequest.getHipId();

        // Placeholder to send dummy patient.
        Patient patient = new Patient();
        patient.setAbhaAddress(careContextRequest.getAbhaAddress());
        patient.setPatientReference("patient123");
        patient.setPatientDisplay("Atul");
        CareContext careContext1 = new CareContext();
        careContext1.setReferenceNumber(UUID.randomUUID().toString());
        careContext1.setDisplay("ABDM-WRAPPER-"+new Date());
        List<CareContext> careContexts = new ArrayList<>();
        careContexts.add(careContext1);

        patient.setCareContexts(careContexts);

        return patient;
    }

    @PostMapping(value="/health-information")
    public @ResponseBody ResponseEntity<HealthInformationResponse> fetchHealthInformation(
            @RequestBody HealthInformationBundleRequest healthInformationBundleRequest) throws IOException {
        log.debug("healthInformationBundleRequest" + healthInformationBundleRequest);
        String filePath = fhirFilePath;
        String bundle= new String(Files.readAllBytes(Paths.get(filePath)));
        HealthInformationResponse healthInformationResponse=new HealthInformationResponse();
        List<HealthInformationBundle> healthInformationBundles=new ArrayList<>();
        for(ConsentCareContexts careContexts:healthInformationBundleRequest.getCareContextsWithPatientReferences()){
            HealthInformationBundle healthInformationBundle=new HealthInformationBundle();
            healthInformationBundle.setBundleContent(bundle);
            healthInformationBundle.setCareContextReference(careContexts.getCareContextReference());
            healthInformationBundles.add(healthInformationBundle);
        }
        healthInformationResponse.setHealthInformationBundle(healthInformationBundles);
        return new ResponseEntity<>(healthInformationResponse, HttpStatus.OK);
    }
}
