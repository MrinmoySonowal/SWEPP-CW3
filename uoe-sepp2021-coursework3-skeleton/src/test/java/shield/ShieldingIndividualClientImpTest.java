/**
 *
 */

package shield;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  //private ShieldingIndividualClient client;
  private ShieldingIndividualClientImp clientImp;
  private CateringCompanyClientImp cateringImp;
  private String validRngCHI;
  private final String POSTCODE_REGEX_STRICT = "EH[0-9][0-9]_[0-9][A-Z][A-Z]";
  String testCHI = "1210782341";
  String testCaterName = "nearestCaterer";
  String testCaterPostcode = "EH55_2BT";

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

    Random rand = new Random();
    String dateTime = DateTimeFormatter.ofPattern("ddMMyy").format(LocalDateTime.now());
    String lastFour = String.valueOf(rand.nextInt(9999 - 1000) + 1000);
    this.validRngCHI = dateTime + lastFour;
  }

  // TODO (BIG): replace all unrelated methods in UnitTests with setters (public)

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
    assertFalse(clientImp.checkValidCHI(badCHI), "Working method should return false for invalid CHI");
    assertTrue(clientImp.checkValidCHI(this.validRngCHI),"Working method should return true for valid CHI");
  }

  @Test
  @DisplayName("Test helper wrapper method getAllDefaultFoodBoxesFromServer")
  public void testShieldingIndividualGetAllDefaultFoodBoxesFromServer() {
    assertNotEquals(clientImp.getAllDefaultFoodBoxesFromServer(), Collections.EMPTY_MAP,
            "Working method should return empty hashmap for invalid dietary preference");

    String request = String.format("/showFoodBox?orderOption=catering&dietaryPreference=%s", " ");
    String response = new String();
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
    assertEquals(client.showFoodBoxes("none"), Arrays.asList("1","3","4"),
            "Working method should return IDs for boxes with diet = 'none'");
    assertEquals(client.showFoodBoxes("pollotarian"), Collections.singletonList("2"),
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
      client.getDistance("eh165ay", "eh569ug");
    });
    expectedMessage = String.format("postcode (%s) is of wrong format", "eh165ay");
    actualMessage = badPostcodeErr2.getMessage();
    assertEquals(expectedMessage, actualMessage, "Working method should return True");
  }

  @Test
  //TODO to be moved to the shielding individual tests
  public void testPostcodeFormatting() {
    String iffyPostcode = "eH6 7uu";
    String goodPostcode = clientImp.formatPostcode(iffyPostcode);
    assertTrue(Pattern.matches(POSTCODE_REGEX_STRICT, goodPostcode),
            "Successfully formatted postcode should match strict postcode format");

    String iffyPostcode1 = "eH61 7uu";
    String goodPostcode1 = clientImp.formatPostcode(iffyPostcode1);
    assertTrue(Pattern.matches(POSTCODE_REGEX_STRICT, goodPostcode1),
            "Successfully formatted postcode should match strict postcode format");

    // TODO: test the following possibilities too
    String iffyPostcode2 = "eH6_7uu";
    String iffyPostcode3 = "eH61_7uu";
  }

  @Test
  @DisplayName("Test correct operation of getClosestCateringCompany")
  public void testShieldingIndividualGetClosestCaterer() {
    //client.getClosestCateringCompany();  // TODO

  }




}
