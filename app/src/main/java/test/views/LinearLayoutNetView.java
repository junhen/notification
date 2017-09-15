package test.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.leui.notification.test.R;

/**
 * Created by xiaoxin on 17-4-17.
 */

public class LinearLayoutNetView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = "LinearLayoutNetView";
    private final Activity mActivity;
    private EditText mUrlText;
    private Button mNetViewButton, mNetViewButton02;
    private WebView mWebView;
    private Button mControlBack, mmControlForward, mControlIn, mControlOut;
    private Button mLoadData;
    private float mOriginalScale;
    private static final Object mSynchroObject = new Object();
    private static final String APP_CACHE_DIRNAME = "/webcache"; // web缓存目录

    public LinearLayoutNetView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mActivity = (Activity)context;
    }

    @Override
    protected void onFinishInflate() {
        //mActivity.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        mUrlText = (EditText) findViewById(R.id.editText);
        mNetViewButton = (Button) findViewById(R.id.net_view_button);
        mNetViewButton.setOnClickListener(this);
        mWebView = (WebView) findViewById(R.id.web_view);
        mOriginalScale = mWebView.getScale();

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);    //支持Javascript脚本语言
        webSettings.setAllowFileAccess(true);      //允许WebView访问文件数据
        webSettings.setBuiltInZoomControls(true);  //支持内容缩放控制

        //设置缓存模式
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 开启DOM storage API 功能
        webSettings.setDomStorageEnabled(true);
        // 开启database storage API功能
        webSettings.setDatabaseEnabled(true);
        String cacheDirPath = getContext().getFilesDir().getAbsolutePath() + APP_CACHE_DIRNAME;
        Log.d(TAG, "cachePath = "+cacheDirPath);
        // 设置数据库缓存路径
        webSettings.setAppCachePath(cacheDirPath);
        webSettings.setAppCacheEnabled(true);
        //use in handling JavaScript dialogs, favicons, titles, and the progress.
        //This will replace the current handler.
        mWebView.setWebChromeClient(new MyWebChromeClient());
        //will receive various notifications and requests.
        //This will replace the current handler.
        mWebView.setWebViewClient(new MyWebViewClient());
        //用于被网页中的javascript调用.
        /** {@link #loadData()} 输入的仅仅是对象和对象名*/
        mWebView.addJavascriptInterface(new WebAppInterface(mActivity), "android");
        mNetViewButton02 = (Button) findViewById(R.id.net_view_button_02);
        mNetViewButton02.setOnClickListener(this);
        mLoadData = (Button) findViewById(R.id.display_webview_loaddata);
        mLoadData.setOnClickListener(this);
        mControlBack = (Button) findViewById(R.id.control_back);
        mControlBack.setOnClickListener(this);
        mmControlForward = (Button) findViewById(R.id.control_forward);
        mmControlForward.setOnClickListener(this);
        mControlIn = (Button) findViewById(R.id.control_zoom_in);
        mControlIn.setOnClickListener(this);
        mControlOut = (Button) findViewById(R.id.control_zoom_out);
        mControlOut.setOnClickListener(this);
        checkState();
        super.onFinishInflate();
    }

    @Override
    public void onClick(View v) {
        synchronized (mSynchroObject) {
            if (v.getId() == R.id.net_view_button) {
                displayNetView();
            } else if (v.getId() == R.id.net_view_button_02) {
                startWebViewActivity();
            } else if (v.getId() == R.id.display_webview_loaddata) {
                loadData();
            } else if (v.getId() == R.id.control_back) {
                backWebView();
            } else if (v.getId() == R.id.control_forward) {
                forwardWebView();
            } else if (v.getId() == R.id.control_zoom_in) {
                zoomInWebView();
            } else if (v.getId() == R.id.control_zoom_out) {
                zoomOutWebView();
            }
        }
        checkState();
    }

    private void checkState() {
        synchronized (mSynchroObject) {
            if (mWebView.canGoBack()) {
                mControlBack.setEnabled(true);
            } else {
                mControlBack.setEnabled(false);
            }

            if (mWebView.canGoForward()) {
                mmControlForward.setEnabled(true);
            } else {
                mmControlForward.setEnabled(false);
            }

            if (mWebView.getScale() < 4 * mOriginalScale) {
                mControlIn.setEnabled(true);
            } else {
                mControlIn.setEnabled(false);
            }

            if (mWebView.getScale() > 0.25 * mOriginalScale) {
                mControlOut.setEnabled(true);
            } else {
                mControlOut.setEnabled(false);
            }
        }
    }

    private void loadData() {
        //String summary = "<html><body>You scored <b>192</b> points.</body></html>";
        String summary = "<html><body>" +
                "<input type=\"button\" value=\"Say hello\" onClick=\"showAndroidToast('Hello Android!')\" />\n" +
                "\n" +
                "<script type=\"text/javascript\">\n" +
                "    function showAndroidToast(toast) {\n" +
                "        android.showToast(toast);\n" +
                "    }\n" +
                "</script>" +
                "</body></html>";
        mWebView.loadData(summary, "text/html", null);

    }

    private void backWebView() {
        if(mWebView.canGoBack()){
            mWebView.goBack();//返回上个页面
        }
    }

    private void forwardWebView() {
        if (mWebView.canGoForward()) {
            mWebView.goForward();
        }
    }

    private void zoomInWebView() {
        float scale = mWebView.getScale();
        if(scale < 4 * mOriginalScale){
            mWebView.setInitialScale((int)(scale * 2 * 100));
        }
    }


    private void zoomOutWebView() {
        float scale = mWebView.getScale();
        Log.d(TAG,"XINX   zoomOutWebView    scale = "+scale);
        if(scale > 0.25 * mOriginalScale){
            mWebView.setInitialScale((int)(scale * 0.5 * 100));
            Log.d(TAG,"XINX   zoomOutWebView   1111   scale = "+scale);
        }
    }
    private void displayNetView() {
        String urlString = mUrlText.getText().toString();
        if(URLUtil.isNetworkUrl(urlString)){
            mWebView.loadUrl(urlString);
        }else{
            mUrlText.setText("address error, input again!");
        }
    }

    private void startWebViewActivity() {
        String urlString = mUrlText.getText().toString();
        Uri uri = Uri.parse(urlString);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        getContext().startActivity(intent);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && mWebView.canGoBack()){
            mWebView.goBack();//返回上个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);//退出整个应用程序
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(mActivity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
        }

    }

    class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            //mActivity.setProgress(newProgress * 1000);
        }

        @Override
        public void getVisitedHistory(ValueCallback<String[]> callback) {
            super.getVisitedHistory(callback);
        }
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Log.d(TAG,"XINX   showToast    toast = "+toast);
            Toast.makeText(mActivity, toast, Toast.LENGTH_SHORT).show();
        }
    }

}
