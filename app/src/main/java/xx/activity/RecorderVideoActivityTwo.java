package xx.activity;

import android.app.Activity;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

import com.leui.notification.test.R;

import xx.util.FileUtil;
import xx.util.UploadUtil;

public class RecorderVideoActivityTwo extends Activity
        implements SurfaceHolder.Callback,
        MediaRecorder.OnErrorListener,
        MediaRecorder.OnInfoListener {

    private static final String TAG = "Recorder-TAG";
    //private static String actionUrl = "http://10.100.1.208/receive_file.php";
    private static String actionUrl = "https://www.baidu.com/";

    private SurfaceView mSurfaceview;
    private SurfaceHolder mSurfaceHolder;
    private Button mBtnStartStop;
    private Button mBtnPlay;
    private boolean mStartedFlg = false;//是否正在录像
    private boolean mIsPlay = false;//是否正在播放录像
    private MediaRecorder mRecorder;
    private ImageView mImageView;
    private Camera camera;
    private MediaPlayer mediaPlayer;
    private String path;
    private String date;
    private ArrayList<String> paths = new ArrayList<>();
    private TextView textView;
    private int text = 0;


    private Handler mUpHandler;
    private UploadThread mUploadThread;

    private android.os.Handler handler = new android.os.Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "runnable run" + ",  threadid : " + Process.myTid());
            text++;
            textView.setText(text + "");
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate  "+ UploadUtil.BOUNDARY);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recorder_video_two);

        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        //mSurfaceview.setVisibility(View.GONE);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mBtnStartStop = (Button) findViewById(R.id.btnStartStop);
        mBtnPlay = (Button) findViewById(R.id.btnPlayVideo);
        textView = (TextView) findViewById(R.id.text);
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsPlay) {
                    if (mediaPlayer != null) {
                        mIsPlay = false;
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
                if (!mStartedFlg) {
                    mImageView.setVisibility(View.GONE);

                    try {
                        startRecodeVideo();
                        mStartedFlg = true;
                        mBtnStartStop.setText("Stop");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //stop
                    if (mStartedFlg) {
                        try {
                            releaseRecodeVideo();
                            mBtnStartStop.setText("Start");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mStartedFlg = false;
                }
            }
        });

        mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsPlay = true;
                mImageView.setVisibility(View.GONE);
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }
                mediaPlayer.reset();
                Uri uri = Uri.parse(path);
                mediaPlayer = MediaPlayer.create(RecorderVideoActivityTwo.this, uri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDisplay(mSurfaceHolder);
                try {
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        });

        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //启动一个上传视频的HandlerThread线程
        mUploadThread = new UploadThread("上传video");
        mUploadThread.start();
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
        Log.d(TAG, "onResume mUploadThread.isAlive(): "+mUploadThread.isAlive());
        if(mUploadThread.isAlive()) mUploadThread.quit();
        super.onDestroy();
    }

    //初始化MediaRecorder和Camera对象，并设置MediaRecorder的参数
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
        handler.postDelayed(runnable, 1000);
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
        mRecorder.setOnErrorListener(RecorderVideoActivityTwo.this);
        mRecorder.setOnInfoListener(RecorderVideoActivityTwo.this);

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
        if(path != null)
            sendRecorde();
        handler.removeCallbacks(runnable);
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
     *
     * @return
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR);           // 获取年份
        int month = ca.get(Calendar.MONTH);         // 获取月份
        int day = ca.get(Calendar.DATE);            // 获取日
        int minute = ca.get(Calendar.MINUTE);       // 分
        int hour = ca.get(Calendar.HOUR);           // 小时
        int second = ca.get(Calendar.SECOND);       // 秒

        String date = "" + year + (month + 1) + day + hour + minute + second;
        Log.d(TAG, "getDate  date:" + date);

        return date;
    }

    /**
     * 获取SD path
     *
     * @return
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

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
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
        final String lastPath = path;
        path = null;
        paths.add(path);
        mUpHandler.post(new Runnable() {
            @Override
            public void run() {
                /*String pathCP = getSDPath();
                if (pathCP != null) {
                    File dir = new File(pathCP + "/recordtestcp");
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    pathCP = dir + "/" + date + ".mp4";
                    Log.d(TAG, "onCreate   end path: " + path + ",  threadid : ");
                    Log.d(TAG, "onCreate   end pathCP: " + pathCP + ",  threadid : " + Process.myTid());
                    FileUtil.copyFile(path, pathCP);
                } else {
                    Log.d(TAG, "onCreate   end pathCP: " + pathCP);
                    return;
                }*/

                //这里是以后上传代码用，现在不要看这个方法
                try {
                    Thread.currentThread().sleep(12000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                uploadFile(actionUrl, lastPath);
            }
        });
    }

    //记录一个新视频
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

    //上传的线程，主要是通过handler的post，在此线程执行
    class UploadThread extends HandlerThread {
        public UploadThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            mUpHandler = new Handler(getLooper());
        }
    }

    /* 上传文件至Server，uploadUrl：接收文件的处理页面 */
    private void uploadFile(String uploadUrl, String srcPath) {
        Log.d(TAG, "uploadFile  uploadUrl: "+uploadUrl+",   srcPath: "+srcPath);
        if(true) {
            return;
        }
        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        try {
            URL url = new URL(uploadUrl);
            Log.d(TAG, "uploadFile  1111111111111111111");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url
                    .openConnection();
            Log.d(TAG, "uploadFile  22222222222222");
            // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
            // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
            httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\"; filename=\""
                    + srcPath.substring(srcPath.lastIndexOf("/") + 1) + "\""
                    + end);
            dos.writeBytes(end);
            Log.d(TAG, "uploadFile  3333333333333  dos: "+dos);

            FileInputStream fis = new FileInputStream(srcPath);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            // 读取文件
            while ((count = fis.read(buffer)) != -1)
            {
                dos.write(buffer, 0, count);
            }
            fis.close();

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            Log.d(TAG, "uploadFile  44444444444  dos: "+dos);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();

            Log.d(TAG, "uploadFile  5555555555    result: "+result);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            dos.close();
            is.close();

        } catch (Exception e) {
            Log.d(TAG, "uploadFile Exception e: "+e);
            e.printStackTrace();
            setTitle(e.getMessage());
        }
    }
}
