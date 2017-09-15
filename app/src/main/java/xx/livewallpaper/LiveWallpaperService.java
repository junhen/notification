package xx.livewallpaper;

/**
 * Created by xiaoxin on 17-6-21.
 */

import android.content.SharedPreferences;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Android Live Wallpaper Archetype
 * <a href="http://my.oschina.net/arthor" target="_blank" rel="nofollow">@author</a> antoine vianey
 * under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 */
public class LiveWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new SampleEngine();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class SampleEngine extends Engine {

        private static final String TAG = "LiveWallpaperService.SampleEngine";
        private LiveWallpaperPainting painting;

        SampleEngine() {
            SurfaceHolder holder = getSurfaceHolder();
            painting = new LiveWallpaperPainting(holder,
                    getApplicationContext());
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            Log.d(TAG, "onCreate");
            super.onCreate(surfaceHolder);
            // register listeners and callbacks here
            setTouchEventsEnabled(true);
        }

        @Override
        public void onDestroy() {
            Log.d(TAG, "onDestroy");
            super.onDestroy();
            // remove listeners and callbacks here
            painting.stopPainting();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            Log.d(TAG, "onVisibilityChanged");
            if (visible) {
                // register listeners and callbacks here
                painting.resumePainting();
            } else {
                // remove listeners and callbacks here
                painting.pausePainting();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format,
                                     int width, int height) {
            Log.d(TAG, "onSurfaceChanged");
            super.onSurfaceChanged(holder, format, width, height);
            painting.setSurfaceSize(width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "onSurfaceCreated");
            super.onSurfaceCreated(holder);
            // start painting
            painting.start();
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "onSurfaceDestroyed");
            super.onSurfaceDestroyed(holder);
            boolean retry = true;
            painting.stopPainting();
            while (retry) {
                try {
                    painting.join();
                    retry = false;
                } catch (InterruptedException e) {}
            }
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                                     float xStep, float yStep, int xPixels, int yPixels) {
            Log.d(TAG, "onOffsetsChanged");
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            Log.d(TAG, "onTouchEvent");
            super.onTouchEvent(event);
            painting.doTouchEvent(event);
        }

    }

}