# DiscoveryApi

All URIs are relative to *http://localhost:8082/v1*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**requestOtpPost**](DiscoveryApi.md#requestOtpPost) | **POST** /request/otp | The Initiating of otp in discovery flow |
| [**verifyOtpPost**](DiscoveryApi.md#verifyOtpPost) | **POST** /verify/otp | The Verification of otp in discovery flow |


<a id="requestOtpPost"></a>
# **requestOtpPost**
> RequestStatusResponse requestOtpPost(requestOtpPostRequest)

The Initiating of otp in discovery flow

### Example
```java
// Import classes:
import com.nha.abdm.wrapper.client.invoker.ApiClient;
import com.nha.abdm.wrapper.client.invoker.ApiException;
import com.nha.abdm.wrapper.client.invoker.Configuration;
import com.nha.abdm.wrapper.client.invoker.models.*;
import com.nha.abdm.wrapper.client.api.DiscoveryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    DiscoveryApi apiInstance = new DiscoveryApi(defaultClient);
    RequestOtpPostRequest requestOtpPostRequest = new RequestOtpPostRequest(); // RequestOtpPostRequest | requesting for OTP
    try {
      RequestStatusResponse result = apiInstance.requestOtpPost(requestOtpPostRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DiscoveryApi#requestOtpPost");
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
| **requestOtpPostRequest** | [**RequestOtpPostRequest**](RequestOtpPostRequest.md)| requesting for OTP | |

### Return type

[**RequestStatusResponse**](RequestStatusResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **400** | Invalid request body supplied |  -  |
| **404** | Address not found |  -  |
| **422** | Validation exception |  -  |

<a id="verifyOtpPost"></a>
# **verifyOtpPost**
> RequestStatusResponse verifyOtpPost(verifyOtpPostRequest)

The Verification of otp in discovery flow

### Example
```java
// Import classes:
import com.nha.abdm.wrapper.client.invoker.ApiClient;
import com.nha.abdm.wrapper.client.invoker.ApiException;
import com.nha.abdm.wrapper.client.invoker.Configuration;
import com.nha.abdm.wrapper.client.invoker.models.*;
import com.nha.abdm.wrapper.client.api.DiscoveryApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    DiscoveryApi apiInstance = new DiscoveryApi(defaultClient);
    VerifyOtpPostRequest verifyOtpPostRequest = new VerifyOtpPostRequest(); // VerifyOtpPostRequest | Verifies OTP
    try {
      RequestStatusResponse result = apiInstance.verifyOtpPost(verifyOtpPostRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling DiscoveryApi#verifyOtpPost");
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
| **verifyOtpPostRequest** | [**VerifyOtpPostRequest**](VerifyOtpPostRequest.md)| Verifies OTP | |

### Return type

[**RequestStatusResponse**](RequestStatusResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **400** | Invalid request body supplied |  -  |
| **404** | Address not found |  -  |
| **422** | Validation exception |  -  |

