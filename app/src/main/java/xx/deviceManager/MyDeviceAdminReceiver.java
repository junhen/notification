package xx.deviceManager;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by xiaoxin on 17-11-1.
 */

public class MyDeviceAdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "MyDeviceAdminReceiver";
    @Override
    public void onEnabled(Context context, Intent intent) {
        // 设备管理：可用
        Log.d(TAG, "设备管理：可用");
        DeviceMethod.getInstance(context.getApplicationContext()).LockNow();
        Toast.makeText(context, "设备管理：可用", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(final Context context, Intent intent) {
        // 设备管理：不可用
        Log.d(TAG, "设备管理：不可用");
        Toast.makeText(context, "设备管理：不可用", Toast.LENGTH_SHORT).show();
        //如果取消了激活就再次提示激活
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //TODO
                DeviceMethod.getInstance(context.getApplicationContext()).onActivate();
            }
        }, 3000);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.d(TAG, "这是一个可选的消息，警告有关禁止用户的请求");
           /* // 这里处理 不可编辑设备。这里可以造成死机状态
            Intent intent2 = new Intent(context, NoticeSetting.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent2);
            context.stopService(intent);// 是否可以停止*/

        return "这是一个可选的消息，警告有关禁止用户的请求";
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        // 设备管理：密码己经改变
        Log.d(TAG, "设备管理：密码己经改变");
        Toast.makeText(context, "设备管理：密码己经改变", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        // 设备管理：改变密码失败
        Log.d(TAG, "设备管理：可用");
        Toast.makeText(context, "设备管理：改变密码失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        // 设备管理：改变密码成功
        Log.d(TAG, "设备管理：改变密码成功");
        Toast.makeText(context, "设备管理：改变密码成功", Toast.LENGTH_SHORT).show();
    }
}
