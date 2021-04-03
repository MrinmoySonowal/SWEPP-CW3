import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import shield.DietType;
import shield.ShieldingIndividualClientImp;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        try {
            String contents = "";
            Type listType = new TypeToken<List<testtest.MessagingContents>>() {
            }.getType();
            List<MessagingContents> responseBoxes = new Gson().fromJson(contents, listType);
            //System.out.println(responseBoxes);
            if (responseBoxes == null) throw new NullPointerException("NPE!");

            for (MessagingContents responseBox : responseBoxes) {
                System.out.println(responseBox.quantity);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


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

    }


}

