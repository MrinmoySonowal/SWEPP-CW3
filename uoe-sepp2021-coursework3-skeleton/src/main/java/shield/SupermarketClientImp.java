/**
 *
 */

package shield;

public class SupermarketClientImp implements SupermarketClient {
  private String name;
  public SupermarketClientImp(String endpoint) {
  }

  @Override
  public boolean registerSupermarket(String name, String postCode) {
    return false;
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
