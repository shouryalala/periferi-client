package com.eightyeightysix.shourya.almondclient;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/*
 * Created by shourya on 4/6/17.
 */

public class ZonePinchSurfaceView extends SurfaceView implements Runnable, FeedActivity.pinchListener{
    Thread t = null;
    SurfaceHolder holder;
    Paint paintRed, paintBlue;
    Canvas canvas;
//    boolean debug = false;

    private PointF index, thumb;
    private float radius;
    private PointF center;
    private static int zoneCount = 1;
    private static float radii[];
    private static float radii_temp[];
    private static float radiiExpand[];

    boolean pinched = false;
    boolean isRunning = false;

    private static final String DEBUG_TAG = "AlmondLog::" + ZonePinchSurfaceView.class.getSimpleName();
    //private Bitmap bmpIcon;

    public ZonePinchSurfaceView(Context context) {
        super(context);
        pinchInit();
    }

    public ZonePinchSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        pinchInit();
    }

    public ZonePinchSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        holder = getHolder();
        pinchInit();
    }

    private void pinchInit() {
        holder = getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);

        index = new PointF(-1,-1);
        thumb = new PointF(-1,-1);
        center = new PointF(-1,-1);

        paintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRed.setColor(Color.RED);
        paintRed.setStyle(Paint.Style.STROKE);
        paintRed.setStrokeWidth(8);


        paintBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBlue.setColor(Color.BLUE);
        paintBlue.setStyle(Paint.Style.STROKE);
        paintBlue.setStrokeWidth(7);

        getZoneCount();
    }

    private void getZoneCount(){
        //TODO Import number of zones from server
        zoneCount = 3;   //temp;
    }

    private void initRadii(float head) {
        radii = new float[zoneCount];
        radiiExpand = new float[zoneCount - 1];
        radii_temp = new float[zoneCount];

        radii[zoneCount-1] = head;
        //radii_temp[zoneCount - 1] = radii[zoneCount - 1];
        for(int j=(zoneCount-2); j>=0; j--) {
            radii[j] = head * ((float)(j+1)/zoneCount);
            radiiExpand[j] = radii[j] + (head/(2*zoneCount));
            //radii_temp[j] = radii[j];
        }
        radii_temp = radii;
        Log.d(DEBUG_TAG, "radii:"+ radii[2] + "," + radii[1] + "," + radii[0]);
        Log.d(DEBUG_TAG, "radii_temp:"+ radii_temp[2] + "," + radii_temp[1] + "," + radii_temp[0]);
    }

    @Override
    public void run() {
        while(isRunning) {
            if(!holder.getSurface().isValid()) {
                continue;
            }
            canvas = holder.lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            for(int j=0; j< zoneCount; j++) {
                Log.d(DEBUG_TAG, "Radii: "+radii[0] + "," + radii[1] + "," + radii[0]);
                canvas.drawCircle(center.x,center.y, radii_temp[j], paintRed);
                if(j != zoneCount-1){
                    canvas.drawCircle(center.x,center.y, radiiExpand[j], paintBlue);
                }
           }
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void resume() {
        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    private void pause() {
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

    private int getPointCircleRel(PointF pnt, PointF ctr, float radExpand, float radBound) {
        int f = (int)getDistance(pnt, ctr);
        int rE = (int)radExpand;
        int rB = (int)radBound;
        if(f == rE)                  //on circleExpand
            return 0;
        else if(f > rE)              //outside circleExpand
            return 1;
        else{                        //inside circleExpand
            if(f == rB )             // on CircleBound & inside circleExpand
                return 2;
            else if(f > rB)          //outside circleBound & inside circleExpand
                return 3;
            else                     //inside circleBound & inside circleExpand
                return 4;
        }
    }

    private static double getDistance(PointF pin1, PointF pin2) {
        double sq = Math.pow((pin1.x - pin2.x),2) + Math.pow((pin1.y - pin2.y),2);
        return Math.sqrt(sq);
    }

    @Override
    public void setPinchRadius(PointF index1, PointF thumb1) {
        this.index = index1;
        this.thumb = thumb1;
        Log.d(DEBUG_TAG, "Received: Index= "+ index.x +"," + index.y+"\nThumb="+ thumb.x +"," + thumb.y);
        Log.d(DEBUG_TAG, "Center: "+ center.x+","+center.y);

        if(!pinched) {
            pinched = true;
            Log.d(DEBUG_TAG, "Initialize radii");
            center.set((index.x+thumb.x)/2, (index.y+thumb.y)/2);
            radius = (float)(getDistance(center, index));
            initRadii(radius);
            resume();
        }
        else {
            int index_status = getPointCircleRel(index, center, radiiExpand[1], radiiExpand[0]);
            int thumb_status = getPointCircleRel(thumb, center, radiiExpand[1], radiiExpand[0]);
            if (index_status == 3 && thumb_status == 3) {
                if(radii_temp[1] < ((radii[1] + radiiExpand[1])/2)) {
                    radii_temp[1]+=10;
                }
            }
            else if (index_status == 2 && thumb_status == 2) {
                    Toast.makeText(getContext(),"STRIKE",Toast.LENGTH_LONG).show();
            }
        }
    }

   // @Override debugging
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

            canvas = holder.lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            holder.unlockCanvasAndPost(canvas);
        }

    }
}
