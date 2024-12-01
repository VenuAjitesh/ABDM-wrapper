package com.nha.abdm.hip;

import in.nha.abdm.wrapper.client.api.LinkApi;
import in.nha.abdm.wrapper.client.api.PatientsApi;
import in.nha.abdm.wrapper.client.invoker.ApiException;
import in.nha.abdm.wrapper.client.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping(path = "/v3")
public class PatientV3Controller {

    @Value("${filePath}")
    private String fhirFilePath;

    private static final Logger log = LogManager.getLogger(PatientV3Controller.class);
    private static final String requestId = "263ad643-ffb9-4c7d-b5bc-e099577e7e99";
    private static final List <String> hiTypes = List.of("DiagnosticReport","DischargeSummary","HealthDocumentRecord","ImmunizationRecord","OPConsultation","Prescription","WellnessRecord");
    private final Random random=new Random(hiTypes.size());
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
    public ProfileV3Acknowledgement ProfileAcknowledgement(@RequestBody ShareProfileV3Request shareProfileRequest){
         ProfileV3Acknowledgement profileAcknowledgement=new ProfileV3Acknowledgement();
        profileAcknowledgement.setStatus("SUCCESS");
        profileAcknowledgement.setAbhaAddress(shareProfileRequest.getPatient().getAbhaAddress());
        TokenProfile tokenProfile=new TokenProfile();
        tokenProfile.setTokenNumber(shareProfileRequest.getToken());
        tokenProfile.setExpiry("1800");
        tokenProfile.setContext(shareProfileRequest.getContext());
        profileAcknowledgement.setProfile(tokenProfile);
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
    public @ResponseBody Patient discoverPatient(@RequestBody PatientDiscoveryV3Request patientDiscoveryV3Request) {

        // TODO: Logic to discover patient in HIP database using abhaAddress, verifiedIdentifiers or unverifiedIdentifiers.

        // Use this hipId to route your discovery request.
        String hipId = patientDiscoveryV3Request.getHipId();

        // Placeholder to send dummy patient.
        Patient patient = new Patient();
        patient.setAbhaAddress(patientDiscoveryV3Request.getPatient().getId());
        patient.setName(patientDiscoveryV3Request.getPatient().getName());
        patient.setGender(Patient.GenderEnum.M);
        patient.setDateOfBirth("1986-10-13");
        patient.setPatientDisplay(patientDiscoveryV3Request.getPatient().getName());
        patient.setPatientReference(patientDiscoveryV3Request.getPatient().getId());
        CareContext careContext1 = new CareContext();
        careContext1.setReferenceNumber("Prescription/"+UUID.randomUUID().toString());
        careContext1.setDisplay("care-context-display41");
        careContext1.setHiType(hiTypes.get((int)(Math.random()*hiTypes.size())));
        CareContext careContext2 = new CareContext();
        careContext2.setReferenceNumber("care-context-reference42");
        careContext2.setDisplay("care-context-display42");
        careContext2.setHiType(hiTypes.get((int)(Math.random()*hiTypes.size())));
        List<CareContext> careContexts = new ArrayList<>();
        careContexts.add(careContext1);
        careContexts.add(careContext2);
        patient.setCareContexts(careContexts);
        patient.setHipId(patientDiscoveryV3Request.getHipId());

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
        patient.setHipId(careContextRequest.getHipId());
        CareContext careContext1 = new CareContext();
        careContext1.setReferenceNumber("Prescription/"+UUID.randomUUID().toString());
        careContext1.setDisplay("ABDM-WRAPPER-"+new Date());
        careContext1.setHiType((hiTypes.get((int)(Math.random()*hiTypes.size()))));
        CareContext careContext2 = new CareContext();
        careContext2.setReferenceNumber("OP/"+UUID.randomUUID().toString());
        careContext2.setDisplay("ABDM-WRAPPER-"+new Date());
        careContext2.setHiType((hiTypes.get((int)(Math.random()*hiTypes.size()))));
        List<CareContext> careContexts = new ArrayList<>();
        careContexts.add(careContext1);
        careContexts.add(careContext2);
        patient.setCareContexts(careContexts);
        return patient;
    }

    @PostMapping(value="/health-information")
    public @ResponseBody ResponseEntity<HealthInformationResponse> fetchHealthInformation(
            @RequestBody HealthInformationBundleRequest healthInformationBundleRequest) throws IOException {
        log.debug("healthInformationBundleRequest" + healthInformationBundleRequest);
        HealthInformationResponse healthInformationResponse=new HealthInformationResponse();
//        List<HealthInformationBundle> healthInformationBundles=new ArrayList<>();
//        for(ConsentCareContexts careContexts:healthInformationBundleRequest.getCareContextsWithPatientReferences()){
//            HealthInformationBundle healthInformationBundle=new HealthInformationBundle();
//            String randomFilePath = getRandomFileFromDirectory("/FHIR");
//            String bundle = new String(Files.readAllBytes(Paths.get(randomFilePath)));
//            healthInformationBundle.setBundleContent(bundle);
//            healthInformationBundle.setCareContextReference(careContexts.getCareContextReference());
//            healthInformationBundles.add(healthInformationBundle);
//        }
        String filePath = fhirFilePath;
        String bundle= new String(Files.readAllBytes(Paths.get(filePath)));
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
    public FacadeV3Response upsertPatients() throws ApiException {
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
        linkCareContextsRequest.setRequestId(UUID.randomUUID().toString());
        linkCareContextsRequest.setRequesterId("Demo_Atul_HIP");
        linkCareContextsRequest.setAbhaAddress("atul_kumar13@sbx");

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
        linkCareContextsRequest.setRequestId(UUID.randomUUID().toString());
        linkCareContextsRequest.setRequesterId("Demo_Atul_HIP");
        linkCareContextsRequest.setAbhaAddress("atul_kumar13@sbx");

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
    public static String getRandomFileFromDirectory(String directoryPath) throws IOException {
        try (Stream<Path> paths = Files.list(Paths.get(directoryPath))) {
            List<Path> fileList = paths
                    .filter(Files::isRegularFile) // Filter to get only regular files
                    .toList();

            if (fileList.isEmpty()) {
                throw new IOException("No files found in the directory: " + directoryPath);
            }

            // Pick a random file from the list
            Random random = new Random();
            Path randomFile = fileList.get(random.nextInt(fileList.size()));
            return randomFile.toString();
        }
    }

}
