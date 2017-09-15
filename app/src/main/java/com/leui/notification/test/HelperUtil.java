package com.leui.notification.test;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by xiaoxin on 17-3-28.
 */

public class HelperUtil {
    private static WindowManager wm;
    private static Context mContext;
    private static View floatView;
    private static WindowManager.LayoutParams wmParams;

    public static void addFloatView(Context context, final View view){
        removeFloatView();
        if(wm == null) {
            //经过测试，这里两处可以使用activity的context
            wm = (WindowManager) context.getApplicationContext().getSystemService("window");
        }
        mContext = context;
        if(floatView == null){
            floatView = new View(context.getApplicationContext());
            floatView.setBackgroundColor(Color.RED);
            floatView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.performClick();
                }
            });
            floatView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        }
        if(wmParams == null){
            wmParams = new WindowManager.LayoutParams();
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;//显示在锁屏界面之上
            wmParams.format = PixelFormat.TRANSPARENT;

            //设置Window flag
            wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS  //可以拖动到屏幕之外
            ;
            wmParams.gravity = Gravity.RIGHT|Gravity.BOTTOM;
            //wmParams.x = 0;
            //wmParams.y = 1000;

            //设置悬浮窗口长宽数据
            wmParams.width = 200;
            wmParams.height = 200;
            //wmParams.verticalMargin = 200;
            wmParams.setTitle("notification_float");
            wmParams.windowAnimations = android.R.style.Animation_Dialog;
        }
        wm.addView(floatView, wmParams);
    }

    public static void removeFloatView(Context context) {
        if(!context.equals(mContext)){
            return;
        }
        removeFloatView();
    }
    private static void removeFloatView() {
        if(floatView != null){
            wm.removeView(floatView);
            floatView = null;
        }
    }
}
