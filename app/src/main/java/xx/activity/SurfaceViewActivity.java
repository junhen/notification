package xx.activity;

//来自   vendor/mediatek/propietary/framaworks/base/test/widget
/*import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.xx.view.SurfaceHolder;
import android.xx.view.SurfaceView;

public class SurfaceViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(new MySurfaceView(this));
    }

    private class MySurfaceView extends SurfaceView implements
            SurfaceHolder.Callback, Runnable {
        private SurfaceHolder mSurfaceHolder;
        private Paint mPaint;
        private Thread mThread;
        private int mWidth;
        private int mHeight;
        private Canvas mCanvas;

        public MySurfaceView(Context context) {
            super(context);
            mSurfaceHolder = this.getHolder();
            mSurfaceHolder.addCallback(this);
            mThread = new Thread(this);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.RED);

            this.setKeepScreenOn(true);
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            // TODO Auto-generated method stub

        }

        public void surfaceCreated(SurfaceHolder holder) {
            mWidth = this.getWidth();
            mHeight = this.getHeight();
            mThread.start();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub

        }

        public void run() {
            while (true) {
                draw();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        private void draw() {
            try {
                //mCanvas = mSurfaceHolder.lockCanvas();
                mCanvas = mSurfaceHolder.lockCanvas();
                int rand = (int) (Math.random() * 3);
                switch (rand) {
                    case 0:
                        mCanvas.drawColor(Color.WHITE);
                        break;
                    case 1:
                        mCanvas.drawColor(Color.BLACK);
                        break;
                    case 2:
                        mCanvas.drawColor(Color.RED);
                        break;
                }
                mCanvas.drawText("SurfaceViewTest", mWidth / 2, mHeight / 2, mPaint);
            } catch (Exception e) {
                // TODO: handle exception
            } finally {
                if (mCanvas != null) {
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }
        }
    }
}*/

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.leui.notification.test.*;

public class SurfaceViewActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private final static String TAG = "SurfaceViewActivity";
    Button btnsimpleDraw, btnTimerDraw;
    private int clickBn = -1;
    SurfaceView sfv;
    SurfaceHolder sfh;
    private Object mSync = new Object();

    private Timer mTimer;
    int mYAxis[],//保存正弦波的Y轴上的点
            centerY,//中心线
            oldX, oldY,//上一个XY点
            currentX;//当前绘制到的X轴上的点

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface_view);

        btnsimpleDraw = (Button) this.findViewById(R.id.Button01);
        btnTimerDraw = (Button) this.findViewById(R.id.Button02);
        btnsimpleDraw.setOnClickListener(new ClickEvent());
        btnTimerDraw.setOnClickListener(new ClickEvent());
        sfv = (SurfaceView) this.findViewById(R.id.SurfaceView01);
        sfh = sfv.getHolder();

        //动态绘制正弦波的定时器
        initTimer();

        // 初始化y轴数据
        centerY = (getWindowManager().getDefaultDisplay().getHeight() - sfv
                .getTop()) / 2;
        mYAxis = new int[getWindowManager().getDefaultDisplay().getWidth()];
        for (int i = 0; i < mYAxis.length; i++) {// 计算正弦波
            mYAxis[i] = centerY - (int) ((centerY / 2) * Math.sin(i * Math.PI / 180));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private void initTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            synchronized (mSync) {
                cancelTimer();
                oldX = currentX = 0;
                oldY = centerY;
                if (v == btnsimpleDraw) {
                    Log.i(TAG, "btnsimpleDraw");
                    clickBn = 1;
                    simpleDraw(mYAxis.length);//直接绘制正弦波
                } else if (v == btnTimerDraw) {
                    Log.i(TAG, "btnTimerDraw");
                    clickBn = 2;
                    initTimer();
                    try {
                        mTimer.schedule(new MyTimerTask(), 0, 5);//动态绘制正弦波
                    } catch (IllegalStateException e) {
                        Log.i("TAG onClick", "IllegalStateException : " + e);
                    }
                }
            }
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if (currentX == 0) {//如果是在起点，则清屏
                clearDraw();
            }
            drawSurfaceTo(currentX, mYAxis[currentX]);
            currentX += 10;//往前进
            if (currentX >= mYAxis.length) {
                currentX = 0;
                oldY = centerY;
            }
        }

    }

    /*x
         * 绘制指定区域
         */
    void simpleDraw(int length) {
        if (length == 0) {
            oldX = 0;
        }
        synchronized (mSync) {
            Canvas canvas = sfh.lockCanvas(new Rect(oldX, 0, oldX + length, getWindowManager().getDefaultDisplay().getHeight()));// 关键:获取画布
            if (canvas == null) {
                Log.i(TAG, "simpleDraw  canvas == null");
                return;
            }
            Log.i(TAG, "simpleDraw  oldX: " + String.valueOf(oldX) + ", oldX + length: " + String.valueOf(oldX + length));

            canvas.drawColor(Color.BLACK);// 清除画布

            Paint mPaint = new Paint();
            mPaint.setColor(Color.GREEN);// 画笔为绿色
            mPaint.setStrokeWidth(2);// 设置画笔粗细

            int y;
            for (int i = oldX; i < length; i++) {// 绘画正弦波
                y = mYAxis[i];
                canvas.drawLine(oldX, oldY, i, y, mPaint);
                oldX = i;
                oldY = y;
            }
            sfh.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
        }
    }

    /*x
     * 绘制指定区域
     */
    void drawSurfaceTo(int x, int y) {
        if (x == 0) {
            oldX = 0;
        }
        int smallY, largeY;
        if (oldY < y) {
            smallY = oldY;
            largeY = y;
        } else {
            smallY = y;
            largeY = oldY;
        }
        synchronized (mSync) {
            Log.i(TAG, "drawSurfaceTo"+",  clickBn: "+clickBn);
            if(clickBn != 2){
                return;
            }
            Canvas canvas = sfh.lockCanvas(new Rect(oldX, smallY, x, largeY));// 关键:获取画布
            if (canvas == null) {
                //当此view已经退出后还执行timerTast的时候会出现canvas为null
                Log.i(TAG, "drawSurfaceTo  canvas == null");
                return;
            }
            Log.i(TAG, "drawSurfaceTo  oldX: " + String.valueOf(oldX) + ", x: " + String.valueOf(x)+",  clickBn: "+clickBn);

            Paint mPaint = new Paint();
            mPaint.setColor(Color.GREEN);// 画笔为绿色
            mPaint.setStrokeWidth(8);// 设置画笔粗细
            canvas.drawLine(oldX, oldY, x, y, mPaint);
            oldX = x;
            oldY = y;
            sfh.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
        }
    }

    void clearDraw() {
        synchronized (mSync) {
            Canvas canvas = sfh.lockCanvas(null);
            canvas.drawColor(Color.BLACK);// 清除画布
            sfh.unlockCanvasAndPost(canvas);
        }
    }
}
