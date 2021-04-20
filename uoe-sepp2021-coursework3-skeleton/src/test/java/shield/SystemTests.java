package shield;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NOTE TO MARKER:
 * Please remove all empty newlines at the start of the database .txt and .csv files after
 * initialisation -- these unwanted empty lines will break the code (PIAZZA Post @832).
 *
 * Contains tests to verify general functionality and use-case-centric functionality of system.
 */
public class SystemTests {

    private final static String clientPropsFilename = "client.cfg";

    private Properties clientProps;
    private ShieldingIndividualClientImp shieldingImp;
    private CateringCompanyClientImp cateringImp;
    private SupermarketClientImp supermarketImp;
    private String validRngCHI;

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
        shieldingImp = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
        cateringImp = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
        supermarketImp = new SupermarketClientImp(clientProps.getProperty("endpoint"));

        Random rand = new Random();
        String dateTime = DateTimeFormatter.ofPattern("ddMMyy").format(LocalDateTime.now());
        String lastFour = String.valueOf(rand.nextInt(9999 - 1000) + 1000);
        this.validRngCHI = dateTime + lastFour;
    }

    @Test
    @DisplayName("General system test")
    public void systemTests() {
        // register supermarket, catering company and shielding individual
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(cateringImp.registerCateringCompany("caterer1","EH16_5AY"));
        assertTrue(cateringImp.registerCateringCompany("caterer2","EH17_5AY"));
        assertTrue(supermarketImp.registerSupermarket("supermarket", "EH14_2BU"));
        List<String> caterers = List.of("caterer1","caterer2");

        // pick food box and place order
        assertTrue(caterers.contains(shieldingImp.getClosestCateringCompany()));
        List<String> fbIDs = (List<String>) shieldingImp.showFoodBoxes("none");
        List<String> noneFbIds = List.of("1","3","4");
        assertEquals(fbIDs, noneFbIds);
        assertTrue(shieldingImp.pickFoodBox(1));
        assertTrue(shieldingImp.placeOrder());

        List<Integer> orderIds = new ArrayList<>(shieldingImp.getOrdersDict().keySet());
        int ourOrderId = orderIds.get(0);

        // edit order
        int itemQty = shieldingImp.getItemQuantityForFoodBox(2,1); // have not edited the food box
        assertEquals(shieldingImp.getItemQuantityForOrder(2, ourOrderId),itemQty);
        assertTrue(shieldingImp.setItemQuantityForOrder(2, ourOrderId, itemQty-1));
        assertTrue(shieldingImp.editOrder(ourOrderId));
        // edit Order should reduce quantity
        assertEquals(shieldingImp.getItemQuantityForOrder(2, ourOrderId),itemQty-1);

        // request update status
        assertTrue(shieldingImp.requestOrderStatus(ourOrderId));

        // update order status
        assertTrue(cateringImp.updateOrderStatus(ourOrderId, "packed"));

        // cancel order
        assertTrue(shieldingImp.cancelOrder(ourOrderId));
    }

    @Test
    @DisplayName("Test Register Shielding Individual Use Case")
    public void testRegisterShieldingIndividual() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(shieldingImp.isRegistered());
        assertEquals(shieldingImp.getCHI(),this.validRngCHI);
    }

    @Test
    @DisplayName("Test Register Catering Company")
    public void testRegisterCateringCompany() {
        assertTrue(cateringImp.registerCateringCompany("cat1", "EH44_9BH"));
        assertTrue(cateringImp.isRegistered());
        assertEquals(cateringImp.getName(), "cat1");
        assertEquals(cateringImp.getPostCode(), "EH44_9BH");
    }

    @Test
    @DisplayName("Test Register Supermarket")
    public void testRegisterSupermarket() {
        assertTrue(supermarketImp.registerSupermarket("supermarket1", "EH07_9EB"));
        assertTrue(supermarketImp.isRegistered());
        assertEquals(supermarketImp.getName(),"supermarket1" );
        assertEquals(supermarketImp.getPostCode(), "EH07_9EB");
    }

    @Test
    @DisplayName("Test Place Order Main Success Scenario")
    public void testPlaceFoodBoxOrderMainSuccess() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(cateringImp.registerCateringCompany("caterer1","EH16_5AY"));
        assertTrue(cateringImp.registerCateringCompany("caterer2","EH17_5AY"));
        List<String> caterers = List.of("caterer1","caterer2");

        // pick food box and place order
        assertTrue(caterers.contains(shieldingImp.getClosestCateringCompany()));
        List<String> fbIDs = (List<String>) shieldingImp.showFoodBoxes("none");
        List<String> noneFbIds = List.of("1","3","4");
        int numberOfInitialOrders = shieldingImp.getOrderNumbers().size();
        assertEquals(fbIDs, noneFbIds);
        assertTrue(shieldingImp.pickFoodBox(1));
        assertTrue(shieldingImp.placeOrder());
        // Testing if umber of orders increased by 1
        assertEquals(shieldingImp.getOrderNumbers().size(), numberOfInitialOrders+1);

    }

    @Test
    @DisplayName("Test Place Order Extension: edit food box before placing order")
    public void testPlaceFoodBoxOrderExt() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(cateringImp.registerCateringCompany("caterer1","EH16_5AY"));
        assertTrue(cateringImp.registerCateringCompany("caterer2","EH17_5AY"));
        List<String> caterers = List.of("caterer1","caterer2");

        // pick food box and place order
        assertTrue(caterers.contains(shieldingImp.getClosestCateringCompany()));
        List<String> fbIDs = (List<String>) shieldingImp.showFoodBoxes("none");
        List<String> noneFbIds = List.of("1","3","4");
        assertEquals(fbIDs, noneFbIds);
        assertTrue(shieldingImp.pickFoodBox(1));

        // Edit picked food box before placing order
        assertTrue(shieldingImp.changeItemQuantityForPickedFoodBox(2,1));
        assertEquals(shieldingImp.getPickedFoodBox().getItemsDict().get(2).getQuantity(), 1);

        assertTrue(shieldingImp.placeOrder());
    }

    @Test
    @DisplayName("Test Edit Food Box Order Main Success Scenario")
    public void testEditFoodBoxMainSuccess() {
        // register supermarket, catering company and shielding individual
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(cateringImp.registerCateringCompany("caterer1","EH16_5AY"));
        List<String> caterers = List.of("caterer1","caterer2");

        // pick food box and place order
        assertTrue(caterers.contains(shieldingImp.getClosestCateringCompany()));
        List<String> fbIDs = (List<String>) shieldingImp.showFoodBoxes("none");
        List<String> noneFbIds = List.of("1","3","4");
        assertEquals(fbIDs, noneFbIds);
        assertTrue(shieldingImp.pickFoodBox(1));
        assertTrue(shieldingImp.placeOrder());

        // edit order
        List<Integer> orderIds = new ArrayList<>(shieldingImp.getOrdersDict().keySet());
        int ourOrderId = orderIds.get(0);
        int itemQty = shieldingImp.getItemQuantityForFoodBox(2,1); // have not edited the food box
        assertEquals(shieldingImp.getItemQuantityForOrder(2, ourOrderId),itemQty);
        assertTrue(shieldingImp.setItemQuantityForOrder(2, ourOrderId, itemQty-1));
        assertTrue(shieldingImp.editOrder(ourOrderId));
        // edit Order should reduce quantity
        assertEquals(shieldingImp.getItemQuantityForOrder(2, ourOrderId),itemQty-1);
    }

    @Test
    @DisplayName("Test Edit Food Box Order Extension Scenarios")
    public void testEditFoodBoxExts() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(cateringImp.registerCateringCompany("caterer1","EH16_5AY"));
        List<String> caterers = List.of("caterer1","caterer2");

        // pick food box and place order
        assertTrue(caterers.contains(shieldingImp.getClosestCateringCompany()));
        List<String> fbIDs = (List<String>) shieldingImp.showFoodBoxes("none");
        List<String> noneFbIds = List.of("1","3","4");
        assertEquals(fbIDs, noneFbIds);
        assertTrue(shieldingImp.pickFoodBox(1));
        assertTrue(shieldingImp.placeOrder());

        // edit order
        List<Integer> orderIds = new ArrayList<>(shieldingImp.getOrdersDict().keySet());
        int ourOrderId = orderIds.get(0);
        int itemQty = shieldingImp.getItemQuantityForFoodBox(2,1); // have not edited the food box
        assertEquals(shieldingImp.getItemQuantityForOrder(2, ourOrderId),itemQty);

        assertFalse(shieldingImp.setItemQuantityForOrder(2, ourOrderId, itemQty+1),
                "Edit Order should not allow increase in item quantity");

        assertTrue(cateringImp.updateOrderStatus(ourOrderId, "dispatched"));  // order delivered
        assertTrue(shieldingImp.requestOrderStatus(ourOrderId));
        assertFalse(shieldingImp.editOrder(ourOrderId), "Order cannot be edited unless status = placed or packed");
    }

    @Test
    @DisplayName("Test Cancel Order")
    public void testCancelOrder() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(cateringImp.registerCateringCompany("caterer1","EH16_5AY"));
        List<String> caterers = List.of("caterer1","caterer2");

        // pick food box and place order
        assertTrue(caterers.contains(shieldingImp.getClosestCateringCompany()));
        List<String> fbIDs = (List<String>) shieldingImp.showFoodBoxes("none");
        List<String> noneFbIds = List.of("1","3","4");
        assertEquals(fbIDs, noneFbIds);
        assertTrue(shieldingImp.pickFoodBox(1));
        assertTrue(shieldingImp.placeOrder());

        List<Integer> orderIds = new ArrayList<>(shieldingImp.getOrdersDict().keySet());
        int ourOrderId = orderIds.get(0);

        assertTrue(shieldingImp.cancelOrder(ourOrderId));
        assertFalse(shieldingImp.requestOrderStatus(ourOrderId),
                "Method returns false as order doesn't exist anymore");

        AssertionError orderNotFoundError = assertThrows(AssertionError.class, () -> {
            shieldingImp.getStatusForOrder(ourOrderId);
        });
        String expectedErrorMessage = "Order not found";
        assertEquals(orderNotFoundError.getMessage(), expectedErrorMessage,
                "Working method should successfully cancel order");
    }

    @Test
    @DisplayName("Test Cancel Order Extensions")
    public void testCancelOrderExts() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(cateringImp.registerCateringCompany("caterer1","EH16_5AY"));
        List<String> caterers = List.of("caterer1","caterer2");

        // pick food box and place order
        assertTrue(caterers.contains(shieldingImp.getClosestCateringCompany()));
        List<String> fbIDs = (List<String>) shieldingImp.showFoodBoxes("none");
        List<String> noneFbIds = List.of("1","3","4");
        assertEquals(fbIDs, noneFbIds);
        assertTrue(shieldingImp.pickFoodBox(1));
        assertTrue(shieldingImp.placeOrder());

        List<Integer> orderIds = new ArrayList<>(shieldingImp.getOrdersDict().keySet());
        int ourOrderId = orderIds.get(0);

        assertFalse(shieldingImp.cancelOrder(ourOrderId+2),
                "Method returns false when trying to cancel invalid order");

        assertTrue(cateringImp.updateOrderStatus(ourOrderId, "dispatched"));  // order delivered
        assertTrue(shieldingImp.requestOrderStatus(ourOrderId));
        assertFalse(shieldingImp.cancelOrder(ourOrderId),
                "Method returns false when trying to cancel dispatched, delivered or already-cancelled orders");
    }
    @Test
    @DisplayName("Test Catering Company Update Order main success scenarios")
    public void testCateringUpdateOrderStatusMainSuccess() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(cateringImp.registerCateringCompany("caterer1","EH16_5AY"));
        List<String> caterers = List.of("caterer1","caterer2");
        assertTrue(caterers.contains(shieldingImp.getClosestCateringCompany()));
        // pick food box and place order
        List<String> fbIDs = (List<String>) shieldingImp.showFoodBoxes("none");
        List<String> noneFbIds = List.of("1","3","4");
        assertEquals(fbIDs, noneFbIds);
        assertTrue(shieldingImp.pickFoodBox(1));
        assertTrue(shieldingImp.placeOrder());

        List<Integer> orderIds = new ArrayList<>(shieldingImp.getOrdersDict().keySet());
        int ourOrderId = orderIds.get(0);

        assertTrue(cateringImp.updateOrderStatus(ourOrderId, "dispatched"));  // order delivered
        assertTrue(shieldingImp.requestOrderStatus(ourOrderId));
        String expectedStatus = "2" ; // Status for ORDER_DISPATCHED in ShieldingIndividualClientImp class
        assertEquals(shieldingImp.getStatusForOrder(ourOrderId), expectedStatus);

        assertTrue(cateringImp.updateOrderStatus(ourOrderId, "delivered"));  // order delivered
        assertTrue(shieldingImp.requestOrderStatus(ourOrderId));
        expectedStatus = "3" ; // Status for ORDER_DELIVERED in ShieldingIndividualClientImp class
        assertEquals(shieldingImp.getStatusForOrder(ourOrderId), expectedStatus);
    }

    @Test
    @DisplayName("Test Update Supermarket order status")
    public void testUpdateSupermarketOrderStatus() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        Random rand = new Random();
        int orderNumber =rand.nextInt(1000) ;
        assertFalse(supermarketImp.recordSupermarketOrder(this.validRngCHI, orderNumber),
                "Working method should return false as supermarket is not registered yet");
        assertTrue(supermarketImp.registerSupermarket("supermarket1", "EH07_9EB"));
        assertTrue(supermarketImp.isRegistered());
        assertTrue(supermarketImp.recordSupermarketOrder(this.validRngCHI, orderNumber),
                "Working method should return true ");

        assertFalse(supermarketImp.updateOrderStatus(orderNumber, "Gibberish"), "Method should return false for invalid status");
        assertTrue(supermarketImp.updateOrderStatus(orderNumber, "dispatched"));  // order dispatched
    }

    @Test
    @DisplayName("Test Request Order Status")
    public void testRequestOrderStatus() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertTrue(cateringImp.registerCateringCompany("caterer1","EH16_5AY"));
        List<String> caterers = List.of("caterer1","caterer2");
        assertTrue(caterers.contains(shieldingImp.getClosestCateringCompany()));
        // pick food box and place order
        List<String> fbIDs = (List<String>) shieldingImp.showFoodBoxes("none");
        List<String> noneFbIds = List.of("1","3","4");
        assertEquals(fbIDs, noneFbIds);
        assertTrue(shieldingImp.pickFoodBox(1));
        assertTrue(shieldingImp.placeOrder());
        List<Integer> orderIds = new ArrayList<>(shieldingImp.getOrdersDict().keySet());
        int ourOrderId = orderIds.get(0);
        assertTrue(shieldingImp.requestOrderStatus(ourOrderId));
    }

    @Test
    @DisplayName("Test Request Order Status Extension Scenarios")
    public void testRequestOrderStatusExt() {
        assertTrue(shieldingImp.registerShieldingIndividual(this.validRngCHI));
        assertFalse(shieldingImp.requestOrderStatus(-1),
                "Method should return false if orderID does not exist");
    }

}
