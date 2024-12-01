/* (C) 2024 */
package in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables;

import in.nha.abdm.wrapper.v1.hip.hrp.database.mongo.tables.helpers.RequestStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "request-logs")
public class RequestLog {
  @Field("clientRequestId")
  public String clientRequestId;

  @Field("gatewayRequestId")
  public String gatewayRequestId;

  @Field("linkTokenRequestId")
  public String linkTokenRequestId;

  @Field("transactionId")
  public String transactionId;

  @Field("linkRefNumber")
  public String linkRefNumber;

  @Field("abhaAddress")
  public String abhaAddress;

  @Field("status")
  public RequestStatus status;

  @Field("error")
  public Object error;

  @Field("module")
  public String module;

  @Field("otp")
  public String otp;

  @Field("requestDetails")
  public HashMap<String, Object> requestDetails;

  @Field("responseDetails")
  public HashMap<String, Object> responseDetails;

  @Field("consentId")
  public String consentId;

  @Field("entityType")
  public String entityType;

  @Field("createdOn")
  public LocalDateTime createdOn;

  @Field("lastUpdated")
  public LocalDateTime lastUpdated;

  @Field("hipId")
  public String hipId;

  public RequestLog(
      String clientRequestId,
      String gatewayRequestId,
      String abhaAddress,
      String transactionId,
      RequestStatus status) {
    this.clientRequestId = clientRequestId;
    this.gatewayRequestId = gatewayRequestId;
    this.abhaAddress = abhaAddress;
    this.transactionId = transactionId;
    this.status = status;
    this.requestDetails = new HashMap<>();
  }

  public RequestLog(
      String hipId,
      String clientRequestId,
      String gatewayRequestId,
      String abhaAddress,
      String transactionId,
      RequestStatus status) {
    this.hipId = hipId;
    this.clientRequestId = clientRequestId;
    this.gatewayRequestId = gatewayRequestId;
    this.abhaAddress = abhaAddress;
    this.transactionId = transactionId;
    this.status = status;
    this.requestDetails = new HashMap<>();
  }

  public RequestLog() {}
}
