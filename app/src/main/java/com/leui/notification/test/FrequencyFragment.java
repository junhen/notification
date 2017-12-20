package com.leui.notification.test;

import android.app.Fragment;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

import xx.deviceManager.DeviceMethod;
import xx.deviceManager.MyDeviceAdminReceiver;
import xx.util.NewWindowUtil;

public class FrequencyFragment extends Fragment implements OnClickListener, NewWindowUtil.Callback {
    private static final String TAG = FrequencyFragment.class.getSimpleName();
    private static String ON_OFF_KEYGUARD = "on_off_keyguard";

    TextView mStatus;
    EditText mFrequency;
    Button mBtnStart, mBtnStop;
    int notifyID = 123;
    private MainActivity mContext;
    private int msgDelay = 100;//10hz
    private long lastNotificationTime = 0;
    private Randomizer mRandomizer;

    private Runnable updateNotification = new Runnable() {
        @Override
        public void run() {
            sendNotification();
            mStatus.postDelayed(updateNotification, msgDelay);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        mContext = (MainActivity) getActivity();
        View v = inflater.inflate(R.layout.fragment_frequency, container, false);

        mBtnStart = (Button) v.findViewById(R.id.btn_start);
        mBtnStop = (Button) v.findViewById(R.id.btn_stop);

        v.findViewById(R.id.btn_start).setOnClickListener(this);
        v.findViewById(R.id.btn_stop).setOnClickListener(this);
        v.findViewById(R.id.btn_update).setOnClickListener(this);
        v.findViewById(R.id.btn_add_float_view).setOnClickListener(this);
        v.findViewById(R.id.btn_remove_float_view).setOnClickListener(this);
        mStatus = (TextView) v.findViewById(R.id.tv_status);
        mFrequency = (EditText) v.findViewById(R.id.et_frequency);
        mRandomizer = new Randomizer(mContext);
        return v;
    }

    @Override
    public void onDestroyView() {
        Log.e(TAG, "onDestroyView");
        NewWindowUtil.getInstance().removeFloatView(ON_OFF_KEYGUARD);
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                if (mFrequency.getText().length() > 0) {
                    msgDelay = Integer.decode(mFrequency.getText().toString());
                    msgDelay = 1000 / msgDelay;
                }
                v.post(updateNotification);
                v.setEnabled(false);
                mBtnStop.setEnabled(true);
                mStatus.setText("running...");
                break;
            case R.id.btn_stop:
                mStatus.removeCallbacks(updateNotification);
                v.setEnabled(false);
                mBtnStart.setEnabled(true);
                mStatus.setText("stop.");
                break;
            case R.id.btn_update:
                sendNotification();
                break;
            case R.id.btn_add_float_view:
                Log.e(TAG, "btn_add_float_view");
                /*if (mNewWindowUtil == null) {
                    mNewWindowUtil = NewWindowUtil.getInstance(getActivity());
                    mNewWindowUtil.addFloatView(this);
                }*/
                NewWindowUtil.addOverlay(getActivity());
                NewWindowUtil.getInstance().addFloatView(mContext, ON_OFF_KEYGUARD, this);
                break;
            case R.id.btn_remove_float_view:
                Log.e(TAG, "btn_remove_float_view");
                /*if (mNewWindowUtil != null) {
                    mNewWindowUtil.removeFloatView();
                    NewWindowUtil.deleteInstance(getActivity());
                    mNewWindowUtil = null;
                    isStarted = false;
                }*/
                NewWindowUtil.getInstance().removeFloatView(ON_OFF_KEYGUARD);
                break;
            default:
                Log.e(TAG, "unhandle event,xx.view:" + v);
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.M)
    private void sendNotification() {
        Notification.Builder builder = new Notification.Builder(getActivity().getApplicationContext());
        final int time = (int) (System.currentTimeMillis() % 9);
        PendingIntent intent = PendingIntent.getActivity(getActivity(), 0, new Intent(), 0);
        for (int i = 0; i < 3; i++) {
            builder.addAction(mRandomizer.getRandomIconId(), "" + time, intent);
        }
        builder.setContentTitle("Reduced BigText title" + time)
                .setContentText("" + getActivity().getPackageName() + System.currentTimeMillis())
                .setContentInfo("Info" + time)
                .setShowWhen(false)
                .setTicker("getBigTextStyle" + System.currentTimeMillis())
                .setSmallIcon(mRandomizer.getRandomIconId());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            builder.setLargeIcon(Icon.createWithResource(getContext(), mRandomizer.getRandomIconId()));
        }

        Notification notif = new Notification.BigTextStyle(builder)
                .setBigContentTitle("Expanded BigText title" + time)
                .bigText(getActivity().getPackageName() + " " + System.currentTimeMillis() + "\n" + getResources().getString(R.string.big_text))
                .setSummaryText("Summary text" + time)
                .build();

        setNotificationIcon(notif, mRandomizer.getRandomIconId());
        mContext.sendNotification(notifyID, notif);

        mStatus.setText("running:" + 1000 / (System.currentTimeMillis() - lastNotificationTime) + "hz");
        lastNotificationTime = System.currentTimeMillis();
    }

    /**
     * set statusbar notification icon
     *
     * @param notification
     * @param iconId
     * @return
     */
    public static boolean setNotificationIcon(Notification notification, int iconId) {
        boolean result = false;
        Class clazz = notification.getClass();
        try {
            Field setNotificationIconField = clazz.getDeclaredField("notificationIcon");
            setNotificationIconField.setAccessible(true);
            setNotificationIconField.setInt(notification, iconId);
            result = true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            return result;
        }
    }

    //一下代码是加锁和解锁的demo，通过点击add和remove，增加和删除一个悬浮窗floatview
    private NewWindowUtil mNewWindowUtil = null;
    private boolean isStarted = false;
    private int N = 0;
    private boolean hasFloatView = false;
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
            if (!isStarted) return;
            //mWakeLock.acquire(); //屏幕会持续点亮
            mKeyguardLock.disableKeyguard();
            mHandler.postDelayed(runnable2, 500);
        }
    };

    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            mKeyguardLock.reenableKeyguard();
            if (!isStarted) return;
            //mWakeLock.release();
            if (N <= 10000) {
                mHandler.removeCallbacks(runnable3);
                mHandler.postDelayed(runnable3, 2000);
            }
        }
    };
    private Runnable runnable3 = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnable2  run  N = " + N + ",   time = " + (-(time - (time = SystemClock.uptimeMillis()))));
            if (!isStarted) return;
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
        Log.e(TAG, "activeManage  mActivity = " + getActivity() + ",   componentName = " + componentName);
        DeviceMethod.getInstance(mContext).onActivate();
    }


    @Override
    public void onAddView() {
        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = mKeyguardManager.newKeyguardLock(TAG);
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        mDevicePolicyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(getActivity(), MyDeviceAdminReceiver.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onRemoveView() {
        mContext.unregisterReceiver(mBroadcastReceiver);
        mHandler.removeCallbacks(runnable1);
        mHandler.removeCallbacks(runnable2);
        mHandler.removeCallbacks(runnable3);
    }

    @Override
    public void onClickView() {
        isStarted = !isStarted;
        if (isStarted) {
            N = 0;
            mHandler.post(runnable1);
        }
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
}
