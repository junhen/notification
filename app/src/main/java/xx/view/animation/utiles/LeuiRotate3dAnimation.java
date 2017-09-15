package xx.view.animation.utiles;

/**
 * Created by xiaoxin on 17-4-12.
 */

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.Log;

/**
 * An animation that rotates the xx.view on the X,Y,Z axis between two specified angles.
 * This animation also adds a translation on the Z axis (depth) to improve the effect.
 */
public class LeuiRotate3dAnimation extends Animation {
    public static final Byte ROTATE_X_AXIS = 0x00;
    public static final Byte ROTATE_Y_AXIS = 0x01;
    public static final Byte ROTATE_Z_AXIS = 0x02;
    private final float mFromDegrees;
    private final float mToDegrees;
    private final float mCenterX;
    private final float mCenterY;
    private final float mDepthZ;
    private final boolean mReverse;
    private Camera mCamera;
    private Byte mRotateAxis;  // 0：X轴  1：Y轴  2：Z轴
    private float mLeft = 0, mTop = 0;

    /**创建3D旋转动画
     * @param fromDegrees the start angle of the 3D rotation
     * @param toDegrees the end angle of the 3D rotation
     * @param centerX the X center of the 3D rotation
     * @param centerY the Y center of the 3D rotation
     * @param depthZ the Z depth of the 3D rotation
     * @param rotateAxis the rotate axis of the 3D rotation
     * @param reverse true if the translation should be reversed, false otherwise
     */
    public LeuiRotate3dAnimation(float fromDegrees, float toDegrees,
                                 float centerX, float centerY, float depthZ, Byte rotateAxis, boolean reverse, float left, float right) {
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;
        mCenterX = centerX;
        mCenterY = centerY;
        mDepthZ = depthZ;
        mRotateAxis = rotateAxis;
        mReverse = reverse;
        mLeft = left;
        mTop = right;
        Log.d("xinx","LeuiRotate3dAnimation    mLeft = "+mLeft+",   mTop = "+mTop);
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        mCamera = new Camera();
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        final float fromDegrees = mFromDegrees;
        float degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime);

        final float centerX = mCenterX;
        final float centerY = mCenterY;
        final Camera camera = mCamera;

        //t.set(new Transformation());
        final Matrix matrix = t.getMatrix();
        // 将当前的摄像头位置保存下来，以便变换进行完成后恢复成原位
        camera.save();
        if (mReverse) {
            // z的偏移会越来越大。这就会形成这样一个效果，view从近到远
            camera.translate(0.0f, centerY * interpolatedTime, mDepthZ * interpolatedTime);
            float tmpAlpha = (1 - interpolatedTime) * 3;
            float alpha = tmpAlpha  < 1f ? tmpAlpha : 1;
            t.setAlpha(alpha);
        } else {
            // z的偏移会越来越小。这就会形成这样一个效果，我们的View从一个很远的地方向我们移过来，越来越近，最终移到了我们的窗口上面
            camera.translate(0.0f, centerY * (1.0f - interpolatedTime), mDepthZ * (1.0f - interpolatedTime));
            float tmpAlpha = interpolatedTime * 3;
            float alpha = tmpAlpha  < 1f ? tmpAlpha : 1;
            t.setAlpha(alpha);
        }
        // 是给我们的View加上旋转效果，在移动的过程中，视图还会以XYZ轴为中心进行旋转。
        if (ROTATE_X_AXIS.equals(mRotateAxis)) {
            camera.rotateX(degrees);
        } else if (ROTATE_Y_AXIS.equals(mRotateAxis)) {
            camera.rotateY(degrees);
        } else {
            camera.rotateZ(degrees);
        }

        // 这个是将我们刚才定义的一系列变换应用到变换矩阵上面，调用完这句之后，我们就可以将camera的位置恢复了，以便下一次再使用。
        camera.getMatrix(matrix);
        // camera位置恢复
        camera.restore();

        // 下面两句是为了动画是以View中心为旋转点
        matrix.preTranslate(-centerX-mLeft, -centerY-mTop);
        matrix.postTranslate(centerX+mLeft, centerY+mTop);
    }
}


