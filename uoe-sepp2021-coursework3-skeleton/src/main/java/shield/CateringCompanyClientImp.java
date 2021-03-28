/**
 *
 */

package shield;

public class CateringCompanyClientImp implements CateringCompanyClient {
  private final String REG_NEW = "registered new" ;
  private final String ALR_REG = "already registered" ;
  private String endpoint;
  private String name;
  private String postCode;
  private boolean isRegistered;

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
        String errMsg = "";
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
    return false;
  }

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
    return this.postCode;
  }
}
