package xx.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaoxin on 17-10-31.
 *
 * 使用getInstance获取此工具类单例
 * 调用addFloatView增加一个悬浮窗，主要是独一无二的name，还有必须的callback
 * 调用removeFloatView删除对应那么的悬浮窗
 * callback三个功能：初始化，结束收尾，点击事件
 */

public class NewWindowUtil {
    private static final String TAG = "NewWindowUtil";
    private static final List<FloatViewHolder> mFloatViewHolders = new ArrayList<>();
    private static NewWindowUtil mInstance;

    public static synchronized NewWindowUtil getInstance() {
        if (mInstance == null) {
            mInstance = new NewWindowUtil();
        }
        Log.d(TAG, "getInstance = "+mInstance);
        return mInstance;
    }

    public void addFloatView(Context context, String name, Callback callback) {
        addFloatView(context, name, callback, getDefaultView(context, callback), getDefaultLayoutParams());
    }

    public void addFloatView(Context context, String name, Callback callback, View view, WindowManager.LayoutParams lp) {
        if (callback == null) {
            Log.d(TAG, "addFloatView,  callback can not be null.");
            Toast.makeText(context, "has no Callback", Toast.LENGTH_SHORT);
            return;
        }

        if (mFloatViewHolders.size() > 0) {
            synchronized (mFloatViewHolders) {
                for (FloatViewHolder fd : mFloatViewHolders) {
                    if (fd.name.equals(name)) {
                        Log.d(TAG, "addFloatView,  the wm has have the float view.");
                        Toast.makeText(context, "the wm has have the float view.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        FloatViewHolder floatViewHolder = new FloatViewHolder(name, view, callback);
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.addView(floatViewHolder.floatView, lp);
        floatViewHolder.callback.onAddView();
        synchronized (mFloatViewHolders) {
            mFloatViewHolders.add(floatViewHolder);
        }
    }

    public void removeFloatView(String name) {
        if (mFloatViewHolders.size() > 0) {
            synchronized (mFloatViewHolders) {
                for (FloatViewHolder fd : mFloatViewHolders) {
                    if (fd.name.equals(name)) {
                        fd.callback.onRemoveView();
                        WindowManager wm = (WindowManager) fd.floatView.getContext().getSystemService(Context.WINDOW_SERVICE);
                        wm.removeView(fd.floatView);
                        mFloatViewHolders.remove(fd);
                    }
                }
            }
        }
    }

    private View getDefaultView(Context context, final Callback callback) {
        View view = new View(context.getApplicationContext());
        view.setBackgroundColor(Color.GREEN);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onClickView();
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        return view;
    }

    private WindowManager.LayoutParams getDefaultLayoutParams() {
        WindowManager.LayoutParams lp;
        lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;//显示在锁屏界面之上
        lp.format = PixelFormat.TRANSPARENT;

        //设置Window flag
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS  //可以拖动到屏幕之外
        ;
        lp.gravity = Gravity.END | Gravity.BOTTOM;
        //lp.x = 0;
        //lp.y = 1000;

        //设置悬浮窗口长宽数据
        lp.width = 200;
        lp.height = 200;
        //lp.verticalMargin = 200;
        lp.setTitle("notification_float");
        lp.windowAnimations = android.R.style.Animation_Dialog;
        return lp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mFloatViewHolders.size() < 1) {
            sb.append("has no float view");
        } else {
            for (FloatViewHolder fd : mFloatViewHolders) {
                sb.append("name: ").append(fd.name).append(",    flootView: ").append(fd.floatView);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static class FloatViewHolder {
        private String name;
        private View floatView;
        private Callback callback;

        public FloatViewHolder(String name, View floatView, Callback callback) {
            this.name = name;
            this.floatView = floatView;
            this.callback = callback;
        }
    }

    //该接口可以为悬浮窗做初始化和结束收尾的工作，增加点击事件的功能
    public interface Callback {
        void onAddView();

        void onRemoveView();

        void onClickView();
    }
}
