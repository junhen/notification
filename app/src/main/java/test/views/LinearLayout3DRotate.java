package test.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.leui.notification.test.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import xx.view.animation.utiles.LeuiRotate3dAnimation;

/**
 * Created by xiaoxin on 17-4-13.
 */

public class LinearLayout3DRotate extends LinearLayout {

    private static final String TAG = "LinearLayout3DRotate";
    private Button mRotateButton;
    private ImageView mImageView;
    private LeuiRotate3dAnimation mRotate3dAnimation;
    private int mRotateAxis = 0, mStartDegree, mEndDegree, mDuration, mDepth;
    private Spinner mSpinner;
    private InputMethodManager mImm;
    private EditText mDurationTime;

    public LinearLayout3DRotate(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.d(TAG, "xinx    onInterceptTouchEvent  event = "+event);
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "xinx    onTouchEvent  event = "+event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mImm.isActive()) {
                return true;
            }
        }
        if(event.getAction() == MotionEvent.ACTION_CANCEL
                || event.getAction() == MotionEvent.ACTION_UP) {
            mImm.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("XINX", "onFinishInflate");
        mImageView = (ImageView) findViewById(R.id.test_imageView);
        try {
            mImageView.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v) {
                    rotateView(mImageView);
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "onFinishInflate    " + Log.getStackTraceString(new Throwable()));
            e.printStackTrace();
        }
        mRotateButton = (Button) findViewById(R.id.rotate_3d);
        mRotateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                rotateView(mImageView);
                //测试弱引用
                String cb1 = mCallbacks.get(0).get();
                String cb2 = mCallbacks.get(1).get();
                Log.d("XINX", ",   onClick  cb1 = "+cb1);
                Log.d("XINX", ",   onClick  cb2 = "+cb2);
            }
        });
        mSpinner = (Spinner) findViewById(R.id.rotate_axis);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                mRotateAxis = pos <= 0 ? 0 : pos == 1 ? 1 : 2;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });
        mDurationTime = (EditText) findViewById(R.id.duration_time);
        //mImm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        //mImm.showSoftInput(mDurationTime, InputMethodManager.SHOW_IMPLICIT);
        mDurationTime.postDelayed(new Runnable() {
            @Override
            public void run() {
                mDurationTime.requestFocus();
                mImm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                mImm.showSoftInput(mDurationTime, InputMethodManager.SHOW_IMPLICIT);
            }
        },300);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //测试弱引用
        mCallbacks.add(new WeakReference<String>(str1));
        String str2 = new String("22222");
        mCallbacks.add(new WeakReference<String>(str2));
    }

    private final ArrayList<WeakReference<String>> mCallbacks = new ArrayList<WeakReference<String>>();
    String str1 = "aaaaaaaaa";

    private void rotateView(View v){
        mStartDegree = Integer.valueOf(((EditText) findViewById(R.id.start_degree)).getText().toString());
        mEndDegree = Integer.valueOf(((EditText) findViewById(R.id.end_degree)).getText().toString());
        mDuration = Integer.valueOf(((EditText) findViewById(R.id.duration_time)).getText().toString());
        mDepth = Integer.valueOf(((EditText) findViewById(R.id.rotate_depth)).getText().toString());
        float fromDegrees = mStartDegree;
        float toDegrees = mEndDegree;
        float centerX = (float)(v.getWidth() / 2);
        float centerY = (float)(v.getHeight() / 2);
        float depthZ = mDepth;
        long delay = 0;
        Byte rotateAxis = mRotateAxis == 0 ? LeuiRotate3dAnimation.ROTATE_X_AXIS
                        : mRotateAxis == 1 ? LeuiRotate3dAnimation.ROTATE_Y_AXIS
                        : LeuiRotate3dAnimation.ROTATE_Z_AXIS;
        boolean reverse = false;
        mRotate3dAnimation = new LeuiRotate3dAnimation(fromDegrees, toDegrees, centerX, centerY, depthZ,
                rotateAxis, reverse, v.getTranslationX(), v.getTranslationY());
        if (delay > 0) {
            mRotate3dAnimation.setStartOffset(delay);
        }
        mRotate3dAnimation.setDuration(mDuration);
        mRotate3dAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                boolean isHardwareAccelerated = isHardwareAccelerated();
                Log.d("xinx","mRotate3dAnimation    onAnimationStart    isHardwareAccelerated = "+isHardwareAccelerated);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                Log.d("xinx","mRotate3dAnimation    onAnimationRepeat");
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                Log.d("xinx","mRotate3dAnimation    onAnimationEnd");
            }
        });
        v.startAnimation(mRotate3dAnimation);
    }
}
