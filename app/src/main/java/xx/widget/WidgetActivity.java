package xx.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
//import android.widget.RemoteViews.OnClickHandler;

import com.leui.notification.test.R;

import xx.activity.manager.AppManager;
import xx.util.SharePreferencesUtil;
import xx.util.WidgetInfoHelper;

public class WidgetActivity extends Activity implements View.OnClickListener{

    private String TAG = "WidgetActivity";
    private Button mAddWidgetBn, mRemoveWidgetBn;
    private LinearLayout mLl;
    private AppWidgetHost mAppWidgetHost;
    private AppWidgetManager mAppWidgetManager;
    public static final int APPWIDGET_HOST_ID = 0x4B455950;
    public static final String ACTION_KEYGUARD_INSTALL_WIDGET = "com.leui.keyguard.action.INSTALL_WIDGET";
    private static final String EXTRA_KEYGUARD_APPWIDGET_COMPONENT = "com.leui.keyguard.extra.widget.COMPONENT";
    private static final String WIDGET_PACKAGE = "com.leui.notification.test";
    private static final String WIDGET_CLASS = "xx.widget.WidgetProvider";
    private static final String HOLD_WIDGET = "hold_widget";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private ComponentName componentName;
    private boolean mSucess;
    private Display mDisplay;
    private Point mCurrentDisplaySize = new Point();
    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_widget);
        mAddWidgetBn = (Button) findViewById(R.id.add_widget_bn);
        mAddWidgetBn.setOnClickListener(this);
        mRemoveWidgetBn = (Button) findViewById(R.id.remove_widget_bn);
        mRemoveWidgetBn.setOnClickListener(this);
        mLl = (LinearLayout)findViewById(R.id.widget_ll);
        mAppWidgetHost = new AppWidgetHost(this, APPWIDGET_HOST_ID);
        mAppWidgetManager = AppWidgetManager.getInstance(this);

        //直接根据报名和广播名获得ComponentName
        //componentName = new ComponentName(WIDGET_PACKAGE, WIDGET_CLASS);

        //使用action+metadata获得ComponentName
        componentName = WidgetInfoHelper.getWidgeComponentName(this);
        /*if (savedInstanceState != null && savedInstanceState.getInt("appWidgetId") != AppWidgetManager.INVALID_APPWIDGET_ID) {
            mLl.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAddWidgetBn.performClick();
                }
            }, 200);
        }*/
        boolean addWidget = SharePreferencesUtil.getSP(this, HOLD_WIDGET, false);
        Log.d(TAG,"onCreate   addWidget = "+addWidget);
        if (addWidget) {
            mLl.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mAddWidgetBn.performClick();
                }
            }, 200);
        }
        mDisplay = ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        AppManager.getAppManager().addActivity(this);
    }

    @Override
    protected void onRestart() {
        Log.d(TAG,"onRestart");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        Log.d(TAG,"onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"onSaveInstanceState");
        super.onSaveInstanceState(outState);
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            outState.putInt("appWidgetId", appWidgetId);
        }
    }

    @Override
    protected void onStop() {
        Log.d(TAG,"onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            //SharePreferencesUtil.getPrefs(this).edit().putBoolean(HOLD_WIDGET, true).apply();
            SharePreferencesUtil.putSP(this, HOLD_WIDGET, true);
            mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        }
        AppManager.getAppManager().removeActivity(this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        Log.d(TAG,"onRestoreInstanceState  2");
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG,"onRestoreInstanceState  1");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG,"onConfigurationChanged,  newConfig = "+newConfig);
        updateDisplaySize();
        /*ViewGroup.LayoutParams lp = mLl.getLayoutParams();
        Log.d(TAG,"onConfigurationChanged,  lp(.width * height) = ("+lp.width+" * "+lp.height+")");
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        Log.d(TAG,"onConfigurationChanged,  lp(.width * height) = ("+lp.width+" * "+lp.height+")");
        mLl.setLayoutParams(lp);*/
    }

    private int[] getWidgetProviderId() {
        return mAppWidgetManager.getAppWidgetIds(new ComponentName(this, xx.widget.WidgetProvider.class));
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick  v: " + v);

        if(v.getId() == R.id.add_widget_bn) {
            //这个是给乐视锁屏加载widget的广播
            /*Intent intent = new Intent(ACTION_KEYGUARD_INSTALL_WIDGET);
            intent.putExtra(EXTRA_KEYGUARD_APPWIDGET_COMPONENT, componentName);
            sendBroadcast(intent);*/
            initWidget();
        } else if (v.getId() == R.id.remove_widget_bn) {
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                mLl.removeAllViews();
            }
            //AppManager.getAppManager().finishActivity(this);
        }
    }

    //加载widget控件
    private void initWidget() {
        Log.e(TAG, "initWidget");
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.e(TAG, "onClick,  it has hold a appWidget: appWidgetId = " + appWidgetId);
            return;
        }
        appWidgetId = mAppWidgetHost.allocateAppWidgetId();
        Log.e(TAG, "onClick  appWidgetId: " + appWidgetId);
        mSucess = addWidgetView();
        if (!mSucess) {
            Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, componentName);
            // TODO: we need to make sure that this accounts for the options bundle.
            //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_OPTIONS, options);
            startActivityForResult(intent, 11);
        }
    }

    private boolean addWidgetView() {
        mLl.removeAllViews();
        boolean sucess = false;
        try {
            sucess = mAppWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, componentName);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "addWidgetView,  Error when trying to bind AppWidget: " + e);
            mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        }
        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            AppWidgetProviderInfo appWidgetProviderInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
            if (appWidgetProviderInfo != null) {
                AppWidgetHostView appWidgetHostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetProviderInfo);

                FrameLayout widgetContainer = new FrameLayout(this);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
                lp.setMargins(0, 0, 0, 0);
                widgetContainer.addView(appWidgetHostView, lp);

                lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                mLl.addView(widgetContainer, lp);
            } else {
                Log.w(TAG, "addWidgetView, AppWidgetInfo for app widget id " + appWidgetId + ", deleting");
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
                appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
            }
        }
        if (sucess) {
            try {
                mAppWidgetHost.startListening();
            } catch (RuntimeException e) {
                mAppWidgetHost.deleteHost();
                e.printStackTrace();
            }
        }
        Log.e(TAG, "addWidgetView  sucess: " + sucess);
        return sucess;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG,"onActivityResult   requestCode = "+requestCode+",   resultCode = "+resultCode+",   data = "+data);
        if (requestCode == 11) {
            mSucess = addWidgetView();
        }
    }

    public String getVersionCode(Context context){
        PackageManager packageManager=context.getPackageManager();
        PackageInfo packageInfo;
        String versionCode="";
        try {
            packageInfo=packageManager.getPackageInfo(context.getPackageName(),0);
            versionCode=packageInfo.versionCode+"";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /*private OnClickHandler mOnClickHandler = new OnClickHandler() {
        @Override
        public boolean onClickHandler(View xx.view, PendingIntent pendingIntent, Intent fillInIntent) {
            userActivity();
            return super.onClickHandler(xx.view, pendingIntent, fillInIntent);
        }
    };*/

    void updateDisplaySize() {
        mDisplay.getMetrics(mDisplayMetrics);
        mDisplay.getSize(mCurrentDisplaySize);
        Log.d(TAG,"updateDisplaySize: "+
                String.format("%dx%d", mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels)
                +" x:"+mCurrentDisplaySize.x
                +" y:"+mCurrentDisplaySize.y
        );

    }
}
