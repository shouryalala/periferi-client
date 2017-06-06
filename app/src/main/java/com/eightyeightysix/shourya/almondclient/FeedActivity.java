package com.eightyeightysix.shourya.almondclient;

import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class FeedActivity extends BaseActivity {
    ZonePinchSurfaceView pinchView;
    private static final String DEBUG_TAG = "AlmondLog::" + FeedActivity.class.getSimpleName();



    interface pinchListener{
        void setPinchRadius(PointF index, PointF thumb);
        void exitPinch();
        //void debug(PointF index);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getScreenCenter();    //temp

        pinchView = (ZonePinchSurfaceView) findViewById(R.id.pinchView);
        pinchView.setZOrderOnTop(true);

        primary = new PointF(INVALID_POINTER,INVALID_POINTER);
        secondary = new PointF(INVALID_POINTER,INVALID_POINTER);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //pinchInit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void pinchInit() {
        pinchView.resume();
    }

    public void pinchExit() {
        pinchView.pause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //pinchExit();
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
                primary.set(event.getRawX(), event.getRawY());
                //pinchView.debug(primary);
                mDiaPrimary = event.getPointerId(index);
                primary.set(event.getX(index), event.getY(index));
                Log.d(DEBUG_TAG, "ACTION_DOWN: " + primary.x + "," + primary.y);
                return true;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                if(event.getPointerCount() < 3) {
                    mDiaSecondary = event.getPointerId(index);
                    secondary.set(event.getX(index), event.getY(index));
                    Log.d(DEBUG_TAG, "ACTION_POINTER_DOWN: \nPRIMARY: " + primary.x + "," + primary.y + "\nSECONDARY: " + secondary.x + "," + secondary.y);
                    pinchView.setPinchRadius(primary, secondary);
                }
                return true;
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
                }
                return true;
            }
            case MotionEvent.ACTION_POINTER_UP: {
                int id = event.getPointerId(index);
                if(id == mDiaSecondary || id == mDiaPrimary) {
                    refresh();
                    pinchView.exitPinch();
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                //int id = event.getPointerId(index);
                refresh();
                pinchView.exitPinch();
                return true;
            }
            default: return super.onTouchEvent(event);
        }
    }

}
