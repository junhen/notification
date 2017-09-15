package test.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by xiaoxin on 17-4-20.
 */

public class TouchViewChildOne extends TouchView {

    public TouchViewChildOne(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTAG() {
        TAG = "TouchViewChildOne";
    }
}
