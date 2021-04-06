/**
 *
 */

package shield;

public class SupermarketClientImp implements SupermarketClient {
  /** The string representation of the base server endpoint (a HTTP address) */
  private String endpoint;
  private String name;
  private String postcode;
  private int phoneNum;
  private boolean isRegistered;
  private final String REG_NEW = "registered new";
  private final String ALR_REG = "already registered";

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
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {
    return false;
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



    return false;
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
