package xx.activity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.leui.notification.test.R;

public class RecorderVideoActivity extends Activity implements OnClickListener, OnErrorListener, OnInfoListener {

    public static final String TAG = "Recorder-TAG";
    private final static int CONNECT_OUT_TIME = 5000;  //联网超时时间

    //	private UploadThread mUploadThread;
//	private Handler mUploadHandler;
    //按钮
    private Button btnStart;//开始录像并上传
    private Button btnStop;//停止录像
    //显示视频预览的SurfaceView
    private SurfaceView sView;
    //录制视频类
    private MediaRecorder mMediaRecorder = null;
    //调用摄像头硬件
    private Camera camera = null;
    //手机串号
    private String Imei = null;

    //存储视频路径
    private String path = null;

    private String date = null;
    //视频文件
    private File videoFile;
    //上传IMEI路径
    private String ImeiUrl = null;
    //上传video路径
    private String Url = "http://59.49.99.195:34562/videoplatform/communication/mainserver?";

    //记录是否正在进行录制
    private boolean isRecording = false;
    //记录是否正在进行上传
    private boolean isUploading = false;
    //记录是否已经联网
    private boolean isConNet = false;

//	 DataOutputStream dos = null;
//	 InputStream fisTwo = null;
//	 InputStreamReader isr = null;
//	 BufferedReader br = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "oncreat----->");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder_video);

        //初始化界面
        intiView();

        //启动一个上传视频的HandlerThread线程
//        mUploadThread = new UploadThread("上传video");
//        mUploadThread.start();
//        /**
//         * 开启新线程，上传IMEI号
//         */
//        new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				// 上传手机IMEI号
//				uploadImei(Imei);
//			}
//		});

    }


    //上传IMEI

    protected String uploadImei(String IEMI, String methodname) {
        Log.e(TAG, "onClick----->Netstart_uploadImei--->" + methodname);
        ImeiUrl = Url + "method=" + methodname + "&mac=" + Imei;
        Log.e(TAG, "onClick----->Netstart_uploadImei--->ImeiUrl" + ImeiUrl);
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
        HttpConnectionParams.setConnectionTimeout(params, 5000);
        HttpClient httpClient = new DefaultHttpClient(params);
        HttpGet get = new HttpGet(ImeiUrl);

        try {

            HttpResponse response = httpClient.execute(get);
            Log.e("HttpUtil.getJson", "HttpResponse--> " + response);
            HttpEntity httpEntity = response.getEntity();
            Log.e("HttpUtil.getJson", "HttpEntity--> " + httpEntity);
            String jsonString = EntityUtils.toString(httpEntity);
            Log.e("HttpUtil.getJson", "String--> " + jsonString);
            return jsonString;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * 初始化控件
     */
    private void intiView() {
        Log.e(TAG, "intiView----->");

        Imei = getIMEI(this);//获取手机Imei号
        btnStart = (Button) findViewById(R.id.start);
        btnStop = (Button) findViewById(R.id.stop);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnStop.setEnabled(false);

        sView = (SurfaceView) findViewById(R.id.sview);
        // 设置分辨率
        sView.getHolder().setFixedSize(1920, 780);
        // 设置该组件让屏幕不会自动关闭
        sView.getHolder().setKeepScreenOn(true);

    }


    /**
     * * 响应开始按钮
     * 开始录像并存储
     * 开启线程上传
     */
    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick------->");
        switch (v.getId()) {
            case R.id.start:
                Log.e(TAG, "onClick----->start" + !isRecording);
//			if(isNetworkAvailable(this)){

                if (!isRecording) {
//					Log.e(TAG,"onClick----->Net"+isNetworkAvailable(this));
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        @Override
                        public void run() {

                            uploadImei(Imei, "app_login");
                        }
                    });
                    startRecord();
                    btnStart.setEnabled(false);
                    btnStop.setEnabled(true);
                    isRecording = true;
                }
//			}
//			else{
                //提示没有联网
//				Toast.makeText(getApplicationContext(), "请检查一下网络", Toast.LENGTH_SHORT).show();
//			}
                break;
            case R.id.stop:
                Log.e(TAG, "onClick----->stop" + isRecording);
                if (isRecording) {
                    Log.e(TAG, "stop");
                    btnStart.setEnabled(true);
                    isRecording = false;
                    stopRecord();
                    btnStop.setEnabled(false);
                    if (isNetworkAvailable(this)) {
                        Log.e(TAG, "onClick----->Net_stop" + isNetworkAvailable(this));
                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                uploadImei(Imei, "app_exit");
                            }
                        });
                    }
                }
                break;
            default:
                break;
        }
    }


    //开始录像并上传
    private void startRecord() {
        Log.e(TAG, "startRecord------>");
        //初始化MediaRecorder和Camera对象，并设置MediaRecorder的参数
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        }
        if (camera == null) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }

        if (camera != null) {
            camera.setDisplayOrientation(90);
            camera.unlock();
        }
        mMediaRecorder.setCamera(camera);

        // 这两项需要放在setOutputFormat之前
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 设置从摄像头采集图像

        // 设置视频文件的输出格式，必须在设置声音编码格式、图像编码格式之前设置
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        // 设置声音编码的格式
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        // 设置图像编码的格式
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        mMediaRecorder.setVideoSize(640, 480);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
        mMediaRecorder.setOrientationHint(90);
        // 指定使用SurfaceView来预览视频
        mMediaRecorder.setPreviewDisplay(sView.getHolder().getSurface());
        //设置记录会话的最大持续时间（毫秒）
        mMediaRecorder.setMaxDuration(3 * 1000);

        //增加监听器
