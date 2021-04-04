package shield;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class FoodBoxOrder {

    private int orderID;
    private String name;
    private String dietType;
    private String orderStatus;
    private DeliveryStatus deliveryStatus;
    private String deliveryService;
    private Map<Integer, BoxItem> itemsDict = new HashMap<>();
    private String orderSource;
    private LocalDateTime deliveryTime;

    public FoodBoxOrder() {}

    public String getDietType() {
        return dietType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getDeliveryService() {
        return deliveryService;
    }

    public String getOrderSource() {
        return orderSource;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDietType(String dietType) {
        this.dietType = dietType;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public void setDeliveryService(String deliveryService) {
        this.deliveryService = deliveryService;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, BoxItem> getItemsDict() {
        return itemsDict;
    }

    public void setItemsDict(Map<Integer, BoxItem> itemsDict) {
        this.itemsDict = itemsDict;
    }
}
