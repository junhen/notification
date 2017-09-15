/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package notificationlistener;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import com.leui.notification.test.R;


import static android.widget.ImageView.ScaleType.FIT_XY;


public class NotificationListenerActivity extends ListActivity {
    private static final String LISTENER_PATH = "com.leui.notification.test/" +
            "notificationlistener.Listener";
    private static final String TAG = "NotificationListenerActivity";

    private Button mLaunchButton;
    private Button mSnoozeButton;
    private TextView mEmptyText;
    private StatusAdaptor mStatusAdaptor;

    private final BroadcastReceiver mRefreshListener = new BroadcastReceiver() {
        @SuppressLint("LongLogTag")
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "update tickle");
            updateList(intent.getStringExtra(Listener.EXTRA_KEY));
        }
    };

    private final BroadcastReceiver mStateListener = new BroadcastReceiver() {
        @SuppressLint("LongLogTag")
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "state tickle");
            checkEnabled();
            updateList(null);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.long_app_name);
        setContentView(R.layout.listener_notifications_main);
        mLaunchButton = (Button) findViewById(R.id.launch_settings);
        mSnoozeButton = (Button) findViewById(R.id.snooze);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mSnoozeButton.setVisibility(View.GONE);
        }
        mEmptyText = (TextView) findViewById(android.R.id.empty);
        mStatusAdaptor = new StatusAdaptor(this);
        setListAdapter(mStatusAdaptor);
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(mRefreshListener);
        localBroadcastManager.unregisterReceiver(mStateListener);
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(mRefreshListener, new IntentFilter(Listener.ACTION_REFRESH));
        localBroadcastManager.registerReceiver(mStateListener, new IntentFilter(Listener.ACTION_STATE_CHANGE));
    }

    @Override
    public void onResume() {
        super.onResume();
        checkEnabled();
        updateList(null);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String listeners = Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (listeners != null && listeners.contains(LISTENER_PATH)) {
            hasPermission();
        } else {
            hasNoPermission();
        }
    }

    private void checkEnabled() {
        String listeners = Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (listeners != null && listeners.contains(LISTENER_PATH)) {
            hasPermission();
        } else {
            hasNoPermission();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.explanation)
                    .setTitle(R.string.disabled);
            builder.setPositiveButton(R.string.enable_it, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    launchSettings(null);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            builder.create().show();

        }
    }

    private void hasNoPermission() {
        mLaunchButton.setText(R.string.launch_to_enable);
        mSnoozeButton.setEnabled(false);
        mEmptyText.setText(R.string.nothing_to_see);
    }

    private void hasPermission() {
        mLaunchButton.setText(R.string.launch_to_disable);
        mSnoozeButton.setEnabled(true);
        if (Listener.isConnected()) {
            mSnoozeButton.setText(R.string.snooze);
            mEmptyText.setText(R.string.waiting_for_content);
        } else {
            mSnoozeButton.setText(R.string.unsnooze);
            mEmptyText.setText(R.string.disabled);
            //Listener.toggleSnooze(this);
        }
    }

    public void launchSettings(View v) {
        startActivityForResult(
                new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), 0);
    }

    @SuppressLint("LongLogTag")
    public void snooze(View v) {
        Log.d(TAG, "clicked snooze");
        Listener.toggleSnooze(this);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    @SuppressLint("LongLogTag")
    public void dismiss(View v) {
        Log.d(TAG, "clicked dismiss ");
        Object tag = v.getTag();
        if (tag instanceof StatusBarNotification) {
            StatusBarNotification sbn = (StatusBarNotification) tag;
            Log.d(TAG, "  on " + sbn.getKey());
            LocalBroadcastManager.getInstance(this).
                    sendBroadcast(new Intent(Listener.ACTION_DISMISS)
                            .putExtra(Listener.EXTRA_KEY, sbn.getKey()));
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public void launch(View v) {
        //点击通知，进入通知自动的pendingIntent。
        Log.d(TAG, "clicked launch");
        Object tag = v.getTag();
        if (tag instanceof StatusBarNotification) {
            StatusBarNotification sbn = (StatusBarNotification) tag;
            Log.d(TAG, "  on " + sbn.getKey());
            LocalBroadcastManager.getInstance(this).
                    sendBroadcast(new Intent(Listener.ACTION_LAUNCH)
                            .putExtra(Listener.EXTRA_KEY, sbn.getKey()));
        }
    }

    private void updateList(String key) {
        final List<StatusBarNotification> notifications = Listener.getNotifications();
        if (notifications != null) {
            mStatusAdaptor.setData(notifications);
        }
        mStatusAdaptor.update(key);
    }

    private class StatusAdaptor extends BaseAdapter {
        private final Context mContext;
        private List<StatusBarNotification> mNotifications;
        private HashMap<String, Long> mKeyToId;
        private HashSet<String> mKeys;
        private long mNextId;
        private HashMap<String, View> mRecycledViews;
        private String mUpdateKey;

        public StatusAdaptor(Context context) {
            mContext = context;
            mKeyToId = new HashMap<String, Long>();
            mKeys = new HashSet<String>();
            mNextId = 0;
            mRecycledViews = new HashMap<String, View>();
        }

        @Override
        public int getCount() {
            return mNotifications == null ? 0 : mNotifications.size();
        }

        @Override
        public Object getItem(int position) {
            return mNotifications.get(position);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        @Override
        public long getItemId(int position) {
            final StatusBarNotification sbn = mNotifications.get(position);
            final String key = sbn.getKey();
            if (!mKeyToId.containsKey(key)) {
                mKeyToId.put(key, mNextId);
                mNextId ++;
            }
            return mKeyToId.get(key);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public View getView(int position, View convertView, ViewGroup list) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                Log.d(TAG,"xinx  getView "+",  position: "+position+",   convertView == null");
                viewHolder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.listener_notifications_item, null);
                viewHolder.container = (FrameLayout) convertView.findViewById(R.id.remote_view);
                viewHolder.dismiss = convertView.findViewById(R.id.dismiss);
                viewHolder.icon = (ImageView)convertView.findViewById(R.id.small_icon);
                viewHolder.actionNumber = (TextView)convertView.findViewById(R.id.action_number);
                viewHolder.actionNumber1 = (TextView)convertView.findViewById(R.id.action_number_1);
                convertView.setTag(viewHolder);
            } else {
                Log.d(TAG,"xinx  getView "+",  position: "+position+",   convertView != null");
                viewHolder = (ViewHolder)convertView.getTag();
            }
            final StatusBarNotification sbn = mNotifications.get(position);
            View child;
            if (viewHolder.container.getTag() instanceof StatusBarNotification &&
                    viewHolder.container.getChildCount() > 0) {
                // recycle the xx.view
                StatusBarNotification old = (StatusBarNotification) viewHolder.container.getTag();
                if (sbn.getKey().equals(mUpdateKey)) {
                    //this xx.view is out of date, discard it
                    mUpdateKey = null;
                } else {
                    View content = viewHolder.container.getChildAt(0);
                    viewHolder.container.removeView(content);
                    mRecycledViews.put(old.getKey(), content);
                }
            }
            child = mRecycledViews.get(sbn.getKey());
            if (child == null) {
                RemoteViews remoteViews = sbn.getNotification().contentView;
                if(remoteViews != null) {
                    child = remoteViews.apply(mContext, null);
                    viewHolder.actionNumber.setText("" + countNotificationActions(remoteViews, sbn.getPackageName()));
                    viewHolder.actionNumber1.setText(""+(sbn.getNotification().actions != null ? sbn.getNotification().actions.length : 0));
                } else {
                    Notification.Builder builder =
                            Notification.Builder.recoverBuilder(mContext, sbn.getNotification());
                    remoteViews = builder.createContentView();
                    child = remoteViews.apply(mContext, null);
                    viewHolder.actionNumber.setText("" + countNotificationActions(remoteViews, sbn.getPackageName()));
                    viewHolder.actionNumber1.setText(""+(sbn.getNotification().actions != null ? sbn.getNotification().actions.length : 0));
                }
            }
            viewHolder.container.setTag(sbn);
            viewHolder.container.removeAllViews();
            viewHolder.container.addView(child);
            viewHolder.dismiss.setVisibility(sbn.isClearable() ? View.VISIBLE : View.GONE);
            viewHolder.dismiss.setTag(sbn);
            viewHolder.icon.setScaleType(FIT_XY);
            viewHolder.icon.setImageIcon(sbn.getNotification().getSmallIcon());
            //viewHolder.icon.getDrawable().setColorFilter(0xff750000, PorterDuff.Mode.SRC_ATOP);
            viewHolder.icon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = viewHolder.icon.getDrawable() instanceof BitmapDrawable ?
                            ((BitmapDrawable)viewHolder.icon.getDrawable()).getBitmap() : null;
                    if(bitmap == null) {
                        return;
                    }
                    if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
                        return;
                    }
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int[] pixels = new int[width * height];
                    bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
                    int red = 0, green = 0, blue = 0;
                    int num = 0;

                    for (int i = 0; i < pixels.length; i++) {
                        int clr = pixels[i];
                        if(((clr & 0xff000000) >>> 24) == 0) {
                            continue;
                        }
                        num++;
                        red += ((clr & 0x00ff0000) >> 16); // 取高两位
                        green += ((clr & 0x0000ff00) >> 8); // 取中两位
                        blue += (clr & 0x000000ff); // 取低两位
                    }
                    red /= num;
                    green /= num;
                    blue /= num;
                    int mean = (red + green + blue) / 3;
                    if (num < pixels.length / 4 || mean > 0xf0) {
                        viewHolder.icon.getDrawable().setColorFilter(0xff750000, PorterDuff.Mode.SRC_ATOP);
                    } else {
                        viewHolder.icon.getDrawable().clearColorFilter();
                    }
                }
            }, 2000);
            return convertView;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        public void update(String key) {
            if (mNotifications != null) {
                synchronized (mNotifications) {
                    mKeys.clear();
                    for (int i = 0; i < mNotifications.size(); i++) {
                        mKeys.add(mNotifications.get(i).getKey());
                    }
                    mKeyToId.keySet().retainAll(mKeys);
                }
                if (key == null) {
                    mRecycledViews.clear();
                } else {
                    mUpdateKey = key;
                    mRecycledViews.remove(key);
                }
                Log.d(TAG, "notifyDataSetChanged");
                notifyDataSetChanged();
            } else {
                Log.d(TAG, "missed and update");
            }
        }

        public void setData(List<StatusBarNotification> notifications) {
            mNotifications = notifications;
        }

        private int countNotificationActions(RemoteViews rv, String packageName) {
            int actionListSize = 0;
                //Class<?> remoteViewsClass = rv.getClass();
                try {
                    Class<?> remoteViewsClass = Class.forName("android.widget.RemoteViews");
                    Log.d(TAG, "invokeRemoteView(),rv:" + rv +" remoteViewsClass:"+remoteViewsClass);
                    Field actionsField =remoteViewsClass.getDeclaredField("mActions");
                    actionsField.setAccessible(true);
                    Log.d(TAG, "invokeRemoteView(),actionsField:" + actionsField);
                    ArrayList<String> arrayListActions = new ArrayList<String>();
                    ArrayMap<String,Integer> actionMap = new ArrayMap<String, Integer>();
                    if(actionsField != null ){
                        Object actionsObj = actionsField.get(rv);
                        if(actionsObj != null){
                            ArrayList<Object> actionsList = (ArrayList<Object>) actionsObj;
                            actionListSize = actionsList.size();
                            Log.d(TAG,"actionListSize: "+actionListSize);
                            for(int i=0,j=actionsList.size();i<j;i++){
                                Object action = actionsList.get(i);
                                Log.d(TAG,"invokeRemoteView(),action:"+action+" getClass:"+action.getClass());
                                Class<?> actionType = action.getClass();
                                Method getActionName = actionType.getMethod("getActionName");
                                String actionName = (String) getActionName.invoke(action);
                                if(!actionMap.keySet().contains(actionName)) {
                                    arrayListActions.add(actionName);
                                    actionMap.put(actionName, 1);
                                } else {
                                    actionMap.put(actionName, actionMap.get(actionName).intValue()+1);

                                }
                                Log.d(TAG,"invokeRemoteView(),actionName:"+actionName);
                                if (!TextUtils.isEmpty(actionName)&& actionName.startsWith("ReflectionActionsetText")) {
                                    Field actionValueField = actionType.getDeclaredField("value");
                                    actionValueField.setAccessible(true);
                                    Object actionValueObj = actionValueField.get(action);
                                    Log.i(TAG, "invokeRemoteView(),objValue.getClass:"
                                            + (actionValueObj != null ? actionValueObj.getClass(): actionValueObj) + " objValue:" + actionValueObj);
                                    if (actionName.startsWith("ReflectionActionsetTextColor")
                                            && actionValueObj != null && actionValueObj instanceof Integer) {
                                        //
                                    } else if (actionName.startsWith("ReflectionActionsetText")
                                            && actionValueObj != null
                                            && actionValueObj instanceof SpannableString
                                            && !TextUtils.isEmpty(actionValueObj.toString())) {
                                        actionValueField.set(action, actionValueObj.toString());
                                    }
                                }
                            }
                            Log.d(TAG, "countNotificationActions   package: "+packageName);
                            for (String key : actionMap.keySet()) {
                                Log.d(TAG, "countNotificationActions   key: "+key+" : value : "+actionMap.get(key));
                            }
                            Log.d(TAG, "countNotificationActions   ---------------------------------");
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } /*catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }*/ finally {
                    return actionListSize;
                }
        }
    }

    // 自定义的容器类（相当于一个Item），其中放置着需要我们放置数据的控件的名称
    public static class ViewHolder {
        FrameLayout container;
        View dismiss;
        ImageView icon;
        TextView actionNumber;
        TextView actionNumber1;
    }
}
