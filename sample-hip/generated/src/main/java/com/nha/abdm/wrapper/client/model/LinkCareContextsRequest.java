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
import com.nha.abdm.wrapper.client.model.PatientWithCareContext;
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
 * LinkCareContextsRequest
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-01-24T09:50:26.426989326Z[Etc/UTC]")
public class LinkCareContextsRequest {
  public static final String SERIALIZED_NAME_REQUEST_ID = "requestId";
  @SerializedName(SERIALIZED_NAME_REQUEST_ID)
  private String requestId;

  public static final String SERIALIZED_NAME_REQUESTER_ID = "requesterId";
  @SerializedName(SERIALIZED_NAME_REQUESTER_ID)
  private String requesterId;

  public static final String SERIALIZED_NAME_ABHA_ADDRESS = "abhaAddress";
  @SerializedName(SERIALIZED_NAME_ABHA_ADDRESS)
  private String abhaAddress;

  /**
   * Gets or Sets authMode
   */
  @JsonAdapter(AuthModeEnum.Adapter.class)
  public enum AuthModeEnum {
    DEMOGRAPHICS("DEMOGRAPHICS"),
    
    MOBILE_OTP("MOBILE_OTP"),
    
    AADHAAR_OTP("AADHAAR_OTP");

    private String value;

    AuthModeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static AuthModeEnum fromValue(String value) {
      for (AuthModeEnum b : AuthModeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

    public static class Adapter extends TypeAdapter<AuthModeEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final AuthModeEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public AuthModeEnum read(final JsonReader jsonReader) throws IOException {
        String value =  jsonReader.nextString();
        return AuthModeEnum.fromValue(value);
      }
    }
  }

  public static final String SERIALIZED_NAME_AUTH_MODE = "authMode";
  @SerializedName(SERIALIZED_NAME_AUTH_MODE)
  private AuthModeEnum authMode;

  public static final String SERIALIZED_NAME_PATIENT = "patient";
  @SerializedName(SERIALIZED_NAME_PATIENT)
  private PatientWithCareContext patient;

  public LinkCareContextsRequest() {
  }

  public LinkCareContextsRequest requestId(String requestId) {
    
    this.requestId = requestId;
    return this;
  }

   /**
   * Get requestId
   * @return requestId
  **/
  @javax.annotation.Nullable
  public String getRequestId() {
    return requestId;
  }


  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }


  public LinkCareContextsRequest requesterId(String requesterId) {
    
    this.requesterId = requesterId;
    return this;
  }

   /**
   * Get requesterId
   * @return requesterId
  **/
  @javax.annotation.Nullable
  public String getRequesterId() {
    return requesterId;
  }


  public void setRequesterId(String requesterId) {
    this.requesterId = requesterId;
  }


  public LinkCareContextsRequest abhaAddress(String abhaAddress) {
    
    this.abhaAddress = abhaAddress;
    return this;
  }

   /**
   * Get abhaAddress
   * @return abhaAddress
  **/
  @javax.annotation.Nullable
  public String getAbhaAddress() {
    return abhaAddress;
  }


  public void setAbhaAddress(String abhaAddress) {
    this.abhaAddress = abhaAddress;
  }


  public LinkCareContextsRequest authMode(AuthModeEnum authMode) {
    
    this.authMode = authMode;
    return this;
  }

   /**
   * Get authMode
   * @return authMode
  **/
  @javax.annotation.Nullable
  public AuthModeEnum getAuthMode() {
    return authMode;
  }


  public void setAuthMode(AuthModeEnum authMode) {
    this.authMode = authMode;
  }


  public LinkCareContextsRequest patient(PatientWithCareContext patient) {
    
    this.patient = patient;
    return this;
  }

   /**
   * Get patient
   * @return patient
  **/
  @javax.annotation.Nullable
  public PatientWithCareContext getPatient() {
    return patient;
  }


