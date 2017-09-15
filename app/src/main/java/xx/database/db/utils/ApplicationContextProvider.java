package xx.database.db.utils;

import android.content.Context;

public class ApplicationContextProvider {

    private Context mContext;

    private ApplicationContextProvider() {
    }

    private static class ContextProviderHolder {
        private static final ApplicationContextProvider instance = new ApplicationContextProvider();
    }

    public static void setApplicationContext(Context context) {
        ContextProviderHolder.instance.mContext = context;
    }

    public static Context getApplicationContext() {
        return ContextProviderHolder.instance.mContext;
    }

    public static void release() {
        ContextProviderHolder.instance.mContext = null;
    }

}
