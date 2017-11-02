package xx.util;

import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xx.deviceManager.DeviceMethod;
import xx.deviceManager.MyDeviceAdminReceiver;

/**
 * Created by xiaoxin on 17-10-31.
 */

public class NewWindowUtil {
    private static final String TAG = "NewWindowUtil";
    private static int REQUEST_CODE = 0x01;
    private static HashMap<Integer, NewWindowUtil> mNewWindowUtils;

    private WindowManager wm;
    private WindowManager.LayoutParams wmParams;
    private View floatView;
    private Application mApplication;
    private Activity mActivity;
    private Service mService;
    private Context mContext;

    private InitCallback mCallback;

    public void setCallback(InitCallback callback) {
        mCallback = callback;
    }

    public static NewWindowUtil getInstance(Context context) {
        if (mNewWindowUtils == null) mNewWindowUtils = new HashMap<>();
        NewWindowUtil newWindowUtil;
        int contextHashcode = context.hashCode();
        if (!mNewWindowUtils.keySet().contains(contextHashcode)) {
            newWindowUtil = new NewWindowUtil();
            if (context instanceof Application) {
                newWindowUtil.mApplication = (Application) context;
            }
            if (context instanceof Activity) {
                newWindowUtil.mActivity = (Activity) context;
            }
            if (context instanceof Service) {
                newWindowUtil.mService = (Service) context;
            }
            newWindowUtil.mContext = context;
            mNewWindowUtils.put(contextHashcode, newWindowUtil);
        } else {
            newWindowUtil = mNewWindowUtils.get(contextHashcode);
        }
        Log.d(TAG, "getInstance  key = " + context + ",   newWindowUtil = " + newWindowUtil);
        return newWindowUtil;
    }

    public static void deleteInstance(Context context) {
        int contextHashcode = context.hashCode();
        mNewWindowUtils.remove(contextHashcode);
    }

    public void addFloatView(InitCallback callback) {
        addFloatView(callback, getDefaultView(), getDefaultLayoutParams());
    }

    public void addFloatView(InitCallback callback, View view, WindowManager.LayoutParams lp) {
        if(callback == null) {
            Toast.makeText(mContext, "has no InitCallback", Toast.LENGTH_SHORT);
            return;
        }
        if (wm == null) {
            //经过测试，这里两处可以使用activity的context
            wm = (WindowManager) mContext.getApplicationContext().getSystemService("window");
        }
        mCallback = callback;
        removeFloatView();
        floatView = view;
        wmParams = lp;
        wm.addView(floatView, wmParams);
        mCallback.addView();
    }

    public void removeFloatView() {
        if (floatView != null) {
            wm.removeView(floatView);
            floatView = null;
            wmParams = null;
            if (mCallback != null) mCallback.removeView();
        }
    }

    private View getDefaultView() {
        View view = new View(mContext.getApplicationContext());
        view.setBackgroundColor(Color.GREEN);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onClickView();
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        return view;
    }

    private WindowManager.LayoutParams getDefaultLayoutParams() {
        WindowManager.LayoutParams lp;
        lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;//显示在锁屏界面之上
        lp.format = PixelFormat.TRANSPARENT;

        //设置Window flag
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS  //可以拖动到屏幕之外
        ;
        lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
        //lp.x = 0;
        //lp.y = 1000;

        //设置悬浮窗口长宽数据
        lp.width = 200;
        lp.height = 200;
        //lp.verticalMargin = 200;
        lp.setTitle("notification_float");
        lp.windowAnimations = android.R.style.Animation_Dialog;
        return lp;
    }

    /*private   void clickView() {
        if (mView != null)
            mView.performClick();
    }*/

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mContext instanceof Application) {
            sb.append("mApplication = " + mApplication);
        }
        if (mContext instanceof Activity) {
            sb.append("mActivity = " + mActivity);
        }
        if (mContext instanceof Service) {
            sb.append("mService = " + mService);
        }
        return sb.toString();
    }

    public interface InitCallback {
        void addView();
        void removeView();
        void onClickView();
    }
}
