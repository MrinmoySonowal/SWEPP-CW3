/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class SupermarketClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private SupermarketClient client;
  private ShieldingIndividualClientImp shieldingIndv;

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

    client = new SupermarketClientImp(clientProps.getProperty("endpoint"));
    shieldingIndv = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
  }


  @Test
  @DisplayName("Testing register new supermarket")
  public void testSupermarketNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String postcode = "EH16_5AY";
    // Testing for new registration:
    assertTrue(client.registerSupermarket(name, postcode), "Method should return true upon successful registration (new)");
    assertTrue(client.isRegistered(), "Method should return true upon successful registration (new)");
    assertEquals(client.getName(), name, "Input and stored 'name' should be equal upon successful registration (new)");
    assertEquals(client.getPostCode(), postcode, "Input and stored 'postcode' should be equal upon successful registration (new)");
    // Testing for already-registered:
    assertTrue(client.registerSupermarket(name, postcode), "Method should return true upon successful registration (alr registered)");
    assertTrue(client.isRegistered(), "Method should return true upon successful registration (alr registered)");
    assertEquals(client.getName(), name, "Input and stored 'name' should be equal upon successful registration (alr registered)");
    assertEquals(client.getPostCode(), postcode, "Input and stored 'postcode' should be equal upon successful registration (alr registered)");
  }

  @Test
  @DisplayName("Testing record supermarket order")
  public void testSupermarketRecordSupermarketOrder() {
    // first initialise a shielding individual:
    Random rand = new Random();
    String dateTime = DateTimeFormatter.ofPattern("ddMMyy").format(LocalDateTime.now());
    String lastFour = String.valueOf(rand.nextInt(9999 - 1000) + 1000);
    String CHI = dateTime + lastFour;
    shieldingIndv.registerShieldingIndividual(CHI);
    client.registerSupermarket("test", "EH16_5AY");
    int orderID = rand.nextInt(10000);
    assertTrue(client.recordSupermarketOrder(CHI, orderID), "Method should return true for successful order record action");
  }

  @Test
  @DisplayName("Test update order status")
  public void testSupermarketUpdateOrderStatus() {
    Random rand = new Random();
    int orderID = rand.nextInt(1000);
    assertFalse(client.updateOrderStatus(orderID, "dispatched"), "Method cannot update non-existent order");

    String dateTime = DateTimeFormatter.ofPattern("ddMMyy").format(LocalDateTime.now());
    String lastFour = String.valueOf(rand.nextInt(9999 - 1000) + 1000);
    String CHI = dateTime + lastFour;
    shieldingIndv.registerShieldingIndividual(CHI);
    client.registerSupermarket("test", "EH16_5AY");
    client.recordSupermarketOrder(CHI, orderID);

    assertFalse(client.updateOrderStatus(orderID, "Gibberish"), "Method should return false for invalid status");
    assertTrue(client.updateOrderStatus(orderID, "dispatched"), "Method should return true for registered order");
  }

}

