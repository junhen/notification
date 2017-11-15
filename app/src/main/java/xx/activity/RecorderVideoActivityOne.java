package xx.activity;

import android.app.Activity;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;

import com.leui.notification.test.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import okhttp3.Call;
import okhttp3.Response;
import xx.util.OkGoUtil;
import xx.util.UploadUtil;

public class RecorderVideoActivityOne extends Activity implements SurfaceHolder.Callback,
        MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {

    private static final String TAG = "Recorder-TAG";
    private static String actionUrl = "https://www.baidu.com/"; //这里指定你们公司的url

    private SurfaceView mSurfaceview;
    private SurfaceHolder mSurfaceHolder;
    private Button mBtnStartStop;
    private boolean mStartedFlg;//是否正在播放录像
    private MediaRecorder mRecorder;
    private ImageView mImageView;
    private Camera camera;
    private String path;
    private String date;
    private TextView textView;
    private int text = 0;


    private Handler mUploadHandler;
    private UploadThread mUploadThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate  " + UploadUtil.BOUNDARY);
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recorder_video_two);

        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mBtnStartStop = (Button) findViewById(R.id.btnStartStop);
        textView = (TextView) findViewById(R.id.text);
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mStartedFlg) {
                    mImageView.setVisibility(View.GONE);
                    try {
                        mStartedFlg = true;
                        startRecodeVideo();
                        mBtnStartStop.setText("Stop");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        mStartedFlg = false;
                        releaseRecodeVideo();
                        mBtnStartStop.setText("Start");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);
        //setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //启动一个上传视频的HandlerThread线程
        mUploadThread = new UploadThread("上传video");
        mUploadThread.start();

        //初始化OkGo
        OkGoUtil.initOkGo(getApplication());
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (!mStartedFlg) {
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onResume mUploadThread.isAlive(): " + mUploadThread.isAlive());
        if (mUploadThread.isAlive()) mUploadThread.quit();
        super.onDestroy();
    }

    /**
     * 初始化MediaRecorder和Camera对象，并设置MediaRecorder的参数
     */
    private void startRecodeVideo() throws IOException {
        Log.d(TAG, "startRecodeVideo");
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        }

        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        if (camera != null) {
            camera.setDisplayOrientation(90);
            camera.unlock();
        }
        mRecorder.setCamera(camera);
        // 这两项需要放在setOutputFormat之前
        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Set output file format
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // 这两项需要放在setOutputFormat之后
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

        mRecorder.setVideoSize(640, 480);
        mRecorder.setVideoFrameRate(30);
        mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
        mRecorder.setOrientationHint(90);
        //设置记录会话的最大持续时间（毫秒）
        mRecorder.setMaxDuration(10 * 1000);
        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        //增加监听器
        mRecorder.setOnErrorListener(RecorderVideoActivityOne.this);
        mRecorder.setOnInfoListener(RecorderVideoActivityOne.this);

        path = getSDPath();
        if (path != null) {
            File dir = new File(path + "/recordtest");
            if (!dir.exists()) {
                dir.mkdir();
            }
            date = getDate();
            path = dir + "/" + date + ".mp4";
            Log.d(TAG, "startRecodeVideo path: " + path);
        } else {
            Log.d(TAG, "startRecodeVideo path: " + path);
            return;
        }
        mRecorder.setOutputFile(path);
        mRecorder.prepare();
        mRecorder.start();
    }

    private void releaseRecodeVideo() {
        Log.d(TAG, "releaseRecodeVideo");
        if (path != null)
            sendRecorde();
        if (mRecorder != null) {
            mRecorder.setOnErrorListener(null);
            mRecorder.setOnInfoListener(null);
            mRecorder.stop();
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * 获取系统时间
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int hour = ca.get(Calendar.HOUR_OF_DAY);    // 小时
        int minute = ca.get(Calendar.MINUTE);       // 分
        int second = ca.get(Calendar.SECOND);       // 秒

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%04d", year))
                .append(String.format("%02d", month + 1))
                .append(String.format("%02d", day))
                .append(String.format("%02d", hour))
                .append(String.format("%02d", minute))
                .append(String.format("%02d", second));
        String date = sb.toString();
        Log.d(TAG, "getDate  date:" + date);
        return date;
    }

    /**
     * 获取SD path
     */
    public String getSDPath() {
        Log.d(TAG, "getSDPath");
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }

        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated");
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged");
        // 将holder，这个holder为开始在onCreate里面取得的holder，将它赋给mSurfaceHolder
        mSurfaceHolder = surfaceHolder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceDestroyed");
        mSurfaceview = null;
        mSurfaceHolder = null;
        //在surfaceDestroyed中也需要释放一次camera资源
        releaseRecodeVideo();
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.d(TAG, "onError   mr: " + mr + ",  what: " + mr + ",  extra: " + extra);

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.d(TAG, "onInfo mr: " + mr + ",  what: " + mr + ",  extra: " + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.v(TAG, "onInfo max duration reached");
            //记录一个新视频
            resetRecorder();
        }

    }

    /**
     * 使用子线程上传记录好的视频
     */
    public void sendRecorde() {
        Log.d(TAG, "sendRecorde");
        final String filePath = path;
        final String fileName = date + ".mp4";
        mUploadHandler.post(new Runnable() {
            @Override
            public void run() {
                //调用上传录像文件的方法
                uploadFile(actionUrl, filePath, fileName);
            }
        });
    }

    /**
     * 记录一个新视频
     */
    private void resetRecorder() {
        releaseRecodeVideo();
        try {
            startRecodeVideo();
        } catch (IllegalStateException e) {
            Log.d(TAG, "resetRecorder IllegalStateException e:" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "resetRecorder IOException e" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 上传录像文件到网络服务器，使用了OkGo框架
     *
     * @param uploadUrl 服务器的地址
     * @param filePath  录像文件的本地地址
     * @param fileName  录像文件名
     */
    private void uploadFile(String uploadUrl, String filePath, String fileName) {
        Log.d(TAG, "uploadFile uploadUrl: " + uploadUrl + ",  filePath: " + filePath + ",  fileName: " + fileName);
        String txt = "method=d_qrcodecommit&d_account=" + "mDriver.getD_account()" +
                "&safecode=" + "mDriver.getSafecode()" +
                "&company=" + "mDriver.getCompany()";
        txt += "&photo=" + fileName;
        File file = new File(filePath);
        try {
            txt = URLEncoder.encode(txt, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String str = String.format("%06d", txt.length());
        txt = str + txt;

        int len = (int) file.length(); //文件长度
        int fileNameLen = 4;
        //将前面生成的长度和命令字符串放到字节数组中
        int pos = 0;
        byte[] b = new byte[txt.length() + fileNameLen + len];
        System.arraycopy(txt.getBytes(), 0, b, 0, txt.length());
        //将文件长度放到字节数组中
        Log.d(TAG, "uploadFile txt: " + txt);
        pos += txt.length();
        b[pos++] = (byte) ((len & 0xff000000) >> 24);
        b[pos++] = (byte) ((len & 0x00ff0000) >> 16);
        b[pos++] = (byte) ((len & 0x0000ff00) >> 8);
        b[pos++] = (byte) (len & 0x000000ff);
        //将文件内容放到字节数组中
        try {
            if (file != null) {
                InputStream in1 = new FileInputStream(file);
                in1.read(b, pos, len);            //从文件中读到字节数组中
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        OkGo.post(uploadUrl)
                .upBytes(b)
                .execute(new StringCallback() {
                    //请求成功
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        Log.d(TAG, "上传成功： " + s.toString());
                    }

                    //请求失败
                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        Log.d(TAG, "上传失败");
                        super.onError(call, response, e);
                    }
                });
    }

    /**
     * 上传的线程，主要是通过handler的post，在此线程执行
     */
    class UploadThread extends HandlerThread {
        public UploadThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            mUploadHandler = new Handler(getLooper());
        }
    }
}

