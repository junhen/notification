package xx.util;

/**
 * Created by xiaoxin on 17-6-20.
 */

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

public class WidgetInfoHelper {
    public static class WidgetInfo {
        String mPackage;
        String mClass;
        @Override
        public String toString() {
            return "WidgetInfo{" + mPackage + "/" + mClass + "}";
        }
    }

    private static final String TAG = WidgetInfoHelper.class.getSimpleName();
    private static final Intent WIDGET_INTENT = new Intent("com.android.keyguard.widget");
    private static final String WIDGET_META_DATA = "xx.widget.component.name";//区别与锁屏的widget.component.name

    public static ComponentName getWidgeComponentName(Context ctx){
        WidgetInfo wi = getWidgetInfo(ctx);
        if(wi != null){
            return new ComponentName(wi.mPackage,wi.mClass);
        }
        return null;
    }
    public static  WidgetInfo getWidgetInfo(Context ctx){
        return getWidgetInfo(WIDGET_INTENT,ctx);
    }

    private static WidgetInfo getWidgetInfo(Intent intent,Context ctx) {
        WidgetInfo info = new WidgetInfo();
        PackageManager packageManager = ctx.getPackageManager();
        final List<ResolveInfo> appList = packageManager.queryBroadcastReceivers(
                intent, PackageManager.MATCH_ALL | PackageManager.GET_META_DATA);
        if (appList.size() == 0) {
            Log.w(TAG, "appList.size() == 0,intent:"+intent);
            return null;
        }
        //ActivityInfo activityInfo = appList.get(0).activityInfo;
        for(ResolveInfo resolveinfo : appList) {
            Log.w(TAG, "appList.size(): "+appList.size()+",  +resolveinfo: "+resolveinfo);
            ActivityInfo activityInfo = resolveinfo.activityInfo;
            if (activityInfo.metaData != null && !activityInfo.metaData.isEmpty()) {
                String widgetClass = activityInfo.metaData.getString(WIDGET_META_DATA);
                Log.w(TAG, "appList.size(): "+appList.size()+",  +widgetClass: "+widgetClass);
                if (TextUtils.isEmpty(widgetClass)) {
                    Log.w(TAG, "metaData:" + activityInfo.metaData + " intent:" + intent);
                    //return null;
                    continue;
                }
                info.mPackage = activityInfo.packageName;
                info.mClass = widgetClass;
                Log.v(TAG, info.toString() + " activityInfo:" + activityInfo);
                return info;
            }
        }
        return null;
    }
}
