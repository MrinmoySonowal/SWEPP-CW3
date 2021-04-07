/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import java.util.Collection;
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
  private CateringCompanyClient client;

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

    client = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
  }


  @Test
  public void testCateringCompanyNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String badPostcode = String.valueOf(rand.nextInt(10000));

    AssertionError badPostcodeErr = assertThrows(AssertionError.class, () -> {
      client.registerCateringCompany(name, badPostcode);
    });
    String expectedMessage = String.format("Postcode %s is the wrong format", badPostcode);
    String actualMessage = badPostcodeErr.getMessage();
    assertEquals(expectedMessage, actualMessage);

    assertTrue(client.registerCateringCompany(name, "EH16_5AY"));
    //assertTrue(client.isRegistered());
    //assertEquals(client.getName(), name);
  }

  @Test
  public void testCateringCompanyUpdateOrderStatus(){
    Random rand = new Random();
    String[] validStatuses= {"packed", "dispatched", "delivered"};
    String status = validStatuses[rand.nextInt(validStatuses.length)];
  }
}
