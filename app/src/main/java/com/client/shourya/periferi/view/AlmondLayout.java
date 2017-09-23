package com.client.shourya.periferi.view;

import android.content.Context;
import android.graphics.PointF;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.client.shourya.periferi.FeedActivity;
import com.client.shourya.periferi.R;
import com.client.shourya.periferi.gestureui.ZonePinchSurfaceView;

/*
 * Created by shourya on 8/7/17.
 */

public class AlmondLayout extends CoordinatorLayout {
    public static final int INVALID_POINTER = -1;
    public static final double MIN_DISTANCE_BW_FINGERS = 300000;
    private static int mDiaPrimary = INVALID_POINTER;
    private static int mDiaSecondary = INVALID_POINTER;
    //public static Point SCREEN_CENTER;
    //public static int SCREEN_WIDTH;
    //public static int SCREEN_HEIGHT;
    private static ZonePinchSurfaceView pinchView;

    private static PointF primary;
    private static PointF secondary;


    private static final String DEBUG_TAG = "AlmondGestureLog:: " + AlmondLayout.class.getSimpleName();
    public AlmondLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //gestureInit();
    }

    public interface pinchListener{
        void setPinchRadius(PointF index, PointF thumb);
        void exitPinch();
        //void debug(PointF index);
    }


    public void gestureInit() {
        //getScreenCenter();    //temp
        pinchView = (ZonePinchSurfaceView) findViewById(R.id.pinchView);
        pinchView.setZOrderOnTop(true);

        primary = new PointF(INVALID_POINTER,INVALID_POINTER);
        secondary = new PointF(INVALID_POINTER,INVALID_POINTER);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //Log.d(DEBUG_TAG, "Touch Intercepted");
        return onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        int index = MotionEventCompat.getActionIndex(event);

        Log.d(DEBUG_TAG,"The action is " + actionToString(action));
        Log.d(DEBUG_TAG,"The index is " + index);
        Log.d(DEBUG_TAG,"The Pointer ID is " + event.getPointerId(index));

        switch(action) {
            case MotionEvent.ACTION_DOWN: {
                mDiaPrimary = event.getPointerId(index);
                primary.set(event.getX(index), event.getY(index));
                Log.d(DEBUG_TAG, "ACTION_DOWN: " + primary.x + "," + primary.y);
                //return false;       //let child handle further
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                //Log.d(DEBUG_TAG, "Distance between fingers: " + minDistanceApart(primary,event.getX(index),event.getY(index)));
                if(event.getPointerCount() < 3 && minDistanceApart(primary, event.getX(index),event.getY(index))) {
                    if(FeedActivity.pinchTourActive) {
                        FeedActivity.exitPinchTour();
                    }
                    mDiaSecondary = event.getPointerId(index);
                    secondary.set(event.getX(index), event.getY(index));
                    //Log.d(DEBUG_TAG, "ACTION_POINTER_DOWN: \nPRIMARY: " + primary.x + "," + primary.y + "\nSECONDARY: " + secondary.x + "," + secondary.y);
                    if(mDiaPrimary != INVALID_POINTER)
                        pinchView.setPinchRadius(primary, secondary);
                }
                //return true;    //more than one pointer, surface view on duty
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if(mDiaPrimary != INVALID_POINTER) {
                    int priIndex = event.findPointerIndex(mDiaPrimary);
                    primary.set(event.getX(priIndex), event.getY(priIndex));
                    if (event.getPointerCount() > 1 && mDiaSecondary != INVALID_POINTER) {
                        int secIndex = event.findPointerIndex(mDiaSecondary);
                        secondary.set(event.getX(secIndex), event.getY(secIndex));
                        pinchView.setPinchRadius(primary, secondary);
                    }
                    Log.d(DEBUG_TAG, "ACTION_MOVE: \nPRIMARY: " + primary.x + "," + primary.y + "\nSECONDARY: " + secondary.x + "," + secondary.y);
                    //return true;
                }
                else{
                    //return false;   //only one pointer, handle pager
                }
                break;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int id = event.getPointerId(index);
                if(id == mDiaSecondary || id == mDiaPrimary) {
                    refreshGesture();
                    pinchView.exitPinch();
                }
                //return true;
                break;
            }
            case MotionEvent.ACTION_UP: {
                //int id = event.getPointerId(index);
                if(mDiaSecondary != INVALID_POINTER) {
                    refreshGesture();
                    pinchView.exitPinch();
                   // return true;
                }
                else {
                    //return false;
                }
                break;
            }
            default:// return false;
        }
        return (event.getPointerCount() > 1);

    }

    //////gesture_pinch
   /* public void getScreenCenter() {
        //TODO not center!
        Display display = getWindowManager().getDefaultDisplay();
        SCREEN_CENTER = new Point();
        display.getSize(SCREEN_CENTER);
        SCREEN_CENTER.x /= 2;
        SCREEN_CENTER.y /= 2;
        Log.d(DEBUG_TAG, "Screen Dimensions = " + SCREEN_CENTER.x + "," + SCREEN_CENTER.y);
    }*/

    public static String actionToString(int action) {
        switch (action) {

            case MotionEvent.ACTION_DOWN: return "Down";
            case MotionEvent.ACTION_MOVE: return "Move";
            case MotionEvent.ACTION_POINTER_DOWN: return "Pointer Down";
            case MotionEvent.ACTION_UP: return "Up";
            case MotionEvent.ACTION_POINTER_UP: return "Pointer Up";
            case MotionEvent.ACTION_OUTSIDE: return "Outside";
            case MotionEvent.ACTION_CANCEL: return "Cancel";
        }
        return "";
    }

    public void refreshGesture() {
        mDiaSecondary = INVALID_POINTER;
        mDiaPrimary = INVALID_POINTER;
        primary.set(INVALID_POINTER, INVALID_POINTER);
        secondary.set(INVALID_POINTER, INVALID_POINTER);
    }

    private boolean minDistanceApart(PointF mPnt, float mX, float mY) {
        return ((Math.pow((mPnt.x-mX),2) + Math.pow((mPnt.y-mY),2)) > MIN_DISTANCE_BW_FINGERS);
    }
}
