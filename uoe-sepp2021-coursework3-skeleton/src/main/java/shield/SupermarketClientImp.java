/**
 *
 */

package shield;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class SupermarketClientImp implements SupermarketClient {
  /** The string representation of the base server endpoint (a HTTP address) */
  private String endpoint;
  private String name;
  private String postcode;
  private int phoneNum;
  private boolean isRegistered;
  private final String RESPONSE_TRUE = "True" ;
  private final String RESPONSE_FALSE = "False";
  private final String RESPONSE_ERROR = "require individual_id, order_number, supermarket_business_name, and supermarket_postcode. The individual must be registered and the supermarket must be registered";
  private final String REG_NEW = "registered new";
  private final String ALR_REG = "already registered";
  private final List<String> VALID_STATUSES = Arrays.asList("packed", "dispatched", "delivered");
  private final String POSTCODE_REGEX_STRICT = "EH[0-9][0-9]_[0-9][A-Z][A-Z]";

  public SupermarketClientImp(String endpoint) {
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
  public boolean registerSupermarket(String name, String postCode) {
    //assert(Pattern.matches(POSTCODE_REGEX_STRICT, postCode)) : String.format("Postcode %s is the wrong format", postCode);
    // construct the endpoint request
    String request = String.format("/registerSupermarket?business_name=%s&postcode=%s", name, postCode);
    try {
      String response = ClientIO.doGETRequest(this.endpoint + request);
      boolean isValidResponse = (response.equals(REG_NEW) || response.equals(ALR_REG));
      if(!isValidResponse) {
        String errMsg = String.format("WARNING: Unexpected response for %s", request);
        System.err.println(errMsg);
        return false;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    this.name = name;
    this.postcode = postCode;
    this.isRegistered = true;
    return true;
  }

  // **UPDATE2** ADDED METHOD
  /**
   * Returns true if the operation occurred correctly.
   *
   * Note that there is a dedicated server endpoint for implementing this called
   * recordSupermarketOrder
   *
   * @param CHI CHI number of the shiedling individual associated with this order
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {
    String request = String.format("/recordSupermarketOrder?individual_id=%s" +
                                   "&order_number=%s" +
                                   "&supermarket_business_name=%s" +
                                   "&supermarket_postcode=%s",
                                   CHI, orderNumber, this.name, this.postcode);
    try {
      String response = ClientIO.doGETRequest(this.endpoint + request);
      System.out.println(response);
      boolean isValidResponse = response.equals(RESPONSE_TRUE)||response.equals(RESPONSE_ERROR);
      if(!isValidResponse){
        String errMsg = String.format("WARNING: Unexpected response for %s", request);
        System.err.println(errMsg);
        return false;
      }
      return response.equals(RESPONSE_TRUE);
    } catch(Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  // **UPDATE**
  /**
   * Returns true if the operation occurred correctl
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
      return false;
    }
    String request = String.format("/updateSupermarketOrderStatus?order_id=%s&newStatus=%s",orderNumber,status);
    try {
      String response = ClientIO.doGETRequest(this.endpoint + request);
      boolean isValidResponse = response.equals(RESPONSE_TRUE)||response.equals(RESPONSE_FALSE);
      if(!isValidResponse){
        String errMsg = String.format("WARNING: Unexpected response for %s", request);
        System.err.println(errMsg);
        return false;
      }
      return response.equals(RESPONSE_TRUE);
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
    return this.postcode;
  }
}
