/**
 * Interface for the communication endpoints of an application for a shielding
 * individual.
 *
 * These endpoints represent the communication endpoints to the server
 *
 * @author
 *
 */

package shield;

import java.util.Collection;
import java.time.LocalDateTime;

public interface ShieldingIndividualClientEndpoints {
  /**
  * Returns true if the operation occurred correctly
  *
  * @param CHI CHI number of the shiedling individual
  * @return true if the operation occurred correctly
  */
  public boolean registerShieldingIndividual(String CHI);

  // **UPDATE** javadoc comment fix
  /**
  * Returns collection of food box ids if the operation occurred correctly
  *
  * @param dietary preference
  * @return collection of food box ids
  */
  public Collection<String> showFoodBoxes(String dietaryPreference);

  /**
  * Returns true if the operation occurred correctly
  *
  * @param deliveryDateTime the requested delivery date and time
  * @return true if the operation occurred correctly
  */
  public boolean placeOrder(LocalDateTime deliveryDateTime);

  /**
  * Returns true if the operation occurred correctly
  *
  * @param orderNumber the order number
  * @return true if the operation occurred correctly
  */
  public boolean editOrder(int orderNumber);

  /**
  * Returns true if the operation occurred correctly
  *
  * @param orderNumber the order number
  * @return true if the operation occurred correctly
  */
  public boolean cancelOrder(int orderNumber);

  /**
  * Returns true if the operation occurred correctly
  *
  * @param orderNumber the order number
  * @return true if the operation occurred correctly
  */
  public boolean requestOrderStatus(int orderNumber);

  // **UPDATE**
  /**
  * Returns collection of catering companies and their locations
  *
  * @return collection of catering companies and their locations
  */
  public Collection<String> getCateringCompanies();

  // **UPDATE**
  /**
  * Returns the distance between two locations based on their post codes
  *
  * @param postCode1 post code of one location
  * @param postCode2 post code of another location
  * @return the distance as a float between the two locations
  */
  public float getDistance(String postCode1, String postCode2);
}
