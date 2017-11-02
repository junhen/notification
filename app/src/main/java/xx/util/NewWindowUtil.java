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
        Log.d(TAG, "getInstance  key = "+context+",   newWindowUtil = " + newWindowUtil);
        return newWindowUtil;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "action=" + action);
            if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                Log.e(TAG, "竟然可以解锁");
            }
        }
    };

    private int N = 0;
    private KeyguardManager mKeyguardManager;
    private KeyguardManager.KeyguardLock mKeyguardLock;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName componentName;
    private volatile long time = 0;
    private Handler mHandler = new Handler();
    private Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnable1  run  N = " + N + ",   time = " + (-(time - (time = SystemClock.uptimeMillis()))));
            mHandler.removeCallbacks(runnable2);
            //mWakeLock.acquire(); //屏幕会持续点亮
            mKeyguardLock.disableKeyguard();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mKeyguardLock.reenableKeyguard();
                    //mWakeLock.release();
                    if (N <= 10000) mHandler.postDelayed(runnable2, 2000);
                }
            }, 500);
        }
    };

    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnable2  run  N = " + N + ",   time = " + (-(time - (time = SystemClock.uptimeMillis()))));
            //mContext.sendBroadcast(new Intent(Intent.ACTION_SCREEN_OFF));
            //释放锁，屏幕熄灭。
            //mPowerManager.goToSleep(SystemClock.uptimeMillis());
            //mWakeLock.release();
            //mDevicePolicyManager.lockNow();
            // 判断该组件是否有系统管理员的权限
            boolean isAdminActive = mDevicePolicyManager.isAdminActive(componentName);
            Log.d(TAG, "runnable2  isAdminActive = " + isAdminActive);

            if (!isAdminActive) {
                activeManage();
            } else {
                Toast.makeText(mContext, "具有权限,将进行锁屏....", Toast.LENGTH_SHORT).show();
                mDevicePolicyManager.lockNow();
                //mDevicePolicyManager.resetPassword("123321", 0);
            }

            if (isAdminActive) {
                mHandler.removeCallbacks(runnable1);
                mHandler.postDelayed(runnable1, 2000);
                N++;
            }
        }
    };

    private void activeManage() {
        Log.e(TAG, "activeManage  mActivity = " + mActivity + ",   componentName = " + componentName);
        DeviceMethod.getInstance(mContext).onActivate();
        /*if (mActivity == null) return;
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, TAG);
        mActivity.startActivityForResult(intent, REQUEST_CODE);*/
    }

    public void addFloatView() {
        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyguardManager.newKeyguardLock("NewWindowUtil");
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "NewWindowUtil");
        mDevicePolicyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (mActivity != null)
            componentName = new ComponentName(mActivity, MyDeviceAdminReceiver.class);

        removeFloatView();
        if (wm == null) {
            //经过测试，这里两处可以使用activity的context
            wm = (WindowManager) mContext.getApplicationContext().getSystemService("window");
        }
        if (floatView == null) {
            floatView = new View(mContext.getApplicationContext());
            floatView.setBackgroundColor(Color.GREEN);
            floatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //clickView();
                    mHandler.post(runnable1);
                }
            });
            floatView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        }
        if (wmParams == null) {
            wmParams = new WindowManager.LayoutParams();
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;//显示在锁屏界面之上
            wmParams.format = PixelFormat.TRANSPARENT;

            //设置Window flag
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS  //可以拖动到屏幕之外
            ;
            wmParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            //wmParams.x = 0;
            //wmParams.y = 1000;

            //设置悬浮窗口长宽数据
            wmParams.width = 200;
            wmParams.height = 200;
            //wmParams.verticalMargin = 200;
            wmParams.setTitle("notification_float");
            wmParams.windowAnimations = android.R.style.Animation_Dialog;
        }
        wm.addView(floatView, wmParams);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    public void removeFloatView() {
        if (floatView != null) {
            wm.removeView(floatView);
            floatView = null;
            //mView = null;
            mContext.unregisterReceiver(mBroadcastReceiver);
        }
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

    public interface InitCallback{
        void init();
    }
}
