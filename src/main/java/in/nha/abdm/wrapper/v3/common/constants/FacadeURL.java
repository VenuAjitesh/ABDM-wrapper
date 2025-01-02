/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.constants;

public class FacadeURL {

  // HIP Initiated Linking
  public static final String HIP_LINK_STATUS_PATH = "/link-status/{requestId}";
  public static final String HIP_LINK_CARE_CONTEXT_PATH = "/link-carecontexts";
  public static final String ADD_PATIENT_PATH = "/add-patients";
  public static final String SMS_NOTIFY_PATH = "/sms/notify";
  // HIU Consent
  public static final String HIU_CONSENT_INIT_PATH = "/consent-init";
  public static final String HIU_CONSENT_STATUS_PATH = "/consent-status/{requestId}";

  // HIU Data Transfer
  public static final String HIU_V3_HEALTH_INFORMATION_PATH = "/v3/health-information";
  public static final String HIU_FETCH_RECORDS_PATH = "/fetch-records";
  public static final String HIU_FETCH_RECORDS_STATUS_PATH = "/status/{requestId}";

  // Patient
  public static final String PATIENT_V3_PATH = "/v3/patient";
  public static final String PATIENT_ID_PATH = "/{patientId}";
}
