package com.eightyeightysix.shourya.almondclient.gestureui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.eightyeightysix.shourya.almondclient.BaseActivity;
import com.eightyeightysix.shourya.almondclient.FeedActivity;
import com.eightyeightysix.shourya.almondclient.R;
import com.eightyeightysix.shourya.almondclient.view.AlmondLayout;

import java.util.HashMap;

/*
 * Created by shourya on 4/6/17.
 */

public class ZonePinchSurfaceView extends SurfaceView implements Runnable, AlmondLayout.pinchListener {
    Thread t = null;
    SurfaceHolder holder;
    Paint paintRed, paintBlue,paintTransparency;
    Paint paintText;
    Canvas canvas;
    Vibrator v;
//    boolean debug = false;

    private PointF index, thumb;
    private float radius;
    private PointF center;
    private static int zoneCount = 2;
    private static float radii[];
    private static float radii_display[];
    private static float radiiExpand[];
    private static float radii_expand_max[];
    private static int selected_zone;
    private static HashMap<Integer, String> zoneNames;
    private static final float textPosition = (float)1.41421;
    private boolean needVibration = false;

    boolean pinched = false;
    boolean isRunning = false;

    private static final String DEBUG_TAG = "AlmondGestureLog:: " + ZonePinchSurfaceView.class.getSimpleName();
    //private Bitmap bmpIcon;

    public ZonePinchSurfaceView(Context context) {
        super(context);
        pinchInit();
    }

