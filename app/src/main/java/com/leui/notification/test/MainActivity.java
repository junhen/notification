package com.leui.notification.test;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import messagingservice.MessagingFragment;


public class MainActivity extends Activity implements ActionBar.TabListener {
    //private static final String TAG = MainActivity.class.getSimpleName();
	private static final String TAG = "NotificationTest";
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private static final boolean SHOW_WHEN_LOCK = false;
	private static final boolean DISMISS_KEYGUARD = false;
	private static final boolean DISABLE_KEYGUARD = false;
	private Fragment mFragType, mFragPriority,mDemoMode,Frequency,Messager,mActivitys;
	private int NOTIF_REF = 1;
	private ArrayList<Integer> mIds = new ArrayList<>();
	private NotificationManager mNotificaitonManager;
	Display mDisplay;
    Point mCurrentDisplaySize = new Point();
    DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    KeyguardManager mKeyguardManager;
    KeyguardManager.KeyguardLock mKeyguardLock;

    private void tranlateStatusBarAndMargeContent(Window win){
        final int flag = 
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                |WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                |WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                |WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                |WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        getWindow().setFlags(flag,flag);
//        View actionBarLayout = getWindow().getDecorView().findViewById(0x0102034c);
//        MarginLayoutParams lp = (MarginLayoutParams) actionBarLayout.getLayoutParams();
//        lp.topMargin = 72;
//        actionBarLayout.setBackgroundColor(Color.YELLOW);
//        getWindow().getDecorView().setBackgroundColor(Color.BLUE);
//        ViewGroup content = (ViewGroup) actionBarLayout.findViewById(0x01020002);
//        if(content != null && content.getChildCount() > 0){
//            View child = content.getChildAt(0);
//            MarginLayoutParams lp2 = (MarginLayoutParams) child.getLayoutParams();
//            lp2.topMargin = 400;
//        }
//        
//        ColorDrawable mCD = new ColorDrawable(Color.RED);
//       getActionBar().setBackgroundDrawable(mCD);
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		final Window win = getWindow();
		final WindowManager.LayoutParams params = win.getAttributes();
		if(SHOW_WHEN_LOCK) params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		if(DISMISS_KEYGUARD) params.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
		mKeyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
		Log.d(TAG,"onCreate(),isKeyguardLocked:"+mKeyguardManager.isKeyguardLocked());
		if(DISABLE_KEYGUARD){
    		mKeyguardLock = mKeyguardManager.newKeyguardLock("MainActivity");
    		mKeyguardLock.disableKeyguard();
		}
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// For each of the sections in the app, add a tab to the action bar.
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section1).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section2).setTabListener(this));
	    actionBar.addTab(actionBar.newTab().setText(R.string.title_section3).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(R.string.title_section4).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section5).setTabListener(this));
		actionBar.addTab(actionBar.newTab().setText(R.string.title_section6).setTabListener(this));
		mNotificaitonManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		//tranlateStatusBarAndMargeContent(getWindow());
		mDisplay = ((WindowManager)this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		updateDisplaySize();
	}

