package field.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 由于消息是以Json的形式进行传递的，因此需要频繁地进行转换
 * Created by Hong on 2017/7/12.
 */
public class JsonUtil {

    private static final Gson gson;

    static
    {
        gson = new Gson();
    }

    /**
     * 将Java对象转换成Json文件
     * @param object Java对象
     * @return Json文件
     */
    public static String toJson(Object object){
        return gson.toJson(object);
    }

    /**
     * 将Json文件转换成Java对象
     * @param json json文件
     * @param tClass Java对象
     * @param <T> 泛型
     * @return 转换后的Java对象
     */
    public static <T> T toObject(String json, Class<T> tClass){
        return gson.fromJson(json, tClass);
    }

    /**
     * 获取Json文件中有关类名的字符串
     * @param json Json文件
     * @return 有关类名的字符串
     * @throws Exception
     */
    public static String getClassName(String json) throws Exception{
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(json).getAsJsonObject();
        JsonElement jsonElement = jsonObject.get("_class");
        if (jsonElement != null)
            return jsonElement.getAsString();
        return null;
    }

}
