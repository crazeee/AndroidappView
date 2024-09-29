package com.unity.callback;

import android.graphics.SurfaceTexture;
import android.hardware.display.VirtualDisplay;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.Surface;

//import com.unity.ar.MainActivity;
import com.unity.ar.ReflectUtil;

import java.util.HashMap;

public class UnityInterface {

//    public static int sTextureId=0;
//    public static int sBackupTextureId=0;

    public static MotionEvent sDown;
    public static MotionEvent sMove;
    public static MotionEvent sUp;
    public static MotionEvent sCancel;

    public static UnityCallback sUnityCallback;

    private int mWidth= 1080;
    private int mHeight = 1920;



    public static class AppStruct{
        public String appName ="";
        public int textureId=-1;
        public int backupTextureId=-1;
        public int displayId = -1;
        public int frameBufferId = -1;
        public VirtualDisplay display =null;
        public Surface surface = null;
        public SurfaceTexture surfaceTexture = null;
        public UnityAppStatus mUnityAppStatus= UnityAppStatus.E_UNKNOWN;
    }


    public static interface UnityCallback{
        void initShareContext();
        void onRegistApp(String appName);
        void onUnRegisterApp(String appName);
        AppStruct getApp(String appName);
        boolean isExternalAppExist(String appName);
        int UpdateAppStatus(String appName,int status);
        String getAppIcon(String appName);
    }



    public int getTextureId(String appName){
        //onUnityRenderCallBack();
        Log.e("feng","getTextureId appName ="+appName);
        AppStruct app = sUnityCallback.getApp(appName);
        if(app != null){
            Log.e("feng","getTextureId backupTextureId ="+app.backupTextureId);
            return app.backupTextureId;
        }
        return -1;
    }


    public void onUnityRenderCallBack(){
        Log.e("feng","onUnityRenderCallBack threadname ="+Thread.currentThread().getName()+",id="+Thread.currentThread().getId());
        sUnityCallback.initShareContext();
    }


    public void RegisterExternalApp(String appName){
        Log.e("feng","RegisterExternalApp appName ="+appName);
        AppStruct app = sUnityCallback.getApp(appName);
        if(app != null){
            return;
        }
        sUnityCallback.onRegistApp(appName);
    }

    public boolean IsExternalAppExist(String appName)
    {
        return sUnityCallback.isExternalAppExist(appName);
    }

    public void UnregisterExternalApp(String appName){
        Log.e("feng","UnregisterExternalApp appName ="+appName);
        sUnityCallback.onUnRegisterApp(appName);
    }

    public void UpdateAppTouchInfo(String appName, int event ,float coordU, float coordV,float startcoordU, float startcoordV){
        try{
            long when = SystemClock.uptimeMillis();
            MotionEvent e=null;

            if(event == UnityTouchStatus.E_ACTION_DOWN.ordinal()){
                e= sDown;
            }else if(event == UnityTouchStatus.E_ACTION_UP.ordinal()){
                e= sUp;
            }else if(event == UnityTouchStatus.E_ACTION_MOVE.ordinal()){
                e= sMove;
            }else if(event == UnityTouchStatus.E_ACTION_CANCEL.ordinal()){
                e= sCancel;
            }
            Log.e("feng","MotionEvent = "+e);
            if(e == null){
                return;
            }
            Log.e("feng","when = "+e+"---"+coordU);
            e.setLocation(coordU*mWidth,coordV*mHeight);

            //e.setDisplayId(mDisplay.getDisplay().getDisplayId());
            AppStruct app = sUnityCallback.getApp(appName);
            if(app == null){
                return;
            }

            Log.e("feng","setDisplayId = "+app.displayId+"---"+appName);
            ReflectUtil.callMethod(e,"setDisplayId",app.displayId);
            e.setSource(InputDevice.SOURCE_TOUCHSCREEN);
            ReflectUtil.callMethod(e,"setDownTime",when);
            //e.setDownTime(when);
            //final InputManager im = InputManager.getInstance();
            InputManager im = (InputManager)ReflectUtil.callStaticMethodR(InputManager.class,"getInstance");
            //InputManager im = (InputManager)getSystemService(Context.INPUT_SERVICE);
            ReflectUtil.callMethod(im,"injectInputEvent",e,0);
            //im.injectInputEvent(e, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        }catch (Exception e){

        }
    }



    public int UpdateAppStatus(String appName, int status){
        return  sUnityCallback.UpdateAppStatus(appName,status);
    }

    public void UpdateAppRotation(String appName, int rotation){

    }

    public String AcquireAppIconURL(String appName){
        return sUnityCallback.getAppIcon(appName);
    }

    public enum UnityTouchStatus
    {
        E_ACTION_DOWN,
        E_ACTION_UP,
        E_ACTION_MOVE,
        E_ACTION_CANCEL,
        E_UNKNOWN;
    };


    public enum UnityAppStatus
    {
        E_OFF,
        E_ACTIVE,
        E_UNKNOWN;
    };



}
