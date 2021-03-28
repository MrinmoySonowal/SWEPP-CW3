package shield;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Dictionary;

public class FoodBoxOrder {
    public FoodBoxOrder(int foodBoxID){
    }

    private DietType dietType;
    private OrderStatus orderStatus;
    private DeliveryStatus deliveryStatus;
    private String deliveryService;
    private Dictionary<Integer, Integer> itemIDs;
    private Dictionary<DietType, Collection<Integer>> dietDefaultItemIDs;
    private String orderSource;
    private LocalDateTime deliveryTime;




}
