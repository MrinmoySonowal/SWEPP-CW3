import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import shield.BoxItem;
import shield.FoodBoxOrder;
import shield.ShieldingIndividualClientImp;

import javax.swing.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class testtest {

    final class MessagingContents {
        int id;
        String name;
        int quantity;
    }

    private final List<String> DIET_TYPES = List.of("none", "pollotarian", "vegan");

    public static void main(String[] args) {
        /*
        String contents = "[\n" +
                "            {\n" +
                "              \"id\":1,\n" +
                "              \"name\":\"cucumbers\",\n" +
                "              \"quantity\":1\n" +
                "            },\n" +
                "            {\n" +
                "              \"id\":2,\n" +
                "              \"name\":\"tomatoes\",\n" +
                "              \"quantity\":2\n" +
                "            },\n" +
                "            {\n" +
                "              \"id\":6,\n" +
                "              \"name\":\"pork\",\n" +
                "              \"quantity\":1\n" +
                "            }\n" +
                "            ]";
        */

        /*
        try {
            String contents = "";
            Type listType = new TypeToken<List<testtest.MessagingContents>>() {
            }.getType();
            List<MessagingContents> responseBoxes = new Gson().fromJson(contents, listType);
            //System.out.println(responseBoxes);
            //if (responseBoxes == null) throw new NullPointerException("NPE!");
            assert responseBoxes != null : "NPE!";

            for (MessagingContents responseBox : responseBoxes) {
                System.out.println(responseBox.quantity);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        List<BoxItem> testList = new ArrayList<>();
        BoxItem item1 = new BoxItem();
        item1.setId(1);
        item1.setName("item1");
        item1.setQuantity(1);
        BoxItem item2 = new BoxItem();
        item2.setId(2);
        item2.setName("item2");
        item2.setQuantity(2);
        testList.add(item1);
        testList.add(item2);

        HashMap<String, List<BoxItem>> dict = new HashMap<>();
        dict.put("contents", testList);

        Gson gson = new Gson();
        String items = gson.toJson(testList);
        System.out.printf("{\"contents\":%s}%n", items);

        FoodBoxOrder order = new FoodBoxOrder();
        Map<Integer, BoxItem> itemsDict = new HashMap<>();
        itemsDict.put(item1.getId(), item1);
        itemsDict.put(item2.getId(), item2);
        order.setItemsDict(itemsDict);
        System.out.println(order.getItemsDict());
        //order.getItemsDict().get(1).setQuantity(100);
        Map<Integer, BoxItem> newMap = order.getItemsDict();
        newMap.get(1).setQuantity(100);
        System.out.println(newMap);
        System.out.println(order.getItemsDict());

        String testString = "eh165ay";
        String s0 = testString.substring(0,4);
        String s1 = testString.substring(4);
        System.out.println(s0);
        System.out.println(s1);
        */


        boolean b3 = Pattern.matches("EH[0-9][0-9]_[0-9][A-Z][A-Z]","Eh16_5001");
        System.out.println(b3);


        /*
        List<String> testList = new ArrayList<>();
        //System.out.println("sdf");
        for (String str : testList) {
            //System.out.println(str);
        }
        //System.out.println("adf");


        Map<Integer, Integer> hashm = new HashMap<>();
        hashm.put(1,1);
        hashm.put(2,2);
        hashm.put(3,3);
        System.out.println(hashm.size());
        */

        try {
            System.out.println("try start wololo");
            assert(1==2);
            System.out.println("after assert");
        } catch (Exception e) {
            System.out.println("catch block reached");
        }

    }


}

