package xx.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PersistableBundle;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.leui.notification.test.R;

import notificationlistener.NotificationListenerActivity;

public class TestActivityLife extends Activity {

    private static final String TAG = "TestActivityLife-Log";
    private Button mDialog;
    private Button mToast;
    private Button mDialogActivity;
    private Button mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_life);
        mDialog = (Button) findViewById(R.id.activity_life_dialog);
        mDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDialog();
            }
        });
        mToast = (Button) findViewById(R.id.activity_life_toast);
        mToast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newToast();
            }
        });
        mDialogActivity = (Button) findViewById(R.id.activity_life_dialog_activity);
        mDialogActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newDialogActivity();
            }
        });
        mActivity = (Button) findViewById(R.id.activity_life_activity);
        mActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(TAG,"onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState  1");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        Log.d(TAG, "onRestoreInstanceState  2");
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

    private void newDialog() {
        new AlertDialog.Builder(this)
                .setTitle("show a dialog")
                .setNeutralButton("neutral_button", null)
                .setMessage("message")
                .setOnCancelListener(null)
                .create()
                .show();
    }

    private void newToast() {
        Toast.makeText(this, "show a toast", Toast.LENGTH_LONG).show();
    }

    private void newDialogActivity() {
        startActivity(new Intent(this, DialogActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    private void newActivity() {
        startActivity(new Intent(this, NotificationListenerActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
