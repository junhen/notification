package xx.util;

import android.util.Log;
import android.widget.Toast;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by xiaoxin on 17-11-14.
 */

/**
 * 上传工具类
 */
public class UploadUtil {
    private static final String TAG = "UploadUtil";
    private static UploadUtil uploadUtil;
    public static final String BOUNDARY = UUID.randomUUID().toString(); // 边界标识
    // 随机生成
    private static final String PREFIX = "--"; //twoHyphens
    private static final String LINE_END = "\r\n";
    private static final String CONTENT_TYPE = "multipart/form-data"; // 内容类型

    /**
     * 单例模式获取上传工具类
     *
     * @return
     */
    public static UploadUtil getInstance() {
        if (null == uploadUtil) {
            uploadUtil = new UploadUtil();
        }
        return uploadUtil;
    }

    private static int readTimeOut = 10 * 1000; // 读取超时
    private static int connectTimeout = 10 * 1000; // 超时时间

    private static final String CHARSET = "utf-8"; // 设置编码

    /***
     * 上传成功
     */
    public static final int UPLOAD_SUCCESS_CODE = 1;

    /**
     * 文件不存在
     */
    public static final int UPLOAD_FILE_NOT_EXISTS_CODE = 2;

    /**
     * 服务器出错
     */
    public static final int UPLOAD_SERVER_ERROR_CODE = 3;
    protected static final int WHAT_TO_UPLOAD = 1;
    protected static final int WHAT_UPLOAD_DONE = 2;


    /**
     * 上传文件到服务器
     *
     * @param filePath 需要上传的文件的路径
     * @param fileKey  在网页上<input type=file name=xxx/> xxx就是这里的fileKey
     * @param req      请求的URL
     */
    public static String uploadFile(String filePath, String fileKey, String req, Map<String, String> param) {
        if (param != null && param.size() > 0) {
            File file = new File(filePath);
            if (!file.exists()) return null;
            return uploadFile(file, fileKey, req, param);
        }
        return null;
    }

    /**
     * 上传文件
     */
    private static String uploadFile(File file, String fileKey, String RequestURL, Map<String, String> param) {
        String result = null;

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(readTimeOut);
            conn.setConnectTimeout(connectTimeout);
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", CHARSET); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            /**
             * 当文件不为空，把文件包装并且上传
             */
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            StringBuffer sb;
            String params;

            /***
             * 以下是用于上传参数
             */
            if (param != null && param.size() > 0) {
                Iterator<String> it = param.keySet().iterator();
                while (it.hasNext()) {
                    sb = new StringBuffer();
                    String key = it.next();
                    String value = param.get(key);
                    sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
                    sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"").append(LINE_END).append(LINE_END);
                    sb.append(value).append(LINE_END);
                    params = sb.toString();
                    dos.write(params.getBytes());
                }
            }

            sb = new StringBuffer();
            /**
             * 这里重点注意： name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件
             * filename是文件的名字，包含后缀名的 比如:abc.png
             */
            sb.append(PREFIX).append(BOUNDARY).append(LINE_END);
            sb.append("Content-Disposition:form-data; name=\"" + fileKey + "\"; filename=\"" + file.getName() + "\"" + LINE_END);
            sb.append("Content-Type:image/pjpeg" + LINE_END); // 这里配置的Content-type很重要的，用于服务器端辨别文件的类型的
            sb.append(LINE_END);
            params = sb.toString();
            dos.write(params.getBytes());

            /** 上传文件 */
            InputStream is = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                dos.write(bytes, 0, len);
            }
            is.close();


            dos.write(LINE_END.getBytes());
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
            dos.write(end_data);
            dos.flush();
            dos.close();

            //获取响应码 200=成功 当响应成功，获取响应的流
            int res = conn.getResponseCode();
            if (res == 200) {
                InputStream input = conn.getInputStream();
                StringBuffer sb1 = new StringBuffer();
                int ss;
                while ((ss = input.read()) != -1) {
                    sb1.append((char) ss);
                }
                input.close();
                result = sb1.toString();
            }
        } catch (MalformedURLException e) {
            Log.d(TAG, "uploadFile 文件读写异常" + "  MalformedURLException e: " + e);
        } catch (IOException e) {
            Log.d(TAG, "uploadFile 文件读写异常" + "  IOExceptione: " + e);
        }
        return result;
    }

    /**
     * 上传文件至Server，uploadUrl：接收文件的处理页面
     */
    public static void uploadFile(String uploadUrl, String srcPath) {
        Log.d(TAG, "uploadFile  uploadUrl: " + uploadUrl + ",   srcPath: " + srcPath);
        if (true) {
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
            Log.d(TAG, "uploadFile  3333333333333  dos: " + dos);

            FileInputStream fis = new FileInputStream(srcPath);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            // 读取文件
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            Log.d(TAG, "uploadFile  44444444444  dos: " + dos);
            dos.flush();

            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();

            Log.d(TAG, "uploadFile  5555555555    result: " + result);
            //Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            dos.close();
            is.close();

        } catch (Exception e) {
            Log.d(TAG, "uploadFile Exception e: " + e);
            e.printStackTrace();
            //setTitle(e.getMessage());
        }
    }

    /**
     * 上传文件，使用OkGo框架
     * @param uploadUrl
     * @param filePath
     * @param fileName
     */
    public static void uploadFile(String uploadUrl, String filePath, String fileName) {
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
        int feilnamelen = 4;
        //将前面生成的长度和命令字符串放到字节数组中
        int pos = 0;
        byte[] b = new byte[txt.length() + feilnamelen + len];
        System.arraycopy(txt.getBytes(), 0, b, 0, txt.length());
        //将文件长度放到字节数组中
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
                    //                    请求成功
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        Log.d("***上传二维码和照片***", s.toString());
                        /*Result mResult = JsonUtil.JsonToObjs(s, Result.class);
                        if (mResult.code.equals("1")) {
                            ToastShow("提交成功");
                            loadingDialog.dismiss();
                            deleteTempImg();
                            getDriver();
                        } else {
                            ToastShow("提交失败");
                            loadingDialog.dismiss();
                        }*/
                    }

                    //请求失败
                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        /*ToastShow("失败");
                        loadingDialog.dismiss();*/
                    }
                });
    }

}
