package test.views;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leui.notification.test.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiaoxin on 17-4-21.
 */

public class MultiThreadsView extends LinearLayout implements OnClickListener{
    private static final String TAG = "MultiThreadsView";

    /** 总共多少任务（根据CPU个数决定创建活动线程的个数,这样取的好处就是可以让手机承受得住） */
    // private static final int count = Runtime.getRuntime().availableProcessors() * 3 + 2;

    /** 总共多少任务 */
    private static final int count = 3;

    /** 所有任务都一次性开始的线程池  */
    private static ExecutorService mCacheThreadExecutor = null;

    /** 每次执行限定个数个任务的线程池 */
    private static ExecutorService mFixedThreadExecutor = null;

    /** 创建一个可在指定时间里执行任务的线程池，亦可重复执行 */
    private static ScheduledExecutorService mScheduledThreadExecutor = null;

    /** 每次只执行一个任务的线程池 */
    private static ExecutorService mSingleThreadExecutor = null;

    private TextView mDisplay;
    private StringBuilder sb = null;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            sb.append(msg.obj + "\n");
            mDisplay.setText(sb.toString());
        }
    };

    public MultiThreadsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        Log.d(TAG,"onFinishInflate");
        super.onFinishInflate();
        initView();
        initExecutorService();
        mDisplay = (TextView) findViewById(R.id.multi_thread_view_text);
    }

    private void initExecutorService() {
        mCacheThreadExecutor = Executors.newCachedThreadPool();// 一个没有限制最大线程数的线程池
        mFixedThreadExecutor = Executors.newFixedThreadPool(count);// 限制线程池大小为count的线程池
        mScheduledThreadExecutor = Executors.newScheduledThreadPool(count);// 一个可以按指定时间可周期性的执行的线程池
        mSingleThreadExecutor = Executors.newSingleThreadExecutor();// 每次只执行一个线程任务的线程池
    }

    private void initView() {
        findViewById(R.id.mCacheThreadExecutorBtn).setOnClickListener(this);
        findViewById(R.id.mFixedThreadExecutorBtn).setOnClickListener(this);
        findViewById(R.id.mScheduledThreadExecutorBtn).setOnClickListener(this);
        findViewById(R.id.mSingleThreadExecutorBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        sb = new StringBuilder();
        switch (v.getId()) {
            case R.id.mCacheThreadExecutorBtn:
                sb.append("所有任务都一次性开始的线程池\n");
                ExecutorServiceThread(mCacheThreadExecutor);
                break;
            case R.id.mFixedThreadExecutorBtn:
                sb.append("每次执行限定个数个(3)任务的线程池\n");
                ExecutorServiceThread(mFixedThreadExecutor);
                break;
            case R.id.mScheduledThreadExecutorBtn:
                sb.append("创建一个可在指定时间里执行任务的线程池，亦可重复执行 \n");
                ExecutorScheduleThread(mScheduledThreadExecutor);
                break;
            case R.id.mSingleThreadExecutorBtn:
                sb.append("每次只执行一个任务的线程池\n");
                ExecutorServiceThread(mSingleThreadExecutor);
                break;
        }
        mDisplay.setText(sb.toString());
    }

    private void ExecutorServiceThread(ExecutorService executorService) {
        for (int i = 0; i < 9; ++i) {
            final int index = i;
            executorService.execute(new MyRunnable(mHandler, index));
        }
    }

    private void ExecutorScheduleThread(ScheduledExecutorService scheduledExecutorService) {
        for (int i = 0; i < 9; ++i) {
            final int index = i;
            scheduledExecutorService.schedule(new MyRunnable(mHandler, index), 2, TimeUnit.SECONDS);
        }
    }

    class MyRunnable implements Runnable {
        private Handler mHandler;
        private int mIndex;

        public MyRunnable(Handler mHandler, int mIndex) {
            this.mHandler = mHandler;
            this.mIndex = mIndex;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String s = "Thread:" + Thread.currentThread().getId() + " activeCount:" + Thread.activeCount() + " mIndex:" + mIndex;
            Message ms = Message.obtain(mHandler, 0, s);
            mHandler.sendMessage(ms);
            Log.i(TAG, "Thread:" + Thread.currentThread().getId() + " activeCount:" + Thread.activeCount() + " mIndex:" + mIndex);
        }
    }
}
