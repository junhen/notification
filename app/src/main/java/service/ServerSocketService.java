package service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.leui.notification.test.IServerSocketServiceBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xx.util.DateTimeUtil;

/**
 * Created by xiaoxin on 17-4-25.
 */

public class ServerSocketService extends Service {
    private static final String TAG = "ServerSocketService_TAG";

    //定义相关的参数,端口,存储Socket连接的集合,ServerSocket对象
    //以及线程池
    private static final int PORT = 12345;
    private List<Socket> mSocketList = new ArrayList<Socket>();
    private ServerSocket mServerSocket = null;
    private ExecutorService myExecutorService = null;
    private Thread mServerSocketThread;
    private boolean mThreadRunnable = false;
    private MyBinder mBinder = new MyBinder();
    private MyBinderSecond mBinderSecond = new MyBinderSecond();
    private static String mResult;

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        mResult = "ServerSocketService   onCreate   "+ DateTimeUtil.time() + "\n";
        super.onCreate();
        mServerSocketThread = new Thread() {
            @Override
            public void run() {
                mThreadRunnable = true;
                InetAddress address = null;
                try {
                    address = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
                    Log.d(TAG,"InetAddress   UnknownHostException = " + e);
                    e.printStackTrace();
                }
                Log.d(TAG,"InetAddress   " + "本机名：" + address.getHostName());
                Log.d(TAG,"InetAddress   " + "CanonicalHostName：" + address.getCanonicalHostName());
                Log.d(TAG,"InetAddress   " + "IP地址：" + address.getHostAddress());
                byte[] bytes = address.getAddress();
                Log.d(TAG,"InetAddress   " + "字节数组形式的IP地址：" + Arrays.toString(bytes));
                Log.d(TAG,"InetAddress   " + "直接输出InetAddress对象：" + address);
                Log.d(TAG,"InetAddress   " + "-----------------------");

                try {
                    mServerSocket = new ServerSocket(PORT);
                    mResult += "ServerSocket   new\n";
                    //创建线程池
                    myExecutorService = Executors.newCachedThreadPool();
                    mResult += "Executors.newCachedThreadPoo\n";
                    System.out.println("服务端运行中...\n");
                    Log.d(TAG,"mServerSocketThread    子线程运行中...");
                    Socket client = null;
                    while (mThreadRunnable) {
                        client = mServerSocket.accept();
                        mSocketList.add(client);
                        myExecutorService.execute(new ServiceRunnable(client));
                    }
                } catch (Exception e) {
                    Log.d(TAG,"mServerSocketThread   Exception = " + e);
                    mResult += "mServerSocketThread   Exception = " + e + "\n";
                    e.printStackTrace();
                }
            }
        };
        mServerSocketThread.start();

    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG,"onStart");
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");
        return false ? mBinder : mBinderSecond;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG,"onRebind");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mServerSocket = null;
        }
        mThreadRunnable = false;
        mServerSocketThread.interrupt();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG,"onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.d(TAG,"onTrimMemory");
        super.onTrimMemory(level);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG,"onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    class ServiceRunnable implements Runnable {
        private Socket socket;
        private BufferedReader in = null;
        private String msg = "";

        public ServiceRunnable(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                msg = "用户:" + this.socket.getInetAddress() + "~加入了聊天室"
                        + "当前在线人数:" + mSocketList.size();
                this.sendmsg();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if ((msg = in.readLine()) != null) {
                        if (msg.equals("bye")) {
                            System.out.println("~~~~~~~~~~~~~");
                            mSocketList.remove(socket);
                            in.close();
                            msg = "用户:" + socket.getInetAddress() + "退出:" + "当前在线人数:" + mSocketList.size();
                            socket.close();
                            this.sendmsg();
                            break;
                        } else {
                            msg = socket.getInetAddress() + "   说: " + msg;
                            this.sendmsg();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //为连接上服务端的每个客户端发送信息
        public void sendmsg() {
            System.out.println(msg);
            int num = mSocketList.size();
            for (int index = 0; index < num; index++) {
                Socket mSocket = mSocketList.get(index);
                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(new BufferedWriter(
                            new OutputStreamWriter(mSocket.getOutputStream(), "UTF-8")), true);
                    pw.println(msg);
                    mResult += msg + "\n";
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class MyBinder extends Binder {

        public ServerSocketService getService(){
            return ServerSocketService.this;
        }
    }

   private class MyBinderSecond extends IServerSocketServiceBinder.Stub {

       @Override
       public void getservice() throws RemoteException {

       }

       @Override
       public String getResult() throws RemoteException {
           Log.d(TAG, "MyBinderSecond:    Binder.getCallingPid()  = " + Binder.getCallingPid());
           return mResult;

       }
   }
}
