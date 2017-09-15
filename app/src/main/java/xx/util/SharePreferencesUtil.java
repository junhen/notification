package xx.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xiaoxin on 17-4-27.
 */

public class SharePreferencesUtil {
    private static final String TAG = "SharePreferencesUtil";
    private static final String SHARE_PREFERENCES_NAME = "settings_name";
    private static final String KEY_PRE = "key_";
    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();
    private static final String LINE_BREAKS = "\n\n";

    public static final String JSON_TEST = "json_test";
    public static final String TEXT_SCALE = "text_scale";
    public static final String TEXT_SIZE = "text_size";
    public static final String TEXT_LINE_MULTIPLIER = "text_line_multiplier";
    public static final String TEXT_BACKGROUND_COLOR = "text_background_color";
    public static final String COLOR_PANEL = "color_panel";

    //不需要考虑value的类型
    public static <T> void putSP(Context context, String key, T value) {
        SharedPreferences prefs = getPrefs(context);
        if (value instanceof String) {
            prefs.edit().putString(key, (String)value).apply();
        } else if (value instanceof Integer) {
            prefs.edit().putInt(key, (Integer) value).apply();
        } else if (value instanceof Long) {
            prefs.edit().putLong(key, (Long) value).apply();
        } else if (value instanceof Float) {
            prefs.edit().putFloat(key, (Float) value).apply();
        } else if (value instanceof Boolean) {
            prefs.edit().putBoolean(key, (Boolean) value).apply();
        } else {
            Log.d(TAG, "input error type");
        }
    }

    //返回值类型和默认值类型相同即可
    public static <T> T getSP(Context context, String key, T defaultValue) {
        SharedPreferences prefs = getPrefs(context);
        if (defaultValue instanceof String) {
            return (T) prefs.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return (T) Integer.valueOf(prefs.getInt(key, (Integer) defaultValue));
        } else if (defaultValue instanceof Long) {
            return (T) Long.valueOf(prefs.getLong(key, (Long) defaultValue));
        } else if (defaultValue instanceof Float) {
            return (T) Float.valueOf(prefs.getFloat(key, (Float) defaultValue));
        } else if (defaultValue instanceof Boolean) {
            return (T) Boolean.valueOf(prefs.getBoolean(key, (Boolean) defaultValue));
        } else {
            Log.d(TAG, "output error type");
            return null;
        }
    }

    //将新String值链接在旧值上，使用分割符breakIndex隔开
    public static void putSPLinked(Context context, String key, String breakIndex, String value) {
        SharedPreferences prefs = getPrefs(context);
        if (breakIndex == null || breakIndex.length() == 0) {
            breakIndex = LINE_BREAKS;
        }
        String oldValue = prefs.getString(key, "");
        String newValue = (oldValue.length() == 0 ? "" : oldValue + breakIndex) + value;
        prefs.edit().putString(key, newValue).apply();
    }

    //清除key对应的记录
    public static void clearSP(Context context, String key) {
        getPrefs(context).edit().remove(key).apply();
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private Context mContext;
    private String mSharedPreferencesName;
    //模式：MODE_PRIVATE, MODE_WORLD_READABLE, MODE_WORLD_WRITEABLE
    private int mSharedPreferencsMode;
    //多条记录链接时使用的分隔符
    private String mBreakIndex;

    public SharePreferencesUtil(Context context) {
        this(context, SHARE_PREFERENCES_NAME);
    }

    public SharePreferencesUtil(Context context, String sharePreferencesName) {
        this(context, sharePreferencesName, Context.MODE_PRIVATE);
    }
    
    public SharePreferencesUtil(Context context, String sharePreferencesName, int mode) {
        mContext = context;
        mSharedPreferencesName = sharePreferencesName;
        mSharedPreferencsMode = mode;
        mBreakIndex = LINE_BREAKS;
    }
    
    public SharedPreferences getSharedPreferences() {
        return mContext.getSharedPreferences(mSharedPreferencesName, mSharedPreferencsMode);
    }
    
    public SharePreferencesUtil setBreakIndex(String index) {
        mBreakIndex = index;
        return this;
    }

    public void saveValue(String key, String value) {
        SharedPreferences sp = getSharedPreferences();
        sp.edit().putString(key, value).apply();
    }
    
    public void saveValue(String key, String value, boolean replace) {
        SharedPreferences sp = getSharedPreferences();
        String oldValue = sp.getString(key, "");
        String newValue = (replace ? "" : oldValue + mBreakIndex) + value;
        sp.edit().putString(key, newValue).apply();
    }

    public String getvalue(String key) {
        return getSharedPreferences().getString(key, "");
    }
}
