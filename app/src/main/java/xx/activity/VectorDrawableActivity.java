package xx.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.leui.notification.test.R;

/**
 * Created by xiaoxin on 17-6-28.
 */

public class VectorDrawableActivity extends Activity implements View.OnClickListener{

    private Button fp_to_error;
    private Button error_to_fp;
    private Button trusted_to_error;
    private Button error_to_trusted;
    private Button fingerprint_draw_on;
    private Button fingerprint_draw_off;

    private ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vector_drawable);

        fp_to_error = (Button)findViewById(R.id.vector_drawable_bn_fp_to_error);
        error_to_fp = (Button)findViewById(R.id.vector_drawable_bn_error_to_fp);
        trusted_to_error = (Button)findViewById(R.id.vector_drawable_bn_trusted_to_error);
        error_to_trusted = (Button)findViewById(R.id.vector_drawable_bn_error_to_trusted);
        fingerprint_draw_on = (Button)findViewById(R.id.vector_drawable_bn_fingerprint_draw_on);
        fingerprint_draw_off = (Button)findViewById(R.id.vector_drawable_bn_fingerprint_draw_off);

        fp_to_error.setOnClickListener(this);
        error_to_fp.setOnClickListener(this);
        trusted_to_error.setOnClickListener(this);
        error_to_trusted.setOnClickListener(this);
        fingerprint_draw_on.setOnClickListener(this);
        fingerprint_draw_off.setOnClickListener(this);

        mImageView = (ImageView)findViewById(R.id.vector_drawable_image);
    }


    private int getAnimationResForTransition(int id) {
        int resId = -1;
        switch (id) {
            case R.id.vector_drawable_bn_fp_to_error :
                resId = R.drawable.lockscreen_fingerprint_fp_to_error_state_animation;
                break;
            case R.id.vector_drawable_bn_error_to_fp :
                resId = R.drawable.lockscreen_fingerprint_error_state_to_fp_animation;
                break;
            case R.id.vector_drawable_bn_trusted_to_error :
                resId = R.drawable.trusted_state_to_error_animation;
                break;
            case R.id.vector_drawable_bn_error_to_trusted :
                resId = R.drawable.error_to_trustedstate_animation;
                break;
            case R.id.vector_drawable_bn_fingerprint_draw_on :
                resId = R.drawable.lockscreen_fingerprint_draw_on_animation;
                break;
            case R.id.vector_drawable_bn_fingerprint_draw_off :
                resId = R.drawable.lockscreen_fingerprint_draw_off_animation;
                break;
        }
        return resId;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        boolean isAnim = true;
        int iconRes = getAnimationResForTransition(v.getId());
        if (iconRes == R.drawable.lockscreen_fingerprint_draw_off_animation) {
        } else if (iconRes == R.drawable.trusted_state_to_error_animation) {
        } else if (iconRes == R.drawable.error_to_trustedstate_animation) {
        } else {
            //isAnim = false;
        }
        Drawable icon = getResources().getDrawable(iconRes);
        int iconHeight = getResources().getDimensionPixelSize(
                R.dimen.keyguard_affordance_icon_height);
        int iconWidth = getResources().getDimensionPixelSize(
                R.dimen.keyguard_affordance_icon_width);
        if (icon.getIntrinsicHeight() != iconHeight || icon.getIntrinsicWidth() != iconWidth) {
            icon = new IntrinsicSizeDrawable(icon, iconWidth, iconHeight);
        }
        final AnimatedVectorDrawable animation = icon instanceof AnimatedVectorDrawable ? (AnimatedVectorDrawable) icon : null;
        mImageView.setImageDrawable(icon);
        if (animation != null && isAnim) {
            //animation.forceAnimationOnUI();
            animation.start();
        }
    }


    /**
     * A wrapper around another Drawable that overrides the intrinsic size.
     */
    private static class IntrinsicSizeDrawable extends InsetDrawable {

        private final int mIntrinsicWidth;
        private final int mIntrinsicHeight;

        public IntrinsicSizeDrawable(Drawable drawable, int intrinsicWidth, int intrinsicHeight) {
            super(drawable, 0);
            mIntrinsicWidth = intrinsicWidth;
            mIntrinsicHeight = intrinsicHeight;
        }

        @Override
        public int getIntrinsicWidth() {
            return mIntrinsicWidth;
        }

        @Override
        public int getIntrinsicHeight() {
            return mIntrinsicHeight;
        }
    }
}
