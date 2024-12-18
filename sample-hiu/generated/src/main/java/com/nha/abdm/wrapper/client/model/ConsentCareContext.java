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


package in.nha.abdm.wrapper.client.model;

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

import in.nha.abdm.wrapper.client.invoker.JSON;

/**
 * ConsentCareContext
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2024-04-21T21:07:52.071456600+05:30[Asia/Calcutta]")
public class ConsentCareContext {
  public static final String SERIALIZED_NAME_PATIENT_REFERENCE = "patientReference";
  @SerializedName(SERIALIZED_NAME_PATIENT_REFERENCE)
  private String patientReference;

  public static final String SERIALIZED_NAME_CARE_CONTEXT_REFERENCE = "careContextReference";
  @SerializedName(SERIALIZED_NAME_CARE_CONTEXT_REFERENCE)
  private String careContextReference;

  public ConsentCareContext() {
  }

  public ConsentCareContext patientReference(String patientReference) {
    
    this.patientReference = patientReference;
    return this;
  }

   /**
   * Get patientReference
   * @return patientReference
  **/
  @javax.annotation.Nullable
  public String getPatientReference() {
    return patientReference;
  }


  public void setPatientReference(String patientReference) {
    this.patientReference = patientReference;
  }


  public ConsentCareContext careContextReference(String careContextReference) {
    
    this.careContextReference = careContextReference;
    return this;
  }

   /**
   * Get careContextReference
   * @return careContextReference
  **/
  @javax.annotation.Nullable
  public String getCareContextReference() {
    return careContextReference;
  }


  public void setCareContextReference(String careContextReference) {
    this.careContextReference = careContextReference;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConsentCareContext consentCareContext = (ConsentCareContext) o;
    return Objects.equals(this.patientReference, consentCareContext.patientReference) &&
        Objects.equals(this.careContextReference, consentCareContext.careContextReference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(patientReference, careContextReference);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConsentCareContext {\n");
    sb.append("    patientReference: ").append(toIndentedString(patientReference)).append("\n");
    sb.append("    careContextReference: ").append(toIndentedString(careContextReference)).append("\n");
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
    openapiFields.add("patientReference");
    openapiFields.add("careContextReference");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
  }

 /**
  * Validates the JSON Object and throws an exception if issues found
  *
  * @param jsonObj JSON Object
  * @throws IOException if the JSON Object is invalid with respect to ConsentCareContext
  */
  public static void validateJsonObject(JsonObject jsonObj) throws IOException {
      if (jsonObj == null) {
        if (!ConsentCareContext.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in ConsentCareContext is not found in the empty JSON string", ConsentCareContext.openapiRequiredFields.toString()));
        }
      }

      Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
      // check to see if the JSON string contains additional fields
      for (Entry<String, JsonElement> entry : entries) {
        if (!ConsentCareContext.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `ConsentCareContext` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
        }
      }
      if ((jsonObj.get("patientReference") != null && !jsonObj.get("patientReference").isJsonNull()) && !jsonObj.get("patientReference").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `patientReference` to be a primitive type in the JSON string but got `%s`", jsonObj.get("patientReference").toString()));
      }
      if ((jsonObj.get("careContextReference") != null && !jsonObj.get("careContextReference").isJsonNull()) && !jsonObj.get("careContextReference").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `careContextReference` to be a primitive type in the JSON string but got `%s`", jsonObj.get("careContextReference").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!ConsentCareContext.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'ConsentCareContext' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<ConsentCareContext> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(ConsentCareContext.class));

       return (TypeAdapter<T>) new TypeAdapter<ConsentCareContext>() {
           @Override
           public void write(JsonWriter out, ConsentCareContext value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public ConsentCareContext read(JsonReader in) throws IOException {
             JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
             validateJsonObject(jsonObj);
             return thisAdapter.fromJsonTree(jsonObj);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of ConsentCareContext given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of ConsentCareContext
  * @throws IOException if the JSON string is invalid with respect to ConsentCareContext
  */
  public static ConsentCareContext fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, ConsentCareContext.class);
  }

 /**
  * Convert an instance of ConsentCareContext to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

