package sockettest;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.leui.notification.test.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

import xx.util.SharePreferencesUtil;


public class SocketTestActivity extends Activity {

    private static final String TAG = "SocketTestActivity_TAG";
    private static final String SHARE_NAME = "SocketTestActivity";
    private static final String SHARE_KEY = "share_key_";
    //定义相关变量,完成初始化
    private TextView txtshow;
    private EditText editsend;
    private Button btnsend;
    private static final String HOST = "10.58.68.71";
    private static final int PORT = 12345;
    private Socket socket = null;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private String content = "";
    private StringBuilder sb = null;
    private SharePreferencesUtil mShareP;

    //定义一个handler对象,用来刷新界面
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0x1) {
                Log.d(TAG, "handler  content = " + content);
                sb.append(content);
                txtshow.setText(sb.toString());
                mShareP.saveValue(SHARE_KEY, sb.toString(), true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_test);
        mShareP = new SharePreferencesUtil(this, SHARE_NAME);
        sb = new StringBuilder();
        sb.append(mShareP.getvalue(SHARE_KEY));
        txtshow = (TextView) findViewById(R.id.show);
        txtshow.setMovementMethod(new ScrollingMovementMethod());
        editsend = (EditText) findViewById(R.id.edit);
        btnsend = (Button) findViewById(R.id.send);

        //为发送按钮设置点击事件
        btnsend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String msg = editsend.getText().toString();
                new Thread() {
                    @Override
                    public void run() {
                        if (socket != null && socket.isConnected() && !socket.isOutputShutdown()) {
                            out.println(msg);
                        }
                    }
                }.start();
            }
        });
        new Thread(mSocketThreadRunnable).start();
        //Process.killProcess(Process.myPid());
        //System.exit(0);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket = null;
            }

        }
    }

    private Runnable mSocketThreadRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                InetAddress address = null;
                try {
                    address = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    Log.d(TAG, "mSocketThreadRunnable   UnknownHostException e: " + e);
                    e.printStackTrace();
                }
                String hostAddress = address.getHostAddress();
                Log.d(TAG, "mSocketThreadRunnable  IP地址  hostAddress: " + hostAddress);
                //hostAddress = hostAddress == null ? HOST : hostAddress;
                hostAddress = HOST;
                Log.d(TAG, "mSocketThreadRunnable  IP地址  hostAddress: " + hostAddress);
                socket = new Socket(hostAddress, PORT);
                Log.d(TAG, "mSocketThreadRunnable  socket: " + socket);
                if (socket == null) {
                    content = "socket = null   wait  300\n";
                    handler.sendEmptyMessage(0x1);
                    return;
                }
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), "UTF-8"));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream())), true);
                content += socket.getInetAddress() + "\n";
                handler.sendEmptyMessage(0x1);
            } catch (IOException e) {
                Log.d(TAG, "InetAddress   IOException e: " + e);
                e.printStackTrace();
            }

            Log.d(TAG, "mSocketThreadRunnable  socket: " + socket);
            if (socket != null) {
                while (true) {
                    Log.d(TAG, "mSocketThreadRunnable  socket.isConnected(): " + socket.isConnected()
                            + ",   socket.isInputShutdown(): " + socket.isInputShutdown());
                    if (socket.isConnected()) {
                        if (!socket.isInputShutdown()) {
                            try {
                                if ((content = in.readLine()) != null) {
                                    content += "\n";
                                    handler.sendEmptyMessage(0x1);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        Thread.currentThread().sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
}
