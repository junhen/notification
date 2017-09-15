package javacodetest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by xiaoxin on 17-4-11.
 */

public class SynchronizedTest {
    public static final String TEST_METHOD = "testMethod";
    public static final String TEST_SYNCHRONIZED_METHOD = "testSynchoronizedMethod";
    public static final String TEST_SYNCHRONIZED_OBJECT = "testSynchoronizedObject";
    public static final String TEST_SYNCHRONIZED_CLASS_OBJECT = "testSynchoronizedClassObject";
    private static final String TAG = "SynchronizedTest";
    private static CallBack mCallBack;
    public void setCallBack(CallBack callback) {
        Log.d(TAG,"xinx   setCallBack    callback = "+callback);
        mCallBack = callback;
    }

    public void testMethod() {
        Log.d(TAG,"xinx   testMethod    mCallBack = "+mCallBack);
        System.out.println("test开始..");
        mHandler.sendEmptyMessage(0);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("test结束..");
        mHandler.sendEmptyMessage(1);
    }

    public synchronized void testSynchoronizedMethod() {
        System.out.println("test开始..");
        mHandler.sendEmptyMessage(0);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("test结束..");
        mHandler.sendEmptyMessage(1);
    }

    public void testSynchoronizedObject() {
        synchronized(this){
            System.out.println("test开始..");
            mHandler.sendEmptyMessage(0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("test结束..");
            mHandler.sendEmptyMessage(1);
        }
    }

    public void testSynchoronizedClassObject() {
        synchronized (SynchronizedTest.class) {
            System.out.println("test开始..");
            mHandler.sendEmptyMessage(0);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("test结束..");
            mHandler.sendEmptyMessage(1);
        }
    }

    public void syncNo() {
        for (int i = 0; i < 3; i++) {
            Thread thread = new MyThread();
            thread.start();
        }
    }

    public void syncMethod() {
        for (int i = 0; i < 3; i++) {
            Thread thread = new MyThread(TEST_SYNCHRONIZED_METHOD);
            thread.start();
        }
    }

    public void syncObject() {
        SynchronizedTest sync = new SynchronizedTest();
        for (int i = 0; i < 3; i++) {
            Thread thread = new MyThread(sync);
            thread.start();
        }
    }

    public void syncClassObject() {
        for (int i = 0; i < 3; i++) {
            Thread thread = new MyThread(TEST_SYNCHRONIZED_CLASS_OBJECT);
            thread.start();
        }
    }

    public void syncObjectAndMethod() {
        SynchronizedTest sync = new SynchronizedTest();
        for (int i = 0; i < 3; i++) {
            Thread thread = new MyThread(sync, TEST_SYNCHRONIZED_METHOD);
            thread.start();
        }
    }

    private class MyThread extends Thread {
        private SynchronizedTest mSync;
        private String mMethod;


        public MyThread(){
            this(new SynchronizedTest(), TEST_METHOD);
        }

        public MyThread(String method){
            this(new SynchronizedTest(), method);
        }

        public MyThread(SynchronizedTest sync){
            this(sync, TEST_METHOD);
        }

        public MyThread(SynchronizedTest sync,String method){
            mSync = sync;
            mMethod = method;
        }

        public void run() {
            switch (mMethod) {
                case TEST_METHOD :
                    mSync.testMethod();
                    break;
                case TEST_SYNCHRONIZED_METHOD :
                    mSync.testSynchoronizedMethod();
                    break;
                case TEST_SYNCHRONIZED_OBJECT :
                    mSync.testSynchoronizedObject();
                    break;
                case TEST_SYNCHRONIZED_CLASS_OBJECT :
                    mSync.testSynchoronizedClassObject();
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0 :
                    mCallBack.showText("test开始..\n");
                    break;
                case 1 :
                    mCallBack.showText("test结束..\n");
                    break;
            }
        }
    };

    public interface CallBack {
        void showText(String str);
    }
}
