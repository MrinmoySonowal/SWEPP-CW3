/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.time.LocalDateTime;

// student-included imports:
import java.util.Dictionary;
import java.util.List;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

  private String endpoint;
  private String chiNum;
  private boolean isRegistered;
  private Dictionary<Integer, Integer> boxOrderIDs;
  private int closestCatererID;
  //private Location address;
  private boolean isLoggedIn;
  private boolean isCaterer;

  private final String REG_NEW = "registered new";
  private final String ALR_REG = "already registered";

  // internal field only used for transmission purposes
  final class MessagingFoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    transient List<String> contents;
    String delivered_by;
    String diet;
    String id;
    String name;
  }

  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   * Returns true if the operation occurred correctly
   *
   * @param CHI CHI number of the shielding individual
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean registerShieldingIndividual(String CHI) {
    // constructing endpoint request:
    String request = String.format("/registerShieldingIndividual?CHI=%s", chiNum);
    try {
      // perform request:
      String response = ClientIO.doGETRequest(endpoint + request);
      boolean isValidResponse = (response.equals(REG_NEW) || response.equals(ALR_REG));
      if (!isValidResponse) {
        String errMsg = String.format("WARNING: Unexpected response for %s", request);
        System.err.println(errMsg);
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    this.isRegistered = true;
    this.chiNum = CHI;
    return true;
  }

  // **UPDATE** javadoc comment fix
  /**
   * Returns collection of food box ids if the operation occurred correctly
   *
   * @param dietaryPreference dietary preference
   * @return collection of food box ids
   */
  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=none";

    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();

    List<String> boxIds = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        boxIds.add(b.id);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return boxIds;
  }

  @Override
  public boolean placeOrder(LocalDateTime deliveryDateTime) {
    return false;
  }

  @Override
  public boolean editOrder(int orderNumber) {
    return false;
  }

  @Override
  public boolean cancelOrder(int orderNumber) {
    return false;
  }

  @Override
  public boolean requestOrderStatus(int orderNumber) {
    return false;
  }

  // **UPDATE**
  @Override
  public Collection<String> getCateringCompanies() {
    return null;
  }

  // **UPDATE**
  @Override
  public float getDistance(String postCode1, String postCode2) {
    return 0;
  }

  /**
   * Returns if the individual using the client is registered with the server
   *
   * @return true if the individual using the client is registered with the server
   */
  @Override
  public boolean isRegistered() {
    return this.isRegistered;
  }

  /**
   * Returns the CHI number of the shiedling individual
   *
   * @return CHI number of the shiedling individual
   */
  @Override
  public String getCHI() {
    return this.chiNum;
  }

  @Override
  public int getFoodBoxNumber() {
    return 0;
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    return null;
  }

  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {
    return 0;
  }

  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {
    return null;
  }

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    return null;
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    return 0;
  }

  @Override
  public boolean pickFoodBox(int foodBoxId) {
    return false;
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    return false;
  }

  @Override
  public Collection<Integer> getOrderNumbers() {
    return null;
  }

  @Override
  public String getStatusForOrder(int orderNumber) {
    return null;
  }

  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    return null;
  }

  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    return null;
  }

  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    return 0;
  }

  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {
    return false;
  }

  @Override
  public LocalDateTime getDeliveryTimeForOrder(int orderNumber) {
    return null;
  }

  // **UPDATE**
  @Override
  public String getClosestCateringCompany() {
    return null;
  }
}
