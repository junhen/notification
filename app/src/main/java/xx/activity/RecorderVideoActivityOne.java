package xx.activity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

import com.leui.notification.test.R;

public class RecorderVideoActivityOne extends Activity implements SurfaceHolder.Callback,
        MediaRecorder.OnErrorListener, MediaRecorder.OnInfoListener {

    private static final String TAG = "Recorder-TAG";
    private static String actionUrl = "https://www.baidu.com/"; //这里指定你们公司的url
    //上传video路径
    private String videoUrl = "http://59.49.99.195:34562/videoplatform/communication/mainserver?";

    private SurfaceView mSurfaceview;
    private SurfaceHolder mSurfaceHolder;
    private Button mBtnStartStop;
    private boolean mStartedFlg; //是否开始录像
    private MediaRecorder mRecorder;
    private ImageView mImageView;
    private Camera camera;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate  ");
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recorder_video_one);

        mSurfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        mImageView = (ImageView) findViewById(R.id.imageview);
        mBtnStartStop = (Button) findViewById(R.id.btnStartStop);
        mBtnStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mStartedFlg) {
                    mImageView.setVisibility(View.GONE);
                    mStartedFlg = true;
                    startRecordVideo();
                    mBtnStartStop.setText("Stop");
                } else {
                    mStartedFlg = false;
                    stopRecordVideo();
                    mBtnStartStop.setText("Start");
                }
            }
        });

        SurfaceHolder holder = mSurfaceview.getHolder();
        holder.addCallback(this);
        //setType必须设置，要不出错.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (!mStartedFlg) {
            mImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 初始化MediaRecorder和Camera对象，并设置MediaRecorder的参数
     */
    private void startRecordVideo() {
        Log.d(TAG, "startRecordVideo");

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "uploadFileInSubThread in" + ",  threadId : " + Process.myTid());
                uploadIme(videoUrl, "app_login");
            }
        });

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
            if (!dir.exists())
                dir.mkdir();
            path = dir + "/" + getDate() + ".mp4";
            Log.d(TAG, "startRecordVideo path: " + path);
        } else {
            return;
        }
        mRecorder.setOutputFile(path);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.d(TAG, "startRecordVideo IOException e" + e.getMessage());
            e.printStackTrace();
        }
        mRecorder.start();
    }

    private void stopRecordVideo() {
        Log.d(TAG, "stopRecordVideo");
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
            uploadFileInSubThread(path);
            path = null;
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
        stopRecordVideo();
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

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.d(TAG, "onError   mr: " + mr + ",  what: " + mr + ",  extra: " + extra);
    }

    /**
     * 使用子线程上传记录好的视频
     */
    public void uploadFileInSubThread(final String filePath) {
        Log.d(TAG, "uploadFileInSubThread out" + ",  threadId : " + Process.myTid());
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "uploadFileInSubThread in" + ",  threadId : " + Process.myTid());
                uploadFile(videoUrl, filePath);
            }
        });
    }

    /**
     * 记录一个新视频
     */
    private void resetRecorder() {
        stopRecordVideo();
        try {
            startRecordVideo();
        } catch (IllegalStateException e) {
            Log.d(TAG, "resetRecorder IllegalStateException e:" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 上传录像文件到网络服务器，使用了OkGo框架
     *
     * @param uploadUrl 服务器的地址
     * @param filePath  录像文件的本地地址
     */
    private void uploadFile(String uploadUrl, String filePath) {
        //从视频文件完整路径中取出文件名
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        Log.d(TAG, "uploadFile uploadUrl: " + uploadUrl + ",  filePath: " + filePath + ",  fileName: " + fileName
                + ",  threadId : " + Process.myTid());
        OutputStream dos = null;
        InputStream fis = null;
        InputStream fisTwo = null;
        BufferedReader br = null;
        try {
            //定义一个字节数组
            byte[] b;
            {
                //文件数据流前需要加上公司需求的header数据
                String header = "method=app_commit" + "&mac=" + getIMEI(RecorderVideoActivityOne.this) + "&video=" + fileName;
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
                //Log.d(TAG, "uploadFile  3333333333333  b: " + (new String(b)));

                // 读取文件
                fis = new FileInputStream(filePath);
                //3.实现复制
                byte[] temp = new byte[1024];
                int len;
                while ((len = fis.read(temp)) != -1) {
                    System.arraycopy(temp, 0, b, pos, len);
                    pos += len;
                }
                //fis.read(b, pos, fileLength);
            }

            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            Log.d(TAG, "uploadFile,  " + ",  url: " + url + "httpURLConnection: " + httpURLConnection);
            //设置连接超时为5秒
            httpURLConnection.setConnectTimeout(5000);
            httpURLConnection.setReadTimeout(5000);
            // 使用POST方法
            httpURLConnection.setRequestMethod("POST");
            // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
            // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
            //httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            //httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            //httpURLConnection.setRequestProperty("Charset", "UTF-8");
            //httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;");

            //获取网络输出流，并把数据b写入其中
            dos = httpURLConnection.getOutputStream();
            dos.write(b);
            dos.flush();
            Log.d(TAG, "uploadFile  44444444444  dos: " + dos);

            //获取响应码 200=成功 当响应成功，获取响应的流
            final int res = httpURLConnection.getResponseCode();
            Log.d(TAG, "uploadFile  5555555555    res: " + res);
            InputStream input = httpURLConnection.getInputStream();
            Log.d(TAG, "uploadFile  5555555555    input: " + input);
            StringBuffer sb1 = new StringBuffer();
            sb1.append("start: ");
            int ss;
            while ((ss = input.read()) != -1) {
                sb1.append((char) ss);
            }
            input.close();
            String result = sb1.toString();
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
     * 上传手机ime号到网络服务器
     * 使用 Get 方式
     *
     * @param uploadUrl  服务器的地址
     * @param methodName methodName
     */
    private void uploadIme(String uploadUrl, String methodName) {
        Log.d(TAG, "uploadFile uploadUrl: " + uploadUrl + ",  methodName: " + methodName + ",  threadId : " + Process.myTid());
        try {
            //创建URL对象
            //Get请求可以在Url中带参数： ①url + "?bookname=" + name;②url="http://www.baidu.com?name=zhang&pwd=123";
            URL url = new URL(uploadUrl + "method=" + methodName
                    + "&mac=" + getIMEI(RecorderVideoActivityOne.this));
            //返回一个URLConnection对象，它表示到URL所引用的远程对象的连接
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            //在这里设置一些属性，详细见UrlConnection文档，HttpURLConnection是UrlConnection的子类
            //设置连接超时为5秒
            httpURLConnection.setConnectTimeout(5000);
            //设定请求方式(默认为get)
            httpURLConnection.setRequestMethod("GET");
            //建立到远程对象的实际连接
            httpURLConnection.connect();
            //返回打开连接读取的输入流，输入流转化为StringBuffer类型，这一套流程要记住，常用
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            String line = null;
            StringBuffer stringBuffer = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                //转化为UTF-8的编码格式
                line = new String(line.getBytes("UTF-8"));
                stringBuffer.append(line);
            }
            Log.e(TAG, "Get请求返回的数据: " + stringBuffer.toString());
            bufferedReader.close();
            httpURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    private Handler mHandler = new Handler();
}

