/**
 *
 */

package shield;

import java.util.Arrays;
import java.util.List;

public class CateringCompanyClientImp implements CateringCompanyClient {
  private final String ORD_STS_RESP_TRUE = "TRUE" ;
  private final String ORD_STS_RESP_FALSE = "FALSE";
  private final String REG_NEW = "registered new" ;
  private final String ALR_REG = "already registered" ;
  private String endpoint;
  private String name;
  private String postCode;
  private boolean isRegistered;
  private final List<String> VALID_STATUSES = Arrays.asList("packed", "dispatched", "delivered");

  public CateringCompanyClientImp(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public boolean registerCateringCompany(String name, String postCode) {
    String request = String.format("/registerCateringCompany?business_name=%s&postcode=%s",name,postCode);
    try {
      String response = ClientIO.doGETRequest(this.endpoint + request);
      boolean isValidResponse = response.equals(REG_NEW)||response.equals(ALR_REG);
      if(!isValidResponse){
        String errMsg = String.format("WARNING: Unexpected response for %s", request);
        System.err.println(errMsg);
        return false;
      }
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    }
    this.name = name;
    this.postCode = postCode;
    this.isRegistered = true;
    return true;
  }

  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    boolean isValidStatus = VALID_STATUSES.contains(status);
    if(!isValidStatus){
      String errMsg = String.format("%s is not a valid status",status);
      System.err.println(errMsg);
    }
    String request = String.format("/updateOrderStatus?order id=%s&newStatus=%s",orderNumber,status);
    try {
      String response = ClientIO.doGETRequest(this.endpoint + request);
      boolean isValidResponse = response.equals(ORD_STS_RESP_TRUE)||response.equals(ORD_STS_RESP_FALSE);
      if(!isValidResponse){
        String errMsg = String.format("WARNING: Unexpected response for %s", request);
        System.err.println(errMsg);
        return false;
      }
      return response.equals(ORD_STS_RESP_TRUE);
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean isRegistered() {
    return this.isRegistered;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getPostCode() {
    return this.postCode;
  }
}
