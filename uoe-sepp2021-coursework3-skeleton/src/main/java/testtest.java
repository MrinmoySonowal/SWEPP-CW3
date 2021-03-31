import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import shield.ShieldingIndividualClientImp;

import java.lang.reflect.Type;
import java.util.List;

public class testtest {

    final class MessagingContents {
        int id;
        String name;
        int quantity;
    }

    public static void main(String[] args) {
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
        Type listType = new TypeToken<List<testtest.MessagingContents>>() {} .getType();
        List<MessagingContents> responseBoxes = new Gson().fromJson(contents, listType);
        for (MessagingContents responseBox : responseBoxes) {
            System.out.println(responseBox.quantity);
        }
    }

}