  public void setPatient(PatientWithCareContext patient) {
    this.patient = patient;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinkCareContextsRequest linkCareContextsRequest = (LinkCareContextsRequest) o;
    return Objects.equals(this.requestId, linkCareContextsRequest.requestId) &&
        Objects.equals(this.requesterId, linkCareContextsRequest.requesterId) &&
        Objects.equals(this.abhaAddress, linkCareContextsRequest.abhaAddress) &&
        Objects.equals(this.authMode, linkCareContextsRequest.authMode) &&
        Objects.equals(this.patient, linkCareContextsRequest.patient);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, requesterId, abhaAddress, authMode, patient);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinkCareContextsRequest {\n");
    sb.append("    requestId: ").append(toIndentedString(requestId)).append("\n");
    sb.append("    requesterId: ").append(toIndentedString(requesterId)).append("\n");
    sb.append("    abhaAddress: ").append(toIndentedString(abhaAddress)).append("\n");
    sb.append("    authMode: ").append(toIndentedString(authMode)).append("\n");
    sb.append("    patient: ").append(toIndentedString(patient)).append("\n");
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
    openapiFields.add("requestId");
    openapiFields.add("requesterId");
    openapiFields.add("abhaAddress");
    openapiFields.add("authMode");
    openapiFields.add("patient");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
  }

 /**
  * Validates the JSON Object and throws an exception if issues found
  *
  * @param jsonObj JSON Object
  * @throws IOException if the JSON Object is invalid with respect to LinkCareContextsRequest
  */
  public static void validateJsonObject(JsonObject jsonObj) throws IOException {
      if (jsonObj == null) {
        if (!LinkCareContextsRequest.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in LinkCareContextsRequest is not found in the empty JSON string", LinkCareContextsRequest.openapiRequiredFields.toString()));
        }
      }

      Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
      // check to see if the JSON string contains additional fields
      for (Entry<String, JsonElement> entry : entries) {
        if (!LinkCareContextsRequest.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `LinkCareContextsRequest` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
        }
      }
      if ((jsonObj.get("requestId") != null && !jsonObj.get("requestId").isJsonNull()) && !jsonObj.get("requestId").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `requestId` to be a primitive type in the JSON string but got `%s`", jsonObj.get("requestId").toString()));
      }
      if ((jsonObj.get("requesterId") != null && !jsonObj.get("requesterId").isJsonNull()) && !jsonObj.get("requesterId").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `requesterId` to be a primitive type in the JSON string but got `%s`", jsonObj.get("requesterId").toString()));
      }
      if ((jsonObj.get("abhaAddress") != null && !jsonObj.get("abhaAddress").isJsonNull()) && !jsonObj.get("abhaAddress").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `abhaAddress` to be a primitive type in the JSON string but got `%s`", jsonObj.get("abhaAddress").toString()));
      }
      if ((jsonObj.get("authMode") != null && !jsonObj.get("authMode").isJsonNull()) && !jsonObj.get("authMode").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `authMode` to be a primitive type in the JSON string but got `%s`", jsonObj.get("authMode").toString()));
      }
      // validate the optional field `patient`
      if (jsonObj.get("patient") != null && !jsonObj.get("patient").isJsonNull()) {
        PatientWithCareContext.validateJsonObject(jsonObj.getAsJsonObject("patient"));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!LinkCareContextsRequest.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'LinkCareContextsRequest' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<LinkCareContextsRequest> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(LinkCareContextsRequest.class));

       return (TypeAdapter<T>) new TypeAdapter<LinkCareContextsRequest>() {
           @Override
           public void write(JsonWriter out, LinkCareContextsRequest value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public LinkCareContextsRequest read(JsonReader in) throws IOException {
             JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
             validateJsonObject(jsonObj);
             return thisAdapter.fromJsonTree(jsonObj);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of LinkCareContextsRequest given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of LinkCareContextsRequest
  * @throws IOException if the JSON string is invalid with respect to LinkCareContextsRequest
  */
  public static LinkCareContextsRequest fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, LinkCareContextsRequest.class);
  }

 /**
  * Convert an instance of LinkCareContextsRequest to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

