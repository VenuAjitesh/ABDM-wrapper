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


package com.nha.abdm.wrapper.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nha.abdm.wrapper.client.invoker.JSON;

/**
 * VerifyOtpPostRequest
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-04-21T21:09:58.015918700+05:30[Asia/Calcutta]")
public class VerifyOtpPostRequest {
  public static final String SERIALIZED_NAME_LOGIN_HINT = "loginHint";
  @SerializedName(SERIALIZED_NAME_LOGIN_HINT)
  private String loginHint;

  public static final String SERIALIZED_NAME_REQUEST_ID = "requestId";
  @SerializedName(SERIALIZED_NAME_REQUEST_ID)
  private String requestId;

  public static final String SERIALIZED_NAME_AUTH_CODE = "authCode";
  @SerializedName(SERIALIZED_NAME_AUTH_CODE)
  private String authCode;

  public static final String SERIALIZED_NAME_LINK_REF_NUMBER = "linkRefNumber";
  @SerializedName(SERIALIZED_NAME_LINK_REF_NUMBER)
  private String linkRefNumber;

  public VerifyOtpPostRequest() {
  }

  public VerifyOtpPostRequest loginHint(String loginHint) {
    
    this.loginHint = loginHint;
    return this;
  }

   /**
   * Hint for the type of authentication being performed
   * @return loginHint
  **/
  @javax.annotation.Nonnull
  public String getLoginHint() {
    return loginHint;
  }


  public void setLoginHint(String loginHint) {
    this.loginHint = loginHint;
  }


  public VerifyOtpPostRequest requestId(String requestId) {
    
    this.requestId = requestId;
    return this;
  }

   /**
   * Unique identifier for the request
   * @return requestId
  **/
  @javax.annotation.Nullable
  public String getRequestId() {
    return requestId;
  }


  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }


  public VerifyOtpPostRequest authCode(String authCode) {
    
    this.authCode = authCode;
    return this;
  }

   /**
   * Authentication code to be verified
   * @return authCode
  **/
  @javax.annotation.Nonnull
  public String getAuthCode() {
    return authCode;
  }


  public void setAuthCode(String authCode) {
    this.authCode = authCode;
  }


  public VerifyOtpPostRequest linkRefNumber(String linkRefNumber) {
    
    this.linkRefNumber = linkRefNumber;
    return this;
  }

   /**
   * Unique id corresponding to the OTP request
   * @return linkRefNumber
  **/
  @javax.annotation.Nonnull
  public String getLinkRefNumber() {
    return linkRefNumber;
  }


  public void setLinkRefNumber(String linkRefNumber) {
    this.linkRefNumber = linkRefNumber;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    VerifyOtpPostRequest verifyOtpPostRequest = (VerifyOtpPostRequest) o;
    return Objects.equals(this.loginHint, verifyOtpPostRequest.loginHint) &&
        Objects.equals(this.requestId, verifyOtpPostRequest.requestId) &&
        Objects.equals(this.authCode, verifyOtpPostRequest.authCode) &&
        Objects.equals(this.linkRefNumber, verifyOtpPostRequest.linkRefNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(loginHint, requestId, authCode, linkRefNumber);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class VerifyOtpPostRequest {\n");
    sb.append("    loginHint: ").append(toIndentedString(loginHint)).append("\n");
    sb.append("    requestId: ").append(toIndentedString(requestId)).append("\n");
    sb.append("    authCode: ").append(toIndentedString(authCode)).append("\n");
    sb.append("    linkRefNumber: ").append(toIndentedString(linkRefNumber)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


  public static HashSet<String> openapiFields;
  public static HashSet<String> openapiRequiredFields;

  static {
    // a set of all properties/fields (JSON key names)
    openapiFields = new HashSet<String>();
    openapiFields.add("loginHint");
    openapiFields.add("requestId");
    openapiFields.add("authCode");
    openapiFields.add("linkRefNumber");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("loginHint");
    openapiRequiredFields.add("authCode");
    openapiRequiredFields.add("linkRefNumber");
  }

 /**
  * Validates the JSON Object and throws an exception if issues found
  *
  * @param jsonObj JSON Object
  * @throws IOException if the JSON Object is invalid with respect to VerifyOtpPostRequest
  */
  public static void validateJsonObject(JsonObject jsonObj) throws IOException {
      if (jsonObj == null) {
        if (!VerifyOtpPostRequest.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in VerifyOtpPostRequest is not found in the empty JSON string", VerifyOtpPostRequest.openapiRequiredFields.toString()));
        }
      }

      Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
      // check to see if the JSON string contains additional fields
      for (Entry<String, JsonElement> entry : entries) {
        if (!VerifyOtpPostRequest.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `VerifyOtpPostRequest` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : VerifyOtpPostRequest.openapiRequiredFields) {
        if (jsonObj.get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonObj.toString()));
        }
      }
      if (!jsonObj.get("loginHint").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `loginHint` to be a primitive type in the JSON string but got `%s`", jsonObj.get("loginHint").toString()));
      }
      if ((jsonObj.get("requestId") != null && !jsonObj.get("requestId").isJsonNull()) && !jsonObj.get("requestId").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `requestId` to be a primitive type in the JSON string but got `%s`", jsonObj.get("requestId").toString()));
      }
      if (!jsonObj.get("authCode").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `authCode` to be a primitive type in the JSON string but got `%s`", jsonObj.get("authCode").toString()));
      }
      if (!jsonObj.get("linkRefNumber").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `linkRefNumber` to be a primitive type in the JSON string but got `%s`", jsonObj.get("linkRefNumber").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!VerifyOtpPostRequest.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'VerifyOtpPostRequest' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<VerifyOtpPostRequest> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(VerifyOtpPostRequest.class));

       return (TypeAdapter<T>) new TypeAdapter<VerifyOtpPostRequest>() {
           @Override
           public void write(JsonWriter out, VerifyOtpPostRequest value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public VerifyOtpPostRequest read(JsonReader in) throws IOException {
             JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
             validateJsonObject(jsonObj);
             return thisAdapter.fromJsonTree(jsonObj);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of VerifyOtpPostRequest given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of VerifyOtpPostRequest
  * @throws IOException if the JSON string is invalid with respect to VerifyOtpPostRequest
  */
  public static VerifyOtpPostRequest fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, VerifyOtpPostRequest.class);
  }

 /**
  * Convert an instance of VerifyOtpPostRequest to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

