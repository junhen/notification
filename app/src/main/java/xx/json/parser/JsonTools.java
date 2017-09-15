package xx.json.parser;

/**
 * Created by xiaoxin on 17-8-1.
 */

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 解析数据：将json字符串解析还原成原来的数据类型
 *
 * @author 郑明亮
 * @version 1.0
 * @date 2016-2-3 上午12:11:57
 */
public class JsonTools {

    private static final String TAG = "JsonTools";

    public static String createJsonString(String key, Object value) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (value instanceof Person) {
                Log.e(TAG, "createJsonString,  value instanceof Person");
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("name", ((Person) value).getName());
                jsonObject1.put("sex", ((Person) value).getSex());
                jsonObject1.put("QQ", ((Person) value).getQQ());
                jsonObject1.put("contact", ((Person) value).getContact());
                jsonObject.put(key, jsonObject1);
            } else if (value instanceof List) {
                JSONArray mapsArray = new JSONArray();
                int index = -1;
                Log.e(TAG, "createJsonString, value : " + ((List) value).size());
                for (Object ob : (List) value) {
                    if (ob instanceof Person) {
                        index = 0;
                        JSONObject j = new JSONObject();
                        j.put("name", ((Person) ob).getName());
                        j.put("sex", ((Person) ob).getSex());
                        j.put("QQ", ((Person) ob).getQQ());
                        j.put("contact", ((Person) ob).getContact());
                        mapsArray.put(j);
                    } else if (ob instanceof String) {
                        index = 1;
                        mapsArray.put((String) ob);
                    } else if (ob instanceof Map) {
                        index = 2;
                        Map m = (Map) ob;
                        Log.e(TAG, "createJsonString, map m : " + m.size());
                        JSONObject j = new JSONObject();
                        for (Object k : m.keySet()) {
                            j.put((String) k, m.get(k));
                        }
                        Log.e(TAG, "createJsonString, j : " + j);
                        mapsArray.put(j);
                    }
                }
                switch (index) {
                    case 0:
                        jsonObject.put("persons", mapsArray);
                        break;
                    case 1:
                        jsonObject.put("strings", mapsArray);
                        break;
                    case 2:
                        jsonObject.put("maps", mapsArray);
                        break;
                }
            } else {
                jsonObject.put(key, value);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public static Person getPerson(String key, String jsonString) {
        Person person = new Person();
        // 将json字符串转换成json对象
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            // 将json对象根据key(person)，拿到对应的value(Person对象)值
            JSONObject jsonObject2 = jsonObject.getJSONObject(key);
            // 将拿到的对象set到一个person对象中
            person.setName(jsonObject2.getString("name"));
            person.setSex(jsonObject2.getString("sex"));
            person.setQQ(jsonObject2.getString("QQ"));
            person.setContact(jsonObject2.getString("contact"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return person;

    }

    public static List<Person> getPersons(String key, String jsonString) {
        List<Person> list = new ArrayList<Person>();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonString);
            JSONArray Persons = jsonObject.getJSONArray(key);
            for (int i = 0; i < Persons.length(); i++) {
                Person person = new Person();
                JSONObject jsonObject2 = Persons.getJSONObject(i);
                person.setName(jsonObject2.getString("name"));
                person.setSex(jsonObject2.getString("sex"));
                person.setQQ(jsonObject2.getString("QQ"));
                person.setContact(jsonObject2.getString("contact"));
                list.add(person);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }

    public static List<String> getStrings(String key, String jsonString) {
        List<String> list = new ArrayList<String>();
        try {
            Log.e(TAG, "getStrings,  key: " + key + ",  jsonString: " + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray StringArray = jsonObject.getJSONArray(key);
            for (int i = 0; i < StringArray.length(); i++) {
                String str = StringArray.getString(i);
                Log.e(TAG, "getStrings,  i: " + i + ",  str: " + str);
                list.add(str);
            }
        } catch (Exception e) {
            Log.e(TAG, "getStrings,  exception : " + e);
            // TODO: handle exception
        }
        return list;
    }

    public static List<Map<String, Object>> getMaps(String key, String jsonString) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray mapsArray = jsonObject.getJSONArray(key);
            for (int i = 0; i < mapsArray.length(); i++) {
                JSONObject jsonObject2 = mapsArray.getJSONObject(i);
                Map<String, Object> map = new HashMap<String, Object>();
                // 查看Map中的键值对的key值
                Iterator<String> iterator = jsonObject2.keys();

                while (iterator.hasNext()) {
                    String json_key = iterator.next();
                    Object json_value = jsonObject2.get(json_key);
                    if (json_value == null) {
                        //当键值对中的value为空值时，将value置为空字符串；
                        json_value = "";
                    }
                    map.put(json_key, json_value);
                }
                list.add(map);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return list;
    }
}