//		mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnInfoListener(this);

        path = getSDPath();
        if (path != null) {
            File dir = new File(path + "/1ywhtest");
            if (!dir.exists()) {
                dir.mkdir();
            }
            date = getDate();
            path = dir + "/" + date + ".mp4";
            Log.e(TAG, "startRecord------>path" + path);
//			videoFile = new File(path);
        } else {
            Log.e(TAG, "startRecord------>path" + path);
            return;
        }
        mMediaRecorder.setOutputFile(path);

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mMediaRecorder.start();
        Log.e(TAG, "startRecord------>mMediaRecorder------->start");

    }


//    private void stopRecord() {
//		// TODO Auto-generated method stub
//    	Log.e(TAG, "startRecord------>mMediaRecorder------->start");
//			try {
//
//				mMediaRecorder.stop();
//				mMediaRecorder.reset();
//				mMediaRecorder.release();
//				mMediaRecorder = null;
//				if (camera != null) {
//					camera.release();
//					camera = null;
//				}
//			} catch (Exception e) {
//				// TODO: handle exception
//				e.printStackTrace();
//			}
//		}
//


    /**
     * 获取手机IMEI号
     */
    public String getIMEI(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        return imei;
    }


    /**
     * 获取SD path
     *
     * @return
     */
    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }
        return null;
    }

    /**
     * 获取系统时间
     *
     * @return
     */
    public static String getDate() {
        Calendar ca = Calendar.getInstance();
        int year = ca.get(Calendar.YEAR); // 获取年份
        int month = ca.get(Calendar.MONTH); // 获取月份
        int day = ca.get(Calendar.DATE); // 获取日
        int minute = ca.get(Calendar.MINUTE); // 分
        int hour = ca.get(Calendar.HOUR); // 小时
        int second = ca.get(Calendar.SECOND); // 秒

        String date = "" + year + (month + 1) + day + String.format("%02d", hour) + String.format("%02d", minute) + String.format("%02d", second);
        Log.e(TAG, "date:" + date);
        Log.e(TAG, "minute:" + String.format("%02d", minute));

        return date;
    }


//	//上传的线程类，主要是通过handler的post，是的runnable在此线程执行
//    class UploadThread extends HandlerThread {
//        public UploadThread(String name) {
//            super(name);
//        }
//
//        @Override
//        protected void onLooperPrepared() {
//        	mUploadHandler = new Handler(getLooper());
//        }
//    }


    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.e(TAG, "onInfo------->记录一个新视频");

            //记录一个新视频

//			mMediaRecorder.stop();
            resetRecorder();
