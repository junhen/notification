package xx.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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
 * 使用{@link #getInstance}获取此工具类单例
 * 调用{@link #addFloatView}增加一个悬浮窗，主要是独一无二的name，还有必须的callback
 * 调用{@link #removeFloatView}删除对应那么的悬浮窗
 *
 * {@link #(FloatViewHolder)} 一个内部类，用于保存一个悬浮窗的信息
 * {@link #mFloatViewHolders}用于保存所有由此工具类增加的悬浮窗代表类{@link #(FloatViewHolder)}
 */

public class NewWindowUtil {
    private static final String TAG = "NewWindowUtil";
    private static final List<FloatViewHolder> mFloatViewHolders = new ArrayList<>();
    private static NewWindowUtil mInstance;

    /**
     * 获取此工具类的单例
     * @return NewWindowUtil
     */
    public static synchronized NewWindowUtil getInstance() {
        if (mInstance == null) {
            mInstance = new NewWindowUtil();
        }
        Log.d(TAG, "getInstance = "+mInstance);
        return mInstance;
    }

    /**
     * @param context : 上下文，应该是activity，service，application的context
     * @param name ： 悬浮窗的名字，用于标识一个独一无二的，不可以重名
     * @param callback ： 回调，包含三个功能：初始化，结束收尾，点击事件
     */
    public void addFloatView(Context context, String name, Callback callback) {
        addFloatView(context, name, callback, getDefaultView(context, callback), getDefaultLayoutParams(name));
    }

    /**
     * 增加悬浮窗
     * @param view : 悬浮窗对应的view，可以自建，也可以使用默认的
     * @param lp ： 增加悬浮窗是使用的WindowManager.LayoutParams
     */
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

    /**
     * 删除悬浮窗
     * @param name : 删除悬浮窗时，必须指定对应的name
     */
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

    private WindowManager.LayoutParams getDefaultLayoutParams(String name) {
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
        //设置悬浮窗的位置，可以是gravity，也可以直接使用坐标x，y。
        lp.gravity = Gravity.END | Gravity.BOTTOM;
        //lp.x = 0;
        //lp.y = 1000;
        //设置悬浮窗口长宽数据
        lp.width = 200;
        lp.height = 200;
        //lp.verticalMargin = 200;
        lp.setTitle(name);
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

    /**
     * 悬浮窗信息类
     */
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

    /**
     * 该接口可以为悬浮窗做初始化和结束收尾的工作，增加点击事件的功能
     */
    public interface Callback {
        void onAddView();

        void onRemoveView();

        void onClickView();
    }

    public static void addOverlay(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, 0);
            }
        }
    }
}