    public ZonePinchSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        holder = getHolder();
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        pinchInit();
    }

    public ZonePinchSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        holder = getHolder();
        v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        pinchInit();
    }

    private void pinchInit() {
        holder = getHolder();
        holder.setFormat(PixelFormat.TRANSPARENT);

        index = new PointF(-1,-1);
        thumb = new PointF(-1,-1);
        center = new PointF(-1,-1);

        paintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintRed.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimarySilverDark));
        paintRed.setStyle(Paint.Style.STROKE);
        paintRed.setStrokeWidth(8);

        paintTransparency = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTransparency.setColor(Color.WHITE);
        paintTransparency.setStyle(Paint.Style.FILL);
        paintTransparency.setAlpha(200);

        paintBlue = new Paint(Paint.ANTI_ALIAS_FLAG);
        //paintBlue.setColor(Color.BLUE);
        paintBlue.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryRedAccent));
        paintBlue.setStyle(Paint.Style.STROKE);
        paintBlue.setStrokeWidth(7);

        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        TextView txtview = new TextView(getContext());
        txtview.setTextAppearance(getContext(), android.R.style.TextAppearance_DeviceDefault_Large);
        paintText.setTextSize(txtview.getTextSize());
        paintText.setTypeface(txtview.getTypeface());
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryBlack));
        getZoneDetails();
    }

    private void getZoneDetails(){
        //TODO Import number of zones from server
        zoneCount = BaseActivity.fetchCurrentCircleCount();
        zoneNames = BaseActivity.getZoneNames();
    }

    private void initRadii(float head) {
        radii = new float[zoneCount];       //display radii of zones
        radiiExpand = new float[zoneCount - 1];//used to trigger animation
        radii_display = new float[zoneCount];  //used for animating the circle when selected
        radii_expand_max = new float[zoneCount];//defines threshold values for animation

        radii[zoneCount-1] = head;
        radii_expand_max[zoneCount - 1] = head + 50;
        radii_display[zoneCount - 1] = head;
        //radii_temp[zoneCount - 1] = radii[zoneCount - 1];
        for(int j=(zoneCount-2); j>=0; j--) {
            radii[j] = head * ((float)(j+1)/zoneCount);
            radii_display[j] = radii[j];
            radiiExpand[j] = radii[j] + (head/(2*zoneCount));
            //radii_temp[j] = radii[j];
            //TODO figure out appropriate values for different screens.
            radii_expand_max[j] = (radii[j] + radiiExpand[j])/2 - 30;
        }
        selected_zone = zoneCount-1;
//        Log.d(DEBUG_TAG, "radii:"+ radii[2] + "," + radii[1] + "," + radii[0]);
//        Log.d(DEBUG_TAG, "radii_temp:"+ radii_display[2] + "," + radii_display[1] + "," + radii_display[0]);
//        Log.d(DEBUG_TAG, "radii_expand_max:"+ radii_expand_max[2] + "," + radii_expand_max[1] + "," + radii_expand_max[0]);
    }

    @Override
    public void run() {
        while(isRunning) {
            if(!holder.getSurface().isValid()) {
                continue;
            }
            canvas = holder.lockCanvas();
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            canvas.drawRect(0,0,getRight(),getBottom(),paintTransparency);
            canvas.drawColor(Color.argb(75,255,255,255));
            for(int j=0; j< zoneCount; j++) {
                //Log.d(DEBUG_TAG, "Radii: "+radii[0] + "," + radii[1] + "," + radii[2]);
                canvas.drawCircle(center.x,center.y, radii_display[j], paintRed);
                if(j == selected_zone){
                    canvas.drawCircle(center.x, center.y, radii_display[j], paintBlue);
                }

                canvas.drawText(zoneNames.get(j), center.x, center.y-radii_display[j], paintText);
                /*if(j != zoneCount-1){
                    canvas.drawCircle(center.x,center.y, radiiExpand[j], paintBlue);
                }*/
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

    private static boolean isInBetween(float test, float fixBig, float fixSmall) {
        return (test <= fixBig && test > fixSmall);
    }

    private int getCurrentSelection(float dIndex, float dThumb) {
        if(dIndex > radiiExpand[zoneCount - 2] && dThumb > radiiExpand[zoneCount - 2]) {
            //point outside outermost circle
            return (zoneCount-1);
        }

        for (int i = (zoneCount - 2); i > 0; i--) {
            if (dIndex <= radiiExpand[i] && dIndex > radiiExpand[i-1]
                && dThumb <= radiiExpand[i] && dThumb > radiiExpand[i-1]) {
                //point between zone i+1 and i
                return i;
            }
        }

        if(dIndex <= radiiExpand[0] && dThumb <= radiiExpand[0]) {
            //in the innermost zone- settings
            return 0;
        }
        //Index point and thumb point are not on the same zone
        return -1;
    }

    private static double getDistance(PointF pin1, PointF pin2) {
        double sq = Math.pow((pin1.x - pin2.x),2) + Math.pow((pin1.y - pin2.y),2);
        return Math.sqrt(sq);
    }

    private void inflate_circle(int curr) {
        float max = (radiiExpand[curr] + radii[curr])/2;
    }

    @Override
    public void setPinchRadius(PointF index1, PointF thumb1) {
        this.index = index1;
        this.thumb = thumb1;
        Log.d(DEBUG_TAG, "Received: Index= "+ index.x +"," + index.y+"\nThumb="+ thumb.x +"," + thumb.y);
       // Log.d(DEBUG_TAG, "Center: "+ center.x+","+center.y);

        if(!pinched) {
            pinched = true;
            Log.d(DEBUG_TAG, "Initialize radii");
            center.set((index.x+thumb.x)/2, (index.y+thumb.y)/2);
            radius = (float)(getDistance(center, index));
            initRadii(radius);
            resume();
        }
        else {
            int new_selection = getCurrentSelection((float)getDistance(center,index), (float)getDistance(center,thumb));
            Log.d(DEBUG_TAG, "Prev: " + selected_zone + "\nCurrent Selection: " + new_selection);
            //If index and thumb don't point to the same zone, keep previous config
            if(new_selection == -1) new_selection = selected_zone;

            if(selected_zone != new_selection) {
                Log.d(DEBUG_TAG, "Current Expand Radius: " + radii_display[selected_zone] + "Current reqd radius" + radii[selected_zone]);
                if(radii_display[selected_zone] > radii[selected_zone])
                    {
                        Log.d(DEBUG_TAG, "Deflating");
                        radii_display[selected_zone] -= 25;
                    }
                else{
                    Log.d(DEBUG_TAG, "Deflated");
                    radii_display[selected_zone] = radii[selected_zone];
                    selected_zone = new_selection;
                    v.vibrate(50);
                }
            }
            Log.d(DEBUG_TAG, "Selected radii_expand_max" + radii_expand_max[new_selection] + "selected radii_display" + radii_display[new_selection]);
            if(radii_display[new_selection] < radii_expand_max[new_selection]){
                radii_display[new_selection] += 25;
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
        FeedActivity.refreshCircleContent(selected_zone);
    }
}
