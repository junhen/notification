package test.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.leui.notification.test.R;

/**
 * Created by xiaoxin on 17-4-21.
 */

public class TouchViewControlView extends FrameLayout {

    private TouchView mParent, mChildOne, mChildTwo;
    private TouchView mChildOneOne, mChildTwoOne;
    private Spinner mSpinnerParent, mSpinnerChildOne, mSpinnerChildOneOne,
            mSpinnerChildTwo, mSpinnerChildTwoOne;
    private LinearLayout mlinearParent, mlinearChildOne, mlinearChildTwo,
            mlinearChildOneOne, mlinearChildTwoOne;

    public TouchViewControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mParent = (TouchView) findViewById(R.id.touch_view_parent);
        mChildOne = (TouchView) findViewById(R.id.touch_view_child_one);
        mChildTwo = (TouchView) findViewById(R.id.touch_view_child_two);
        mChildOneOne = (TouchView) findViewById(R.id.touch_view_child_one_one);
        mChildTwoOne = (TouchView) findViewById(R.id.touch_view_child_two_one);

        mSpinnerParent = (Spinner) findViewById(R.id.touch_view_control_parent_spinner);
        mSpinnerParent.setOnItemSelectedListener(new MyOnItemSelectedListener(mParent));
        mSpinnerChildOne = (Spinner) findViewById(R.id.touch_view_control_child_one_spinner);
        mSpinnerChildOne.setOnItemSelectedListener(new MyOnItemSelectedListener(mChildOne));
        mSpinnerChildTwo = (Spinner) findViewById(R.id.touch_view_control_child_two_spinner);
        mSpinnerChildTwo.setOnItemSelectedListener(new MyOnItemSelectedListener(mChildTwo));
        mSpinnerChildOneOne = (Spinner) findViewById(R.id.touch_view_control_child_one_one_spinner);
        mSpinnerChildOneOne.setOnItemSelectedListener(new MyOnItemSelectedListener(mChildOneOne));
        mSpinnerChildTwoOne = (Spinner) findViewById(R.id.touch_view_control_child_two_one_pinner);
        mSpinnerChildTwoOne.setOnItemSelectedListener(new MyOnItemSelectedListener(mChildTwoOne));

        mlinearParent = (LinearLayout) findViewById(R.id.touch_view_control_parent_index);
        mlinearChildOne = (LinearLayout) findViewById(R.id.touch_view_control_child_one_index);
        mlinearChildTwo = (LinearLayout) findViewById(R.id.touch_view_control_child_two_index);
        mlinearChildOneOne = (LinearLayout) findViewById(R.id.touch_view_control_child_one_one_index);
        mlinearChildTwoOne = (LinearLayout) findViewById(R.id.touch_view_control_child_two_one_index);
        mParent.setTouchIndexView(mlinearParent);
        mChildOne.setTouchIndexView(mlinearChildOne);
        mChildTwo.setTouchIndexView(mlinearChildTwo);
        mChildOneOne.setTouchIndexView(mlinearChildOneOne);
        mChildTwoOne.setTouchIndexView(mlinearChildTwoOne);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mChildOne.resetParaments(mParent, true, true, false, false, 1);
        mChildTwo.resetParaments(mParent, false, false, true, true, 1);
        mChildOneOne.resetParaments(mParent, true, true, false, false, 2);
        mChildTwoOne.resetParaments(mParent, false, false, true, true, 2);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN) {
            mParent.resetIndex();
            mChildOne.resetIndex();
            mChildTwo.resetIndex();
            mChildOneOne.resetIndex();
            mChildTwoOne.resetIndex();
        }
        boolean result = super.dispatchTouchEvent(ev);
        return result;
    }

    private class MyOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        private TouchView mView;
        public MyOnItemSelectedListener(TouchView view) {
            mView = view;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

            switch (pos) {
                case 0 :
                    mView.setmIntercept(false);
                    mView.setmTouch(false);
                    break;
                case 1 :
                    mView.setmIntercept(true);
                    mView.setmTouch(false);
                    break;
                case 2 :
                    mView.setmIntercept(false);
                    mView.setmTouch(true);
                    break;
                case 3 :
                    mView.setmIntercept(true);
                    mView.setmTouch(true);
                    break;
            }
        }
        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }
}
