package camp.kuznetsov.rn.odnoklassniki;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import javax.annotation.Nullable;

public class JSONUtil {

    @Nullable
    public static WritableMap convertMap(JSONObject jsonObject) {
        if (jsonObject == null)
            return null;

        WritableMap result = Arguments.createMap();

        Iterator<String> iterator = jsonObject.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();

            try {
                Object value = jsonObject.get(key);

                if (value == JSONObject.NULL) {
                    result.putNull(key);
                } else if (value instanceof Boolean) {
                    result.putBoolean(key, (Boolean) value);
                } else if (value instanceof Float || value instanceof Double) {
                    result.putDouble(key, (Double) value);
                } else if (value instanceof Integer || value instanceof Number) {
                    result.putInt(key, (Integer) value);
                } else if (value instanceof String) {
                    result.putString(key, (String) value);
                } else if (value instanceof JSONObject) {
                    result.putMap(key, convertMap((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    result.putArray(key, convertArray((JSONArray) value));
                }
            } catch (JSONException e) { }
        }

        return result;
    }

    @Nullable
    public static WritableArray convertArray(JSONArray jsonArray) {
        if (jsonArray == null)
            return null;

        WritableArray result = Arguments.createArray();

        for (int i = 0 ; i < jsonArray.length(); i++) {
            try {
                Object value = jsonArray.get(i);
                if (value == null) {
                    result.pushNull();
                } else if (value instanceof Boolean) {
                    result.pushBoolean((Boolean) value);
                } else if (value instanceof Double || value instanceof Float) {
                    result.pushDouble((Double) value);
                } else if (value instanceof Integer || value instanceof Number) {
                    result.pushInt((Integer) value);
                } else if (value instanceof String) {
                    result.pushString((String) value);
                } else if (value instanceof JSONObject) {
                    result.pushMap(convertMap((JSONObject) value));
                } else if (value instanceof JSONArray) {
                    result.pushArray(convertArray((JSONArray) value));
                }
            } catch (JSONException e) {}
        }

        return result;
    }
}
