package test.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.leui.notification.test.R;

/**
 * Created by xiaoxin on 17-4-20.
 */

public class TouchViewParent extends TouchView {

    public TouchViewParent(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setTAG() {
        TAG = "TouchViewParent";
    }
}
