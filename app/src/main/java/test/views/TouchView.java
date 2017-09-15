package test.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leui.notification.test.R;

/**
 * Created by xiaoxin on 17-4-20.
 */

public abstract class TouchView extends FrameLayout {
    public TouchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTAG();
    }

    public String TAG;
    private TextView mDispatchIndexText, mInterceptIndexText, mTouchIndexText;
    private int mDispatchIndex, mInterceptIndex, mTouchIndex;

    public void resetIndex(){
        mDispatchIndex = 0;
        mInterceptIndex = 0;
        mTouchIndex = 0;
        mDispatchIndexText.setText("" + mDispatchIndex);
        mInterceptIndexText.setText("" + mInterceptIndex);
        mTouchIndexText.setText("" + mTouchIndex);
    }

    public void setTouchIndexView(LinearLayout ll) {
        this.mDispatchIndexText = (TextView) ll.findViewById(R.id.touch_view_index_dispatch);
        this.mInterceptIndexText = (TextView) ll.findViewById(R.id.touch_view_index_intercept);
        this.mTouchIndexText = (TextView) ll.findViewById(R.id.touch_view_index_touch);
        resetIndex();
    }

    private boolean mIntercept, mTouch;

    public abstract void setTAG();

    public void setmIntercept(boolean mIntercept) {
        this.mIntercept = mIntercept;
    }

    public void setmTouch(boolean mTouch) {
        this.mTouch = mTouch;
    }

    public void resetParaments(FrameLayout view, boolean left, boolean top, boolean right, boolean bottom, int step) {
        int width = view.getWidth();
        int height = view.getHeight();
        LayoutParams lp = (LayoutParams)getLayoutParams();
        lp.height = step == 1 ? height * 4 / 5 : height * 3 / 5;
        lp.width = step == 1 ? width * 4 / 5 : width * 3 / 5;
        Log.d(TAG,"XINX   resetParaments   width = "+width+",  height = "+height+",   lp = "+lp);
        setLayoutParams(lp);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG,"XINX   dispatchTouchEvent   ev = "+ev);
        mDispatchIndexText.setText("" + ++mDispatchIndex);
        boolean result = super.dispatchTouchEvent(ev);
        Log.d(TAG,"XINX   dispatchTouchEvent   ev = "+ev+",   result = "+result);
        return result;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG,"XINX   onInterceptTouchEvent   ev = "+ev);
        mInterceptIndexText.setText("" + ++mInterceptIndex);
        //boolean result = super.onInterceptTouchEvent(ev);
        boolean result = mIntercept;
        Log.d(TAG,"XINX   onInterceptTouchEvent   ev = "+ev+",   result = "+result);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"XINX   onTouchEvent   ev = "+event);
        mTouchIndexText.setText("" + ++mTouchIndex);
        //boolean result = super.onTouchEvent(event);
        boolean result = mTouch;
        Log.d(TAG,"XINX   onTouchEvent   ev = "+event+",   result = "+result);
        return result;
    }
}
