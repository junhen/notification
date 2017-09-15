package test.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by xiaoxin on 17-8-18.
 */

public class TestView extends View {
    private static final String TAG = "MyTestView";

    public TestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "TestView", new Throwable());
    }

    @Override
    public void layout(@Px int l, @Px int t, @Px int r, @Px int b) {
        Log.d(TAG, "layout", new Throwable());
        super.layout(l, t, r, b);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout", new Throwable());
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure", new Throwable());
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onFinishInflate() {
        Log.d(TAG, "onFinishInflate", new Throwable());
        super.onFinishInflate();

    }

    @Override
    protected void onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow", new Throwable());
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow", new Throwable());
        super.onDetachedFromWindow();
    }

}
