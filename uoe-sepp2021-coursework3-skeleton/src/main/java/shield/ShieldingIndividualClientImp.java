/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

// student-included imports:
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {

  private final String ALR_REG = "already registered";
  private final String ORDER_PLACED = "0";
  private final String ORDER_PACKED = "1";
  private final String ORDER_DISPATCHED = "2";
  private final String ORDER_DELIVERED = "3";
  private final String ORDER_CANCELLED = "4";
  private final String ORDER_NOT_FOUND = "-1";
  private final String RESP_TRUE = "True";
  private final String RESP_FALSE = "False";
  private List<String> DIET_TYPES = List.of("none", "pollotarian", "vegan");
  private final String NO_DIET = " ";
  private final String POSTCODE_REGEX = "(e|E)(h|H)([0-9]|)[0-9](_| )[0-9][a-zA-Z][a-zA-Z]";

  public void setRegistered(boolean registered) {
    isRegistered = registered;
  }
  public void setPickedFoodBox(FoodBoxOrder pickedFoodBox) {
    this.pickedFoodBox = pickedFoodBox;
  }
  public void setNearestCatererName(String nearestCatererName) {
    this.nearestCatererName = nearestCatererName;
  }
  public void setNearestCateringPostCode(String nearestCateringPostCode) {
    this.nearestCateringPostCode = nearestCateringPostCode;
  }
  public void setChiNum(String chiNum) {
    this.chiNum = chiNum;
  }
  public void setCateringCompaniesArr(Collection<String> cateringCompaniesArr) {
    this.cateringCompaniesArr = cateringCompaniesArr;
  }
  public void setPostcode(String postcode) {
    this.postcode = postcode;
  }
  public void setOrdersDict(Map<Integer, FoodBoxOrder> ordersDict) {
    this.ordersDict = ordersDict;
  }
  public void setDefaultFoodBoxes(Map<Integer, MyMessagingFoodBox> defaultFoodBoxes) {
    this.defaultFoodBoxes = defaultFoodBoxes;
  }
  public Map<Integer, MyMessagingFoodBox> getterDefaultFoodBoxes() {
    return defaultFoodBoxes;
  }

  /** Internal field only used for transmission purposes;
   * Temporary format for storing food box details (as returned from server). */
  private class MyMessagingFoodBox {
    List<BoxItem> contents = new ArrayList<>();
    String delivered_by;
    String diet;
    Integer id;
    String name;

    /** Creates dictionary of food box items using 'contents' list;
     * Key is item id, Value is food BoxItem ref. */
    private Map<Integer, BoxItem> getContentsDict() {
      //if (this.contents == null) {this.contents = new ArrayList<>();}
      Map<Integer, BoxItem> contentsDict = new HashMap<>();
      for (BoxItem item : this.contents) {
        contentsDict.put(item.getId(), item);
      }
      return contentsDict;
    }
  }

  private String endpoint;
  private String chiNum;
  private boolean isRegistered;
  /** Dictionary storing shielding individual's orders; key is orderID, value is FoodBoxOrder ref. */
  private Map<Integer, FoodBoxOrder> ordersDict = new HashMap<>();
  /** Dictionary storing all default food boxes available in the system; key is food box id, value is MyMessagingFoodBox ref. */
  private Map<Integer, MyMessagingFoodBox> defaultFoodBoxes = new HashMap<>();
  private Collection<String> cateringCompaniesArr;
  private String nearestCatererName;
  private String nearestCateringPostCode;
  /** Stores FoodBoxOrder obj of the user-picked food box. Is picked according to food box id. */
  protected FoodBoxOrder pickedFoodBox;
  private String forename;
  private String surname;
  private String phoneNum;
  protected String postcode;  // TODO change to private after testing


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
    if (!checkValidCHI(CHI)) return false;
    // constructing endpoint request:
    String request = String.format("/registerShieldingIndividual?CHI=%s", CHI);
    try {
      // perform request:
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals(ALR_REG)) {
        this.isRegistered = true;
        // TODO: consider deleting ^ cuz this is just to allow us to pass tests
        return true;
      }
      // unmarshall response:
      Type listType = new TypeToken<List<String>>() {}.getType();
      List<String> personalDetailsArr = new Gson().fromJson(response, listType);

      // TODO ASK in QnA whether to use separate if-statements or use if-elseif-else block.
      if (personalDetailsArr == null) throw new NullPointerException("ERROR: Server GET request failed.");
      if (personalDetailsArr.size() != 4) throw new NullPointerException("ERROR: Server response corrupted.");
      this.postcode = personalDetailsArr.get(0);
      this.forename = personalDetailsArr.get(1);
      this.surname = personalDetailsArr.get(2);
      this.phoneNum = personalDetailsArr.get(3);
      this.isRegistered = true;
      this.chiNum = CHI;
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Check whether CHI is valid format
   *
   * @param CHI
   * @return true or false
   */
  protected boolean checkValidCHI(String CHI) {
    if (CHI.length() != 10) return false;
    String firstSix = CHI.substring(0,6);
    try {
      LocalDate.parse(firstSix, DateTimeFormatter.ofPattern("ddMMyy"));
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  /**
   * Method to obtain full list of default food boxes.
   * Tries first to obtain from local 'cache' of default food box items;
   * If local cache is empty, query server directly.
   *
   * @return dictionary of (all) default food boxes.
   */
  private Map<Integer, MyMessagingFoodBox> getAllDefaultFoodBoxes() {
    if (this.defaultFoodBoxes == null) {
      this.defaultFoodBoxes = getAllDefaultFoodBoxesFromServer();
      return this.defaultFoodBoxes;
    } else {
      return this.defaultFoodBoxes;
    }
  }

  /**
   * Method to get default food boxes for all dietary preferences.
   *
   * @return Map of all default food boxes as found on server
   */
  protected Map<Integer, MyMessagingFoodBox> getAllDefaultFoodBoxesFromServer() {
    Map<Integer, MyMessagingFoodBox> allDefaultFoodBoxesDict = new HashMap<>();
    for (String dietaryPreference : DIET_TYPES) {
      Map<Integer, MyMessagingFoodBox> specificFoodBox = getDefaultFoodBoxesFromServer(dietaryPreference);
      allDefaultFoodBoxesDict.putAll(specificFoodBox);
    }
    return allDefaultFoodBoxesDict;  // ## returns empty hashmap at worst
  }

  /**
   * Method to query Map of default food boxes from server according to dietary preference.
   *
   * @return Map of default food boxes as found on server for dietary preference.
   */
  // TODO test this private mtd explicitly in ShieldingIndividualClientImpTest
  protected Map<Integer, MyMessagingFoodBox> getDefaultFoodBoxesFromServer(String dietaryPreference) {
    if (!(DIET_TYPES.contains(dietaryPreference) || dietaryPreference.equals(NO_DIET))) {
      System.err.printf("%s is an invalid dietary preference", dietaryPreference);
      return new HashMap<>();  // is empty map
    }
    Map<Integer, MyMessagingFoodBox> responseBoxesDict = new HashMap<>();
    // setup the response recepient:
    List<MyMessagingFoodBox> responseBoxes = new ArrayList<>();
    // construct the endpoint request:
    String request = String.format("/showFoodBox?orderOption=catering&dietaryPreference=%s", dietaryPreference);
    try {
      // perform request:
      String response = ClientIO.doGETRequest(endpoint + request);
      //System.out.println(response);
      // unmarshal response:
      Type listType = new TypeToken<List<MyMessagingFoodBox>>() {}.getType();
      responseBoxes = new Gson().fromJson(response, listType);
      if (responseBoxes == null) throw new NullPointerException("ERROR: Server GET Request failed.");

      for (MyMessagingFoodBox b : responseBoxes) {
        responseBoxesDict.put(b.id, b);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getMessage());
    }
    return responseBoxesDict;
    // ## note: responseBoxesDict is an empty hashmap at worst.
  }

  /**
   * Returns collection of food box ids if the operation occurred correctly
   *
   * @param dietaryPreference dietary preference
   * @return collection of food box ids
   */
  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    assert(this.isRegistered()):"Individual must be registered first";
    if (!(DIET_TYPES.contains(dietaryPreference) || dietaryPreference.equals(NO_DIET))) {
      System.err.printf("%s is an invalid dietary preference", dietaryPreference);
      return Collections.EMPTY_LIST;
    }
    this.defaultFoodBoxes = getAllDefaultFoodBoxesFromServer();  // updates our cache
    Collection<Integer> boxIdsInt = getDefaultFoodBoxesFromServer(dietaryPreference).keySet();
    Collection<String> boxIds = new ArrayList<>();
    for (int id : boxIdsInt) {
      boxIds.add(String.valueOf(id));
    }
    return boxIds;
  }

  /**
   * Returns true if the operation occurred correctly
   *
   * @return true if the operation occurred correctly
   */
  // **UPDATE2** REMOVED PARAMETER
  @Override
  public boolean placeOrder() { // will not use LocalDateTime
    assert(this.isRegistered()):"Individual must be registered first";
    if(this.pickedFoodBox == null) return false;
    String request = String.format("/placeOrder?individual_id=%s" +
                                   "&catering_business_name=%s" +
                                   "&catering_postcode=%s",
                                   this.chiNum, this.nearestCatererName, this.nearestCateringPostCode);
    List<BoxItem> boxItemsList = new ArrayList<>(this.pickedFoodBox.getItemsDict().values());
    Gson gson = new Gson();
    String items = gson.toJson(boxItemsList);
    String data = String.format("{\"contents\":%s}", items);
    System.out.println(data);
    // form order data using the pickedFoodBox order
    try {
      String orderID = ClientIO.doPOSTRequest(endpoint + request, data);
      this.pickedFoodBox.setOrderStatus(ORDER_PLACED);
      this.pickedFoodBox.setOrderID(Integer.parseInt(orderID));
      this.ordersDict.put(Integer.parseInt(orderID), this.pickedFoodBox);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Returns true if the operation occurred correctly
   *
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean editOrder(int orderNumber) {
    assert(this.isRegistered()):"Individual must be registered first";
    // check if orderNumber exists:
    if (!this.ordersDict.containsKey(orderNumber)) return false;
    boolean isOrderPlaced = this.ordersDict.get(orderNumber).getOrderStatus().equals(ORDER_PLACED);
    boolean isOrderPacked = this.ordersDict.get(orderNumber).getOrderStatus().equals(ORDER_PACKED);
    if (!isOrderPlaced && !isOrderPacked) return false;
    String request = String.format("/editOrder?order_id=%s", orderNumber);
    List<BoxItem> boxItemsList = new ArrayList<>(this.ordersDict.get(orderNumber).getItemsDict().values());
    Gson gson = new Gson();
    String items = gson.toJson(boxItemsList);
    String data = String.format("{\"contents\":%s}", items);
    try {
      String response = ClientIO.doPOSTRequest(endpoint + request, data);
      boolean isValidResponse = response.equals(RESP_TRUE) || response.equals(RESP_FALSE);
      if (!isValidResponse) {
        System.err.printf("WARNING: Unexpected response: %s", response);
        return false;
      }
      return response.equals(RESP_TRUE);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Returns true if the operation occurred correctly
   *
   * @param orderNumber the order number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean cancelOrder(int orderNumber) {
    assert(this.isRegistered()):"Individual must be registered first";
    if (!this.ordersDict.containsKey(orderNumber)) return false;
    boolean isOrderDispatched = this.ordersDict.get(orderNumber).getOrderStatus().equals(ORDER_DISPATCHED);
    boolean isOrderDelivered = this.ordersDict.get(orderNumber).getOrderStatus().equals(ORDER_DELIVERED);
    boolean isOrderCancelled = this.ordersDict.get(orderNumber).getOrderStatus().equals(ORDER_CANCELLED);
    if(isOrderDispatched || isOrderDelivered || isOrderCancelled) return false;
    String request = String.format("/cancelOrder?order_id=%s", orderNumber);
    try {
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals("True")) {
        this.ordersDict.remove(orderNumber);
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /** Returns true if the operation occurred correctly
   *
   * @param orderID the order number
   * @return true if the operation occurred correctly
   */
  @Override
  public boolean requestOrderStatus(int orderID) {
    assert(this.isRegistered()):"Individual must be registered first";
    if(!this.ordersDict.containsKey(orderID)) return false;
    // constructing endpoint request:
    String request = String.format("/requestStatus?order_id=%s", orderID);
    try {
      // perform request:
      String statusResponse = ClientIO.doGETRequest(endpoint + request);
      // check if response is valid:
      List<String> validStatuses = Arrays.asList(ORDER_PLACED, ORDER_PACKED, ORDER_DISPATCHED,
                                                 ORDER_DELIVERED, ORDER_CANCELLED, ORDER_NOT_FOUND);
      boolean isValidResponse = validStatuses.contains(statusResponse);
      if (!isValidResponse) {
        String errMsg = String.format("WARNING: Unexpected response: %s", statusResponse);
        System.err.println(errMsg);
        return false;
      }
      this.ordersDict.get(orderID).setOrderStatus(statusResponse);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Returns collection of catering companies and their locations
   *
   * @return collection of catering companies and their locations
   */
  @Override
  public Collection<String> getCateringCompanies() {
    assert(this.isRegistered()):"Individual must be registered first";
    // construct endpoint request
    String request = "/getCaterers";
    // construct receiver structure:
    List<String> caterers = new ArrayList<>();
    try {
      // perform request:
      String response = ClientIO.doGETRequest(endpoint + request);
      // unmarshal response:
      Type listType = new TypeToken<List<String>>() {}.getType();
      caterers = new Gson().fromJson(response, listType);
      assert(caterers != null) : "ERROR: Server GET Request failed.";
    } catch (Exception e) {
      e.printStackTrace();
    }
    return caterers;
  }

  // **UPDATE**
  /**
   * Returns the distance between two locations based on their post codes
   *
   * @param postcode1 post code of one location
   * @param postcode2 post code of another location
   * @return the distance as a float between the two locations
   */
  @Override
  public float getDistance(String postcode1, String postcode2) {
    assert(postcode1 != null && postcode2 != null) : "Postcode cannot be null.";
    String request = String.format("/distance?postcode1=%s&postcode2=%s", formatPostcode(postcode1), formatPostcode(postcode2));
    try {
      // perform request:
      String response = ClientIO.doGETRequest(endpoint + request);
      assert (!response.isBlank());
      return Float.parseFloat(response);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return -1;  // returns if exception is thrown and caught
  }

  /**
   * Formats postcode to the form akin to "EH16_5AY"
   *
   * @param postcode
   * @return formatted postcode
   */
  protected String formatPostcode(String postcode) {
    assert(Pattern.matches(POSTCODE_REGEX, postcode)) : String.format("postcode (%s) is of wrong format", postcode);
    if (postcode.length() == 7) {  // e.g. EH7_6BR
      String front = postcode.substring(0,2);
      String num = postcode.substring(2,3);
      String back = postcode.substring(4,7);
      return String.format("%s0%s_%s", front.toUpperCase(), num, back.toUpperCase());
    } else {  // e.g. EH16_5AY
      String front = postcode.substring(0,4);
      String back = postcode.substring(5,8);
      return String.format("%s_%s", front.toUpperCase(), back.toUpperCase());
    }
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

  /**
   * Returns the number of available food boxes after quering the server
   *
   * @return number of available food boxes after quering the server
   */
  @Override
  public int getFoodBoxNumber() {
    this.defaultFoodBoxes = getAllDefaultFoodBoxesFromServer();
    return defaultFoodBoxes.size();
  }

  /**
   * Returns the dietary preference that this specific food box satisfies
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return dietary preference
   */
  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    assert(this.defaultFoodBoxes.containsKey(foodBoxId)) : String.format("%d is an invalid foodBoxId", foodBoxId);
    return this.defaultFoodBoxes.get(foodBoxId).diet;
  }

  /**
   * Returns the number of items in this specific food box
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return number of items in the food box
   */
  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {
    assert(this.defaultFoodBoxes.containsKey(foodBoxId)) : String.format("%d is an invalid foodBoxId", foodBoxId);
    Map<Integer, MyMessagingFoodBox> foodBoxesDict = getAllDefaultFoodBoxes();
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
    assert(this.defaultFoodBoxes.containsKey(foodboxId)) : String.format("%d is an invalid foodBoxId", foodboxId);
    Map<Integer, MyMessagingFoodBox> foodBoxesDict = getAllDefaultFoodBoxes();
    // find set of itemIds for food box with id == foodboxId:
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
    assert(this.defaultFoodBoxes.containsKey(foodBoxId)) : String.format("%d is an invalid foodBoxId", foodBoxId);
    Map<Integer, MyMessagingFoodBox> foodBoxesDict = getAllDefaultFoodBoxes();
    return foodBoxesDict.get(foodBoxId).getContentsDict().get(itemId).getName();
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
    assert(this.defaultFoodBoxes.containsKey(foodBoxId)) : String.format("%d is an invalid foodBoxId", foodBoxId);
    assert(this.defaultFoodBoxes.get(foodBoxId).getContentsDict().containsKey(itemId)) : String.format("%d is an invalid itemId", itemId);
    Map<Integer, MyMessagingFoodBox> foodBoxesDict = getAllDefaultFoodBoxes();
    return foodBoxesDict.get(foodBoxId).getContentsDict().get(itemId).getQuantity();
  }

  /**
   * Returns true if the requested foodbox was picked
   *
   * @param  foodBoxId the food box id as last returned from the server
   * @return true if the requested foodbox was picked
   */
  @Override
  public boolean pickFoodBox(int foodBoxId) {                                       // ### ??? ###
    assert(this.isRegistered()):"Individual must be registered first";
    // update local default food boxes 'cache' via server query:
    this.defaultFoodBoxes = getAllDefaultFoodBoxesFromServer();
    if (!this.defaultFoodBoxes.containsKey(foodBoxId)) return false;

    MyMessagingFoodBox chosenFoodBox = this.defaultFoodBoxes.get(foodBoxId);
    this.pickedFoodBox = new FoodBoxOrder();
    this.pickedFoodBox.setDeliveryService(chosenFoodBox.delivered_by);
    this.pickedFoodBox.setDietType(chosenFoodBox.diet);
    this.pickedFoodBox.setName(chosenFoodBox.name);
    this.pickedFoodBox.setItemsDict(chosenFoodBox.getContentsDict());
    return true;
  }

  /**
   * Returns true if the item quantity for the picked foodbox was changed
   *
   * @param  itemId the food box id as last returned from the server
   * @param  quantity the food box item quantity to be set
   * @return true if the item quantity for the picked foodbox was changed
   */
  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    assert(this.isRegistered()):"Individual must be registered first";
    if (this.pickedFoodBox == null) return false;
    if (!this.pickedFoodBox.getItemsDict().containsKey(itemId)) return false;
    if (quantity < 0 || quantity > this.pickedFoodBox.getItemsDict().get(itemId).getQuantity()) return false;

    this.pickedFoodBox.getItemsDict().get(itemId).setQuantity(quantity);
    return true;
  }

  // **UPDATE2** COMMENT ONLY
  /**
   * Returns the collection of the order numbers placed.
   *
   * This method queries the order ids for a placed order as stored locally by
   * the client.
   *
   * @return collection of the order numbers placed
   */
  @Override
  public Collection<Integer> getOrderNumbers() {
    assert(this.isRegistered()):"Individual must be registered first";
    return new ArrayList<>(this.ordersDict.keySet());
  }

  /**
   * Returns the status of the order for the requested number
   *
   * @param orderNumber the order number
   * @return status of the order for the requested number
   */
  @Override
  public String getStatusForOrder(int orderNumber) {
    assert(this.isRegistered()):"Individual must be registered first";
    // return order status as stored on client-side.
    assert (this.ordersDict.containsKey(orderNumber)) : "Order not found";
    return this.ordersDict.get(orderNumber).getOrderStatus();
  }

  /**
   * Returns the item ids for the items of the requested order
   *
   * @param  orderNumber the order number
   * @return item ids for the items of the requested order
   */
  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    assert(this.isRegistered()):"Individual must be registered first";
    assert (this.ordersDict.containsKey(orderNumber)) : "Order not found";
    return new ArrayList<Integer>(this.ordersDict.get(orderNumber).getItemsDict().keySet());
  }

  /**
   * Returns the name of the item for the requested order
   *
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @return name of the item for the requested order
   */
  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    assert(this.isRegistered()):"Individual must be registered first";
    assert(this.ordersDict.containsKey(orderNumber)) : "Order not found";
    Map<Integer, BoxItem> orderItemsDict = this.ordersDict.get(orderNumber).getItemsDict();
    assert(orderItemsDict.containsKey(itemId));
    return orderItemsDict.get(itemId).getName();
  }

  /**
   * Returns the quantity of the item for the requested order
   *
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @return quantity of the item for the requested order
   */
  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    assert(this.isRegistered()):"Individual must be registered first";
    assert(this.ordersDict.containsKey(orderNumber)) : "Order not found";
    Map<Integer, BoxItem> orderItemsDict = this.ordersDict.get(orderNumber).getItemsDict();
    assert(orderItemsDict.containsKey(itemId)) : "Item not found for order";
    return orderItemsDict.get(itemId).getQuantity();
  }

  /**
   * Returns true if quantity of the item for the requested order was changed
   *
   * @param  itemId the food box id as last returned from the server
   * @param  orderNumber the order number
   * @param  quantity the food box item quantity to be set
   * @return true if quantity of the item for the requested order was changed
   */
  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {
    assert(this.isRegistered()):"Individual must be registered first";
    if (!this.ordersDict.containsKey(orderNumber)) return false;
    Map<Integer, BoxItem> orderItemsDict = this.ordersDict.get(orderNumber).getItemsDict();
    if (!orderItemsDict.containsKey(itemId)) return false;
    if (quantity < 0 && quantity > orderItemsDict.get(itemId).getQuantity()) return false;

    orderItemsDict.get(itemId).setQuantity(quantity);
    return true;
  }

  // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

  // **UPDATE**
  /**
   * Returns closest catering company serving orders based on our location
   *
   * @return business name of catering company
   */
  @Override
  public String getClosestCateringCompany() {
    if (this.cateringCompaniesArr == null) {
      this.cateringCompaniesArr = this.getCateringCompanies();
      // this is meant to be a failsafe;
      // to query server, UI will need to update this.cateringCompaniesArr using getCateringCompanies() beforehand.
    }

    String nearestCatererName = null;
    String nearestCatererPostcode = null;
    // compare distances to ShieldingIndiv post code to find nearest catering company
    float minDist = Float.POSITIVE_INFINITY;
    for (String companyDetails : this.cateringCompaniesArr) {
      String[] details = companyDetails.split(",");
      String id = details[0];
      String name = details[1];
      String postCode = details[2];
      float dist = this.getDistance(this.postcode, postCode);
      if (dist != -1 && dist < minDist) {
        minDist = dist;
        nearestCatererName = name;
        nearestCatererPostcode = postCode;
      }
    }
    this.nearestCatererName = nearestCatererName;
    this.nearestCateringPostCode = nearestCatererPostcode;
    return nearestCatererName;
  }
}