//	 private com.android.internal.widget.LockPatternUtils  mLockPatternUtils;
    private void getPatterUtilsState(){
        /*if(mLockPatternUtils == null)
            mLockPatternUtils = new com.android.internal.widget.LockPatternUtils(this);
        boolean isLockPasswordEnabled = mLockPatternUtils.isLockPasswordEnabled() ;
        Log.v(TAG, "isLockPasswordEnabled:" + isLockPasswordEnabled);
        boolean isLockPatternEnabled = mLockPatternUtils.isLockPatternEnabled() ;
        Log.v(TAG, "isLockPatternEnabled:" + isLockPatternEnabled);*/
    }
	    
	
    private void resetPassword(){
		/*Log.v(TAG, "resetPassword()");
        if(mLockPatternUtils == null)
            mLockPatternUtils = new com.android.internal.widget.LockPatternUtils(this);
        mLockPatternUtils.setPermanentlyLocked(false);
        mLockPatternUtils.setLockPatternEnabled(false);
        mLockPatternUtils.saveLockPattern(null);
        mLockPatternUtils.setLockScreenDisabled(false);
        mLockPatternUtils.clearLock(true);

        // launch the 'choose lock pattern' activity so
        // the user can pick a new one if they want to
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.ChooseLockGeneric");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        Log.v(TAG, "resetPassword(),done");*/
    }

	void updateDisplaySize() {
        mDisplay.getMetrics(mDisplayMetrics);
        mDisplay.getSize(mCurrentDisplaySize);
        Log.d(TAG,"updateDisplaySize: "+
                String.format("%dx%d", mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels)
                +" x:"+mCurrentDisplaySize.x
                +" y:"+mCurrentDisplaySize.y
                );
    }

	public void sendNotification(Notification notif){
		mIds.add(NOTIF_REF);
	    sendNotification(NOTIF_REF++, notif);
	}

	public ArrayList<Integer> getIds() {
		return mIds;
	}

	public void dismissNotification(int id) {
		mNotificaitonManager.cancel("sameTag", id);
	}

	public void sendNotification(int notif_id,Notification notif){
		mNotificaitonManager.notify("sameTag",notif_id, notif);
    }

	public void cancelNotification(int notif_id){
		mNotificaitonManager.cancel("sameTag",notif_id);
	}

	public void showDefaultNotification(View v) {	}
	
	public void killSelf() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);   
		//manager.restartPackage(getPackageName());
		manager.killBackgroundProcesses(getPackageName());
		System.exit(0); 
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d(TAG,"onRestoreInstanceState  1");
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
		Log.d(TAG,"onRestoreInstanceState  2");
		super.onRestoreInstanceState(savedInstanceState, persistentState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG,"onSaveInstanceState");
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		Log.d(TAG,"onTabSelected   tab: "+tab.getText().toString());
		// When the given tab is selected, show the tab contents in the container
		switch (tab.getPosition()) {
			case 0:
				if (mFragType == null) {
					mFragType = new TypeFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, mFragType).commit();
				break;
			case 1:
				if (mFragPriority == null) {
					mFragPriority = new PriorityFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, mFragPriority).commit();
				break;
			case 2:
			    if (mDemoMode == null) {
			        mDemoMode = new DemoModeFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.container, mDemoMode).commit();
                resetPassword();
			    break;
			case 3:
                if (Frequency == null) {
                    Frequency = new FrequencyFragment();
                }
                getFragmentManager().beginTransaction().replace(R.id.container, Frequency).commit();
                break;
			case 4:
				if (Messager == null) {
					Messager = new MessagingFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, Messager).commit();
				break;
			case 5:
				if (mActivitys == null) {
					mActivitys = new ActivitysFragment();
				}
				getFragmentManager().beginTransaction().replace(R.id.container, mActivitys).commit();
				break;
			default:
				Fragment fragment = new DummySectionFragment();
				Bundle args = new Bundle();
				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, tab.getPosition() + 1);
				fragment.setArguments(args);
				getFragmentManager().beginTransaction()
						.replace(R.id.container, fragment)
						.commit();
				break;
		}

	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		public DummySectionFragment() {
		}

		public static final String ARG_SECTION_NUMBER = "section_number";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			TextView textView = new TextView(getActivity());
			textView.setGravity(Gravity.CENTER);
			Bundle args = getArguments();
			textView.setText("Not implemented yet, sorry ^^'");
			return textView;
		}
	}
	
	@TargetApi(Build.VERSION_CODES.M)
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
	    Log.d(TAG,"onWindowFocusChanged(),hasFocus:"+hasFocus+" isKeyguardLocked:"+mKeyguardManager.isKeyguardLocked()
				+",   isKeyguardSecure:"+mKeyguardManager.isKeyguardSecure()
				+",   isDeviceLocked:"+mKeyguardManager.isDeviceLocked()
				+",   isDeviceSecure:"+mKeyguardManager.isDeviceSecure());
	}
	@Override
    public void onResume() {
	    Log.d(TAG,"onResume");
        super.onResume();
//      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getPatterUtilsState();
        getPermissionApps();
    }
	
	PackageManager mPm;
	String permission = "android.permission.DISABLE_KEYGUARD";
    private void getPermissionApps() {
        mPm = this.getPackageManager();
        List<PackageInfo> apps;
        String[] permsArray = {permission};
        apps = mPm.getPackagesHoldingPermissions(permsArray, PackageManager.GET_SIGNATURES|PackageManager.GET_INTENT_FILTERS);
        if(apps != null && apps.size() > 0 ){
            Log.d(TAG,"getPermissionApps() with permission:"+permission);
            for (PackageInfo info : apps) {
                //only print none system apps
                if((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 )
                    Log.i(TAG,"\t"+info.toString());
            }
        }
    }

    private String getAppWidgetHostsSection() throws Exception {
        String appWidget = getString(R.string.dumpsys_appwidget);
        runCmd(new String[]{"dumpsys","notification"});
        Log.d(TAG,"getAppWidgetHostsSection(),appWidget:"+appWidget);
        return appWidget.split("Hosts:")[1];
    }

    private String runCmd(String[] cmd){
        ShellExecute cmdexe = new ShellExecute();
        String result = "";
        try {
            result = cmdexe.execute(cmd,"/");
        }catch (IOException e) {
            e.printStackTrace();
        }
		Log.d(TAG,"runCmd  result = "+result);
        return result;
    }
    
    private ArrayList<String> getInstalledLaunchers()throws Exception{
        String hostsSection = getAppWidgetHostsSection();
        Log.d(TAG,"getInstalledLaunchers(),hostsSection:"+hostsSection);
        ArrayList launchers = new ArrayList<String>();/*Lists.newArrayList();*/
        String hostRegexpText = Build.VERSION.SDK_INT >= 21 ? ".*hostId.*pkg:([a-z0-9.]*).*"
                : ".*hostId.*(com.[a-z0-9.]*).*";
        Pattern hostRegexp = Pattern.compile(hostRegexpText, 32);
        for (String hostsLine : hostsSection.split("\n")) {
            Matcher matcher = hostRegexp.matcher(hostsLine);
            if (matcher.matches()) {
                Log.d(TAG,"getInstalledLaunchers(),Found installed launcher from package: "+ matcher.group(1));
//                LogUtil.CLog
//                        .logAndDisplay(Log.LogLevel.INFO, "Found installed launcher from package: "
//                                + matcher.group(1), new Object[0]);
                launchers.add(matcher.group(1));
            }
        }
        return launchers;
    }

    
    @Override
    public void onStart() {
        Log.d(TAG,"onStart");
        try {
            getInstalledLaunchers();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onStart();
    }
    @Override
    public void onStop() {
        Log.d(TAG,"onStop");
        super.onStop();
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_BEHIND);
    }
    @Override
    public void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
    }
    @Override
    public void onRestart() {
        Log.d(TAG,"onRestart");
        super.onRestart();
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        if(DISABLE_KEYGUARD && mKeyguardLock != null){
            mKeyguardLock.reenableKeyguard();
        }
        super.onDestroy();
    }

	@Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG,"onNewIntent,intent: "+intent);
        super.onNewIntent(intent);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG,"onConfigurationChanged(), newConfig:"+newConfig);
        super.onConfigurationChanged(newConfig);
        updateDisplaySize();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG,"onActivityResult, requestCode: "+requestCode+",  resultCode : "+resultCode + ",   data : "+data);
		super.onActivityResult(requestCode, resultCode, data);
	}
}
