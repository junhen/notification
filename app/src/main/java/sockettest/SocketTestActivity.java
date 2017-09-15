package sockettest;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
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

import xx.util.SharePreferencesUtil;


public class SocketTestActivity extends Activity implements Runnable {

    private static final String TAG = "SocketTestActivity_TAG";
    private static final String SHARE_NAME = "SocketTestActivity";
    private static final String SHARE_KEY = "share_key_";
    //定义相关变量,完成初始化
    private TextView txtshow;
    private EditText editsend;
    private Button btnsend;
    private static final String HOST = "10.58.68.136";
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
                sb.append(content);
                txtshow.setText(sb.toString());
                mShareP.saveValue(SHARE_KEY, sb.toString(), true);
            } else if (msg.what == 0x2) {
                removeCallbacks(initSocket);
                post(initSocket);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_test);
        mShareP = new SharePreferencesUtil(this, SHARE_NAME);
        sb = new StringBuilder();
        sb.append(mShareP.getvalue(SHARE_KEY));
        txtshow = (TextView) findViewById(R.id.txtshow);
        txtshow.setMovementMethod(new ScrollingMovementMethod());
        //txtshow.setText(mShareP.getvalue(SHARE_KEY));
        editsend = (EditText) findViewById(R.id.editsend);
        btnsend = (Button) findViewById(R.id.btnsend);

        //当程序一开始运行的时候就实例化Socket对象,与服务端进行连接,获取输入输出流
        //因为4.0以后不能再主线程中进行网络操作,所以需要另外开辟一个线程
        handler.sendEmptyMessage(0x2);

        //为发送按钮设置点击事件
        btnsend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String msg = editsend.getText().toString();
                new Thread() {
                    @Override
                    public void run() {
                        if (socket.isConnected()) {
                            if (!socket.isOutputShutdown()) {
                                out.println(msg);
                            } else {

                            }
                        }
                    }
                }.start();
            }
        });
        new Thread(SocketTestActivity.this).start();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
        if(socket != null && socket.isConnected()){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket = null;
            }

        }
    }

    //重写run方法,在该方法中输入流的读取
    @Override
    public void run() {
        try {
            while (true) {
                if (socket != null) {
                    if (socket.isConnected()) {
                        if (!socket.isInputShutdown()) {
                            if ((content = in.readLine()) != null) {
                                content += "\n";
                                handler.sendEmptyMessage(0x1);
                            }
                        }
                    }
                }
                synchronized (this) {
                    try {
                        wait(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            content += "run exception = "+e+"\n";
            handler.sendEmptyMessage(0x1);
            e.printStackTrace();
        }
    }

    private Runnable initSocket = new Runnable() {
        @Override
        public void run() {
            new Thread() {
                @Override
                public void run() {
                    try {
                        InetAddress address = null;
                        try {
                            address = InetAddress.getLocalHost();
                        } catch (UnknownHostException e) {
                            Log.d(TAG,"InetAddress   UnknownHostException = " + e);
                            e.printStackTrace();
                        }
                        String hostAddress = address.getHostAddress();
                        hostAddress = hostAddress == null ? HOST : hostAddress;
                        Log.d(TAG,"InetAddress   " + "IP地址：" + hostAddress);
                        socket = new Socket(hostAddress, PORT);
                        while(socket == null) {
                            content = "socket = null   wait  300\n";
                            handler.sendEmptyMessage(0x1);
                            synchronized (this) {
                                try {
                                    wait(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        mInitSocketed = true;
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                                socket.getOutputStream())), true);
                        content += socket.getInetAddress() + "\n";
                        handler.sendEmptyMessage(0x1);
                    } catch (IOException e) {
                        content += e + "\n";
                        handler.sendEmptyMessage(0x1);
                        handler.sendEmptyMessage(0x2);
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    };
    private long waittime = 0;
    private boolean mInitSocketed = false;
}
