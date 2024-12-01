# ConsentApi

All URIs are relative to *http://localhost:8082/v1*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**consentStatusRequestIdGet**](ConsentApi.md#consentStatusRequestIdGet) | **GET** /consent-status/{requestId} | Get status of Consent request. |
| [**initConsent**](ConsentApi.md#initConsent) | **POST** /consent-init | Initiates consent request |


<a id="consentStatusRequestIdGet"></a>
# **consentStatusRequestIdGet**
> ConsentStatusResponse consentStatusRequestIdGet(requestId)

Get status of Consent request.

### Example
```java
// Import classes:
import in.nha.abdm.wrapper.client.invoker.ApiClient;
import in.nha.abdm.wrapper.client.invoker.ApiException;
import in.nha.abdm.wrapper.client.invoker.Configuration;
import in.nha.abdm.wrapper.client.invoker.models.*;
import in.nha.abdm.wrapper.client.api.ConsentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    ConsentApi apiInstance = new ConsentApi(defaultClient);
    String requestId = "requestId_example"; // String | Request Id of the consent request.
    try {
      ConsentStatusResponse result = apiInstance.consentStatusRequestIdGet(requestId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ConsentApi#consentStatusRequestIdGet");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **requestId** | **String**| Request Id of the consent request. | |

### Return type

[**ConsentStatusResponse**](ConsentStatusResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **400** | Invalid request body supplied |  -  |
| **404** | Address not found |  -  |
| **422** | Validation exception |  -  |

<a id="initConsent"></a>
# **initConsent**
> FacadeResponse initConsent(initConsentRequest)

Initiates consent request

Initiates consent request

### Example
```java
// Import classes:
import in.nha.abdm.wrapper.client.invoker.ApiClient;
import in.nha.abdm.wrapper.client.invoker.ApiException;
import in.nha.abdm.wrapper.client.invoker.Configuration;
import in.nha.abdm.wrapper.client.invoker.models.*;
import in.nha.abdm.wrapper.client.api.ConsentApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    ConsentApi apiInstance = new ConsentApi(defaultClient);
    InitConsentRequest initConsentRequest = new InitConsentRequest(); // InitConsentRequest | Request body for initiate consent request
    try {
      FacadeResponse result = apiInstance.initConsent(initConsentRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling ConsentApi#initConsent");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **initConsentRequest** | [**InitConsentRequest**](InitConsentRequest.md)| Request body for initiate consent request | |

### Return type

[**FacadeResponse**](FacadeResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **202** | Request accepted |  -  |
| **400** | Invalid request body supplied |  -  |
| **404** | Address not found |  -  |
| **422** | Validation exception |  -  |

