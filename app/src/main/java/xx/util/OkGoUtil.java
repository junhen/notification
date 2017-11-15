package xx.util;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.BitmapCallback;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.cookie.store.PersistentCookieStore;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by xiaoxin on 17-11-15.
 */

public class OkGoUtil {
    private static final String TAG = "OkGoUtil";
    private static Application mApplication = null;

    /**
     * 进行全局配置，一般在 Aplication，或者基类的onCreate方法中，只需要调用一次即可，
     * 可以配置调试开关，全局的超时时间，公共的请求头和请求参数等信息。
     * 如果在Aplication中初始化，需要在清单文件中注册 Aplication。
     *
     * @param application
     */
    public static void initOkGo(Application application){
        if(mApplication != null) {
            Log.d(TAG, "initOkGo has been exected");
            return;
        }
        mApplication = application;

        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
        HttpHeaders headers = new HttpHeaders();
        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文
        headers.put("commonHeaderKey2", "commonHeaderValue2");
        HttpParams params = new HttpParams();
        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
        params.put("commonParamsKey2", "这里支持中文参数");
        //-----------------------------------------------------------------------------------//

        OkGo.init(application);

        //以下设置的所有参数是全局参数,同样的参数可以在请求的时候再设置一遍,那么对于该请求来讲,请求中的参数会覆盖全局参数
        //好处是全局参数统一,特定请求可以特别定制参数
        try {
            //以下都不是必须的，根据需要自行选择,一般来说只需要 debug,缓存相关,cookie相关的 就可以了
            OkGo.getInstance()

                    // 打开该调试开关,打印级别INFO,并不是异常,是为了显眼,不需要就不要加入该行
                    // 最后的true表示是否打印okgo的内部异常，一般打开方便调试错误
                    .debug("OkGo", Level.INFO, true)

                    //如果使用默认的 60秒,以下三行也不需要传
                    .setConnectTimeout(OkGo.DEFAULT_MILLISECONDS)  //全局的连接超时时间
                    .setReadTimeOut(OkGo.DEFAULT_MILLISECONDS)     //全局的读取超时时间
                    .setWriteTimeOut(OkGo.DEFAULT_MILLISECONDS)    //全局的写入超时时间

                    //可以全局统一设置缓存模式,默认是不使用缓存,可以不传,具体其他模式看 github 介绍 https://github.com/jeasonlzy/
                    .setCacheMode(CacheMode.NO_CACHE)

                    //可以全局统一设置缓存时间,默认永不过期,具体使用方法看 github 介绍
                    .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)

                    //可以全局统一设置超时重连次数,默认为三次,那么最差的情况会请求4次(一次原始请求,三次重连请求),不需要可以设置为0
                    .setRetryCount(3)

                    //如果不想让框架管理cookie（或者叫session的保持）,以下不需要
                    //      .setCookieStore(new MemoryCookieStore())            //cookie使用内存缓存（app退出后，cookie消失）
                    .setCookieStore(new PersistentCookieStore())        //cookie持久化存储，如果cookie不过期，则一直有效

                    //可以设置https的证书,以下几种方案根据需要自己设置
                    .setCertificates()                                  //方法一：信任所有证书,不安全有风险
                    //      .setCertificates(new SafeTrustManager())            //方法二：自定义信任规则，校验服务端证书
                    //      .setCertificates(getAssets().open("srca.cer"))      //方法三：使用预埋证书，校验服务端证书（自签名证书）
                    //              //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
                    //      .setCertificates(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"))//

                    //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
                    //      .setHostnameVerifier(new SafeHostnameVerifier())

                    //可以添加全局拦截器，不需要就不要加入，错误写法直接导致任何回调不执行
                    //      .addInterceptor(new Interceptor() {
                    //            @Override
                    //            public Response intercept(Chain chain) throws IOException {
                    //                 return chain.proceed(chain.request());
                    //            }
                    //       })

                    //这两行同上，不需要就不要加入
                    .addCommonHeaders(headers)  //设置全局公共头
                    .addCommonParams(params);   //设置全局公共参数

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get请求获取数据
     * @param url
     */
    private void getByOkGo(String url){
        OkGo.get(url)                            // 请求方式和请求url
                .tag(this)                       // 请求的 tag, 主要用于取消对应的请求
                .cacheKey("cacheKey")            // 设置当前请求的缓存key,建议每个不同功能的请求设置一个
                .cacheMode(CacheMode.DEFAULT)    // 缓存模式，详细请看缓存介绍
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        //mTextView2.setText(s);
                    }
                });
    }

    /**
     * post请求获取数据，其中params方法用来添加请求参数，params添加参数的时候,
     * 最后一个isReplace为可选参数,默认为true,即代表相同key的时候,后添加的会覆盖先前添加的；
     * post请求
     * @param url
     */
    private void postByOkGo(String url){
        OkGo.post(url)
                .tag(this)
                .cacheKey("cachePostKey")
                .cacheMode(CacheMode.DEFAULT)
                .params("method", "album.item.get")
                .params("appKey", "myKey")
                .params("format", "json")
                .params("albumId", "Lqfme5hSolM")
                .params("pageNo", "1")
                .params("pageSize", "2")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        //mTextView2.setText(s);
                    }

                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        //mTextView2.setText(e.getMessage());
                    }
                });
    }

    /**
     * 文件下载，FileCallback具有三个重载的构造方法，文件目录如果不指定,默认下载的目录为 sdcard/download/，文件名如果不指定,则按照以下规则命名:
     * 首先检查用户是否传入了文件名,如果传入,将以用户传入的文件名命名
     * 如果没有传入,那么将会检查服务端返回的响应头是否含有Content-Disposition=attachment;filename=FileName.txt该种形式的响应头,
     * 如果有,则按照该响应头中指定的文件名命名文件,如FileName.txt
     * 如果上述响应头不存在,则检查下载的文件url,例如:http://image.baidu.com/abc.jpg,那么将会自动以abc.jpg命名文件
     * 如果url也把文件名解析不出来,那么最终将以nofilename命名文件；
     *
     * 下载文件
     * @param url 下载地址
     * @param destFileDir 保存文件路径
     * @param destFileName 保存文件名
     */
    private void downLoad(String url, String destFileDir, String destFileName){
        OkGo.get(url)//
                .tag(this)//
                .execute(new FileCallback(destFileDir, destFileName) {  //文件下载时，可以指定下载的文件目录和文件名
                    @Override
                    public void onSuccess(File file, Call call, Response response) {
                        // file 即为文件数据，文件保存在指定目录
                    }

                    @Override
                    public void downloadProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
                        //这里回调下载进度(该回调在主线程,可以直接更新ui)
                        //currentSize totalSize以字节byte为单位
                    }
                });
    }

    /**
     * 多文件上传
     * @param url
     * @param keyName
     * @param files 文件集合
     */
    private void uploadFiles(String url, String keyName, List<File> files){
        OkGo.post(url)//
                .tag(this)//
                //.isMultipart(true)       // 强制使用 multipart/form-data 表单上传（只是演示，不需要的话不要设置。默认就是false）
                //.params("param1", "paramValue1")        // 这里可以上传参数
                //.params("file1", new File("filepath1"))   // 可以添加文件上传
                //.params("file2", new File("filepath2"))     // 支持多文件同时添加上传
                .addFileParams(keyName, files)    // 这里支持一个key传多个文件
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        //上传成功
                        Toast.makeText(mApplication, "上传成功", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void upProgress(long currentSize, long totalSize, float progress, long networkSpeed) {
                        //这里回调上传进度(该回调在主线程,可以直接更新ui)
                        //mProgressBar.setProgress((int) (100 * progress));
                        //mTextView2.setText("已上传" + currentSize/1024/1024 + "MB, 共" + totalSize/1024/1024 + "MB;");
                    }
                });
    }

    /**
     * 请求网络图片
     * @param url
     */
    private void getBitmap(String url) {
        OkGo.get(url)//
                .tag(this)//
                .execute(new BitmapCallback() {
                    @Override
                    public void onSuccess(Bitmap bitmap, Call call, Response response) {
                        // bitmap 即为返回的图片数据
                        //mImageView.setImageBitmap(bitmap);
                    }
                });
    }

    //取消请求。每个请求前都设置了一个参数tag，取消则通过OkGo.cancel(tag)执行。 例如：在Activity中，当Activity销毁取消请求，可以在onDestory里面统一取消；
    /*@Override
    protected void onDestroy() {
        super.onDestroy();

        //根据 Tag 取消请求
        OkGo.getInstance().cancelTag(this);

        //取消所有请求
        OkGo.getInstance().cancelAll();
    }*/


    //具体实例
    /*public void getData() {
        //命令字符串前加6个字节的长度
        //loadingDialog.show();
        String txt = "method=d_qrcodecommit&d_account=" + mDriver.getD_account() +
                "&safecode=" + mDriver.getSafecode() +
                "&company=" + mDriver.getCompany();
        if(photo_f != null){
            txt += "&photo=" + photo_f.getName();
        }
        if(Ewm_f != null){
            txt += "&wechat=" + Ewm_f.getName();
        }
        if(Zfb_f != null){
            txt +=  "&alipay=" + Zfb_f.getName();
        }
        try {
            txt = URLEncoder.encode(txt,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String str = String.format("%06d", txt.length());
        txt = str+txt;
        int len = 0;
        int len2 = 0;
        int len3 = 0;
        int feilnamelen = 0;
        if(photo_f != null){
            feilnamelen += 4;
            len = (int) photo_f.length();        //文件长度
        }
        if(Ewm_f != null){
            feilnamelen += 4;
            len2 = (int) Ewm_f.length();        //文件长度
        }
        if(Zfb_f != null){
            feilnamelen += 4;
            len3 = (int) Zfb_f.length();        //文件长度
        }

        //将前面生成的长度和命令字符串放到字节数组中
        int pos = 0;
        int pos2 = 0;
        int pos3 = 0;
        byte[] b = new byte[txt.length() + feilnamelen + (int) len + len2 + len3];
        System.arraycopy(txt.getBytes(), 0, b, 0, txt.length());
        //将文件长度放到字节数组中
        if(photo_f != null){
            pos += txt.length();
            b[pos++] = (byte) ((len & 0xff000000) >> 24);
            b[pos++] = (byte) ((len & 0x00ff0000) >> 16);
            b[pos++] = (byte) ((len & 0x0000ff00) >> 8);
            b[pos++] = (byte) (len & 0x000000ff);
        }
        if(Ewm_f != null){
            pos2 = txt.length()+feilnamelen-4+len;
            b[pos2++] = (byte) ((len2 & 0xff000000) >> 24);
            b[pos2++] = (byte) ((len2 & 0x00ff0000) >> 16);
            b[pos2++] = (byte) ((len2 & 0x0000ff00) >> 8);
            b[pos2++] = (byte) (len2 & 0x000000ff);
        }
        if(Zfb_f != null){
            pos3 = txt.length()+feilnamelen-4+len+len2;
            b[pos3++] = (byte) ((len3 & 0xff000000) >> 24);
            b[pos3++] = (byte) ((len3 & 0x00ff0000) >> 16);
            b[pos3++] = (byte) ((len3 & 0x0000ff00) >> 8);
            b[pos3++] = (byte) (len3 & 0x000000ff);
        }
        //将文件内容放到字节数组中
        try {
            if(photo_f != null){
                InputStream in1 = new FileInputStream(photo_f);
                in1.read(b, pos, len);            //从文件中读到字节数组中
            }
            if(Ewm_f != null){
                InputStream in2 = new FileInputStream(Ewm_f);
                in2.read(b, pos2, len2);            //从文件中读到字节数组中
            }
            if(Zfb_f != null){
                InputStream in3 = new FileInputStream(Zfb_f);
                in3.read(b, pos3, len3);            //从文件中读到字节数组中
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        OkGo.post(Constant.DOMAIN)
                .upBytes(b)
                .execute(new StringCallback() {
                    //                    请求成功
                    @Override
                    public void onSuccess(String s, Call call, Response response) {
                        Log.d("***上传二维码和照片***", s.toString());
                        Result mResult = JsonUtil.JsonToObjs(s, Result.class);
                        if (mResult.code.equals("1")) {
                            ToastShow("提交成功");
                            loadingDialog.dismiss();
                            deleteTempImg();
                            getDriver();
                        } else {
                            ToastShow("提交失败");
                            loadingDialog.dismiss();
                        }
                    }
                    //请求失败
                    @Override
                    public void onError(Call call, Response response, Exception e) {
                        super.onError(call, response, e);
                        ToastShow("失败");
                        loadingDialog.dismiss();
                    }
                });
    }*/


}
