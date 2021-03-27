/**
 *
 */

package shield;

public class SupermarketClientImp implements SupermarketClient {
  /** The string representation of the base server endpoint (a HTTP address) */
  private String endpoint;
  private String name;
  private int phoneNum;
  private final String REG_NEW = "registered new";
  private final String ALR_REG = "already registered";
  public SupermarketClientImp(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public boolean registerSupermarket(String name, String postCode) {
    // construct the endpoint request
    String request = String.format("/registerSupermarket?business name=%s&postcode=%s", name, postCode);
    try {
      String response = ClientIO.doGETRequest ( endpoint + request);
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
    return true;
  }

  // **UPDATE**
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    return false;
  }

  @Override
  public boolean isRegistered() {
    return false;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getPostCode() {
    return null;
  }
}
