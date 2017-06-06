package com.eightyeightysix.shourya.almondclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/*
 * Created by shourya on 4/6/17.
 */

public class ZonePinchSurfaceView extends SurfaceView implements Runnable, FeedActivity.pinchListener{
    Thread t = null;
    SurfaceHolder holder;
    Paint paintRed, paintBlue;
//    boolean debug = false;

    private PointF index, thumb;
    private float radius;
    private PointF center;

    boolean pinched = false;
    boolean isRunning = false;

    private static final String DEBUG_TAG = "AlmondLog::" + ZonePinchSurfaceView.class.getSimpleName();
    //private Bitmap bmpIcon;

    public ZonePinchSurfaceView(Context context) {
        super(context);
        holder = getHolder();
        pinchInit();
    }

    public ZonePinchSurfaceView(Context context,
                         AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        pinchInit();
    }

    public ZonePinchSurfaceView(Context context,
                         AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        holder = getHolder();
        pinchInit();
    }

    @Override
    public void run() {
        while(isRunning) {
            if(!holder.getSurface().isValid()) {
                continue;
            }
            holder.setFormat(PixelFormat.TRANSPARENT);
            Canvas c = holder.lockCanvas();
            c.drawColor(0, PorterDuff.Mode.CLEAR);
           // Log.d(DEBUG_TAG, "(thread)Radius: "+ radius);
           // Log.d(DEBUG_TAG, "ScreenCenter: x="+ BaseActivity.SCREEN_CENTER.x + ",y=" + BaseActivity.SCREEN_CENTER.y);
            //c.drawCircle(BaseActivity.SCREEN_CENTER.x, BaseActivity.SCREEN_CENTER.y, radius, paintRed);
            //center.x = (index.x + thumb.y)/2
            c.drawCircle(center.x,center.y, radius, paintRed);
            holder.unlockCanvasAndPost(c);
        }
    }

    public void pause() {
        isRunning = false;
        while(true) {
            try{
                t.join();
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
            break;
        }
        t = null;
    }

    public void resume() {
        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    /*private static double getDistance(Point center, PointF pin) {
        double sq = Math.pow((center.x - pin.x),2) + Math.pow((center.y - pin.y),2);
        return Math.sqrt(sq);
    }*/

    private static double getDistance(PointF pin1, PointF pin2) {
        double sq = Math.pow((pin1.x - pin2.x),2) + Math.pow((pin1.y - pin2.y),2);
        return Math.sqrt(sq);
    }

    private void pinchInit() {
        index = new PointF(-1,-1);
        thumb = new PointF(-1,-1);
        center = new PointF(-1,-1);
        paintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRed.setColor(Color.RED);
        paintRed.setStyle(Paint.Style.FILL);
  }

    @Override
    public void setPinchRadius(PointF index1, PointF thumb1) {
        this.index = index1;
        this.thumb = thumb1;
        Log.d(DEBUG_TAG, "Received: Index= "+ index.x +"," + index.y+"\nThumb="+ thumb.x +"," + thumb.y);
        /*double a = getDistance(BaseActivity.SCREEN_CENTER, index);
        double b = getDistance(BaseActivity.SCREEN_CENTER, thumb);
        Log.d(DEBUG_TAG, "Radius:index= "+ a + "thumb= " + b);
        radius = (float)((a > b)?a:b);
        */
        center.set((index.x+thumb.x)/2, (index.y+thumb.y)/2);
        Log.d(DEBUG_TAG, "Center: "+ center.x+","+center.y);

        radius = (float)(getDistance(center, index));
        float radius2 = (float)(getDistance(center, thumb));
        Log.d(DEBUG_TAG, "Center->index=" + radius + ", Center->thumb=" + radius2);


        if(!pinched) {
            pinched = true;
            resume();
        }
    }

   // @Override
    public void debug(PointF index) {
        Canvas c = holder.lockCanvas();

        c.drawCircle(index.x,index.y, 30, paintRed);
        holder.unlockCanvasAndPost(c);
    }

    @Override
    public void exitPinch() {
        if(pinched) {
            pinched = false;
            index.set(-1, -1);
            thumb.set(-1, -1);
            pause();

            holder.setFormat(PixelFormat.TRANSPARENT);
            Canvas c = holder.lockCanvas();
            c.drawColor(0, PorterDuff.Mode.CLEAR);
            holder.unlockCanvasAndPost(c);

        }

    }
}
