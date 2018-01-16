package com.leui.notification.test;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;
import android.util.Log;
import android.net.Uri;

/**
 * @author lwh
 *  使用自定义通知图标功能需要在源码中编译
 *
 */

public class TypeFragment extends Fragment {
    private static final String TAG = TypeFragment.class.getSimpleName();
    private static final boolean CUSTOMIZED_NOTIFICATION_ICON_ENABLE = true;
    
	private Button mDefault, mBigText, mInbox, mBigPicture, mRandom, mOld,mExplain;
	private Button mDismissTwoNotifications;

	private Context mContext;

	private Randomizer mRandomizer;

	private Switch mButtonsEnabled,mTickerEnable,mActionTextEnable,
			mCustomizedNotificationIcon,mOnGoingSwitch,mPriorityEnable,
			mDefaultEnable;

	private RadioGroup mButtonsGroup, mPriorityGroup,mDefaultGroup;

	private TextView   mPackageNameAndInternal, mExplainText;
	
	private TextView mFocusChanged;
	private AudioManager audioManager;
	
    private WindowManager wm;
    private View floatView;
    private WindowManager.LayoutParams wmParams;
    private int temp = 0;
	boolean mNeedExplain = false;
	private static final String DEFAULT_GROUP_KEY = "default_group";

	@TargetApi(Build.VERSION_CODES.M)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_types, container, false);
		mFocusChanged = (TextView)v.findViewById(R.id.focus_change);
		mFocusChanged.setText("audio_focus_no_change");
		// Version independent
		mDefault = (Button) v.findViewById(R.id.type_default);
		mOld = (Button) v.findViewById(R.id.type_old);
		mDefault.setOnClickListener(listener);
		mOld.setOnClickListener(listener);
		v.findViewById(R.id.expand_notifications_panel ).setOnClickListener(listener);
		v.findViewById(R.id.ongoing_show ).setOnClickListener(listener);
		v.findViewById(R.id.ongoing_dismiss ).setOnClickListener(listener);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			// Jelly Bean only
			mRandom = (Button) v.findViewById(R.id.type_random);
			mBigText = (Button) v.findViewById(R.id.type_big_text);
			mInbox = (Button) v.findViewById(R.id.type_inbox);
			mBigPicture = (Button) v.findViewById(R.id.type_big_picture);

			mPackageNameAndInternal = (TextView) v.findViewById(R.id.package_and_internal_stat);
			mExplainText = (TextView) v.findViewById(R.id.explain_button_text_holder);
			
			mButtonsEnabled = (Switch) v.findViewById(R.id.type_buttons_switch);
			mTickerEnable = (Switch) v.findViewById(R.id.ticker_switch);
			mActionTextEnable  = (Switch) v.findViewById(R.id.action_text_switch);
			mCustomizedNotificationIcon  = (Switch) v.findViewById(R.id.customized_notification_icon);
			mOnGoingSwitch = (Switch) v.findViewById(R.id.ongoing_switch);
			mPriorityEnable = (Switch) v.findViewById(R.id.priority_group_switch);
			mDefaultEnable = (Switch) v.findViewById(R.id.default_switch);
			mButtonsGroup = (RadioGroup) v.findViewById(R.id.type_buttons_group);
			mPriorityGroup = (RadioGroup) v.findViewById(R.id.priority_group);
			mDefaultGroup = (RadioGroup) v.findViewById(R.id.default_group);
			mExplain = (Button) v.findViewById(R.id.explain_button_text_button);
			mDismissTwoNotifications = (Button) v.findViewById(R.id.dismiss_two_notifications);

			 v.findViewById(R.id.notification_light).setOnClickListener(listener);
			 v.findViewById(R.id.type_missed_call).setOnClickListener(listener);
			v.findViewById(R.id.type_self_content).setOnClickListener(listener);
			v.findViewById(R.id.default_summary).setOnClickListener(listener);
			v.findViewById(R.id.two_default).setOnClickListener(listener);
			mBigText.setOnClickListener(listener);
			mInbox.setOnClickListener(listener);
			mBigPicture.setOnClickListener(listener);
			mRandom.setOnClickListener(listener);
			mExplain.setOnClickListener(listener);
			mDismissTwoNotifications.setOnClickListener(listener);
			
		}
		initPackageAndInternal(0);
		//updateFloatViewVisible(true);
		HelperUtil.addOverlay(getActivity());
		HelperUtil.addFloatView(getContext(), mRandom);
		return v;
	}

	View.OnClickListener listener = new View.OnClickListener() {
		@TargetApi(Build.VERSION_CODES.LOLLIPOP)
		@Override
		public void onClick(View v) {
			if(R.id.expand_notifications_panel == v.getId()){
				v.getContext().sendBroadcast(new Intent("MSG_OPEN_NOTIFICATION_PANEL"));
				OpenNotify(v.getContext());
				return;
			}else if(R.id.notification_light == v.getId()){
				final Notification mNotif = getInboxStyle(new Notification.Builder(mContext));
				mNotif.defaults = Notification.DEFAULT_ALL;
				mNotif.ledARGB = Color.WHITE;
				mNotif.ledOffMS = 4000;
				mNotif.ledOnMS = 4000;
				mNotif.flags |= Notification.FLAG_SHOW_LIGHTS;
				mNotif.flags |= mOnGoingSwitch.isChecked() ? Notification.FLAG_ONGOING_EVENT:0;
				//mNotif.priority = Notification.PRIORITY_HIGH;
				//mNotif.defaults &= ~Notification.DEFAULT_SOUND;
				//mNotif.sound = Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.notification);
				//mNotif.defaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE;
				mNotif.priority = Notification.PRIORITY_MAX;

				Intent intent = new Intent();
				intent.setClass(mContext, MainActivity.class);
				PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent,0);
				PendingIntent pi1 = PendingIntent.getBroadcast(mContext, 0, intent, 0);
				mNotif.contentIntent = pi;
				v.postDelayed(new Runnable() {
					@Override
					public void run() {
						((MainActivity)mContext).sendNotification(R.id.notification_light,mNotif);
					}
				}, 3000);
				return;
			}else if(R.id.ongoing_show == v.getId()){
				v.postDelayed(new Runnable() {
					@Override
					public void run() {
						Notification mNotif = getInboxStyle(new Notification.Builder(mContext));
						//mNotif.extras.putBoolean("ONGOING_EVENT_AUTO_DISMISS", true);
						mNotif.flags |= mOnGoingSwitch.isChecked() ? Notification.FLAG_ONGOING_EVENT:0;
						((MainActivity)mContext).sendNotification(R.id.ongoing_show,mNotif);
						temp++;
        			    /*if(temp > 3){
        			        //throw new RuntimeException("RuntimeException");
        			        //mContext.finish();
        			        mContext.killSelf();
        			    }*/
					}
				}, 300);
				return;
			}else if(R.id.ongoing_dismiss == v.getId()){
				v.postDelayed(new Runnable() {
					@Override
					public void run() {
						((MainActivity)mContext).cancelNotification(R.id.ongoing_show);
					}
				}, 1300);
				return;
			}else if(R.id.explain_button_text_button == v.getId()){
				if(mNeedExplain) {
					mNeedExplain = false;
					mExplain.setText("show explain");
					mExplainText.setText("");
				} else {
					mNeedExplain = true;
					mExplain.setText("hide explain");
					mExplainText.setText(v.getContext().getResources().getText(R.string.explain_button_text));
				}
				return;
			}else if(R.id.dismiss_two_notifications == v.getId()){
				Log.d(TAG,"XINX    listener    dismiss_two_notifications");
				MainActivity context = ((MainActivity)mContext);
				ArrayList<Integer> ids = context.getIds();
				int id;
				for(int i = 0; i < ids.size(); i++) {
					//android.os.Process.killProcess(android.os.Process.myPid());
					id = ids.get(i);
					Log.d(TAG,"XINX    listener    dismiss_two_notifications   i = "+i+",   id = "+id);
					context.dismissNotification(id);
				}
				return;
			}
			Notification notif = null;
			Notification.Builder builder = new Notification.Builder(mContext);
			setPriority(builder);
			setDefault(builder);
			builder.setColor(0xffff0000);
			// If random, add random buttons and take a random type
			if (v.getId() == R.id.type_random) {
				setRandomButtons(builder);
				switch (new Random().nextInt(4)) {
					case 0:
						// default
						notif = getDefaultNotification(builder);
						break;
					case 1:
						// big text
						notif = getBigTextStyle(builder);
						break;
					case 2:
						// big picture
						notif = getBigPictureStyle(builder);
						break;
					case 3:
						// inbox
						notif = getInboxStyle(builder);
						break;
				}
			} else {
				// Set selected buttons
				setButtons(builder, null);
				// And the selected type
				switch (v.getId()) {
					case R.id.type_big_text:
						notif = getBigTextStyle(builder);
						break;
					case R.id.type_inbox:
						notif = getInboxStyle(builder);
						break;
					case R.id.type_big_picture:
						notif = getBigPictureStyle(builder);
						break;
					case R.id.type_old:
						notif = getOldNotification();
						notif.flags &= ~Notification.FLAG_SHOW_LIGHTS;
						notif.ledARGB = 0x00000000;
						notif.ledOnMS = 100;
						notif.ledOffMS = 200;
						break;
					case R.id.type_missed_call:
						notif = getMissedCall(builder);
						break;
					case R.id.type_self_content:
						notif = getSelfContent(builder);
						break;
					case R.id.default_summary:
						notif = getDefaultSummaryNotification(builder);
						break;
					case R.id.two_default:
						notif = getDefaultNotification(builder);
						Log.d(TAG,"new notification = "+notif);

						if(CUSTOMIZED_NOTIFICATION_ICON_ENABLE){
							if(mCustomizedNotificationIcon.isChecked()){
								setNotificationIcon(notif,mRandomizer.getRandomIconId());
							}
						}
						notif.flags |= Notification.FLAG_AUTO_CANCEL;
						//这里使用相同得id来发送两条
						((MainActivity)mContext).sendNotification(notif, 1000);
						((MainActivity)mContext).sendNotification(notif, 1000);
						return;
					default:
						notif = getDefaultNotification(builder);
						notif.flags |= Notification.FLAG_SHOW_LIGHTS;
						notif.ledARGB = 0xff00ff00;
						notif.ledOnMS = 300;
						notif.ledOffMS = 1000;
						break;
				}
			}
			Log.d(TAG,"new notification = "+notif);

			if(CUSTOMIZED_NOTIFICATION_ICON_ENABLE){
				if(mCustomizedNotificationIcon.isChecked()){
					setNotificationIcon(notif,mRandomizer.getRandomIconId());
				}
//    				    notif.notificationIcon = android.R.drawable.ic_delete;
			}
			notif.flags |= Notification.FLAG_AUTO_CANCEL;
			((MainActivity)mContext).sendNotification(notif);
		}

	};

	private void initPackageAndInternal(int orientation){
	    if(mPackageNameAndInternal != null){
	        mPackageNameAndInternal.setText(getClass().getPackage().getName()+"\n isInternal:"+isInternalApp(getActivity(),getClass().getPackage().getName())
	                +"\n orientation:"+orientation);
	    }
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(TAG,"onConfigurationChanged");
	    super.onConfigurationChanged(newConfig);
	    initPackageAndInternal(newConfig.orientation);
	}
	
	@TargetApi(Build.VERSION_CODES.M)
	@Override
	public void onDestroy() {
	    //updateFloatViewVisible(false);
		HelperUtil.removeFloatView();
		Log.d(TAG,"onDestroy");
		audioManager.abandonAudioFocus(mListener);
	    super.onDestroy();
	}
    public static boolean isInternalApp(Context context, String pkg) {
        boolean result = false;
        if (!TextUtils.isEmpty(pkg)) {
            try {
                ApplicationInfo appInfo = context.getPackageManager()
                        .getApplicationInfo(pkg, 0);
                result = appInfo != null
                        && (appInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate");
		mContext = getActivity();

		mRandomizer = new Randomizer(mContext);
		audioManager =
	            (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

		audioManager.requestAudioFocus(mListener, 1, AudioManager.AUDIOFOCUS_GAIN);

		audioManager.abandonAudioFocus(null);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Notification getDefaultNotification(Notification.Builder builder) {
		Icon largeIcon = Icon.createWithResource(mContext, R.drawable.ic_launcher);
		builder.setSmallIcon(R.drawable.ic_launcher)
				.setWhen(System.currentTimeMillis())
				.setContentTitle("Default Title")
				.setContentText("Default Text   Default Text" +
						"   Default Text   Default Text   Default Text   Default Text" +
						"   Default Text   Default Text   Default Text   Default Text")
				.setContentInfo("Default Info")
				.setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class),0))
				.setGroup(DEFAULT_GROUP_KEY)
				.setLargeIcon(largeIcon);
		setTicker(builder, "getDefaultNotification");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			// Yummy jelly beans
			return builder.build();
		} else {
			return builder.getNotification();
		}
	}

	@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
	private Notification getBigTextStyle(Notification.Builder builder) {
	    Intent intent = new Intent();
	    intent.setClass(mContext, MainActivity.class);
	    PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent,0);
	    PendingIntent pi1 = PendingIntent.getBroadcast(mContext, 0, intent, 0);
		try {
			builder.setContentTitle("BigText title")
                    .setContentText("BigText text")
                    .setContentIntent(pi)
                    .setContentInfo("BigText Info")
                    .setGroup("bigText")
                    .setGroupSummary(true)
                    .setProgress(100, 30, false)
                    .setSmallIcon(R.drawable.ic_action_search)
                    //.setLargeIcon(mRandomizer.getRandomImage());
                    .setLargeIcon(((BitmapDrawable)mContext.getPackageManager()
							.getApplicationIcon("com.sina.weibo"/*"com.leui.notification.test"*/)).getBitmap());
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		setTicker(builder, "getBigTextStyle");

		return new Notification.BigTextStyle(builder)
				.bigText(getResources().getString(R.string.big_text))
				.setBigContentTitle("BigText Expanded title")
				.setSummaryText("BigText Summary text")
				.build();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
	private Notification getBigPictureStyle(Notification.Builder builder) {
		// In this case the icon in reduced mode will be the same as the picture
		// when expanded.
		// And when expanded, the icon will be another one.
		Bitmap large = mRandomizer.getRandomImage();
		Bitmap notSoLarge = mRandomizer.getRandomImage();
		builder.setContentTitle("BigPicture title")
				//.setContentText("BigPicture content text")
				.setContentText(Html.fromHtml(  
         			"<b><font color=#ff0000> text3:</font></b>  Text with a " +
         			"<a href=\"http://www.google.com\">link</a> " +
         			"created in the Java source code using HTML."))
				.setContentInfo("BigPicture Info")
				.setSmallIcon(R.drawable.ic_action_search)
				.setLargeIcon(large)
				.setGroup("bigPicture");
		setTicker(builder, "getBigPictureStyle");

		SpannableString ss = new SpannableString("text4: Click here to dial");
		ss.setSpan(new StyleSpan(Typeface.BOLD), 0, 6,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);   
		ss.setSpan(new URLSpan("tel:4155551212"), 13, 17,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); 	
		return new Notification.BigPictureStyle(builder)
				.bigPicture(large)
				.bigLargeIcon(notSoLarge)
				.setBigContentTitle(ss)
				.setBigContentTitle("BigPicture ex title")
				.setSummaryText("Summary text")
				.build();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
	private Notification getInboxStyle(Notification.Builder builder) {
		Intent intent1 = new Intent(mContext, MainActivity.class);
	    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    PendingIntent intent = PendingIntent.getActivity(mContext, 0, intent1, 0);
		builder.setContentTitle("Inbox title")
				.setContentText("Inbox content")
				.setContentInfo("Inbox Info")
				.setSubText("Inbox  SubText")
				.setGroup("inbox")
				.setContentIntent(intent)
				.setSmallIcon(R.drawable.ic_action_search)
				//.setPriority(Notification.PRIORITY_MAX)
				//.setVibrate(new long[] { 1000, 1000, 1000, 1000})
				//.setCategory(Notification.CATEGORY_CALL)
				.setLargeIcon(mRandomizer.getRandomImage());
		builder.getExtras().putBoolean("leui.enableNotificationTurningON", true);
		setTicker(builder, "getInboxStyle");

		Notification.InboxStyle n = new Notification.InboxStyle(builder)
				.setBigContentTitle("Inbox Expanded title")
				.setSummaryText("Inbox Summary text");
		// Add 10 lines
		for (int i = 0; i < 10; i++) {
			n.addLine("This is the line : " + (i + 1)+"   abcdefghijklmnopqrstuvwxyz");
		}

		return n.build();
	}
	private Notification getMissedCall(Notification.Builder builder) {
        builder
                .setSmallIcon(R.drawable.ic_action_search)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("马队")
                .setContentText("两个未接来电")
                .setContentInfo("Missed Info")
                .setLargeIcon(mRandomizer.getRandomImage());

        if(mTickerEnable.isChecked())
            builder.setTicker("getMissedCall");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Yummy jelly beans
            return builder.build();
        } else {
            return builder.getNotification();
        }

    }

	private Notification getSelfContent(Notification.Builder builder) {
		Intent intent1 = new Intent();
		File fileDir = getActivity().getExternalFilesDir(null);
		Log.i(TAG, "external dir is " + fileDir);
		String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		Log.i(TAG, "external dir is filePath = " + filePath);
		// File imageFile = new File(filePath + "/" + "wangshu.jpg");
		// add image link
		// if (imageFile != null) {
			//File imageFile = new File(fileDir.getAbsolutePath() + "/" + "preview.jpg");
			//Log.i(TAG, "share image path is " + imageFile.toString() + " exists = " + imageFile.exists());
			intent1.setAction(Intent.ACTION_SEND);
			intent1.setType("image/*");
			//Uri uri = Uri.fromFile(imageFile);
			//Uri uri = Uri.parse("android.resource://package_name/raw/jelly.png");
		    Uri uri = Uri.parse("android.resource://" + getActivity().getApplicationContext().getPackageName() + "/" + "jelly.png");
		    //Uri uri = Uri.fromFile(new File(getActivity().getFilesDir(), "jelly.png"));
			//Uri uri = FileProvider.getUriForFile(mContext, "com.letv.android.ota.fileProvider", imageFile);
			intent1.putExtra(Intent.EXTRA_STREAM, uri);
			intent1.addFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//}
		Intent intent2 = new Intent();
		intent2.setComponent(new ComponentName(mContext, MainActivity.class));
		Log.i(TAG, "external dir is intent1 = " + intent1 +",   intent2 = "+intent2);
		PendingIntent pendIntent = PendingIntent.getActivity(mContext, 0, intent1, 0);
		PendingIntent pendIntent2 = PendingIntent.getActivity(mContext, 0, intent2, 0);
		builder.setSmallIcon(R.drawable.ic_action_search)
				.setContent(getUpdateRemoteviews(0,"title", "titleContent","content",
						R.drawable.preview,pendIntent,pendIntent2));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			// Yummy jelly beans
			return builder.build();
		} else {
			return builder.getNotification();
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	private Notification getDefaultSummaryNotification(Builder builder) {

		/*Icon largeIcon = Icon.createWithResource(mContext, R.drawable.ic_launcher);
		return builder.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(largeIcon)
				.setWhen(System.currentTimeMillis())
				.setContentTitle("Default Title")
				.setContentText("Default Text   Default Text" +
						"   Default Text   Default Text   Default Text   Default Text" +
						"   Default Text   Default Text   Default Text   Default Text")
				.setGroup(DEFAULT_GROUP_KEY)
				.setGroupSummary(true)
				.build();*/
		builder.setGroup("bigPicture")
				.setGroupSummary(true);
		return getBigPictureStyle(builder);
	}

	private Notification getOldNotification() {
		Notification notif = new Notification(R.drawable.ic_launcher, "getOldNotification", System.currentTimeMillis());
		//notif.setLatestEventInfo(mContext, "Old title", "Old notification content text", PendingIntent.getActivity(mContext, 0, new Intent(), 0));
		return notif;
	}

	private void setRandomButtons(Notification.Builder builder) {
		setButtons(builder, new Random().nextInt(4));
	}

	private void setButtons(Notification.Builder builder, Integer buttons) {
		// Buttons only in Jelly Bean
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			// Get number of buttons
			if (buttons == null) {
				// If not specified, check the input
				buttons = 0;
				if (mButtonsEnabled.isChecked()) {
					switch (mButtonsGroup.getCheckedRadioButtonId()) {
					case R.id.radio0:
						buttons = 1;
						break;
					case R.id.radio1:
						buttons = 2;
						break;
					case R.id.radio2:
						buttons = 3;
						break;
					}
				}
			}
			// Add as many buttons as you have to
			//PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent,0);
			Intent intent1 = new Intent();
			intent1.setClass(mContext, MainActivity.class);

			PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent1, 0);
			for (int i = 0; i < buttons; i++) {
				builder.addAction(mRandomizer.getRandomIconId(),
				        mActionTextEnable.isChecked()? "Action " + (i + 1):"", pendingIntent);
			}
		}
	}

	private void setPriority(Notification.Builder builder){
		Log.d(TAG,"setPriority     mPriorityEnable.isChecked() = "+mPriorityEnable.isChecked()
		+",   mPriorityGroup.getCheckedRadioButtonId() = "+mPriorityGroup.getCheckedRadioButtonId());
		if(!mPriorityEnable.isChecked()) {
			return;
		}
		switch (mPriorityGroup.getCheckedRadioButtonId()) {
			case R.id.priority_group_max:
				builder.setPriority(Notification.PRIORITY_MAX);
				break;
			case R.id.priority_group_high:
				builder.setPriority(Notification.PRIORITY_HIGH);
				break;
			case R.id.priority_group_default:
				builder.setPriority(Notification.PRIORITY_DEFAULT);
				break;
			case R.id.priority_group_low:
				builder.setPriority(Notification.PRIORITY_LOW);
				break;
			case R.id.priority_group_min:
				builder.setPriority(Notification.PRIORITY_MIN);
				break;
		}
	}

	private void setDefault(Notification.Builder builder) {
		Log.d(TAG,"setDefault     mDefaultEnable.isChecked() = "+mDefaultEnable.isChecked()
				+",   mDefaultGroup.getCheckedRadioButtonId() = "+mDefaultGroup.getCheckedRadioButtonId());
		if(!mDefaultEnable.isChecked()) {
			return;
		}
		switch (mDefaultGroup.getCheckedRadioButtonId()) {
			case R.id.default_all:
				builder.setDefaults(Notification.DEFAULT_ALL);
				break;
			case R.id.default_sound:
				builder.setDefaults(Notification.DEFAULT_SOUND);
				break;
			case R.id.default_vibrate:
				builder.setDefaults(Notification.DEFAULT_VIBRATE);
				break;
			case R.id.default_lights:
				builder.setDefaults(Notification.DEFAULT_LIGHTS);
				break;
		}
	}

	private void setTicker(Notification.Builder builder, String ticket) {
		if(mTickerEnable.isChecked()) {
			builder.setTicker(ticket);
		}
	}

	public void OpenNotify(Context context) {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        try {
            Object service = context.getSystemService("statusbar");//Context.STATUS_BAR_SERVICE
            Class<?> statusbarManager = Class
                    .forName("android.app.StatusBarManager");
            Method expand = null;
            if (service != null) {
                if (currentApiVersion <= Build.VERSION_CODES.JELLY_BEAN) {
                    expand = statusbarManager.getMethod("expand");
                } else {
                    expand = statusbarManager
                            .getMethod("expandNotificationsPanel");
                }
                expand.setAccessible(true);
                expand.invoke(service);
            }
        } catch (Exception e) {
        }
    }
	
	/**
     * set statusbar notification icon 
     * @param notification
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
			Log.d(TAG,"setNotificationIcon    IllegalAccessException e = "+e);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
			Log.d(TAG,"setNotificationIcon    IllegalAccessException e = "+e);
            e.printStackTrace();
        }finally{
			Log.d(TAG,"setNotificationIcon    IllegalAccessException result = "+result);
            return result;
        }
    }

	AudioManager.OnAudioFocusChangeListener mListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			Log.d(TAG, "onAudioFocusChange    focusChange= " + focusChange);
			switch (focusChange) {
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					mFocusChanged.setText(focusChange + " = AUDIOFOCUS_LOSS_TRANSIENT");
					break;
				case AudioManager.AUDIOFOCUS_LOSS:
					mFocusChanged.setText(focusChange + " = AUDIOFOCUS_LOSS");
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
					mFocusChanged.setText(focusChange + " = AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
					break;
				case AudioManager.AUDIOFOCUS_GAIN:
					mFocusChanged.setText(focusChange + " = AUDIOFOCUS_GAIN");
					break;
				default:
					mFocusChanged.setText(focusChange + "");
					break;
			}
		}
	};

	private RemoteViews remoteviews;

	public RemoteViews getUpdateRemoteviews(int iconId, String title, String titleContent, String content,
											int srcId, PendingIntent switchIntent, PendingIntent switchIntent2) {
		if (remoteviews == null)
			remoteviews = new RemoteViews(mContext.getPackageName(), R.layout.le_update_notify);
		if (iconId != 0) {
			remoteviews.setImageViewResource(R.id.notify_icon, iconId);
		}
		if (TextUtils.isEmpty(title)) {
			remoteviews.setViewVisibility(R.id.notify_title, View.GONE);
		} else {
			// display title when title isn't empty
			remoteviews.setViewVisibility(R.id.notify_title, View.VISIBLE);
			remoteviews.setTextViewText(R.id.notify_title, title);
		}
		if (TextUtils.isEmpty(titleContent)) {
			remoteviews.setViewVisibility(R.id.notify_title_content, View.GONE);
		} else {
			// display titleContent when titleContent isn't empty
			remoteviews.setViewVisibility(R.id.notify_title_content, View.VISIBLE);
			remoteviews.setTextViewText(R.id.notify_title_content, titleContent);
		}
		if (TextUtils.isEmpty(content)) {
			remoteviews.setViewVisibility(R.id.notify_content, View.GONE);
		} else {
			// display content when content isn't empty
			remoteviews.setViewVisibility(R.id.notify_content, View.VISIBLE);
			remoteviews.setTextViewText(R.id.notify_content, content);
		}
		if (srcId == 0) {
			remoteviews.setViewVisibility(R.id.notify_image, View.GONE);
		} else {
			// display notify_image when srcId isn't 0
			remoteviews.setViewVisibility(R.id.notify_image, View.VISIBLE);
			remoteviews.setImageViewResource(R.id.notify_image, srcId);
			remoteviews.setOnClickPendingIntent(R.id.notify_image, switchIntent);

			remoteviews.setViewVisibility(R.id.notify_image_two, View.VISIBLE);
			remoteviews.setImageViewResource(R.id.notify_image_two, srcId);
			remoteviews.setOnClickPendingIntent(R.id.notify_image_two, switchIntent2);
		}
		return remoteviews;
	}

}
