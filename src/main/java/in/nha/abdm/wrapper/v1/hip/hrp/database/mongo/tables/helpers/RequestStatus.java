/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers;

public enum RequestStatus {
  PATIENT_NOT_FOUND("Patient not found or invalid abhaAddress"),
  INITIATING("Request is being initiated"),
  LINK_TOKEN_REQUEST_ACCEPTED(
      "Request for link token generation ACCEPTED by gateway, records will be linked automatically soon."),
  LINK_TOKEN_REQUEST_ERROR("Request for link token generation FAILED by gateway"),
  AUTH_INIT_ACCEPTED("HIP Initiated link auth init request accepted by gateway"),
  AUTH_INIT_ERROR("Error thrown by Gateway for HIP Initiated link auth init"),
  AUTH_ON_INIT_ERROR("Error thrown by Gateway for HIP Initiated link auth on init"),
  AUTH_CONFIRM_ACCEPTED("HIP Initiated link aut confirm request accepted by gateway"),
  AUTH_CONFIRM_ERROR("Error thrown by Gateway for HIP Initiated link auth confirm"),
  AUTH_ON_CONFIRM_ERROR("Error thrown by Gateway for HIP Initiated link auth on confirm"),
  ADD_CARE_CONTEXT_ACCEPTED("Add Care Context request accepted by gateway"),
  ADD_CARE_CONTEXT_ERROR("Error thrown by Gateway for HIP Initiated link add care context"),
  AUTH_ON_ADD_CARE_CONTEXT_ERROR(
      "Error thrown by Gateway for HIP Initiated link auth on add care context"),
  CARE_CONTEXT_LINKED("Care Context(s) were linked"),
  DEEP_LINKING_SMS_INITIATED("DeepLinking request has been accepted by gateway"),
  DEEP_LINKING_SMS_ERROR("Error thrown by Gateway for DeepLinking SMS request"),
  CARECONTEXT_NOTIFY_ERROR("Error thrown by Gateway on context/notify for updation of careContext"),
  CARECONTEXT_NOTIFY_ACCEPTED(
      "updation of careContext context/notify, has been ACCEPTED by Gateway"),
  USER_INIT_REQUEST_RECEIVED_BY_WRAPPER(
      "User initiated link request received by wrapper from gateway"),
  CONSENT_INIT_ACCEPTED("Consent init request accepted by gateway"),
  CONSENT_INIT_ERROR("Error thrown by Gateway for consent init"),
  CONSENT_ON_INIT_ERROR("Error thrown by Gateway for consent on init"),
  CONSENT_ON_INIT_RESPONSE_RECEIVED("Response received from gateway for consent on init"),
  CONSENT_STATUS_ACCEPTED("Consent status request accepted by gateway"),
  CONSENT_STATUS_ERROR("Error thrown by Gateway for consent status"),
  CONSENT_ON_STATUS_ERROR("Error thrown by Gateway for on consent status"),
  CONSENT_ON_STATUS_RESPONSE_RECEIVED("Response received from gateway for consent status"),
  CONSENT_HIU_NOTIFY_ERROR("Something went wrong while executing consent hiu notify"),
  CONSENT_ON_NOTIFY_RESPONSE_RECEIVED("Response received from gateway for hiu notify"),
  CONSENT_FETCH_ACCEPTED("Consent fetch request accepted by gateway"),
  CONSENT_FETCH_ERROR("Error thrown by Gateway for consent fetch"),
  CONSENT_NOTIFY_ERROR("None of the careContexts present for the requested HI-Type"),
  CONSENT_ON_FETCH_SUCCESS("Response received from gateway for consent fetch"),
  CONSENT_ON_FETCH_ERROR("Error thrown by Gateway for on consent fetch"),
  CONSENT_REVOKED("Consent has been revoked by user"),
  CONSENT_EXPIRED("Consent has expired"),
  CONSENT_DENIED("Consent has been denied by user"),
  HIP_ON_NOTIFY_SUCCESS("Data onNotify accepted by gateway"),
  HIP_ON_NOTIFY_ERROR("Error thrown by Gateway for on Notify"),
  HEALTH_INFORMATION_REQUEST_SUCCESS("Health Information request done by HIU accepted by gateway"),
  HEALTH_INFORMATION_REQUEST_ERROR("Error thrown by Gateway for request done by HIU"),
  HEALTH_INFORMATION_ON_REQUEST_SUCCESS(
      "Health Information onRequest done by HIP accepted by gateway"),
  HEALTH_INFORMATION_ON_REQUEST_ERROR("Error thrown by Gateway for onRequest done by HIP"),
  ENCRYPTED_HEALTH_INFORMATION_RECEIVED("Encrypted Health Information received by HIU from HIP"),
  ENCRYPTED_HEALTH_INFORMATION_ERROR(
      "Error while receiving encrypted Health Information by HIU from HIP"),
  DECRYPTION_ERROR("Unable to decrypt the data sent by HIP"),
  FETCHING_BUNDLE("Fetching bundle from HIP"),
  FETCHING_BUNDLE_ERROR("Error while fetching bundle from HIP"),
  FETCHING_BUNDLE_SUCCESS("Bundle fetched successfully from HIP"),
  ENCRYPTION_ERROR("Error while encrypting the data"),
  ENCRYPTION_SUCCESS("Data encrypted successfully"),
  DATA_TRANSFER_INITIATED("Data transfer initiated successfully"),
  DATA_TRANSFER_ERROR("Error while initiating data transfer"),
  DATA_TRANSFER_SUCCESS("Data transfer completed successfully");

  private String value;

  RequestStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
