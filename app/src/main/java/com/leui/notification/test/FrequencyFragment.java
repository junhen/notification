package com.leui.notification.test;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.leui.notification.test.R;

import java.lang.reflect.Field;


public class FrequencyFragment extends Fragment implements OnClickListener {
    private static final String tag = FrequencyFragment.class.getSimpleName();
    
    TextView mStatus;
    EditText mFrequency;
    Button  mBtnStart,mBtnStop;
    int notifyID = 123;
    private MainActivity mContext;
    private int msgDelay = 100;//10hz
    private long lastNotificationTime = 0;
    private Randomizer mRandomizer;
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    mContext = (MainActivity) getActivity();
		View v = inflater.inflate(R.layout.fragment_frequency, container, false);

		mBtnStart = (Button) v.findViewById(R.id.btn_start);
		mBtnStop = (Button) v.findViewById(R.id.btn_stop);

		v.findViewById(R.id.btn_start).setOnClickListener(this);
	    v.findViewById(R.id.btn_stop).setOnClickListener(this);
	    v.findViewById(R.id.btn_update).setOnClickListener(this);
	    mStatus = (TextView) v.findViewById(R.id.tv_status);
	    mFrequency = (EditText) v.findViewById(R.id.et_frequency);
	    mRandomizer = new Randomizer(mContext);
	    
		return v;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
    private void sendNotification(){
	    Notification notif = null;
        Notification.Builder builder = new Notification.Builder(getActivity().getApplicationContext());
        final int time = (int) (System.currentTimeMillis()%9);
        PendingIntent intent = PendingIntent.getActivity(getActivity(), 0, new Intent(), 0);
        for (int i = 0; i < 3; i++) {
            builder.addAction(mRandomizer.getRandomIconId(), ""+time, intent);
        }
        
        builder
        .setContentTitle("Reduced BigText title"+time)
        .setContentText(""+getActivity().getPackageName()+System.currentTimeMillis())
        .setContentInfo("Info"+time)
        .setShowWhen(false)
        .setTicker("getBigTextStyle"+System.currentTimeMillis())
        .setSmallIcon(mRandomizer.getRandomIconId())
        .setLargeIcon(Icon.createWithResource(getContext(), mRandomizer.getRandomIconId()));

        notif = new Notification.BigTextStyle(builder)
            .bigText(""+getActivity().getPackageName()+" "+System.currentTimeMillis()+"\n"+getResources().getString(R.string.big_text))
            .setBigContentTitle("Expanded BigText title"+time)
            .setSummaryText("Summary text"+time)
            .build();
        
        setNotificationIcon(notif,mRandomizer.getRandomIconId());
        mContext.sendNotification(notifyID,notif);
        
        mStatus.setText("running:"+1000/(System.currentTimeMillis()-lastNotificationTime)+"hz");
        lastNotificationTime = System.currentTimeMillis();
	}
	
	/**
     * set statusbar notification icon 
     * @param Notification
     * @param iconId id
     * @return
     */
    public static  boolean setNotificationIcon(Notification notification,int iconId){
        boolean result = false;
        Class clazz = notification.getClass();
        try {
            Field setNotificationIconField = clazz.getDeclaredField("notificationIcon");
            setNotificationIconField.setAccessible(true);
            setNotificationIconField.setInt(notification, iconId);
            result = true;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }finally{
            return result;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_start:
                if(mFrequency.getText().length() > 0){
                    msgDelay = Integer.decode(mFrequency.getText().toString());
                    msgDelay = 1000/msgDelay;
                }
                v.post(updateNotification);
                v.setEnabled(false);
                mBtnStop.setEnabled(true);
                mStatus.setText("running...");
                break;
            case R.id.btn_stop:
                mStatus.removeCallbacks(updateNotification);
                v.setEnabled(false);
                mBtnStart.setEnabled(true);
                mStatus.setText("stop.");
                break;
            case R.id.btn_update:
                sendNotification();
                break;
                default:
                    Log.e(tag,"unhandle event,xx.view:"+v);
        }
    }

    
    Runnable updateNotification = new Runnable() {
        @Override
        public void run() {
            sendNotification();
            mStatus.postDelayed(updateNotification, msgDelay);
       }
    };
}
