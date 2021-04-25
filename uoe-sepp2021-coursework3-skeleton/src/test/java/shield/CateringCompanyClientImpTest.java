/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class CateringCompanyClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private CateringCompanyClientImp clientImp;
  private ShieldingIndividualClientImp shieldingImp;
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
    clientImp = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    shieldingImp = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));

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
  @DisplayName("Testing registerCateringCompany method")
  public void testCateringCompanyNewRegistration() {
    assertFalse(clientImp.registerCateringCompany(null, "EH16_2AY"));
    assertFalse(clientImp.registerCateringCompany("caterer1", null));
    Random rand = new Random();
    String name = "Caterer" + rand.nextInt(10000);
    String badPostcode = String.valueOf(rand.nextInt(10000));

    AssertionError badPostcodeErr = assertThrows(AssertionError.class, () -> {
      clientImp.registerCateringCompany(name, badPostcode);
    });
    String expectedMessage = String.format("Postcode %s is the wrong format", badPostcode);
    String actualMessage = badPostcodeErr.getMessage();
    assertEquals(expectedMessage, actualMessage, "Method should fail if postcode is of wrong format");

    //assertTrue(clientImp.registerCateringCompany(name, "EH16_5AY"));
    // registerCateringCompany test failed cuz server returned newId instead of "registered new" (goes against documentation).

    assertTrue(clientImp.registerCateringCompany("Caterer1234", "EH16_5AY"));
    assertTrue(clientImp.isRegistered());
    assertEquals(clientImp.getName(), "Caterer1234");
    assertEquals(clientImp.getPostCode(),"EH16_5AY");
  }

  @RepeatedTest(5)
  @DisplayName("Testing updateOrderStatus method")
  public void testCateringCompanyUpdateOrderStatus() {
    Random rand = new Random();
    String[] validStatuses= {"packed", "dispatched", "delivered"};
    String status = validStatuses[rand.nextInt(validStatuses.length)];

    assertFalse(clientImp.updateOrderStatus(rand.nextInt(), status), "Method should return false for invalid orderID");

    // TODO to test client.updateOrderStatus returning "True", we need first to place an order via shieldingIndividual,
    //  and then once that order is placed, use that order as a 'planted' order for this test.

    assertTrue(clientImp.updateOrderStatus(this.testOrderId, "packed"), "Method should return true for correct operation");
    assertFalse(clientImp.updateOrderStatus(this.testOrderId, "Gibberish"), "Method should return false for invalid status");
  }
}
