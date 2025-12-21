/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers;

public class FieldIdentifiers {

  public static final String GATEWAY_REQUEST_ID = "gatewayRequestId";
  public static final String CLIENT_REQUEST_ID = "clientRequestId";
  public static final String REQUEST_DETAILS = "requestDetails";
  public static final String STATUS = "status";
  public static final String ERROR = "error";
  public static final String ABHA_ADDRESS = "abhaAddress";
  public static final String CONSENTS = "consents";
  public static final String RESPONSE_DETAILS = "responseDetails";
  public static final String CONSENT_ID = "consentId";
  public static final String PRIVATE_KEY = "privateKey";
  public static final String TRANSACTION_ID = "transactionId";
  public static final String NONCE = "nonce";
  public static final String HIP_ID = "hipId";
  public static final String IS_DEFAULT = "isDefault";
  public static final String CREATED_AT = "createdAt";
  public static final String UPDATED_AT = "updatedAt";
  public static final String CARE_CONTEXTS = "careContexts";
  public static final String EXPIRY = "expiry";
  public static final String LINK_TOKEN = "linkToken";
  public static final String TIME_STAMP = "timestamp";
  public static final String ENTITY = "entity";
  public static final String ENTITY_TYPE = "entityType";
  public static final String LAST_UPDATED = "lastUpdated";
  public static final String CREATED_ON = "createdOn";
  public static final String MODULE = "module";
  public static final String LINK_TOKEN_REQUEST_ID = "linkTokenRequestId";

  // Patient table.
  public static final String NAME = "name";
  public static final String GENDER = "gender";
  public static final String DATE_OF_BIRTH = "dateOfBirth";
  public static final String PATIENT_REFERENCE = "patientReference";
  public static final String PATIENT_DISPLAY = "patientDisplay";
  public static final String PATIENT_MOBILE = "patientMobile";

  // Nested fields.
  public static final String LINK_RECORDS_REQUEST = "linkRecordsRequest";
  public static final String HIP_ON_INIT_RESPONSE = "hipOnInitResponse";
  public static final String HIP_ON_ADD_CARE_CONTEXT_RESPONSE = "hipOnAddCareContext";
  public static final String CONSENT_REQUEST_ID = "consentRequestId";
  public static final String CONSENT_ON_INIT_RESPONSE = "consentOnInitResponse";
  public static final String CONSENT_ON_STATUS_RESPONSE = "consentOnStatusResponse";
  public static final String CONSENT_ON_NOTIFY_RESPONSE = "consentOnNotifyResponse";
  public static final String HIP_ON_CONFIRM_RESPONSE = "hipOnConfirmResponse";
  public static final String HIP_NOTIFY_REQUEST = "HIPConsentNotification";
  public static final String HEALTH_INFORMATION_REQUEST = "HIPHealthInformationRequest";
  public static final String ENCRYPTED_HEALTH_INFORMATION = "encryptedHealthInformation";
  public static final String INIT_RESPONSE = "InitResponse";
  public static final String CONSENT_INIT_REQUEST = "consentInitRequest";
  public static final String DISCOVER_REQUEST = "DiscoverRequest";
  public static final String ON_DISCOVER_RESPONSE = "OnDiscoverResponse";
  public static final String LINK_REFERENCE_NUMBER = "linkRefNumber";
  public static final String SHARE_PROFILE_REQUEST = "profileShareRequest";
  public static final String SHARE_PROFILE_RESPONSE = "onShareProfileRequest";

  public static final String HIP_DEEP_LINKING = "HIP-deepLinking";
  public static final String HIP_INITIATED_LINKING = "HIP-hipInitiatedLinking";
  public static final String HIP_DISCOVERY = "HIP-discovery";
  public static final String HIP_SCAN_AND_SHARE = "HIP-scanAndShare";
  public static final String HIP_USER_LINKING = "HIP-userInitiatedLinking";
  public static final String HIP_CONSENT = "HIP-consent";
  public static final String HIP_DATA_TRANSFER = "HIP-dataTransfer";
  public static final String HIU_CONSENT = "HIU-consent";
  public static final String HIU_DATA_REQUEST = "HIU-dataRequest";

  // Table names
  public static final String TABLE_CONSENT_PATIENT = "consent-patient";
  public static final String TABLE_PATIENT = "patients";
  public static final String TABLE_LINK_TOKEN = "link-tokens";
}
