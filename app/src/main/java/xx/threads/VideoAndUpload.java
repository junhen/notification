package xx.threads;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * 调用的时候“VideoAndUpload.getInstance().makeVideo(true);”即可
 * Created by xiaoxin on 17-11-13.
 */

public class VideoAndUpload {

    //标识录像及上传是否开启
    private boolean mStart;
    private VideoThread mVideoThread;
    private UploadThread mUploadThread;
    private Handler mHandler;

    //单例获取工具类
    private static VideoAndUpload mInstance;

    private VideoAndUpload() {
    }

    public static synchronized VideoAndUpload getInstance() {
        if (mInstance == null) {
            mInstance = new VideoAndUpload();
        }
        return mInstance;
    }

    //开始录像并上传
    public void start() {
        mStart = true;
        mUploadThread = new UploadThread("上传线程");
        mUploadThread.start();
        mVideoThread = new VideoThread();
        mVideoThread.start();
    }

    //停止录像
    public void stop() {
        mStart = false;
    }

    //上传视频的runnable
    private Runnable uploadRunnable = new Runnable() {
        @Override
        public void run() {
            /*这里是上传的代码*/
            //TODO
            if(!mStart && mUploadThread.isAlive())
                mUploadThread.quit();
        }
    };

    //录像的线程,每三十秒记录一次数据并上传
    class VideoThread extends Thread {
        @Override
        public void run() {
            while (!mStart) {
                /*这里写具体的录像程序, 应该是30秒一个循环*/
                //TODO
                if (mHandler != null) {
                    mHandler.removeCallbacks(uploadRunnable);
                    mHandler.post(uploadRunnable);
                }
            }
        }
    }

    //上传的线程，主要是通过handler的post，是的runnable在此线程执行
    class UploadThread extends HandlerThread {
        public UploadThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            mHandler = new Handler(getLooper());
        }
    }
}
