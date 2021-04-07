/**
 *
 */

package shield;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

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
  private final String POSTCODE_REGEX = "EH[0-9][0-9]_[0-9][A-Z][A-Z]";

  public CateringCompanyClientImp(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * Returns true if the operation occurred correctly
   *
   * @param name name of the business
   * @param postCode post code of the business
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean registerCateringCompany(String name, String postCode) {
    assert (Pattern.matches(POSTCODE_REGEX, postCode)):String.format("Postcode %s is the wrong format", postCode);
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

  /**
   * Returns true if the operation occurred correctly
   *
   * @param orderNumber the order number
   * @param status status of the order for the requested number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    boolean isValidStatus = VALID_STATUSES.contains(status);
    if(!isValidStatus){
      String errMsg = String.format("%s is not a valid status",status);
      System.err.println(errMsg);
    }
    String request = String.format("/updateOrderStatus?order_id=%s&newStatus=%s",orderNumber,status);
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
