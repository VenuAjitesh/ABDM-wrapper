/* (C) 2024 */
package in.nha.abdm.wrapper.v3.common.constants;

public class GatewayURL {
  // Profile Share
  public static final String PROFILE_SHARE_PATH = "/api/v3/hip/patient/share";

  // HIP Initiated Linking
  public static final String ON_GENERATE_LINK_TOKEN_PATH = "/api/v3/hip/token/on-generate-token";
  public static final String ON_ADD_CARE_CONTEXT_PATH = "/api/v3/link/on_carecontext";

  // HIP Context notify
  public static final String LINK_CONTEXT_ON_NOTIFY_PATH = "/api/v3/links/context/on-notify";

  // HIP Deep Linking
  public static final String DEEP_LINKING_ON_NOTIFY_PATH = "/api/v3/patients/sms/on-notify";

  // DISCOVER
  public static final String DISCOVER_PATH = "/api/v3/hip/patient/care-context/discover";
  public static final String INIT_LINKING_PATH = "/api/v3/hip/link/care-context/init";
  public static final String CONFIRM_LINKING_PATH = "/api/v3/hip/link/care-context/confirm";

  // HIP Data Transfer
  public static final String HIP_CONSENT_NOTIFY_PATH = "/api/v3/consent/request/hip/notify";
  public static final String HIP_HEALTH_INFORMATION_REQUEST_PATH =
      "/api/v3/hip/health-information/request";

  // HIU Consent
  public static final String CONSENT_ON_INIT_PATH = "/api/v3/hiu/consent/request/on-init";
  public static final String CONSENT_ON_STATUS_PATH = "/api/v3/hiu/consent/request/on-status";
  public static final String CONSENT_HIU_NOTIFY_PATH = "/api/v3/hiu/consent/request/notify";
  public static final String CONSENT_ON_FETCH_PATH = "/api/v3/hiu/consent/on-fetch";

  // HIU Data Transfer
  public static final String HIU_HEALTH_INFORMATION_ON_REQUEST_PATH =
      "/api/v3/hiu/health-information/on-request";
}