//			startRecord();
        }

    }


    /**
     * 记录一个新视频
     */
    private void resetRecorder() {
//		releaseRecodeVideo();
        Log.e(TAG, "resetRecorder------->");
        stopRecord();
        startRecord();

    }


    private void stopRecord() {
        Log.e(TAG, "stopRecord-------->");
//		if (isNetworkAvailable(this)){
        if (path != null) {
            Log.e(TAG, "stopRecord-------->path" + path);
            uploadFileInSubThread(path);
            path = null;
        }
//		}else{
        //提示没有联网
//			Toast.makeText(getApplicationContext(), "请检查一下网络", Toast.LENGTH_SHORT).show();
//		}

        if (mMediaRecorder != null) {
//        	mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.stop();
//        	mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (camera != null) {
            camera.release();
            camera = null;
        }

    }

//	private void releaseRecodeVideo() {
//		if (isNetworkAvailable(this)){
//			if(path != null)
//			{
//				uploadFileInSubThread(path);
//				path=null;
//			}
//		}else{
//			//提示没有联网
//			Toast.makeText(getApplicationContext(), "请检查一下网络", Toast.LENGTH_SHORT).show();
//		}
//
//        if (mMediaRecorder != null) {
////        	mMediaRecorder.setOnErrorListener(null);
//        	mMediaRecorder.setOnInfoListener(null);
//        	mMediaRecorder.stop();
////        	mMediaRecorder.reset();
//        	mMediaRecorder.release();
//        	mMediaRecorder = null;
//        }
//        if (camera != null) {
//            camera.release();
//            camera = null;
//        }
//
//	}

    /**
     * 使用子线程上传记录好的视频
     */
    public void uploadFileInSubThread(final String filePath) {
        Log.e(TAG, "uploadFileInSubThread");
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                uploadFile(Url, filePath);
            }
        });
    }


    /**
     * 使用子线程上传记录好的视频
     */
