# PatientsApi

All URIs are relative to *http://localhost:8082/v1*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**patientCareContextsPost**](PatientsApi.md#patientCareContextsPost) | **POST** /patient-care-contexts | Gets Care Contexts of the given patient id |
| [**patientDiscoverPost**](PatientsApi.md#patientDiscoverPost) | **POST** /patient-discover | Discover patient&#39;s accounts |
| [**shareProfilePost**](PatientsApi.md#shareProfilePost) | **POST** /share/profile | Sharing patient details with a facility via scan and share |
| [**smsNotifyPost**](PatientsApi.md#smsNotifyPost) | **POST** /sms/notify | Sending sms to patient via ABDM |
| [**upsertPatients**](PatientsApi.md#upsertPatients) | **PUT** /add-patients | Insert or Update a list of patients |


<a id="patientCareContextsPost"></a>
# **patientCareContextsPost**
> Patient patientCareContextsPost(careContextRequest)

Gets Care Contexts of the given patient id

### Example
```java
// Import classes:
import com.nha.abdm.wrapper.client.invoker.ApiClient;
import com.nha.abdm.wrapper.client.invoker.ApiException;
import com.nha.abdm.wrapper.client.invoker.Configuration;
import com.nha.abdm.wrapper.client.invoker.models.*;
import com.nha.abdm.wrapper.client.api.PatientsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    PatientsApi apiInstance = new PatientsApi(defaultClient);
    CareContextRequest careContextRequest = new CareContextRequest(); // CareContextRequest | 
    try {
      Patient result = apiInstance.patientCareContextsPost(careContextRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PatientsApi#patientCareContextsPost");
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
| **careContextRequest** | [**CareContextRequest**](CareContextRequest.md)|  | |

### Return type

[**Patient**](Patient.md)

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

<a id="patientDiscoverPost"></a>
# **patientDiscoverPost**
> Patient patientDiscoverPost(patientDiscoveryRequest)

Discover patient&#39;s accounts

Request for patient care context discover, made by CM for a specific HIP. It is expected that HIP will subsequently return either zero or one patient record with (potentially masked) associated care contexts   1. **At least one of the verified identifier matches**   2. **Name (fuzzy), gender matches**   3. **If YoB was given, age band(+-2) matches**   4. **If unverified identifiers were given, one of them matches**   5. **If more than one patient records would be found after aforementioned steps, then patient who matches most verified and unverified identifiers would be returned.**   6. **If there would be still more than one patients (after ranking) error would be returned**   7. **Intended HIP should be able to resolve and identify results returned in the subsequent link confirmation request via the specified transactionId**   8. **Intended HIP should store the discovery results with transactionId and care contexts discovered for subsequent link initiation** 

### Example
```java
// Import classes:
import com.nha.abdm.wrapper.client.invoker.ApiClient;
import com.nha.abdm.wrapper.client.invoker.ApiException;
import com.nha.abdm.wrapper.client.invoker.Configuration;
import com.nha.abdm.wrapper.client.invoker.models.*;
import com.nha.abdm.wrapper.client.api.PatientsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    PatientsApi apiInstance = new PatientsApi(defaultClient);
    PatientDiscoveryRequest patientDiscoveryRequest = new PatientDiscoveryRequest(); // PatientDiscoveryRequest | 
    try {
      Patient result = apiInstance.patientDiscoverPost(patientDiscoveryRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PatientsApi#patientDiscoverPost");
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
| **patientDiscoveryRequest** | [**PatientDiscoveryRequest**](PatientDiscoveryRequest.md)|  | |

### Return type

[**Patient**](Patient.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, application/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | OK |  -  |
| **400** | **Causes:**   * Empty verified identifiers.   * Format mismatch of any of attributes.     | type   | Format/Allowed Values|     | ------- | ----------------    |     | gender  | M/F/O/U |     | MOBILE  | valid mobile number with proper country code |  |  -  |
| **401** | **Causes:**   * Unauthorized request  |  -  |
| **500** | **Causes:**   * Downstream system(s) is down.   * Unhandled exceptions.  |  -  |

<a id="shareProfilePost"></a>
# **shareProfilePost**
> ProfileAcknowledgement shareProfilePost(shareProfileRequest)

Sharing patient details with a facility via scan and share

### Example
```java
// Import classes:
import com.nha.abdm.wrapper.client.invoker.ApiClient;
import com.nha.abdm.wrapper.client.invoker.ApiException;
import com.nha.abdm.wrapper.client.invoker.Configuration;
import com.nha.abdm.wrapper.client.invoker.models.*;
import com.nha.abdm.wrapper.client.api.PatientsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    PatientsApi apiInstance = new PatientsApi(defaultClient);
    ShareProfileRequest shareProfileRequest = new ShareProfileRequest(); // ShareProfileRequest | 
    try {
      ProfileAcknowledgement result = apiInstance.shareProfilePost(shareProfileRequest);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PatientsApi#shareProfilePost");
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
| **shareProfileRequest** | [**ShareProfileRequest**](ShareProfileRequest.md)|  | |

### Return type

[**ProfileAcknowledgement**](ProfileAcknowledgement.md)

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

<a id="smsNotifyPost"></a>
# **smsNotifyPost**
> FacadeResponse smsNotifyPost(smsNotify)

Sending sms to patient via ABDM

### Example
```java
// Import classes:
import com.nha.abdm.wrapper.client.invoker.ApiClient;
import com.nha.abdm.wrapper.client.invoker.ApiException;
import com.nha.abdm.wrapper.client.invoker.Configuration;
import com.nha.abdm.wrapper.client.invoker.models.*;
import com.nha.abdm.wrapper.client.api.PatientsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    PatientsApi apiInstance = new PatientsApi(defaultClient);
    SmsNotify smsNotify = new SmsNotify(); // SmsNotify | 
    try {
      FacadeResponse result = apiInstance.smsNotifyPost(smsNotify);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PatientsApi#smsNotifyPost");
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
| **smsNotify** | [**SmsNotify**](SmsNotify.md)|  | |

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
| **400** | Invalid request body supplied |  -  |
| **404** | Address not found |  -  |
| **422** | Validation exception |  -  |

<a id="upsertPatients"></a>
# **upsertPatients**
> FacadeResponse upsertPatients(patient)

Insert or Update a list of patients

Insert or Update a list of patients

### Example
```java
// Import classes:
import com.nha.abdm.wrapper.client.invoker.ApiClient;
import com.nha.abdm.wrapper.client.invoker.ApiException;
import com.nha.abdm.wrapper.client.invoker.Configuration;
import com.nha.abdm.wrapper.client.invoker.models.*;
import com.nha.abdm.wrapper.client.api.PatientsApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:8082/v1");

    PatientsApi apiInstance = new PatientsApi(defaultClient);
    List<Patient> patient = Arrays.asList(); // List<Patient> | Insert or update a list of patients in the wrapper database
    try {
      FacadeResponse result = apiInstance.upsertPatients(patient);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling PatientsApi#upsertPatients");
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
| **patient** | [**List&lt;Patient&gt;**](Patient.md)| Insert or update a list of patients in the wrapper database | |

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
| **400** | Invalid request body supplied |  -  |
| **404** | Address not found |  -  |
| **422** | Validation exception |  -  |

