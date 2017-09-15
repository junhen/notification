package xx.widget;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by xiaoxin on 17-6-5.
 */

public class WidgetService extends Service {
    private static final String TAG="WidgetService";

    // 更新 widget 的广播对应的action
    private final String ACTION_UPDATE_ALL = "xx.widget.UPDATE_ALL";
    // 周期性更新 widget 的周期
    private static final int UPDATE_TIME = 5000;
    // 周期性更新 widget 的线程
    private UpdateThread mUpdateThread;
    private Context mContext;
    // 更新周期的计数
    private int mCount = 0;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        // 创建并开启线程UpdateThread
        mContext = this.getApplicationContext();
        mUpdateThread = new UpdateThread();
        mUpdateThread.start();
        super.onCreate();
    }

    //服务开始时，即调用startService()时，onStartCommand()被执行。
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, "onDestroy");
        // 中断线程，即结束线程。
        if (mUpdateThread != null) {
            mUpdateThread.interrupt();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return null;
    }

    private class UpdateThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                mCount = 0;
                while (true) {
                    Log.d(TAG, "run ... mCount:"+mCount);
                    mCount++;
                    Intent updateIntent=new Intent(ACTION_UPDATE_ALL);
                    mContext.sendBroadcast(updateIntent);
                    Thread.sleep(UPDATE_TIME);
                }
            } catch (InterruptedException e) {
                // 将 InterruptedException 定义在while循环之外，意味着抛出 InterruptedException 异常时，终止线程。
                e.printStackTrace();
            }
        }
    }
}
