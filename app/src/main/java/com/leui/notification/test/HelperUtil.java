package com.leui.notification.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by xiaoxin on 17-3-28.
 */

public class HelperUtil {
    private static WindowManager wm;
    private static View floatView;
    private static View mView;
    private static WindowManager.LayoutParams wmParams;

    public static void addFloatView(Context context, View view) {
        removeFloatView();
        if (wm == null) {
            //经过测试，这里两处可以使用activity的context
            wm = (WindowManager) context.getApplicationContext().getSystemService("window");
        }
        if (floatView == null) {
            mView = view;
            floatView = new View(context.getApplicationContext());
            floatView.setBackgroundColor(/*Color.RED*/0x88ff0000);
            floatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickView();
                }
            });
            floatView.setOnTouchListener(new View.OnTouchListener() {
                float initPointX = 0;
                float initPointY = 0;
                float lastPointX = 0;
                float lastPointY = 0;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //return false;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initPointX = lastPointX = event.getRawX();
                            initPointY = lastPointY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            wmParams.x += (int)(event.getRawX() - lastPointX);
                            wmParams.y += (int)(event.getRawY() - lastPointY);
                            lastPointX = event.getRawX();
                            lastPointY = event.getRawY();
                            wm.updateViewLayout(floatView, wmParams);
                            break;
                        case  MotionEvent.ACTION_UP :
                            if(Math.abs(event.getRawX() - initPointX) < 5
                                    && Math.abs(event.getRawY() - initPointY) < 5) {
                                clickView();
                            }
                        default:
                            break;
                    }
                    return true;
                }
            });
        }
        if (wmParams == null) {
            wmParams = new WindowManager.LayoutParams();
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;//显示在锁屏界面之上
            wmParams.format = PixelFormat.TRANSLUCENT;

            //设置Window flag
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS  //可以拖动到屏幕之外
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            ;
            //wmParams.gravity = Gravity.RIGHT | Gravity.TOP;
            wmParams.x = 0;
            wmParams.y = 1000;

            //设置悬浮窗口长宽数据
            //wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            wmParams.width = 200;
            wmParams.height = 200;
            //wmParams.verticalMargin = 200;
            wmParams.setTitle("notification_float");
            wmParams.windowAnimations = android.R.style.Animation_Dialog;
        }
        wm.addView(floatView, wmParams);
    }

    public static void removeFloatView() {
        if (floatView != null) {
            wm.removeView(floatView);
            floatView = null;
            mView = null;
        }
    }

    public static void addOverlay(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, 0);
            }
        }
    }

    private static void clickView() {
        if (mView != null)
            mView.performClick();
    }
}
