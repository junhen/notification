package xx.livewallpaper;

/**
 * Created by xiaoxin on 17-mRadioX-21.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Android Live Wallpaper painting thread Archetype
 * <a href="http://my.oschina.net/arthor" target="_blank" rel="nofollow">@author</a> antoine vianey
 * GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 */
public class LiveWallpaperPainting extends Thread {

    /** Reference to the View and the context */
    private SurfaceHolder surfaceHolder;
    private Context context;

    static {
        Log.d("xinx","11111111123");
    }

    /** State */
    private boolean wait;
    private boolean run;

    /** Dimensions */
    private int width;
    private int height;

    /** Time tracking */
    private long previousTime;
    private long currentTime;

    // 定义画笔
    private Paint mPaint = new Paint();

    public LiveWallpaperPainting(SurfaceHolder surfaceHolder,
                                 Context context) {
        // keep a reference of the context and the surface
        // the context is needed if you want to inflate
        // some resources from your livewallpaper .apk
        this.surfaceHolder = surfaceHolder;
        this.context = context;
        // don't animate until surface is created and displayed
        this.wait = true;
        {
            mPaint.setColor(0xffffffff);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(2);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStyle(Paint.Style.STROKE);
            dm = context.getResources().getDisplayMetrics();
            mWindowWidth =dm.widthPixels;
            mWindowHeight =dm.heightPixels;
        }
    }

    /**
     * Pauses the live wallpaper animation
     */
    public void pausePainting() {
        this.wait = true;
        synchronized(this) {
            this.notify();
        }
    }

    /**
     * Resume the live wallpaper animation
     */
    public void resumePainting() {
        this.wait = false;
        synchronized(this) {
            this.notify();
        }
    }

    /**
     * Stop the live wallpaper animation
     */
    public void stopPainting() {
        this.run = false;
        synchronized(this) {
            this.notify();
        }
    }

    @Override
    public void run() {
        this.run = true;
        Canvas c = null;
        while (run) {
            try {
                c = this.surfaceHolder.lockCanvas(null);
                synchronized (this.surfaceHolder) {
                    currentTime = System.currentTimeMillis();
                    updatePhysics();
                    doDraw(c);
                    previousTime = currentTime;
                }
            } finally {
                if (c != null) {
                    this.surfaceHolder.unlockCanvasAndPost(c);
                }
            }
            // pause if no need to animate
            synchronized (this) {
                if (wait) {
                    try {
                        wait();
                    } catch (Exception e) {}
                }
            }
        }
    }

    /**
     * Invoke when the surface dimension change
     */
    public void setSurfaceSize(int width, int height) {
        this.width = width;
        this.height = height;
        synchronized(this) {
            this.notify();
        }
    }

    /**
     * Invoke while the screen is touched
     */
    public void doTouchEvent(MotionEvent event) {
        mTouchX = event.getX();
        mTouchY = event.getY();
        mRadioX = (float) (6 * 1.5);
        mRadioY = (float) (8 * 1.5);
        isUp = event.getAction() == MotionEvent.ACTION_UP;
        if(isUp) {
            mTouchX = mTouchY = -1;
            mRadioX = 6;
            mRadioY = 8;
        }
        // handle the event here
        // if there is something to animate
        // then wake up
        this.wait = false;
        synchronized(this) {
            notify();
        }
    }

    /**
     * Do the actual drawing stuff
     */
    private void doDraw(Canvas canvas) {
        if(canvas != null) {
            //canvas.save();
            // 绘制背景色
            canvas.drawColor(0xff880000);
            {
                cx1 += mRadioX;
                cy1 += mRadioY;
                // 如果cx1、cy1移出屏幕后从左上角重新开始
                if (cx1 > mWindowWidth)
                    cx1 = 0;
                if (cy1 > mWindowHeight)
                    cy1 = 0;


                cx2 -= mRadioX;
                cy2 -= mRadioY;
                // 如果cx2、cy2移出屏幕后从右下角重新开始
                if (cx2 < 0)
                    cx2 = mWindowWidth;
                if (cy2 < 0)
                    cy2 = mWindowHeight;


                cx3 -= mRadioX;
                cy3 += mRadioY;
                // 如果cx3、cy3移出屏幕后从右上角重新开始
                if (cx3 < 0)
                    cx3 = mWindowWidth;
                if (cy3 > mWindowHeight)
                    cy3 = 0;


                cx4 += mRadioX;
                cy4 -= mRadioY;
                // 如果cx4、cy4移出屏幕后从左下角重新开始
                if (cx4 > mWindowWidth)
                    cx4 = 0;
                if (cy4 < 0)
                    cy4 = mWindowHeight;


                // 绘制圆圈
                canvas.drawCircle(cx1, cy1, 30, mPaint);
                canvas.drawCircle(cx2, cy2, 40, mPaint);
                canvas.drawCircle(cx3, cy3, 50, mPaint);
                canvas.drawCircle(cx4, cy4, 60, mPaint);
            }
            if (isUp) return;
            if (mTouchX >= 0 && mTouchY >= 0)
            {
                canvas.drawCircle(mTouchX, mTouchY, 50, mPaint);
                canvas.drawCircle(mTouchX-100, mTouchY-100, 50, mPaint);
                canvas.drawCircle(mTouchX-100, mTouchY+100, 50, mPaint);
                canvas.drawCircle(mTouchX+100, mTouchY-100, 50, mPaint);
                canvas.drawCircle(mTouchX+100, mTouchY+100, 50, mPaint);
                //canvas.restore();
            }
        }
    }

    /**
     * Update the animation, sprites or whatever.
     * If there is nothing to animate set the wait
     * attribute of the thread to true
     */
    private void updatePhysics() {
        // if nothing was updated :
        // this.wait = true;
    }

    private float mTouchX = -1, mTouchY = -1;
    private boolean isUp;

    private DisplayMetrics dm;
    private float mWindowHeight, mWindowWidth;



    //左上角坐标
    private float cx1 = 15;
    private float cy1 = 20;

    //右下角坐标
    private float cx2 = mWindowWidth;
    private float cy2 = mWindowHeight;

    //右上角坐标
    private float cx3 = mWindowWidth;
    private float cy3 = 20;

    //左下角坐标
    private float cx4 = 15;
    private float cy4 = mWindowHeight;

    private float mRadioX = 6, mRadioY = 8;

}