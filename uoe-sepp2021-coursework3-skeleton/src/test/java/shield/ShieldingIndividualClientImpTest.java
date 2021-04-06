/**
 *
 */

package shield;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;

import java.util.Random;

/**
 *
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private ShieldingIndividualClient client;

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

    client = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
  }


  @Test
  @DisplayName("Testing correct value for registerShieldingIndividual")
  public void testShieldingIndividualNewRegistration() {
    Random rand = new Random();
    String chi = String.valueOf(rand.nextInt(10000));
    //System.out.println(client.showFoodBoxes("none"));
    //System.out.println(client.getCateringCompanies());

    // Test functionality for new registration:
    assertTrue(client.registerShieldingIndividual(chi), "Working method (new registration) must return true.");
    assertTrue(client.isRegistered(), "Field must be true once registered.");
    // TODO clarify: how to get indiv details from server if alr registered (and using new client obj)
    assertEquals(client.getCHI(), chi, "Client-stored CHI must be the same as inputted CHI.");
    // TODO how to test functions that are not part of the java interfaces?

    // Test functionality for "already registered":
    assertTrue(client.registerShieldingIndividual(chi), "Working method (already registered) must return true.");
    assertTrue(client.isRegistered(), "Field must be true once registered.");
    assertEquals(client.getCHI(), chi, "Client-stored CHI must be the same as inputted CHI.");
    //client.pickFoodBox(1);
    //client.getClosestCateringCompany();  //TODO clarify postcode formatting error from server function (e.g. eh0111)
    //client.placeOrder();
  }

  @Test
  public void testShieldingIndividualShowFoodBodes() {
    // we check values against those in food_boxes.txt
    assertEquals(client.showFoodBoxes("none"), Arrays.asList("1","3","4"));
    assertEquals(client.showFoodBoxes("pollotarian"), Collections.singletonList("2"));
    assertEquals(client.showFoodBoxes("vegan"), Collections.singletonList("5"));


  }

}
