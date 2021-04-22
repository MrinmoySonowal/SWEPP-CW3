/**
 *
 */

package shield;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NOTE TO MARKER:
 * Please remove all empty newlines at the start of the database .txt and .csv files after
 * initialisation -- these unwanted empty lines will break the code (PIAZZA Post @832).
 *
 * Contains tests to verify unit / method-centric functionality.
 */
public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private ShieldingIndividualClientImp clientImp;
  private CateringCompanyClientImp cateringImp;
  private String validRngCHI;
  private final String POSTCODE_REGEX_STRICT = "EH[0-9][0-9]_[0-9][A-Z][A-Z]";
  String testCHI = "1210782341";
  String testCaterName = "nearestCaterer";
  String testCaterPostcode = "EH55_2BT";
  int testOrderId;

  private Properties loadProperties(String propsFilename) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Properties props = new Properties();

    try {
      InputStream propsStream = loader.getResourceAsStream(propsFilename);
      props.load(propsStream);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return props;
  }

  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);
    clientImp = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    cateringImp = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));

    String requestRegUser = String.format("/registerShieldingIndividual?CHI=%s", this.testCHI);
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + requestRegUser);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String requestRegCaterer = String.format("/registerCateringCompany?business_name=%s&postcode=%s",
            this.testCaterName,this.testCaterPostcode);
    try {
      ClientIO.doGETRequest(clientProps.getProperty("endpoint") + requestRegCaterer);
    } catch (Exception e) {
      e.printStackTrace();
    }

    String requestPlaceOrder = String.format("/placeOrder?individual_id=%s" +
                    "&catering_business_name=%s" +
                    "&catering_postcode=%s",
            this.testCHI, this.testCaterName, this.testCaterPostcode);
    String data = "{\"contents\":[]}";
    try {
      this.testOrderId = Integer.parseInt(ClientIO.doPOSTRequest(clientProps.getProperty("endpoint") + requestPlaceOrder, data));
    } catch (Exception e) {
      e.printStackTrace();
    }

    Random rand = new Random();
    String dateTime = DateTimeFormatter.ofPattern("ddMMyy").format(LocalDateTime.now());
    String lastFour = String.valueOf(rand.nextInt(9999 - 1000) + 1000);
    this.validRngCHI = dateTime + lastFour;
  }

  @Test
  @DisplayName("Testing correct value for registerShieldingIndividual")
  public void testShieldingIndividualNewRegistration() {
    // Test functionality for new registration:
    assertTrue(clientImp.registerShieldingIndividual(this.validRngCHI),
            "Working method (new registration) must return true.");
    assertTrue(clientImp.isRegistered(),
            "Field must be true once registered.");
    assertEquals(clientImp.getCHI(), this.validRngCHI,
            "Client-stored CHI must be the same as inputted CHI.");

    // Test functionality for "already registered":
    assertTrue(clientImp.registerShieldingIndividual(this.validRngCHI),
            "Working method (already registered) must return true.");
    assertTrue(clientImp.isRegistered(),
            "Field must be true once registered.");
    assertEquals(clientImp.getCHI(), this.validRngCHI,
            "Client-stored CHI must be the same as inputted CHI.");

    // TODO clarify: how to get indiv details from server if alr registered (and using new client obj),
    //  ANS: write as part of report as a limitation

    // TODO how to test functions that are not part of the java interfaces?
    //  ANS: dont need to test the private mtds unless they're complex enough in which test them separately

    // TODO clarify postcode formatting error from server function (e.g. eh0111),
    //  ANS: should be of correct format, but need to do our own checks
  }

  @Test
  @DisplayName("Test correct operation of checkValidCHI")
  public void testShieldingIndividualCheckValidCHI() {
    String badCHI = "3402661234";  // 34 Feb does not exist
    assertFalse(clientImp.checkValidCHI(null), "Working method should return false when CHI is null");
    assertFalse(clientImp.checkValidCHI(" "), "Working method should return false when for invalid CHI");
    assertFalse(clientImp.checkValidCHI(badCHI), "Working method should return false for invalid CHI");
    assertTrue(clientImp.checkValidCHI(this.validRngCHI),"Working method should return true for valid CHI");
  }

  @Test
  @DisplayName("Test helper wrapper method getAllDefaultFoodBoxesFromServer")
  public void testShieldingIndividualGetAllDefaultFoodBoxesFromServer() {
    assertNotEquals(clientImp.getAllDefaultFoodBoxesFromServer(), Collections.EMPTY_MAP,
            "Working method should return empty hashmap for invalid dietary preference");

    String request = String.format("/showFoodBox?orderOption=catering&dietaryPreference=%s", " ");
    String response = "";
    try {
      response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);
    } catch (Exception e) {
      e.printStackTrace();
    }
    Gson gson = new Gson();
    String items = gson.toJson(clientImp.getAllDefaultFoodBoxesFromServer().values());
    assertEquals(response, items,
            "Working method should return matching content as naked HTTP request");
  }

  @Test
  @DisplayName("Test helper method getDefaultFoodBoxesFromServer")
  public void testShieldingIndividualGetDefaultFoodBoxesFromServer() {
    assertEquals(clientImp.getDefaultFoodBoxesFromServer("Gibberish"), Collections.EMPTY_MAP,
            "Working method should return empty hashmap for invalid dietary preference");

    List<String> DIET_TYPES = List.of("none", "pollotarian", "vegan", " ");
    for (String dietType : DIET_TYPES) {
      String request = String.format("/showFoodBox?orderOption=catering&dietaryPreference=%s", dietType);
      String response = "";
      try {
        response = ClientIO.doGETRequest(clientProps.getProperty("endpoint") + request);
      } catch (Exception e) {
        e.printStackTrace();
      }
      Gson gson = new Gson();
      String items = gson.toJson(clientImp.getDefaultFoodBoxesFromServer(dietType).values());
      assertEquals(response, items,
              "Working method should return matching content as naked HTTP request");
    }
  }

  @Test
  @DisplayName("Test correct operation of showFoodBoxes")
  public void testShieldingIndividualShowFoodBoxes() {
    clientImp.setRegistered(true);

    // we check values against those in food_boxes.txt
    assertEquals(clientImp.showFoodBoxes("none"), Arrays.asList("1","3","4"),
            "Working method should return IDs for boxes with diet = 'none'");
    assertEquals(clientImp.showFoodBoxes("pollotarian"), Collections.singletonList("2"),
            "Working method should return IDs for boxes with diet = 'pollotarian'");
    assertEquals(clientImp.showFoodBoxes("vegan"), Collections.singletonList("5"),
            "Working method should return IDs for boxes with diet = 'vegan'");

    assertNotEquals(clientImp.showFoodBoxes(" "), Collections.EMPTY_LIST,
            "'No dietary preference' should be an allowed diet");

    assertEquals(clientImp.showFoodBoxes("Gibberish"), Collections.EMPTY_LIST,
            "Working method should return no IDs since 'Gibberish' is not a valid diet type");
  }

  @Test
  @DisplayName("Test correct operation of placeOrder")
  public void testShieldingIndividualPlaceOrder() {
    clientImp.setChiNum(this.testCHI);
    clientImp.setRegistered(true);

    clientImp.setNearestCatererName(this.testCaterName);
    clientImp.setNearestCateringPostCode(this.testCaterPostcode);

    FoodBoxOrder foodBox = new FoodBoxOrder();
    foodBox.setOrderID(1);
    clientImp.setPickedFoodBox(foodBox);

    assertTrue(clientImp.placeOrder(), "Working method should return true");
  }

  @Test
  @DisplayName("Test correct operation of pickFoodBox")
  public void testShieldingIndividualPickFoodBox() {
    clientImp.setRegistered(true);

    assertTrue(clientImp.pickFoodBox(1), "Working method should return True");
    assertTrue(clientImp.pickFoodBox(2), "Working method should return True");
    assertTrue(clientImp.pickFoodBox(3), "Working method should return True");
    assertTrue(clientImp.pickFoodBox(4), "Working method should return True");
    assertTrue(clientImp.pickFoodBox(5), "Working method should return True");

    assertFalse(clientImp.pickFoodBox(19),
            "Working method should return False since there is no food box in food_boxes.txt of ID = 19");
  }

  @Test
  @DisplayName("Test correct operation of getDistance")
  public void testShieldingIndividualGetDistance() {
    assertTrue(clientImp.getDistance("EH16_5AY", "EH56_9UG") >= 0, "Working method should return True");

    AssertionError badPostcodeErr0 = assertThrows(AssertionError.class, () -> {
      clientImp.getDistance("eh165ay", "EH56_9UG");
    });
    String expectedMessage = String.format("postcode (%s) is of wrong format", "eh165ay");
    String actualMessage = badPostcodeErr0.getMessage();
    assertEquals(expectedMessage, actualMessage, "Working method should return True");

    AssertionError badPostcodeErr1 = assertThrows(AssertionError.class, () -> {
      clientImp.getDistance("EH16_5AY", "eh569ug");
    });
    expectedMessage = String.format("postcode (%s) is of wrong format", "eh569ug");
    actualMessage = badPostcodeErr1.getMessage();
    assertEquals(expectedMessage, actualMessage, "Working method should return True");

    AssertionError badPostcodeErr2 = assertThrows(AssertionError.class, () -> {
      clientImp.getDistance("eh165ay", "eh569ug");
    });
    expectedMessage = String.format("postcode (%s) is of wrong format", "eh165ay");
    actualMessage = badPostcodeErr2.getMessage();
    assertEquals(expectedMessage, actualMessage, "Working method should return True");
  }

  @Test
  @DisplayName("Test correct operation of formatPostode (for acceptable postcode formats)")
  public void testGoodPostcodeFormatting() {
    List<String> IFFY_POSTCODES = List.of("eH6 7uu", "Eh61 7uu", "Eh6_7uu", "eH61_7uu");
    for (String iffyPostcode : IFFY_POSTCODES) {
      String goodPostcode = clientImp.formatPostcode(iffyPostcode);
      assertTrue(Pattern.matches(POSTCODE_REGEX_STRICT, goodPostcode),
              "Successfully formatted postcode should match strict postcode format");
    }
  }

  @Test
  @DisplayName("Test correct operation of formatPostcode (for unacceptable postcode formats)")
  public void testBadPostcodeFormatting() {
    List<String> IFFY_POSTCODES = List.of("TH6 7uu", "fH6_27uu", "eH623_7uu", "eH61_71uu");
    for (String iffyPostcode : IFFY_POSTCODES) {
      AssertionError badPostcodeErr = assertThrows(AssertionError.class, () -> {
        clientImp.formatPostcode(iffyPostcode);
      });
      String expectedMessage = String.format("postcode (%s) is of wrong format", iffyPostcode);
      String actualMessage = badPostcodeErr.getMessage();
      assertEquals(expectedMessage, actualMessage,
              "Invalid postcodes should fail assertion in formatPostcode");
    }
  }

  @Test
  @DisplayName("Test correct operation of getClosestCateringCompany")
  public void testShieldingIndividualGetClosestCaterer() {
    List<String> testCatList = List.of("0,testCat1,EH15_1QW", "1,testCat2,EH01_2BT", "2,testCat3,EH12_7FF");
    clientImp.setRegistered(true);
    clientImp.setPostcode("EH16_5AU");
    clientImp.setCateringCompaniesArr(testCatList);
    String expectedCaterer = "testCat1";
    assertEquals(clientImp.getClosestCateringCompany(), expectedCaterer,
            "Working method should return closest postcode");
  }

  @Test
  @DisplayName("Test correct operation of editOrder")
  public void testShieldingIndividualEditOrder() {
    // this method is only called after order is editted via other methods
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.editOrder(this.testOrderId);
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to order");

    clientImp.setRegistered(true);
    FoodBoxOrder order = new FoodBoxOrder();
    order.setOrderID(this.testOrderId);
    Map<Integer, FoodBoxOrder> testMap = new HashMap<>();
    testMap.put(this.testOrderId,order);
    clientImp.setOrdersDict(testMap);

    String ORDER_NOT_FOUND = "-1";
    order.setOrderStatus(ORDER_NOT_FOUND);
    assertFalse(clientImp.editOrder(this.testOrderId),
            "Working method should not allow changing of 'not-found' orders");

    for (int i = 2; i < 4+1; i++) {
      // iterates through Dispatched, Delivered, Cancelled
      order.setOrderStatus(String.valueOf(i));
      assertFalse(clientImp.editOrder(this.testOrderId),
              "Working method should not allow order changes unless status is Placed or Packed");
    }

    String ORDER_PLACED = "0";
    order.setOrderStatus(ORDER_PLACED);  // set order placed
    assertTrue(clientImp.editOrder(this.testOrderId));
  }


  @Test
  @DisplayName("Test correct operation of cancelOrder")
  public void testShieldingIndividualCancelOrder(){
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.cancelOrder(this.testOrderId);
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to cancel order");

    clientImp.setRegistered(true);
    FoodBoxOrder order = new FoodBoxOrder();
    order.setOrderID(this.testOrderId);
    Map<Integer, FoodBoxOrder> testMap = new HashMap<>();
    testMap.put(this.testOrderId,order);
    clientImp.setOrdersDict(testMap);

    String ORDER_NOT_FOUND = "-1";
    order.setOrderStatus(ORDER_NOT_FOUND);
    assertFalse(clientImp.editOrder(this.testOrderId),
            "Working method should not allow cancelling of 'not-found' orders");

    for (int i = 2; i < 4+1; i++) {
      // iterates through Dispatched, Delivered, Cancelled
      order.setOrderStatus(String.valueOf(i));
      assertFalse(clientImp.cancelOrder(this.testOrderId),
              "Working method should not allow order changes unless status is Placed or Packed");
    }
    String ORDER_PLACED = "0";
    order.setOrderStatus(ORDER_PLACED);  // set order placed
    assertTrue(clientImp.cancelOrder(this.testOrderId),
            "Working method should return true");
  }

  @Test
  @DisplayName("Test correct operation of getCateringCompanies")
  public void testShieldingIndividualGetCateringCompanies() {
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.getCateringCompanies();
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to get caterers");

    clientImp.setRegistered(true);
    assertNotEquals(clientImp.getCateringCompanies(), Collections.EMPTY_LIST,
            "Method should not return empty list (since we have registered a caterer in setup()");
  }

  @Test
  @DisplayName("Test correct operation of requestOrderStatus")
  public void testShieldingIndividualRequestOrderStatus() {
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.requestOrderStatus(this.testOrderId);
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to request for order status");

    clientImp.setRegistered(true);
    FoodBoxOrder order = new FoodBoxOrder();
    order.setOrderID(this.testOrderId);
    Map<Integer, FoodBoxOrder> testMap = new HashMap<>();

    assertFalse(clientImp.requestOrderStatus(this.testOrderId),
            "Method should not allow requests for order statuses of non-existent orders (client-side)");

    testMap.put(this.testOrderId,order);
    clientImp.setOrdersDict(testMap);
    assertTrue(clientImp.requestOrderStatus(this.testOrderId));
  }

  @Test
  @DisplayName("Test correct operation of getFoodBoxNumber")
  public void testShieldingIndividualGetFoodBoxNumber(){
    assertEquals(clientImp.getFoodBoxNumber(), 5, "Working method should return 5 (int)");
    // There are 5 default food boxes in the default server file food_boxes.txt
  }

  @Test
  @DisplayName("Test correct operation of getDietaryPreferenceForFoodBox")
  public void testShieldingIndividualGetDietaryPreferenceForFoodBox() {
    clientImp.setDefaultFoodBoxes(clientImp.getAllDefaultFoodBoxesFromServer());  // method already verified from before
    assertEquals(clientImp.getDietaryPreferenceForFoodBox(1), "none");
    assertEquals(clientImp.getDietaryPreferenceForFoodBox(2), "pollotarian");
    assertEquals(clientImp.getDietaryPreferenceForFoodBox(3), "none");
    assertEquals(clientImp.getDietaryPreferenceForFoodBox(4), "none");
    assertEquals(clientImp.getDietaryPreferenceForFoodBox(5), "vegan");
  }

  @Test
  @DisplayName("Test correct operation of changeItemQuantityForPickedFoodBox")
  public void testShieldingIndvChangeItemQuantityForPickedFoodBox() {
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.changeItemQuantityForPickedFoodBox(1,1);
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to cancel order");

    clientImp.setRegistered(true);
    FoodBoxOrder pickedFB = new FoodBoxOrder();
    pickedFB.setOrderID(1);
    FoodBoxItem boxItem1 = new FoodBoxItem();
    boxItem1.setId(2);
    boxItem1.setQuantity(3);
    Map<Integer, FoodBoxItem> boxItemsDict = new HashMap<>();
    boxItemsDict.put(1,boxItem1);
    pickedFB.setItemsDict(boxItemsDict);

    clientImp.setPickedFoodBox(pickedFB);
    clientImp.pickFoodBox(1);
    //quantity = 2 for item 2 for foodBoxId 1 in food_boxes.txt
    assertFalse(clientImp.changeItemQuantityForPickedFoodBox(2,3),
            "Working method should return false when qty is greater than existing qty");

    assertFalse(clientImp.changeItemQuantityForPickedFoodBox(2,-1),
            "Working method should return false when quantity is less than 0");

    assertTrue(clientImp.changeItemQuantityForPickedFoodBox(2,1),
            "Working method should return true for correct quantity");
  }

  @Test
  @DisplayName("Test correct operation of getItemsNumberForFoodBox")
  public void testShieldingIndvGetItemNumbersForFoodBox() {
    AssertionError fbNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemsNumberForFoodBox(1);
    });
    String expectedMessage = "1 is an invalid foodBoxId";
    String actualMessage = fbNotFoundErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to cancel order");

    clientImp.setDefaultFoodBoxes(clientImp.getAllDefaultFoodBoxesFromServer());
    assertEquals(clientImp.getItemsNumberForFoodBox(1), 3);
    assertEquals(clientImp.getItemsNumberForFoodBox(2), 3);
    assertEquals(clientImp.getItemsNumberForFoodBox(3), 3);
    assertEquals(clientImp.getItemsNumberForFoodBox(4), 4);
    assertEquals(clientImp.getItemsNumberForFoodBox(5), 3);
  }

  @Test
  @DisplayName("Test correct operation of getItemIdsForFoodBox")
  public void testShieldingIndvGetItemIdsForFoodBox() {
    AssertionError fbNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemsNumberForFoodBox(1);
    });
    String expectedMessage = "1 is an invalid foodBoxId";
    String actualMessage = fbNotFoundErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to cancel order");

    clientImp.setDefaultFoodBoxes(clientImp.getAllDefaultFoodBoxesFromServer());
    List<Integer> fb1Items = List.of(1,2,6);
    assertEquals(clientImp.getItemIdsForFoodBox(1), fb1Items);
    List<Integer> fb2Items = List.of(1,3,7);
    assertEquals(clientImp.getItemIdsForFoodBox(2), fb2Items);
    List<Integer> fb3Items = List.of(3,4,8);
    assertEquals(clientImp.getItemIdsForFoodBox(3), fb3Items);
    List<Integer> fb4Items = List.of(8,9,11,13);
    assertEquals(clientImp.getItemIdsForFoodBox(4), fb4Items);
    List<Integer> fb5Items = List.of(9,11,12);
    assertEquals(clientImp.getItemIdsForFoodBox(5), fb5Items);
  }

  @Test
  @DisplayName("Test correct operation of getItemNameForFoodBox")
  public void testShieldingIndividualGetItemNameForFoodBox() {
    AssertionError fbNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemNameForFoodBox(1,1);
    });
    String expectedMessage = "1 is an invalid foodBoxId";
    String actualMessage = fbNotFoundErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to cancel order");

    clientImp.setDefaultFoodBoxes(clientImp.getAllDefaultFoodBoxesFromServer());
    assertEquals(clientImp.getItemNameForFoodBox(1, 1), "cucumbers");
    assertEquals(clientImp.getItemNameForFoodBox(3, 2), "onions");
    assertEquals(clientImp.getItemNameForFoodBox(4, 3), "carrots");
    assertEquals(clientImp.getItemNameForFoodBox(8, 4), "bacon");
    assertEquals(clientImp.getItemNameForFoodBox(12, 5), "mango");
  }

  @Test
  @DisplayName("Test correct operation of getItemQuantityForFoodBox")
  public void testShieldingIndividualGetItemQuantityForFoodBox() {
    AssertionError fbNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemQuantityForFoodBox(1,1);
    });
    String expectedMessage = "1 is an invalid foodBoxId";
    String actualMessage = fbNotFoundErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to cancel order");

    clientImp.setDefaultFoodBoxes(clientImp.getAllDefaultFoodBoxesFromServer());

    AssertionError itemNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemQuantityForFoodBox(13,1);
    });
    String expectedMessage1 = "13 is an invalid itemId";
    String actualMessage1 = itemNotFoundErr.getMessage();
    assertEquals(expectedMessage1, actualMessage1,
            "Method should not allow non-existent querying for non-existent items");

    assertEquals(clientImp.getItemQuantityForFoodBox(1, 1), 1);
    assertEquals(clientImp.getItemQuantityForFoodBox(3, 2), 1);
    assertEquals(clientImp.getItemQuantityForFoodBox(4, 3), 2);
    assertEquals(clientImp.getItemQuantityForFoodBox(8, 4), 1);
    assertEquals(clientImp.getItemQuantityForFoodBox(12, 5), 1);
  }

  @Test
  @DisplayName("Test correct operation of getOrderNumbers")
  public void testShieldingIndividualGetOrderNumbers() {
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.getOrderNumbers();
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to get order numbers");

    clientImp.setRegistered(true);
    Map<Integer, FoodBoxOrder> ordersDict = new HashMap<>();
    clientImp.setOrdersDict(ordersDict);
    assertEquals(clientImp.getOrderNumbers(), Collections.EMPTY_LIST);

    FoodBoxOrder order = new FoodBoxOrder();
    order.setOrderID(this.testOrderId);
    ordersDict.put(this.testOrderId, order);
    assertEquals(clientImp.getOrderNumbers(), List.of(this.testOrderId));
  }

  @Test
  @DisplayName("Test correct operation of getStatusForOrder")
  public void testShieldingIndividualGetStatusForOrder() {
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.getStatusForOrder(this.testOrderId);
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to get order status");

    clientImp.setRegistered(true);
    Map<Integer, FoodBoxOrder> ordersDict = new HashMap<>();
    clientImp.setOrdersDict(ordersDict);

    AssertionError orderNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getStatusForOrder(this.testOrderId);
    });
    String expectedMessage1 = "Order not found";
    String actualMessage1 = orderNotFoundErr.getMessage();
    assertEquals(expectedMessage1, actualMessage1,
            "Method should not allow querying for statuses of non-existent orders");

    FoodBoxOrder order = new FoodBoxOrder();
    order.setOrderID(this.testOrderId);
    order.setOrderStatus("0");
    ordersDict.put(this.testOrderId, order);
    assertEquals(clientImp.getStatusForOrder(this.testOrderId), "0");

  }

  @Test
  @DisplayName("Test correct operation of getItemIdsForOrder")
  public void testShieldingIndividualGetItemIdsForOrder(){
    AssertionError fbNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemIdsForOrder(this.testOrderId);
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = fbNotFoundErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to get item ids for order");

    clientImp.setRegistered(true);
    Map<Integer, FoodBoxOrder> ordersDict = new HashMap<>();
    clientImp.setOrdersDict(ordersDict);

    AssertionError orderNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemIdsForOrder(this.testOrderId);
    });
    String expectedMessage1 = "Order not found";
    String actualMessage1 = orderNotFoundErr.getMessage();
    assertEquals(expectedMessage1, actualMessage1,
            "Method should not allow querying for itemIds of non-existent orders");

    FoodBoxOrder order = new FoodBoxOrder();
    order.setOrderID(this.testOrderId);
    ordersDict.put(this.testOrderId, order);

    Map<Integer, FoodBoxItem> itemsDict = new HashMap<>();
    order.setItemsDict(itemsDict);
    assertEquals(clientImp.getItemIdsForOrder(this.testOrderId), Collections.EMPTY_LIST );

    FoodBoxItem item1 = new FoodBoxItem();
    itemsDict.put(1,item1);
    FoodBoxItem item2 = new FoodBoxItem();
    itemsDict.put(2,item2);
    assertEquals(clientImp.getItemIdsForOrder(this.testOrderId), List.of(1,2));
  }

  @Test
  @DisplayName("Test correct operation of getItemNameForOrder")
  public void testShieldingIndividualGetItemNameForOrder() {
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemNameForOrder(1, this.testOrderId);
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to get item name");

    clientImp.setRegistered(true);
    Map<Integer, FoodBoxOrder> ordersDict = new HashMap<>();
    clientImp.setOrdersDict(ordersDict);

    AssertionError orderNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemNameForOrder(1, this.testOrderId);
    });
    String expectedMessage1 = "Order not found";
    String actualMessage1 = orderNotFoundErr.getMessage();
    assertEquals(expectedMessage1, actualMessage1,
            "Method should not allow querying for statuses of non-existent orders");

    FoodBoxOrder order = new FoodBoxOrder();
    Map<Integer, FoodBoxItem> itemsDict = new HashMap<>();
    FoodBoxItem item1 = new FoodBoxItem();
    item1.setName("carrots");
    itemsDict.put(1,item1);
    FoodBoxItem item2 = new FoodBoxItem();
    item2.setName("banana");
    itemsDict.put(2,item2);

    AssertionError itemNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemNameForOrder(1, this.testOrderId);
    });
    String expectedMessage2 = "Order not found";
    String actualMessage2 = itemNotFoundErr.getMessage();
    assertEquals(expectedMessage2, actualMessage2,
            "Method should not allow querying for statuses of non-existent items");

    order.setItemsDict(itemsDict);
    ordersDict.put(this.testOrderId, order);
    assertEquals(clientImp.getItemNameForOrder(1, this.testOrderId), "carrots");
    assertEquals(clientImp.getItemNameForOrder(2, this.testOrderId), "banana");
  }

  @Test
  @DisplayName("Test correct operation of getItemQuantityForOrder")
  public void testShieldingIndividualGetItemQuantityForOrder() {
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemQuantityForOrder(1, this.testOrderId);
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to get item name");

    clientImp.setRegistered(true);
    Map<Integer, FoodBoxOrder> ordersDict = new HashMap<>();
    clientImp.setOrdersDict(ordersDict);

    AssertionError orderNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemQuantityForOrder(1, this.testOrderId);
    });
    String expectedMessage1 = "Order not found";
    String actualMessage1 = orderNotFoundErr.getMessage();
    assertEquals(expectedMessage1, actualMessage1,
            "Method should not allow querying for statuses of non-existent orders");

    FoodBoxOrder order = new FoodBoxOrder();
    Map<Integer, FoodBoxItem> itemsDict = new HashMap<>();
    FoodBoxItem item1 = new FoodBoxItem();
    item1.setQuantity(1);
    itemsDict.put(1,item1);
    FoodBoxItem item2 = new FoodBoxItem();
    item2.setQuantity(2);
    itemsDict.put(2,item2);
    order.setItemsDict(itemsDict);
    ordersDict.put(this.testOrderId, order);

    AssertionError itemNotFoundErr = assertThrows(AssertionError.class, () -> {
      clientImp.getItemQuantityForOrder(3, this.testOrderId);
    });
    String expectedMessage2 = "Item not found for order";
    String actualMessage2 = itemNotFoundErr.getMessage();
    assertEquals(expectedMessage2, actualMessage2,
            "Method should not allow querying for statuses of non-existent items");

    assertEquals(clientImp.getItemQuantityForOrder(1, this.testOrderId), 1);
    assertEquals(clientImp.getItemQuantityForOrder(2, this.testOrderId), 2);
  }

  @Test
  @DisplayName("Test correct operation of setItemQuantityForOrder")
  public void testShieldingIndividualSetItemQuantityForOrder() {
    AssertionError notRegisteredErr = assertThrows(AssertionError.class, () -> {
      clientImp.setItemQuantityForOrder(1, this.testOrderId, 1);
    });
    String expectedMessage = "Individual must be registered first";
    String actualMessage = notRegisteredErr.getMessage();
    assertEquals(expectedMessage, actualMessage,
            "Method should not allow unregistered users to get item name");

    clientImp.setRegistered(true);
    Map<Integer, FoodBoxOrder> ordersDict = new HashMap<>();
    clientImp.setOrdersDict(ordersDict);

    assertFalse(clientImp.setItemQuantityForOrder(1, this.testOrderId,1 ),
            "Working method should return false when orderId is not found");

    FoodBoxOrder order = new FoodBoxOrder();
    Map<Integer, FoodBoxItem> itemsDict = new HashMap<>();
    FoodBoxItem item1 = new FoodBoxItem();
    int item1Quantity = 5;
    item1.setQuantity(item1Quantity);
    itemsDict.put(1,item1);
    FoodBoxItem item2 = new FoodBoxItem();
    item2.setQuantity(10);
    itemsDict.put(2,item2);
    order.setItemsDict(itemsDict);
    ordersDict.put(this.testOrderId, order);

    assertFalse(clientImp.setItemQuantityForOrder(3, this.testOrderId,1),
            "Working method should return false when itemId is not found");
    assertFalse(clientImp.setItemQuantityForOrder(1, this.testOrderId,item1Quantity+2),
            "Working method should not allow user to increase quantity");
    assertTrue(clientImp.setItemQuantityForOrder(1, this.testOrderId, 1));
    assertTrue(clientImp.setItemQuantityForOrder(2, this.testOrderId,1));
  }
}