//	private void sendRecorde() {
//		// TODO Auto-generated method stub
//		final String filePath = path;
//        final String fileName = date + ".mp4";
//        mUploadHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                //调用上传录像文件的方法
//                uploadFile(videoUrl, filePath, fileName);
//                File file = new File(filePath);
//                file.delete();
//
//            }
//        });
//	}
    protected void uploadFile(String Url, String filePath) {
        Log.e(TAG, "uploadFile------->");
        //从视频文件完整路径中取出文件名
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        Log.e(TAG, "uploadFile------->fileName" + fileName);
        String header = "method=app_commit&mac=" + Imei + "&video=" + fileName;
        Log.e(TAG, "uploadFile------->header----->" + header);
        try {
            header = URLEncoder.encode(header, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        header = String.format("%06d", header.length()) + header;
        Log.e(TAG, "uploadFile------->headerutf-8----->" + header);

        File upfile = new File(filePath);

        Log.e(TAG, "uploadFile------>upfile" + upfile.exists());
        Log.e(TAG, "uploadFile------>upfilename" + upfile.getName());
        Log.e(TAG, "uploadFile------->filePath----->" + filePath);

        int fileLength = (int) upfile.length(); //文件长度
        Log.e(TAG, "uploadFile------->fileLength" + fileLength);
        int fileNameLength = 4;

        //定义一个字节数组
        byte[] b = new byte[header.length() + fileNameLength + fileLength];
        int pos = 0;

        //将前面生成的长度和命令字符串放到字节数组中
        System.arraycopy(header.getBytes(), 0, b, 0, header.length());
        pos += header.length();
        Log.e(TAG, "uploadFile------->pos" + pos);
        //将文件长度放到字节数组中

        b[pos++] = (byte) ((fileLength & 0xff000000) >> 24);
        b[pos++] = (byte) ((fileLength & 0x00ff0000) >> 16);
        b[pos++] = (byte) ((fileLength & 0x0000ff00) >> 8);
        b[pos++] = (byte) (fileLength & 0x000000ff);
        //将文件内容放到字节数组中
        InputStream in = null;
        try {
            if (upfile != null) {
                in = new FileInputStream(upfile);
                in.read(b, pos, fileLength);            //从文件中读到字节数组中
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
        Log.e(TAG, "uploadFile------->Post-->b" + in.toString());
//	    String sback = Post(videoUrl, b);
        Post(Url, b);
        String code;
//	    if(sback!=null){
//			try {
//				 Log.e(TAG, "uploadFile------->sback"+sback);
//					JSONObject jObject=new JSONObject(sback);
//
//				    code=jObject.getString("code");
//
//				if(code.equals("1")){
////					file.delete();
//				}
//				else{
//					//重传
//					Post(videoUrl, b);
//				}
//
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//	    }else{
//	    	//提示上传出错的toast
//	    }
//


    }


    private String Post(String url, byte[] filebyte) {
        Log.e(TAG, "Post------->purl: " + url);
        String str = null;
        try {
            // 第一步：创建必要的URL对象
            URL httpUrl = new URL(url);
            // 第二步：根据URL对象，获取HttpURLConnection对象
            HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
            // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
            // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
//	            connection.setChunkedStreamingMode(128 * 1024);// 128K
            // 第三步：为HttpURLConnection对象设置必要的参数（是否允许输入数据、连接超时时间、请求方式）
            connection.setConnectTimeout(CONNECT_OUT_TIME);
            connection.setReadTimeout(CONNECT_OUT_TIME);
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            // 第四步：向服务器写入数据
            OutputStream out = connection.getOutputStream();
            //            String content = "name=" + name + "&pwd=" + pwd;// 无论服务器转码与否，这里不需要转码，因为Android系统自动已经转码为utf-8啦
            out.write(filebyte);
            out.flush();
            out.close();

            int response = connection.getResponseCode();            //获得服务器的响应码
            Log.e(TAG, "Post------->response" + response);
//	            if(response == HttpURLConnection.HTTP_OK) {
//	                InputStream inptStream = connection.getInputStream();
//	                //处理服务器的响应结果
//	                return dealResponseResult(inptStream);
//	            }
            // 第五步：开始读取服务器返回数据
//	            BufferedReader reader = new BufferedReader(new InputStreamReader(
//	                    connection.getInputStream()));
//	            final StringBuffer buffer = new StringBuffer();
//
//	            while ((str = reader.readLine()) != null) {
//	                buffer.append(str);
//	            }
//	            reader.close();
//	            Log.e(TAG, "Post------->str"+str);
            return str;
        } catch (Exception e) {

            e.printStackTrace();

//		        return "err: " + e.getMessage().toString();
            return null;
        }


    }


	/*
        * Function  :   处理服务器的响应结果（将输入流转化成字符串）
	    * Param     :   inputStream服务器的响应输入流
	    */
//	   public static String dealResponseResult(InputStream inputStream) {
//	       String resultData = null;      //存储处理结果
//	       ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//	      byte[] data = new byte[1024];
//	      int len = 0;
//	       try {
//	          while((len = inputStream.read(data)) != -1) {
//	             byteArrayOutputStream.write(data, 0, len);
//	          }
//	     } catch (IOException e) {
//	         e.printStackTrace();
//	        }
//	       resultData = new String(byteArrayOutputStream.toByteArray());
//	       return resultData;
//	   }


    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        // TODO Auto-generated method stub
        Log.e(TAG, "onError   mr: " + mr + ",  what: " + mr + ",  extra: " + extra);
    }

    /**
     * 检测当的网络（WLAN、3G/2G）状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        Log.e(TAG, "isNetworkAvailable------->");
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.e(TAG, "onDestroy------->");
        super.onDestroy();


    }

    //	/**
//	 * 监听返回--是否退出程序
//	 */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown------->");
        boolean flag = true;
        String title = null;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 是否退出应用
            if (path != null) {
                title = "有未上传的视频，确定要退出吗？";
            } else {
                title = "确定要app退出吗？";
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setTitle(title);
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Log.e(TAG, "DialogInterface------->AsyncTask");
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        @Override
                        public void run() {

                            uploadImei(Imei, "app_exit");
                        }
                    });
                    if (isRecording) {

                        if (mMediaRecorder != null) {
                            mMediaRecorder.setOnErrorListener(null);
                            mMediaRecorder.setOnInfoListener(null);
                            mMediaRecorder.stop();
                            mMediaRecorder.reset();
                            mMediaRecorder.release();
                            mMediaRecorder = null;
                        }
                        if (camera != null) {
                            camera.release();
                            camera = null;
                        }
                    }


                    dialog.dismiss();
                    finish();
//	                  android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                    //退出
//	                  AppManager.getAppManager().AppExit(cont);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }

        return flag;
    }

}