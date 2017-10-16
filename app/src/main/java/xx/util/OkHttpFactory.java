package xx.util;

/**
 * Created by xiaoxin on 17-9-13.
 */

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
//import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * Created by spring on 16-8-17.
 * 获取OkHttp实例
 */
public class OkHttpFactory {
    private static OkHttpFactory ourInstance = new OkHttpFactory();
    private static final int CONNECT_TIMEOUT_TIME = 15;
    private static OkHttpClient.Builder sBuilder;

    /**
     * 下载进度
     */
    public interface ProgressListener {
        /**
         * @param bytesRead     当前读取字节数
         * @param contentLength 总字节长度
         * @param done          是否完成标识
         */
        void update(long bytesRead, long contentLength, boolean done);
    }

    public static final OkHttpFactory getInstance() {
        return ourInstance;
    }

    private OkHttpFactory() {
        sBuilder = new OkHttpClient.Builder();
        sBuilder//.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                //.retryOnConnectionFailure(true)
                .connectTimeout(CONNECT_TIMEOUT_TIME, TimeUnit.SECONDS)
                //.addNetworkInterceptor(new TokenInterceptor())
                //.addNetworkInterceptor(mRegionInterceptor)
                ;
    }

    /**
     * 常用OkHttp的配置
     * @return
     */
    public OkHttpClient getOkHttpClient() {
        OkHttpClient okHttpClient = sBuilder.build();
        return okHttpClient;
    }

    /**
     * 下载文件OkHttp的配置
     *
     * @param progressListener
     * @return
     */
    public OkHttpClient getDownloadOkHttpClient(final ProgressListener progressListener) {
        OkHttpClient okHttpClient = sBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), progressListener))
                        .build();
            }
        }).build();
        return okHttpClient;
    }

    private static class ProgressResponseBody extends ResponseBody {
        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // 增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    //回调，如果contentLength()不知道长度，会返回-1
                    progressListener.update(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                    return bytesRead;
                }
            };
        }
    }
}