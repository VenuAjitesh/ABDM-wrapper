/*
 * Swagger HIP Facade - OpenAPI 3.0
 * This is a set of interfaces based on the OpenAPI 3.0 specification for a wrapper client
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package com.nha.abdm.wrapper.client.api;

import com.nha.abdm.wrapper.client.invoker.ApiCallback;
import com.nha.abdm.wrapper.client.invoker.ApiClient;
import com.nha.abdm.wrapper.client.invoker.ApiException;
import com.nha.abdm.wrapper.client.invoker.ApiResponse;
import com.nha.abdm.wrapper.client.invoker.Configuration;
import com.nha.abdm.wrapper.client.invoker.Pair;
import com.nha.abdm.wrapper.client.invoker.ProgressRequestBody;
import com.nha.abdm.wrapper.client.invoker.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import com.nha.abdm.wrapper.client.model.RequestOtpPostRequest;
import com.nha.abdm.wrapper.client.model.RequestStatusResponse;
import com.nha.abdm.wrapper.client.model.VerifyOtpPostRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.GenericType;

public class DiscoveryApi {
    private ApiClient localVarApiClient;
    private int localHostIndex;
    private String localCustomBaseUrl;

    public DiscoveryApi() {
        this(Configuration.getDefaultApiClient());
    }

    public DiscoveryApi(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return localVarApiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public int getHostIndex() {
        return localHostIndex;
    }

    public void setHostIndex(int hostIndex) {
        this.localHostIndex = hostIndex;
    }

    public String getCustomBaseUrl() {
        return localCustomBaseUrl;
    }

    public void setCustomBaseUrl(String customBaseUrl) {
        this.localCustomBaseUrl = customBaseUrl;
    }

    /**
     * Build call for requestOtpPost
     * @param requestOtpPostRequest requesting for OTP (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Invalid request body supplied </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Address not found </td><td>  -  </td></tr>
        <tr><td> 422 </td><td> Validation exception </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call requestOtpPostCall(RequestOtpPostRequest requestOtpPostRequest, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = requestOtpPostRequest;

        // create path and map variables
        String localVarPath = "/request/otp";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            "application/json"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call requestOtpPostValidateBeforeCall(RequestOtpPostRequest requestOtpPostRequest, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'requestOtpPostRequest' is set
        if (requestOtpPostRequest == null) {
            throw new ApiException("Missing the required parameter 'requestOtpPostRequest' when calling requestOtpPost(Async)");
        }

        return requestOtpPostCall(requestOtpPostRequest, _callback);

    }

    /**
     * The Initiating of otp in discovery flow
     * 
     * @param requestOtpPostRequest requesting for OTP (required)
     * @return RequestStatusResponse
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Invalid request body supplied </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Address not found </td><td>  -  </td></tr>
        <tr><td> 422 </td><td> Validation exception </td><td>  -  </td></tr>
     </table>
     */
    public RequestStatusResponse requestOtpPost(RequestOtpPostRequest requestOtpPostRequest) throws ApiException {
        ApiResponse<RequestStatusResponse> localVarResp = requestOtpPostWithHttpInfo(requestOtpPostRequest);
        return localVarResp.getData();
    }

    /**
     * The Initiating of otp in discovery flow
     * 
     * @param requestOtpPostRequest requesting for OTP (required)
     * @return ApiResponse&lt;RequestStatusResponse&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Invalid request body supplied </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Address not found </td><td>  -  </td></tr>
        <tr><td> 422 </td><td> Validation exception </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<RequestStatusResponse> requestOtpPostWithHttpInfo(RequestOtpPostRequest requestOtpPostRequest) throws ApiException {
        okhttp3.Call localVarCall = requestOtpPostValidateBeforeCall(requestOtpPostRequest, null);
        Type localVarReturnType = new TypeToken<RequestStatusResponse>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * The Initiating of otp in discovery flow (asynchronously)
     * 
     * @param requestOtpPostRequest requesting for OTP (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Invalid request body supplied </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Address not found </td><td>  -  </td></tr>
        <tr><td> 422 </td><td> Validation exception </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call requestOtpPostAsync(RequestOtpPostRequest requestOtpPostRequest, final ApiCallback<RequestStatusResponse> _callback) throws ApiException {

        okhttp3.Call localVarCall = requestOtpPostValidateBeforeCall(requestOtpPostRequest, _callback);
        Type localVarReturnType = new TypeToken<RequestStatusResponse>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
    /**
     * Build call for verifyOtpPost
     * @param verifyOtpPostRequest Verifies OTP (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Invalid request body supplied </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Address not found </td><td>  -  </td></tr>
        <tr><td> 422 </td><td> Validation exception </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call verifyOtpPostCall(VerifyOtpPostRequest verifyOtpPostRequest, final ApiCallback _callback) throws ApiException {
        String basePath = null;
        // Operation Servers
        String[] localBasePaths = new String[] {  };

        // Determine Base Path to Use
        if (localCustomBaseUrl != null){
            basePath = localCustomBaseUrl;
        } else if ( localBasePaths.length > 0 ) {
            basePath = localBasePaths[localHostIndex];
        } else {
            basePath = null;
        }

        Object localVarPostBody = verifyOtpPostRequest;

        // create path and map variables
        String localVarPath = "/verify/otp";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();

        final String[] localVarAccepts = {
            "application/json"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            "application/json"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        if (localVarContentType != null) {
            localVarHeaderParams.put("Content-Type", localVarContentType);
        }

        String[] localVarAuthNames = new String[] {  };
        return localVarApiClient.buildCall(basePath, localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call verifyOtpPostValidateBeforeCall(VerifyOtpPostRequest verifyOtpPostRequest, final ApiCallback _callback) throws ApiException {
        // verify the required parameter 'verifyOtpPostRequest' is set
        if (verifyOtpPostRequest == null) {
            throw new ApiException("Missing the required parameter 'verifyOtpPostRequest' when calling verifyOtpPost(Async)");
        }

        return verifyOtpPostCall(verifyOtpPostRequest, _callback);

    }

    /**
     * The Verification of otp in discovery flow
     * 
     * @param verifyOtpPostRequest Verifies OTP (required)
     * @return RequestStatusResponse
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Invalid request body supplied </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Address not found </td><td>  -  </td></tr>
        <tr><td> 422 </td><td> Validation exception </td><td>  -  </td></tr>
     </table>
     */
    public RequestStatusResponse verifyOtpPost(VerifyOtpPostRequest verifyOtpPostRequest) throws ApiException {
        ApiResponse<RequestStatusResponse> localVarResp = verifyOtpPostWithHttpInfo(verifyOtpPostRequest);
        return localVarResp.getData();
    }

    /**
     * The Verification of otp in discovery flow
     * 
     * @param verifyOtpPostRequest Verifies OTP (required)
     * @return ApiResponse&lt;RequestStatusResponse&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Invalid request body supplied </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Address not found </td><td>  -  </td></tr>
        <tr><td> 422 </td><td> Validation exception </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<RequestStatusResponse> verifyOtpPostWithHttpInfo(VerifyOtpPostRequest verifyOtpPostRequest) throws ApiException {
        okhttp3.Call localVarCall = verifyOtpPostValidateBeforeCall(verifyOtpPostRequest, null);
        Type localVarReturnType = new TypeToken<RequestStatusResponse>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * The Verification of otp in discovery flow (asynchronously)
     * 
     * @param verifyOtpPostRequest Verifies OTP (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> OK </td><td>  -  </td></tr>
        <tr><td> 400 </td><td> Invalid request body supplied </td><td>  -  </td></tr>
        <tr><td> 404 </td><td> Address not found </td><td>  -  </td></tr>
        <tr><td> 422 </td><td> Validation exception </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call verifyOtpPostAsync(VerifyOtpPostRequest verifyOtpPostRequest, final ApiCallback<RequestStatusResponse> _callback) throws ApiException {

        okhttp3.Call localVarCall = verifyOtpPostValidateBeforeCall(verifyOtpPostRequest, _callback);
        Type localVarReturnType = new TypeToken<RequestStatusResponse>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
}
