package nha.abdm.hiu;

import in.nha.abdm.wrapper.client.api.ConsentApi;
import in.nha.abdm.wrapper.client.api.DataTransferApi;
import in.nha.abdm.wrapper.client.invoker.ApiException;
import in.nha.abdm.wrapper.client.model.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1")
public class HIUController {
    private static final String healthInformationRequestId = UUID.randomUUID().toString();
    private static final String consentRequestId = UUID.randomUUID().toString();

    @PostMapping({"/test-wrapper/consent-init"})
    public FacadeResponse initiateConsent() throws ApiException {
        InitConsentRequest initConsentRequest = new InitConsentRequest();
        initConsentRequest.setRequestId(consentRequestId);
        initConsentRequest.setTimestamp(DateTimeFormatter.ISO_INSTANT.format(Instant.now()));

        ConsentRequest consentRequest = new ConsentRequest();
        Purpose purpose = new Purpose();
        purpose.setCode("CAREMGT");
        purpose.setText("string");
        consentRequest.setPurpose(purpose);


        IdRequest idRequest = new IdRequest();
        idRequest.setId("atul_kumar13@sbx");
        consentRequest.setPatient(idRequest);


        IdRequest idRequest2 = new IdRequest();
        idRequest2.setId("Predator_HIP");
        consentRequest.setHiu(idRequest2);

        IdRequest idRequest3 = new IdRequest();
        idRequest3.setId("Atul_Demo_HIP");
        consentRequest.setHip(idRequest3);

        List<ConsentCareContext> careContexts = new ArrayList<>();
        ConsentCareContext careContext = new ConsentCareContext();
        careContext.setCareContextReference("care-context-reference15");
        careContext.setPatientReference("patient123");
        careContexts.add(careContext);
        consentRequest.setCareContexts(careContexts);

        ConsentRequester consentRequester = new ConsentRequester();
        consentRequester.setName("Some requester-2");
        ConsentRequestIdentifier consentRequestIdentifier = new ConsentRequestIdentifier();
        consentRequestIdentifier.setSystem("https://www.mciindia.org");
        consentRequestIdentifier.setType("REG_NO");
        consentRequestIdentifier.setValue("MH1001");
        consentRequester.setIdentifier(consentRequestIdentifier);

        consentRequest.setRequester(consentRequester);

        List<HiTypeEnum> hiTypes = new ArrayList<>();
        hiTypes.add(HiTypeEnum.OPCONSULTATION);

        consentRequest.setHiTypes(hiTypes);

        Permission permission = new Permission();
        permission.setAccessMode(Permission.AccessModeEnum.VIEW);

        DateRange dateRange = new DateRange();
        dateRange.setFrom("2021-09-25T12:52:34.925Z");
        dateRange.setTo("2024-02-04T12:52:34.925Z");
        permission.setDateRange(dateRange);

        permission.setDataEraseAt("2024-11-25T12:52:34.925Z");

        Frequency frequency = new Frequency();
        frequency.setUnit(Frequency.UnitEnum.HOUR);
        frequency.setValue(1);
        frequency.setRepeats(0);
        permission.setFrequency(frequency);

        consentRequest.setPermission(permission);

        initConsentRequest.setConsent(consentRequest);

        ConsentApi consentApi = new ConsentApi();
        return consentApi.initConsent(initConsentRequest);
    }

    @GetMapping({"/test-wrapper/consent-status"})
    public ConsentStatusResponse consentStatus() throws ApiException {

        ConsentApi consentApi = new ConsentApi();
        return consentApi.consentStatusRequestIdGet(consentRequestId);
    }
    @PostMapping({"/test-wrapper/health-information"})
    public FacadeResponse healthInformation() throws ApiException {
        DataTransferApi dataTransferApi = new DataTransferApi();
        HIUClientHealthInformationRequest hiuClientHealthInformationRequest=new HIUClientHealthInformationRequest();
        hiuClientHealthInformationRequest.setRequestId(healthInformationRequestId);
        hiuClientHealthInformationRequest.setConsentId("89dc356a-4e23-4c12-a32f-9ebab2311f10");
        return dataTransferApi.fetchHealthInformation(hiuClientHealthInformationRequest);
    }
    @GetMapping({"/test-wrapper/health-information-status"})
    public HealthInformationResponse healthInformationStatus() throws ApiException {
        DataTransferApi dataTransferApi = new DataTransferApi();
        return dataTransferApi.healthInformationStatusRequestIdGet(healthInformationRequestId);
    }
}
