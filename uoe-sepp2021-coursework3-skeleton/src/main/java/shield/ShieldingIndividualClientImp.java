/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.time.LocalDateTime;

// student-included imports:
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

  private final String REG_NEW = "registered new";
  private final String ALR_REG = "already registered";
  private final String ORDER_PLACED = "0";
  private final String ORDER_PACKED = "1";
  private final String ORDER_DISPATCHED = "2";
  private final String ORDER_DELIVERED = "3";
  private final String ORDER_CANCELLED = "4";
  private final String ORDER_NOT_FOUND = "-1";
  private List<String> DIET_TYPES = List.of("none", "pollotarian", "vegan");

  // internal field only used for transmission purposes
  final class MyMessagingFoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    //List<HashMap<String, String>> contents;
    List<BoxItem> contents;
    String delivered_by;
    String diet;
    String id;
    String name;

    private Map<Integer, BoxItem> getContentsDict() {
      if (this.contents == null) {contents = new ArrayList<>();}  // method returns null
      Map<Integer, BoxItem> contentsDict = new HashMap<>();
      for (BoxItem item : this.contents) {
        contentsDict.put(item.id, item);
      }
      return contentsDict;
    }
  }

  private String endpoint;
  private String chiNum;
  private boolean isRegistered;
  public Map<Integer, FoodBoxOrder> ordersDict = new HashMap<>();
  private Map<Integer, MyMessagingFoodBox> defaultFoodBoxes = new HashMap<>();
  private String dietaryPreference;
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

  private Map<Integer, MyMessagingFoodBox> getDefaultFoodBoxesDict() {
    if (this.defaultFoodBoxes == null) {
      this.defaultFoodBoxes = getDefaultFoodBoxesDictFromServer();
      return this.defaultFoodBoxes;
    } else {
      return this.defaultFoodBoxes;
    }
  }

  /* MAGIC SERVER QUERY FUNCTION: */
  private Map<Integer, MyMessagingFoodBox> getDefaultFoodBoxesDictFromServer() {
    Map<Integer, MyMessagingFoodBox> responseBoxesDict = new HashMap<>();

    for (String dietaryPrefrence : DIET_TYPES) {
      // setup the response recepient:
      List<MyMessagingFoodBox> responseBoxes = new ArrayList<>();
      // construct the endpoint request:
      String request = String.format("/showFoodBox?orderOption=catering&dietaryPreference=%s", dietaryPrefrence);
      try {
        // perform request:
        String response = ClientIO.doGETRequest(endpoint + request);
        // unmarshal response:
        Type listType = new TypeToken<List<MyMessagingFoodBox>>() {
        }.getType();
        responseBoxes = new Gson().fromJson(response, listType);

        if (responseBoxes == null) throw new NullPointerException("ERROR: Server GET Request failed.");

        for (MyMessagingFoodBox b : responseBoxes) {
          responseBoxesDict.put(Integer.parseInt(b.id), b);
        }
      } catch (Exception e) {
        e.printStackTrace();
        System.err.println(e.getMessage());
      }
    }
      return responseBoxesDict;
      // TODO: Figure out how to deal with the case where responseBoxesDict = null
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
    Map<Integer, MyMessagingFoodBox> defaultFoodBoxesDict = getDefaultFoodBoxesDictFromServer();
    List<String> boxIds = new ArrayList<>();
    for (Integer id : defaultFoodBoxesDict.keySet()) {
      boxIds.add(String.valueOf(id));
    }
    /*
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
    */
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


  /**
   * Returns the dietary preference that this specific food box satisfies
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return dietary preference
   */
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
    Map<Integer, MyMessagingFoodBox> foodBoxesDict = getDefaultFoodBoxesDict();
    return foodBoxesDict.get(foodBoxId).contents.size();
  }

  /**
   * Returns the collection of item ids of the requested foodbox
   *
   * @param  foodboxId the food box id as last returned from the server
   * @return collection of item ids of the requested foodbox
   */
  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {
    Map<Integer, MyMessagingFoodBox> foodBoxesDict = getDefaultFoodBoxesDict();
    return new ArrayList<>(foodBoxesDict.get(foodboxId).getContentsDict().keySet());
  }

  /**
   * Returns the item name of the item in the requested foodbox
   *
   * @param  itemId the food box id as last returned from the server
   * @param  foodBoxId the food box id as last returned from the server
   * @return the requested item name
   */
  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    Map<Integer, MyMessagingFoodBox> foodBoxesDict = getDefaultFoodBoxesDict();
    return foodBoxesDict.get(foodBoxId).name;
  }

  /**
   * Returns the item quantity of the item in the requested foodbox
   *
   * @param  itemId the food box id as last returned from the server
   * @param  foodBoxId the food box id as last returned from the server
   * @return the requested item quantity
   */
  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    Map<Integer, MyMessagingFoodBox> foodBoxesDict = getDefaultFoodBoxesDict();
    return foodBoxesDict.get(foodBoxId).getContentsDict().get(itemId).quantity;
  }

  /**
   * Returns true if the requested foodbox was picked
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return true if the requested foodbox was picked
   */
  @Override
  public boolean pickFoodBox(int foodBoxId) {                                       // ### ??? ###
    Map<Integer, MyMessagingFoodBox> foodBoxesDict = getDefaultFoodBoxesDict();
    if (!foodBoxesDict.containsKey(foodBoxId)) {return false;}
    MyMessagingFoodBox chosenFoodBox = foodBoxesDict.get(foodBoxId);
    this.pickedFoodBox = new FoodBoxOrder();
    this.pickedFoodBox.setDeliveryService(chosenFoodBox.delivered_by);
    this.pickedFoodBox.setDietType(chosenFoodBox.diet);
    this.pickedFoodBox.setName(chosenFoodBox.name);
    this.pickedFoodBox.itemsDict = chosenFoodBox.getContentsDict();
    return true;
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
