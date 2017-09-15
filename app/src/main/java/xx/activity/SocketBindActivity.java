package xx.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.leui.notification.test.IServerSocketServiceBinder;
import com.leui.notification.test.R;

import service.ServerSocketService;

public class SocketBindActivity extends Activity {


    private static final String TAG = "SocketBindActivity-TAG";
    IServerSocketServiceBinder mAidlService;
    private MyConnection mConnection = new MyConnection();
    private IBinder mBinder;
    private TextView mDisplayText;

    private Handler mHandler = new Handler();
    private Runnable mDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                final String result = mAidlService.getResult();
                if (result != null) mDisplayText.setText(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };
    private boolean mDisplayThreadRunnable = false;
    private Thread mDisplayThread = new Thread(new Runnable() {
        @Override
        public void run() {
            mDisplayThreadRunnable = true;
            while(mDisplayThreadRunnable) {
                mHandler.post(mDisplayRunnable);
                synchronized (this) {
                    try {
                        this.wait(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }

        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "SocketBindActivity    onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_bind);
        mDisplayText = (TextView)findViewById(R.id.display_result);
        Intent intent = new Intent();
        intent.setClass(this, ServerSocketService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "SocketBindActivity    onDestroy");
        mDisplayThreadRunnable = false;
        unbindService(mConnection);
        super.onDestroy();
    }


    class MyConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "MyConnection    onServiceConnected");
            mBinder = service;
            mAidlService = IServerSocketServiceBinder.Stub.asInterface(service);
            mDisplayThread.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDisplayThread.stop();
            Log.d(TAG, "MyConnection    onServiceDisconnected");
        }
    }



}
