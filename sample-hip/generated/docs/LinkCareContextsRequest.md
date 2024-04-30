

# LinkCareContextsRequest


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**requestId** | **String** |  |  [optional] |
|**requesterId** | **String** |  |  [optional] |
|**abhaAddress** | **String** |  |  [optional] |
|**authMode** | [**AuthModeEnum**](#AuthModeEnum) |  |  [optional] |
|**hiTypes** | [**List&lt;HiTypesEnum&gt;**](#List&lt;HiTypesEnum&gt;) |  |  [optional] |
|**patient** | [**PatientWithCareContext**](PatientWithCareContext.md) |  |  [optional] |



## Enum: AuthModeEnum

| Name | Value |
|---- | -----|
| DEMOGRAPHICS | &quot;DEMOGRAPHICS&quot; |
| MOBILE_OTP | &quot;MOBILE_OTP&quot; |
| AADHAAR_OTP | &quot;AADHAAR_OTP&quot; |



## Enum: List&lt;HiTypesEnum&gt;

| Name | Value |
|---- | -----|
| DIAGNOSTICREPORT | &quot;DiagnosticReport&quot; |
| DISCHARGESUMMARY | &quot;DischargeSummary&quot; |
| HEALTHDOCUMENTRECORD | &quot;HealthDocumentRecord&quot; |
| IMMUNIZATIONRECORD | &quot;ImmunizationRecord&quot; |
| OPCONSULTATION | &quot;OPConsultation&quot; |
| PRESCRIPTION | &quot;Prescription&quot; |
| WELLNESSRECORD | &quot;WellnessRecord&quot; |



