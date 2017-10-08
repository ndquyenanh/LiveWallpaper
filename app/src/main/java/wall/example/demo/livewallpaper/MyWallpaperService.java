package wall.example.demo.livewallpaper;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sev_user on 03-Dec-14.
 */
public class MyWallpaperService extends WallpaperService {

    /**
     * Must be implemented to return a new instance of the wallpaper's engine.
     * Note that multiple instances may be active at the same time, such as
     * when the wallpaper is currently set as the active wallpaper and the user
     * is in the wallpaper picker viewing a preview of it as well.
     */
    @Override
    public Engine onCreateEngine() {
        return new MyWallpaperEngine();
    }

    class MyWallpaperEngine extends Engine {

        Handler handler = new Handler();
        Runnable drawRunnable = new Runnable() {

            @Override
            public void run() {
                draw();
            }
        };

        List<MyPoint> circles = new ArrayList<>();
        Paint paint = new Paint();

        int width;
        int height;

        boolean mVisible = true;
        int maxNumber;
        boolean touchEnabled;

        public MyWallpaperEngine() {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyWallpaperService.this);
            maxNumber = preferences.getInt("numberOfCircles", 40);
            touchEnabled = preferences.getBoolean("Touch", true);

            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(100);
            paint.setStrokeJoin(Paint.Join.ROUND);

            handler.post(drawRunnable);
        }

        /**
         * Called to inform you of the wallpaper becoming visible or
         * hidden.  <em>It is very important that a wallpaper only use
         * CPU while it is visible.</em>.
         *
         * @param visible
         */
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            mVisible = visible;
            if (mVisible) {
                handler.post(drawRunnable);
            } else {
                handler.removeCallbacks(drawRunnable);
            }
        }

        /**
         * Convenience for {@link android.view.SurfaceHolder.Callback#surfaceDestroyed
         * SurfaceHolder.Callback.surfaceDestroyed()}.
         *
         * @param holder
         */
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);

            mVisible = false;
            handler.removeCallbacks(drawRunnable);
        }

        /**
         * Convenience for {@link android.view.SurfaceHolder.Callback#surfaceChanged
         * SurfaceHolder.Callback.surfaceChanged()}.
         *
         * @param holder
         * @param format
         * @param width
         * @param height
         */
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            this.width = width;
            this.height = height;
        }

        /**
         * Called as the user performs touch-screen interaction with the
         * window that is currently showing this wallpaper.  Note that the
         * events you receive here are driven by the actual application the
         * user is interacting with, so if it is slow you will get fewer
         * move events.
         *
         * @param event
         */
        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);

            if (touchEnabled) {

                float x = event.getX();
                float y = event.getY();

                SurfaceHolder holder = getSurfaceHolder();
                Canvas canvas = null;

                try {
                    canvas = holder.lockCanvas();

                    if (canvas != null) {
                        canvas.drawColor(Color.BLACK);
                        circles.clear();

                        int size = circles.size() + 1;
                        MyPoint myPoint = new MyPoint("Number: " + size, x, y);
                        circles.add(myPoint);

                        drawCircles(canvas,circles);
                    }

                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }

                }
            }
        }

        void draw() {

            SurfaceHolder holder = getSurfaceHolder();
            Canvas canvas = null;

            try {
                canvas = holder.lockCanvas();

                if (canvas != null) {

                    if (circles.size() > maxNumber) {
                        circles.clear();
                    }

                    int x = (int) (width * Math.random());
                    int y = (int) (height * Math.random());

                    circles.add(new MyPoint("" + circles.size() + 1, x, y));
                    drawCircles(canvas,circles);
                }
            } finally {
                if (canvas != null) {

                    holder.unlockCanvasAndPost(canvas);
                }
            }

            handler.removeCallbacks(drawRunnable);
            if (mVisible) {
                handler.postDelayed(drawRunnable, 5000);
            }
        }

        void drawCircles(Canvas canvas, List<MyPoint> circles) {
            canvas.drawColor(Color.BLACK);

            for (MyPoint circle : circles) {
                canvas.drawCircle(circle.x, circle.y, 20, paint);
            }
        }
    }

}
