import me.ponktacology.tag.Constants;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

public class Test {

    public static void main(String[] args) {
       for (Field field : Constants.class.getFields()) {
           System.out.println(field.getName());
       }
    }
}
