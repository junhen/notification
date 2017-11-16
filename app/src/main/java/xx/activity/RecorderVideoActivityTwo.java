package xx.activity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

import com.leui.notification.test.R;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import okhttp3.Call;
import okhttp3.Response;
import xx.util.FileUtil;
import xx.util.OkGoUtil;
import xx.util.UploadUtil;

import org.jetbrains.annotations.NotNull;

public class RecorderVideoActivityTwo extends Activity implements SurfaceHolder.Callback,
        MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {

    private static final String TAG = "Recorder-TAG";
    //private static String actionUrl = "http://10.100.1.208/receive_file.php";
    private static String actionUrl = "https://www.baidu.com/";
    //上传video路径
    private String videoUrl = "http://59.49.99.195:34562/videoplatform/communication/mainserver?";

    private SurfaceView mSurfaceView;
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
    private TextView textView;
    private int text = 0;


    private Handler mUploadHandler;
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
        Log.d(TAG, "onCreate  " + UploadUtil.BOUNDARY);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recorder_video_two);

        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
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
                    mImageView.setVisibility(View.VISIBLE);
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
                if (path == null) {
                    Log.d(TAG, "mediaPlayer   path == null ");
                    return;
                }
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
                mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        Log.d(TAG, "mediaPlayer   onInfo  what: " + what + ",  extra: " + extra);
                        return false;
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.d(TAG, "mediaPlayer   onCompletion  mp: " + mp);
                        mImageView.setVisibility(View.VISIBLE);
                    }
                });
                try {
                    mediaPlayer.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
        });

        SurfaceHolder holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        // setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //启动一个上传视频的 HandlerThread 线程
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
        if (path != null) {
            sendRecorde();
            //为了避免重复上传
            path = null;
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
        mSurfaceView = null;
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
        final String filePath = path;
        final String fileName = date + ".mp4";

        /*AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                uploadFileWithOkGo(videoUrl, filePath);
            }
        });*/
        mUploadHandler.post(new Runnable() {
            @Override
            public void run() {
                String pathCP = getSDPath();
                if (pathCP != null) {
                    File dir = new File(pathCP + "/recordtestcp");
                    if (!dir.exists())
                        dir.mkdir();
                    pathCP = dir + "/" + fileName;
                    Log.d(TAG, "sendRecorde   "
                            + ",  filePath: " + filePath
                            + ",  pathCP: " + pathCP
                            + ",  threadid : " + Process.myTid());
                    //copy文件到另一个目录下
                    //FileUtil.copyFile(filePath, pathCP);
                    //FileUtil.copyFile(filePath, pathCP, false);
                    //FileUtil.copyFile(new File(filePath), new File(pathCP));
                    try {
                        //float time = FileUtil.forChannel(new File(filePath), new File(pathCP));
                        //float time = FileUtil.forImage(new File(filePath), new File(pathCP));
                        //float time = FileUtil.forTransfer(new File(filePath), new File(pathCP));
                        float time = FileUtil.forJava(new File(filePath), new File(pathCP));
                        Log.d(TAG, "sendRecorde   ent time: " + time / 1000);
                    } catch (Exception e) {
                        Log.d(TAG, "sendRecorde   Exception e: " + e);
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "sendRecorde  pathCP: " + null);
                    return;
                }

                //这里是以后上传代码用，现在不要看这个方法
                //uploadFile(actionUrl, filePath, fileName);
                //uploadFile(actionUrl, filePath);
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
     */
    private void uploadFileWithOkGo(String uploadUrl, @NotNull String filePath) {
        Log.d(TAG, "uploadFileWithOkGo uploadUrl: " + uploadUrl + ",  filePath: " + filePath);
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        String header = "?method=app_commit"+ "&mac=" + getIMEI(RecorderVideoActivityTwo.this)+ "&video=" + fileName;
        try {
            header = URLEncoder.encode(header, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        header = String.format("%06d", header.length()) + header;
        Log.d(TAG, "uploadFileWithOkGo header: " + header);
        try {
            header = URLEncoder.encode(header, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //从视频文件全路径filePath生成视频文件
        File file = new File(filePath);
        int fileLength = (int) file.length(); //文件长度
        int fileNameLength = 4;

        //定义一个字节数组
        byte[] b = new byte[header.length() + fileNameLength + fileLength];
        int pos = 0;

        //将header放到字节数组中
        System.arraycopy(header.getBytes(), 0, b, 0, header.length());
        pos += header.length();

        //将文件长度放到字节数组中
        b[pos++] = (byte) ((fileLength & 0xff000000) >> 24);
        b[pos++] = (byte) ((fileLength & 0x00ff0000) >> 16);
        b[pos++] = (byte) ((fileLength & 0x0000ff00) >> 8);
        b[pos++] = (byte) (fileLength & 0x000000ff);

        //将文件内容放到字节数组中
        InputStream in = null;
        try {
            if (file != null) {
                in = new FileInputStream(file);
                in.read(b, pos, fileLength); //从文件中读到字节数组中
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //使用OkGo框架实现网络传输
        OkGo.post(uploadUrl)
                .upBytes(b)
                .execute(new StringCallback() {
                    //请求成功
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        Log.d(TAG, "onSuccess,  s: " + s.toString()+", response: "+response);


                    }

                    //请求失败
                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        Log.d(TAG, "上传失败");
                    }
                });
    }

    private void uploadFile(String uploadUrl, String filePath) {
        //从视频文件完整路径中取出文件名
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        Log.d(TAG, "uploadFile uploadUrl: " + uploadUrl + ",  filePath: "
                + filePath + ",  fileName: " + fileName
                + ",  threadid : " + Process.myTid());

        DataOutputStream dos = null;
        InputStream fis = null;
        InputStream fisTwo = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            URL url = new URL(uploadUrl);
            Log.d(TAG, "uploadFile  1111111111111  url: "+url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "uploadFile  2222222222222  httpURLConnection: "+httpURLConnection);
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
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;");

            //定义一个字节数组
            byte[] b;
            {
                //文件数据流前需要加上公司需求的header数据
                String header = "method=app_commit"
                        + "&mac=" + "123456789123456789"
                        + "&video=" + fileName;
                try {
                    header = URLEncoder.encode(header, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                header = String.format("%06d", header.length()) + header;

                //从视频文件全路径filePath生成视频文件
                File file = new File(filePath);
                int fileLength = (int) file.length(); //文件长度
                int fileNameLength = 4;

                b = new byte[header.length() + fileNameLength + fileLength];
                int pos = 0;

                //将前面生成的长度和命令字符串放到字节数组中
                System.arraycopy(header.getBytes(), 0, b, 0, header.length());
                pos += header.length();

                //将文件长度放到字节数组中
                b[pos++] = (byte) ((fileLength & 0xff000000) >> 24);
                b[pos++] = (byte) ((fileLength & 0x00ff0000) >> 16);
                b[pos++] = (byte) ((fileLength & 0x0000ff00) >> 8);
                b[pos++] = (byte) (fileLength & 0x000000ff);

                // 读取文件
                fis = new FileInputStream(filePath);
                fis.read(b, pos, fileLength);
            }
            Log.d(TAG, "uploadFile  3333333333333  b: " + (new String(b)));

            //获取网络输出流，并把数据b写入其中
            dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.write(b);
            dos.flush();
            Log.d(TAG, "uploadFile  44444444444  dos: " + dos);

            //接收网络反馈数据
            /*fisTwo = httpURLConnection.getInputStream();
            isr = new InputStreamReader(fisTwo, "utf-8");
            br = new BufferedReader(isr);
            String result = br.readLine();*/
            //获取响应码 200=成功 当响应成功，获取响应的流
            int res = httpURLConnection.getResponseCode();
            String result = null;
            Log.d(TAG, "uploadFile  5555555555    res: " + res);
            if (res == 200) {
                InputStream input = httpURLConnection.getInputStream();
                StringBuffer sb1 = new StringBuffer();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb1.append((char) ss);
                }
                input.close();
                result = sb1.toString();
            }
            Log.d(TAG, "uploadFile  5555555555    result: " + result);

        } catch (Exception e) {
            Log.d(TAG, "uploadFile 66666  Exception e: " + e);
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fisTwo != null) {
                try {
                    fisTwo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取手机IMEI号
     */
    public String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)
                context.getSystemService(context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        return imei;
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
