# DataTransferApi

All URIs are relative to *http://localhost:8082/v1*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**fetchHealthInformation**](DataTransferApi.md#fetchHealthInformation) | **POST** /health-information/fetch-records | Submits a request to fetch health information |
| [**healthInformationStatusRequestIdGet**](DataTransferApi.md#healthInformationStatusRequestIdGet) | **GET** /health-information/status/{requestId} | Get status of Health Information request. |


<a id="fetchHealthInformation"></a>
# **fetchHealthInformation**
> FacadeResponse fetchHealthInformation(hiUClientHealthInformationRequest)

Submits a request to fetch health information

Submits a request to fetch health information

### Example
```java
// Import classes:
import in.nha.abdm.wrapper.client.invoker.ApiClient;
import in.nha.abdm.wrapper.client.invoker.ApiException;
import in.nha.abdm.wrapper.client.invoker.Configuration;
import in.nha.abdm.wrapper.client.invoker.models.*;
import in.nha.abdm.wrapper.client.api.DataTransferApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    DataTransferApi apiInstance = new DataTransferApi(defaultClient);
    HIUClientHealthInformationRequest hiUClientHealthInformationRequest = new HIUClientHealthInformationRequest(); // HIUClientHealthInformationRequest | 
    try {
      FacadeResponse result = apiInstance.fetchHealthInformation(hiUClientHealthInformationRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DataTransferApi#fetchHealthInformation");
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
| **hiUClientHealthInformationRequest** | [**HIUClientHealthInformationRequest**](HIUClientHealthInformationRequest.md)|  | |

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
| **202** | Request Accepted |  -  |
| **400** | Invalid request body supplied |  -  |
| **404** | Address not found |  -  |
| **422** | Validation exception |  -  |

<a id="healthInformationStatusRequestIdGet"></a>
# **healthInformationStatusRequestIdGet**
> HealthInformationResponse healthInformationStatusRequestIdGet(requestId)

Get status of Health Information request.

### Example
```java
// Import classes:
import in.nha.abdm.wrapper.client.invoker.ApiClient;
import in.nha.abdm.wrapper.client.invoker.ApiException;
import in.nha.abdm.wrapper.client.invoker.Configuration;
import in.nha.abdm.wrapper.client.invoker.models.*;
import in.nha.abdm.wrapper.client.api.DataTransferApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    DataTransferApi apiInstance = new DataTransferApi(defaultClient);
    String requestId = "requestId_example"; // String | Request Id of the health information request.
    try {
      HealthInformationResponse result = apiInstance.healthInformationStatusRequestIdGet(requestId);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DataTransferApi#healthInformationStatusRequestIdGet");
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
| **requestId** | **String**| Request Id of the health information request. | |

### Return type

[**HealthInformationResponse**](HealthInformationResponse.md)

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

