/*
 * Swagger HIU Facade - OpenAPI 3.0
 * This is a set of interfaces based on the OpenAPI 3.0 specification for a wrapper client
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package in.nha.abdm.wrapper.client.api;

import in.nha.abdm.wrapper.client.invoker.ApiException;
import in.nha.abdm.wrapper.client.model.ConsentStatusResponse;
import in.nha.abdm.wrapper.client.model.FacadeResponse;
import in.nha.abdm.wrapper.client.model.InitConsentRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API tests for ConsentApi
 */
@Disabled
public class ConsentApiTest {

    private final ConsentApi api = new ConsentApi();

    /**
     * Get status of Consent request.
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void consentStatusRequestIdGetTest() throws ApiException {
        String requestId = null;
        ConsentStatusResponse response = api.consentStatusRequestIdGet(requestId);
        // TODO: test validations
    }

    /**
     * Initiates consent request
     *
     * Initiates consent request
     *
     * @throws ApiException if the Api call fails
     */
    @Test
    public void initConsentTest() throws ApiException {
        InitConsentRequest initConsentRequest = null;
        FacadeResponse response = api.initConsent(initConsentRequest);
        // TODO: test validations
    }

}
