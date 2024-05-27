package com.nha.abdm.hip;

import com.nha.abdm.wrapper.client.api.LinkApi;
import com.nha.abdm.wrapper.client.api.PatientsApi;
import com.nha.abdm.wrapper.client.invoker.ApiException;
import com.nha.abdm.wrapper.client.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping(path = "/v1")
public class PatientController {

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
        requestStatusResponse.setRequestId(verifyOTPRequest.getRequestId());
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
        String filePath = "src/main/resources/OP_Consultation_fhir_bundle.json";
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
  
    @PostMapping({"/test-wrapper/upsert-patients"})
    public FacadeResponse upsertPatients() throws ApiException {
        PatientsApi patientsApi = new PatientsApi();

        List<Patient> patients = new ArrayList<>();

        Patient patient = new Patient();
        patient.setAbhaAddress("atul_kumar13@sbx");
        patient.setName("Atul Kumar");
        patient.setPatientDisplay("Atul");
        patient.setPatientReference("patient123");
        patient.setGender(Patient.GenderEnum.M);
        patient.setPatientMobile("+91-9742181684");
        patient.setDateOfBirth("1986-10-13");

        patients.add(patient);

        return patientsApi.upsertPatients(patients);
    }

    @PostMapping({"/test-wrapper/link-carecontexts-demographics"})
    public FacadeResponse linkCareContextsDemographics() throws ApiException {
        LinkApi linkApi = new LinkApi();

        CareContext careContext1 = new CareContext();
        careContext1.setReferenceNumber("care-context-reference31");
        careContext1.setDisplay("care-context-display31");

        CareContext careContext2 = new CareContext();
        careContext2.setReferenceNumber("care-context-reference32");
        careContext2.setDisplay("care-context-display32");

        List<CareContext> careContexts = new ArrayList<>();
        careContexts.add(careContext1);
        careContexts.add(careContext2);

        PatientWithCareContext patient = new PatientWithCareContext();
        patient.setId("atul_kumar13@sbx");
        patient.setReferenceNumber("patient123");
        patient.setCareContexts(careContexts);

        LinkCareContextsRequest linkCareContextsRequest = new LinkCareContextsRequest();
        linkCareContextsRequest.setRequestId(requestId);
        linkCareContextsRequest.setRequesterId("Demo_Atul_HIP");
        linkCareContextsRequest.setAbhaAddress("atul_kumar13@sbx");
        linkCareContextsRequest.setAuthMode(LinkCareContextsRequest.AuthModeEnum.DEMOGRAPHICS);
        linkCareContextsRequest.setPatient(patient);

        return linkApi.linkCareContexts(linkCareContextsRequest);
    }

    @PostMapping({"/test-wrapper/link-carecontexts-mobile-otp"})
    public FacadeResponse linkCareContextsMobileOtp() throws  ApiException {
        LinkApi linkApi = new LinkApi();

        CareContext careContext1 = new CareContext();
        careContext1.setReferenceNumber("care-context-reference17");
        careContext1.setDisplay("care-context-display17");

        CareContext careContext2 = new CareContext();
        careContext2.setReferenceNumber("care-context-reference18");
        careContext2.setDisplay("care-context-display18");

        List<CareContext> careContexts = new ArrayList<>();
        careContexts.add(careContext1);
        careContexts.add(careContext2);

        PatientWithCareContext patient = new PatientWithCareContext();
        patient.setId("atul_kumar13@sbx");
        patient.setReferenceNumber("patient123");
        patient.setCareContexts(careContexts);

        LinkCareContextsRequest linkCareContextsRequest = new LinkCareContextsRequest();
        linkCareContextsRequest.setRequestId(requestId);
        linkCareContextsRequest.setRequesterId("Demo_Atul_HIP");
        linkCareContextsRequest.setAbhaAddress("atul_kumar13@sbx");
        linkCareContextsRequest.setAuthMode(LinkCareContextsRequest.AuthModeEnum.MOBILE_OTP);
        linkCareContextsRequest.setPatient(patient);

        return linkApi.linkCareContexts(linkCareContextsRequest);
    }

    @PostMapping({"/test-wrapper/verify-otp"})
    public FacadeResponse verifyOtp(@RequestBody String otp) throws ApiException {
        LinkApi linkApi = new LinkApi();

        VerifyOTPRequest verifyOTPRequest = new VerifyOTPRequest();
        verifyOTPRequest.setLoginHint(VerifyOTPRequest.LoginHintEnum.HIPLINKING);
        verifyOTPRequest.setRequestId(requestId);
        System.out.println("otp:" + otp);
        verifyOTPRequest.setAuthCode(otp);
        return linkApi.verifyOTP(verifyOTPRequest);
    }

    @GetMapping({"/test-wrapper/link-status"})
    public String linkStatus() throws ApiException {
        LinkApi linkApi = new LinkApi();

        // To make this periodic poll, requestId can be persisted to facility's / HIP's database.
        RequestStatusResponse response = linkApi.linkStatusRequestIdGet(requestId);
        System.out.println(response.getStatus());
        if (response.getError() != null) {
            System.out.println("Error: " + response.getError().getMessage());
            return response.getError().getMessage();
        }
        return  response.getStatus();
    }
}
