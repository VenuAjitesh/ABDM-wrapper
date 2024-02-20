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


package com.nha.abdm.wrapper.client.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
 * HealthInformationResponse
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-02-13T21:01:05.570301947Z[Etc/UTC]")
public class HealthInformationResponse {
  public static final String SERIALIZED_NAME_STATUS = "status";
  @SerializedName(SERIALIZED_NAME_STATUS)
  private String status;

  public static final String SERIALIZED_NAME_ERROR = "error";
  @SerializedName(SERIALIZED_NAME_ERROR)
  private String error;

  public static final String SERIALIZED_NAME_HTTP_STATUS_CODE = "httpStatusCode";
  @SerializedName(SERIALIZED_NAME_HTTP_STATUS_CODE)
  private String httpStatusCode;

  public static final String SERIALIZED_NAME_DECRYPTED_HEALTH_INFORMATION_ENTRIES = "decryptedHealthInformationEntries";
  @SerializedName(SERIALIZED_NAME_DECRYPTED_HEALTH_INFORMATION_ENTRIES)
  private List<String> decryptedHealthInformationEntries;

  public HealthInformationResponse() {
  }

  public HealthInformationResponse status(String status) {
    
    this.status = status;
    return this;
  }

   /**
   * Get status
   * @return status
  **/
  @javax.annotation.Nullable
  public String getStatus() {
    return status;
  }


  public void setStatus(String status) {
    this.status = status;
  }


  public HealthInformationResponse error(String error) {
    
    this.error = error;
    return this;
  }

   /**
   * Get error
   * @return error
  **/
  @javax.annotation.Nullable
  public String getError() {
    return error;
  }


  public void setError(String error) {
    this.error = error;
  }


  public HealthInformationResponse httpStatusCode(String httpStatusCode) {
    
    this.httpStatusCode = httpStatusCode;
    return this;
  }

   /**
   * Get httpStatusCode
   * @return httpStatusCode
  **/
  @javax.annotation.Nullable
  public String getHttpStatusCode() {
    return httpStatusCode;
  }


  public void setHttpStatusCode(String httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
  }


  public HealthInformationResponse decryptedHealthInformationEntries(List<String> decryptedHealthInformationEntries) {
    
    this.decryptedHealthInformationEntries = decryptedHealthInformationEntries;
    return this;
  }

  public HealthInformationResponse addDecryptedHealthInformationEntriesItem(String decryptedHealthInformationEntriesItem) {
    if (this.decryptedHealthInformationEntries == null) {
      this.decryptedHealthInformationEntries = new ArrayList<>();
    }
    this.decryptedHealthInformationEntries.add(decryptedHealthInformationEntriesItem);
    return this;
  }

   /**
   * Get decryptedHealthInformationEntries
   * @return decryptedHealthInformationEntries
  **/
  @javax.annotation.Nullable
  public List<String> getDecryptedHealthInformationEntries() {
    return decryptedHealthInformationEntries;
  }


  public void setDecryptedHealthInformationEntries(List<String> decryptedHealthInformationEntries) {
    this.decryptedHealthInformationEntries = decryptedHealthInformationEntries;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HealthInformationResponse healthInformationResponse = (HealthInformationResponse) o;
    return Objects.equals(this.status, healthInformationResponse.status) &&
        Objects.equals(this.error, healthInformationResponse.error) &&
        Objects.equals(this.httpStatusCode, healthInformationResponse.httpStatusCode) &&
        Objects.equals(this.decryptedHealthInformationEntries, healthInformationResponse.decryptedHealthInformationEntries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(status, error, httpStatusCode, decryptedHealthInformationEntries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HealthInformationResponse {\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
    sb.append("    httpStatusCode: ").append(toIndentedString(httpStatusCode)).append("\n");
    sb.append("    decryptedHealthInformationEntries: ").append(toIndentedString(decryptedHealthInformationEntries)).append("\n");
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
    openapiFields.add("status");
    openapiFields.add("error");
    openapiFields.add("httpStatusCode");
    openapiFields.add("decryptedHealthInformationEntries");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
  }

 /**
  * Validates the JSON Object and throws an exception if issues found
  *
  * @param jsonObj JSON Object
  * @throws IOException if the JSON Object is invalid with respect to HealthInformationResponse
  */
  public static void validateJsonObject(JsonObject jsonObj) throws IOException {
      if (jsonObj == null) {
        if (!HealthInformationResponse.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in HealthInformationResponse is not found in the empty JSON string", HealthInformationResponse.openapiRequiredFields.toString()));
        }
      }

      Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
      // check to see if the JSON string contains additional fields
      for (Entry<String, JsonElement> entry : entries) {
        if (!HealthInformationResponse.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `HealthInformationResponse` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
        }
      }
      if ((jsonObj.get("status") != null && !jsonObj.get("status").isJsonNull()) && !jsonObj.get("status").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `status` to be a primitive type in the JSON string but got `%s`", jsonObj.get("status").toString()));
      }
      if ((jsonObj.get("error") != null && !jsonObj.get("error").isJsonNull()) && !jsonObj.get("error").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `error` to be a primitive type in the JSON string but got `%s`", jsonObj.get("error").toString()));
      }
      if ((jsonObj.get("httpStatusCode") != null && !jsonObj.get("httpStatusCode").isJsonNull()) && !jsonObj.get("httpStatusCode").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `httpStatusCode` to be a primitive type in the JSON string but got `%s`", jsonObj.get("httpStatusCode").toString()));
      }
      // ensure the optional json data is an array if present
      if (jsonObj.get("decryptedHealthInformationEntries") != null && !jsonObj.get("decryptedHealthInformationEntries").isJsonArray()) {
        throw new IllegalArgumentException(String.format("Expected the field `decryptedHealthInformationEntries` to be an array in the JSON string but got `%s`", jsonObj.get("decryptedHealthInformationEntries").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!HealthInformationResponse.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'HealthInformationResponse' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<HealthInformationResponse> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(HealthInformationResponse.class));

       return (TypeAdapter<T>) new TypeAdapter<HealthInformationResponse>() {
           @Override
           public void write(JsonWriter out, HealthInformationResponse value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public HealthInformationResponse read(JsonReader in) throws IOException {
             JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
             validateJsonObject(jsonObj);
             return thisAdapter.fromJsonTree(jsonObj);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of HealthInformationResponse given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of HealthInformationResponse
  * @throws IOException if the JSON string is invalid with respect to HealthInformationResponse
  */
  public static HealthInformationResponse fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, HealthInformationResponse.class);
  }

 /**
  * Convert an instance of HealthInformationResponse to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}
