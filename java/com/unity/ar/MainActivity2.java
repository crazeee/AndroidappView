package com.unity.ar;

import static android.hardware.display.DisplayManager.DISPLAY_CATEGORY_PRESENTATION;

import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.content.Intent;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity2 extends Activity {

    ViewGroup mRoot;

    int count = 0;

    public static final String ACTION_ORIENTATION_CHANGE ="ACTION_ORIENTATION_CHANGE";

    public static final String PACKAGE_FROM ="PACKAGE_FROM";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mRoot = (ViewGroup) findViewById(R.id.root);
        MyTextView textView = new MyTextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setText("我是界面二");
        textView.setTextSize(50);

        DisplayManager mDisplayManager = (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);

        Display[] displays = mDisplayManager.getDisplays();
        Log.e("feng","display size = "+displays.length);
        for (int i = 0;i<displays.length;i++){
            Log.e("feng","id = "+displays[i].getDisplayId());
        };
        Display[] ds = mDisplayManager.getDisplays(DISPLAY_CATEGORY_PRESENTATION);
        Log.e("feng","ds size = "+ds.length);
        for (int i = 0;i<ds.length;i++){
            Log.e("feng","id = "+displays[i]);
            Log.e("feng","----------------------------------------------------");
            Log.e("feng","id = "+displays[i].getDisplayId());
        };




        mRoot.postDelayed(new Runnable() {
            @Override
            public void run() {
                int orientation = ((count++)%2)==1? ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;
                //setRequestedOrientation(orientation);
                mRoot.postDelayed(this,5000);
            }
        },5000);

    }

    public boolean dispatchTouchEvent(MotionEvent event){
        long when = SystemClock.uptimeMillis();
        Log.w("feng1","MainActivity2 when = "+when+"---"+event.getDownTime()+"---"+event.getAction());
        return  super.dispatchTouchEvent(event);
    }






    @SuppressLint("AppCompatCustomView")
    public static class MyTextView extends TextView{

        public MyTextView(Context context) {
            super(context);
        }

        public MyTextView(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
        }

        public MyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public MyTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            return super.onTouchEvent(event);
        }
    }
}



