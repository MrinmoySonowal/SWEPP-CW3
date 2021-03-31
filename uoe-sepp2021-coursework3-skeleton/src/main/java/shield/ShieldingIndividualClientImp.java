/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.time.LocalDateTime;

// student-included imports:
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

  private final String REG_NEW = "registered new";
  private final String ALR_REG = "already registered";
  private final String ORDER_PLACED = "0";
  private final String ORDER_PACKED = "1";
  private final String ORDER_DISPATCHED = "2";
  private final String ORDER_DELIVERED = "3";
  private final String ORDER_CANCELLED = "4";
  private final String ORDER_NOT_FOUND = "-1";

  // internal field only used for transmission purposes
  final class MyMessagingFoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    //List<HashMap<String, String>> contents;
    List<BoxItem> contents;
    String delivered_by;
    String diet;
    String id;
    String name;
  }

  private String endpoint;
  private String chiNum;
  private boolean isRegistered;
  public Dictionary<Integer, FoodBoxOrder> ordersDict = new Hashtable<>();
  private List<MyMessagingFoodBox> defaultFoodBoxes;
  private String dietaryPrefrence;
  private int closestCatererID;
  //private Location address;
  private boolean isLoggedIn;
  private boolean isCaterer;
  private FoodBoxOrder pickedFoodBox;

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
      List<String> validResponses = Arrays.asList(REG_NEW, ALR_REG);
      boolean isValidResponse = validResponses.contains(response);
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

  private List<MyMessagingFoodBox> getDefaultFoodBoxes(String dietaryPrefrence) {
    if (this.defaultFoodBoxes == null) {
      this.defaultFoodBoxes = getDefaultFoodBoxesFromServer(dietaryPrefrence);
      return this.defaultFoodBoxes;
    } else {
      return this.defaultFoodBoxes;
    }
  }

  private List<MyMessagingFoodBox> getDefaultFoodBoxesFromServer(String dietaryPrefrence) {
    // construct the endpoint request:
      String request = String.format("/showFoodBox?orderOption=catering&dietaryPreference=%s", dietaryPrefrence);
      // setup the response recepient:
      List<MyMessagingFoodBox> responseBoxes = new ArrayList<>();
      try {
        // perform request:
        String response = ClientIO.doGETRequest(endpoint + request);
        // unmarshal response:
        Type listType = new TypeToken<List<MyMessagingFoodBox>>() {} .getType();
        responseBoxes = new Gson().fromJson(response, listType);
      } catch (IOException e) {
        e.printStackTrace();
      }

      return responseBoxes;
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
    String request = String.format("/showFoodBox?orderOption=catering&dietaryPreference=%s", dietaryPreference);

    // setup the response recepient
    List<MyMessagingFoodBox> responseBoxes = new ArrayList<MyMessagingFoodBox>();

    List<String> boxIds = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MyMessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      //this.defaultFoodBoxes = responseBoxes;
      //System.out.println((responseBoxes.get(0).contents.get(0).name));

      // gather required fields
      for (MyMessagingFoodBox b : responseBoxes) {
        boxIds.add(b.id);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return boxIds;
  }

  /**
   * Returns true if the operation occurred correctly
   *
   * @param deliveryDateTime the requested delivery date and time
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean placeOrder(LocalDateTime deliveryDateTime) { // will not use LocalDateTime
    String request = String.format("/placeOrder?individual id=%s", this.chiNum);

    // form order data using the pickedFoodBox order


    // @TODO when order is placed, add this new order (FoodBox obj) to the dictionary 'orders'.
    int orderID = 0; // ### CHANGE ###
    ordersDict.put(orderID, this.pickedFoodBox);


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

  /** ## ## ## ## ## ##
   * Returns true if the operation occurred correctly
   *
   * @param orderID the order number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean requestOrderStatus(int orderID) {
    // constructing endpoint request:
    String request = String.format("/requestStatus?order id=%s", orderID);
    try {
      // perform request:
      String response = ClientIO.doGETRequest(endpoint + request);
      List<String> validStatuses = Arrays.asList(ORDER_PLACED, ORDER_PACKED, ORDER_DISPATCHED,
                                                 ORDER_DELIVERED, ORDER_CANCELLED, ORDER_NOT_FOUND);
      boolean isValidResponse = validStatuses.contains(response);
      if (!isValidResponse) {
        String errMsg = String.format("WARNING: Unexpected response for %s", request);
        System.err.println(errMsg);
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }

    /* # !! UPDATE FOOD BOX CLASS STATUS !! FIGURE OUT WAY TO DO THIS. !! # */

    return true;
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

  /**
   * Returns the number of items in this specific food box
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return number of items in the food box
   */
  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {
    //TODO what does "as last returned from the server" mean?
    List<MyMessagingFoodBox> foodBoxes = getDefaultFoodBoxes("none");
    int numOfItems = 0;
    for (MyMessagingFoodBox b : foodBoxes) {
      if (Integer.parseInt(b.id) == foodBoxId) {
        numOfItems = b.contents.size();
        break;
      }
    }
    return numOfItems;
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

  /**
   * Returns true if the requested foodbox was picked
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return true if the requested foodbox was picked
   */
  @Override
  public boolean pickFoodBox(int foodBoxId) {                                       // ### ??? ###
    String foodBoxIdStr = String.valueOf(foodBoxId);
    Collection<String> foodBoxIdArr = showFoodBoxes(this.dietaryPrefrence);
    boolean isFoodBoxExist = foodBoxIdArr.contains(foodBoxIdStr);

    List<MyMessagingFoodBox> defaultFoodBoxes = getDefaultFoodBoxes(this.dietaryPrefrence);

    if (isFoodBoxExist) {
      for (MyMessagingFoodBox b: defaultFoodBoxes) {
        if (Integer.parseInt(b.id) == foodBoxId) {
          this.pickedFoodBox = new FoodBoxOrder();
          this.pickedFoodBox.setDeliveryService(b.delivered_by);
          this.pickedFoodBox.setDietType(b.diet);
          this.pickedFoodBox.setName(b.name);
          this.pickedFoodBox.setItemsList(b.contents);
        }
      }
      return true;
    } else {
      return false;
    }
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
