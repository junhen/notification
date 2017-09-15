package com.leui.notification.test;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.leui.notification.test.R;

import java.lang.reflect.Method;


public class DemoModeFragment extends Fragment implements OnClickListener {
    private static final String TAG = DemoModeFragment.class.getSimpleName();
    
    public static final String ACTION_DEMO = "com.android.systemui.demo";
    public static final String COMMAND_ENTER = "enter";
    public static final String COMMAND_EXIT = "exit";
    public static final String COMMAND_CLOCK = "clock";
    public static final String COMMAND_BATTERY = "battery";
    public static final String COMMAND_NETWORK = "network";
    public static final String COMMAND_BARS = "bars";
    public static final String COMMAND_STATUS = "status";
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_demo_mode, container, false);

		v.findViewById(R.id.btn_enter).setOnClickListener(this);
	    v.findViewById(R.id.btn_exit).setOnClickListener(this);
		v.findViewById(R.id.btn_battery).setOnClickListener(this);
		v.findViewById(R.id.btn_clock).setOnClickListener(this);
		v.findViewById(R.id.btn_network).setOnClickListener(this);
		v.findViewById(R.id.btn_status_icon).setOnClickListener(this);
        v.findViewById(R.id.btn_status_bar_background).setOnClickListener(this);
		
		return v;
	}

    @Override
    public void onClick(View v) {
        Intent i = new Intent(ACTION_DEMO);
        Bundle bundle = new Bundle();
        switch(v.getId()){
            case R.id.btn_enter:
                bundle.putString("command", COMMAND_ENTER);
                disable(v.getContext(), 0x00000001);
                break;
            case R.id.btn_exit:
                bundle.putString("command", COMMAND_EXIT);
                disable(v.getContext(), 0x00000000);
                break;
            case R.id.btn_clock:
                bundle.putString("command", COMMAND_CLOCK);
                bundle.putString("millis", "");
                // or
                bundle.putString("hhmm", "1234");
                break;
            case R.id.btn_battery:
                bundle.putString("command", COMMAND_BATTERY);
                bundle.putString("level", "5");
                bundle.putString("plugged", "true");
                break;
            case R.id.btn_network:
                bundle.putString("command", COMMAND_NETWORK);
                
                bundle.putString("airplane", "show");
                bundle.putString("fully", "true");
                
                bundle.putString("wifi", "show");
                bundle.putString("level", "2");
                
                bundle.putString("mobile", "show");
                bundle.putString("datatype", "1x");     //1x 3g 4g e g h lte roam 
                bundle.putString("level", "3");
                break;
                
            case R.id.btn_status_bar_background:
                bundle.putString("command", COMMAND_BARS);
                bundle.putString("mode", "opaque");     //opaque translucent transparent
                break;
                
            case R.id.btn_status_icon:
                bundle.putString("volume", "silent");                   //silent vibrate
                bundle.putString("bluetooth", "disconnected");  //disconnected connected
                bundle.putString("location", "show");
                bundle.putString("alarm", "show");
                bundle.putString("sync", "show");
                bundle.putString("tty", "show");
                bundle.putString("eri", "show");
                bundle.putString("mute", "show");
                bundle.putString("speakerphone", "show");
                break;
                
                default:
                    bundle = null;
                    Log.e(TAG,"unhandle event,xx.view:"+v);
        }
        if(bundle != null){
            i.putExtras(bundle);
            v.getContext().sendBroadcast(i);
        }
    }
    public void disable(Context context, int diablemask) {
        /*try {
            Object service = context.getSystemService("statusbar");//Context.STATUS_BAR_SERVICE
            Log.d(TAG,"diable    sevice = "+service+",   diablemask = "+diablemask);
            if (service != null) {
                Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
                Method getService;
                getService = getMethod(statusbarManager, "getService", new Class[]{});
                Log.d(TAG,"diable    getService = "+getService);
                getService.setAccessible(true);
                Object managerservice = getService.invoke(service);
                //Class<?> statusbarManagerServive = Class.forName("com.android.server.statusbar.StatusBarManagerService");
                Class<?> statusbarManagerServive = managerservice.getClass();
                Log.d(TAG,"diable    statusbarManagerServive = "+statusbarManagerServive);
                Method disable2 = statusbarManagerServive.getMethod("disable2", int.class, IBinder.class, String.class);
                Log.d(TAG,"diable    disable2 = "+disable2);
                disable2.setAccessible(true);
                disable2.invoke(managerservice, diablemask, new Binder(), getActivity().getPackageName());
            }

        } catch (Exception e) {
            Log.d(TAG,"diable    Exception e = "+e);
            Log.d(TAG,"diable    Exception e = "+Log.getStackTraceString(new Throwable()));
        }*/

    }

    public static Method getMethod(Class clazz, String methodName,
                                   final Class[] classes) throws Exception {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return method;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName,
                            classes);
                }
            }
        }
        return method;
    }

}
