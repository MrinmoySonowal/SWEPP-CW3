package shield;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Dictionary;

public class FoodBoxOrder {
    public FoodBoxOrder(int foodBoxID){
    }

    private String dietType;
    private String orderStatus;
    private DeliveryStatus deliveryStatus;
    private String deliveryService;
    private Dictionary<Integer, Integer> itemIDs;
    //private Dictionary<DietType, Collection<Integer>> dietDefaultItemIDs;
    private String orderSource;
    private LocalDateTime deliveryTime;

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

    public Dictionary<Integer, Integer> getItemIDs() {
        return itemIDs;
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

    public void setItemIDs(Dictionary<Integer, Integer> itemIDs) {
        this.itemIDs = itemIDs;
    }

    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
}
