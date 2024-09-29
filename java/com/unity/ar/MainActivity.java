package com.unity.ar;

import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.input.InputManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.opengl.GLUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;


import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Presentation;
import android.content.ComponentName;
import android.graphics.SurfaceTexture;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;


import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.unity.callback.UnityInterface;
import com.unity3d.player.UnityPlayerActivity;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends UnityPlayerActivity implements SurfaceTexture.OnFrameAvailableListener,UnityInterface.UnityCallback{

    public static  final String TAG = "feng";
    //MySurfaceView surfaceView;
    DisplayManager mDisplayManager;

    private int mWidth;
    private int mHeight;

    //private LinearLayout mContainer1;
    //private TextureView mTextureView;
    private HandlerThread mHandlerThread;
    private Handler mHandler;


    private Handler mMainHandler=new Handler(Looper.getMainLooper());
    public HashMap<String,UnityInterface.AppStruct> mApps = new HashMap<String,UnityInterface.AppStruct>();

    //private  int mTextureId;

    //private int mUnityTextureId;


    private EGLConfig m_eglConfig = null;
    private EGLSurface m_eglSurface = EGL14.EGL_NO_SURFACE;
    private EGLDisplay m_eglDisplay = EGL14.EGL_NO_DISPLAY;
    private EGLContext m_eglContext = EGL14.EGL_NO_CONTEXT;



    public static final int MSG_WHAT_REGISTER_APP = 0x10000;
    public static final int MSG_WHAT_UNREGISTER_APP = 0x10001;
    public static final int MSG_WHAT_INIT_CONTEXT = 0x10002;


    //private SurfaceTexture mViewSurfaceTexture;
    private Bitmap mBitmap;


    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback(){
        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            //mSurface=holder.getSurface();
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
           // mSurface=holder.getSurface();
            mWidth = width;
            mHeight = height;
            //createVirtualAndShowPresentation();
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {


        }
    };

    public static MainActivity sInstance;


    public static MainActivity getInstance(){
        return sInstance;
    }




    private void createSharedContext() {
        m_eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (m_eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }
        int[] version = new int[2];
        if (!EGL14.eglInitialize(m_eglDisplay, version, 0, version, 1)) {
            m_eglDisplay = null;
            throw new RuntimeException("unable to initialize EGL14");
        }
        int[] configAttribs = {
                EGL14.EGL_BUFFER_SIZE, 32,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_SURFACE_TYPE, EGL14.EGL_WINDOW_BIT,
                EGL14.EGL_NONE
        };
        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(m_eglDisplay, configAttribs, 0, configs, 0, configs.length, numConfigs, 0)) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        m_eglConfig = configs[0];
        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };


        m_eglContext = EGL14.eglCreateContext(m_eglDisplay, m_eglConfig,mSharedEglContext, contextAttribs, 0);
        if (m_eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }

        int[] surfaceAttribList = {
                EGL14.EGL_WIDTH, mWidth,
                EGL14.EGL_HEIGHT, mHeight,
                EGL14.EGL_NONE
        };

        m_eglSurface = EGL14.eglCreatePbufferSurface(m_eglDisplay,m_eglConfig,surfaceAttribList,0);
        if(m_eglSurface == EGL14.EGL_NO_SURFACE){
            Log.e(TAG,"m_eglSurface is failed");
        }
        if(!EGL14.eglMakeCurrent(m_eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, m_eglContext)){
            Log.e(TAG,"eglMakeCurrent failed!");
        }

        Log.e(TAG,"createGL is end");

    }



    public void updateSurface(){
        if(m_eglSurface!=null)return;
        int[] contextAttribs = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                EGL14.EGL_NONE
        };

//        m_eglSurface = EGL14
//                .eglCreateWindowSurface(m_eglDisplay, m_eglConfig,mSurface,contextAttribs, 0);
        EGL14.eglMakeCurrent(m_eglDisplay, m_eglSurface, m_eglSurface, m_eglContext);


    }


    private volatile EGLContext mSharedEglContext =EGL14.EGL_NO_CONTEXT;
    private volatile EGLConfig mSharedEglConfig;


    public static boolean issFlag=true;

    public void initShareContext(){

        if(issFlag){
            issFlag=false;
        }else{
            return;
        }
        mSharedEglContext = EGL14.eglGetCurrentContext();
        if (mSharedEglContext == EGL14.EGL_NO_CONTEXT) {
            Log.e(TAG,"eglGetCurrentContext failed");
            return;
        }
        EGLDisplay sharedEglDisplay = EGL14.eglGetCurrentDisplay();
        if (sharedEglDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.e(TAG,"sharedEglDisplay failed");
            return;
        }
        // 获取Unity绘制线程的EGLConfig
        int[] numEglConfigs = new int[1];
        EGLConfig[] eglConfigs = new EGLConfig[1];
        if (!EGL14.eglGetConfigs(sharedEglDisplay, eglConfigs, 0, eglConfigs.length,numEglConfigs, 0)) {
            return;
        }
        mSharedEglConfig = eglConfigs[0];
        mHandler.sendEmptyMessage(MSG_WHAT_INIT_CONTEXT);

    }

    private void saveRgb2Bitmap(Buffer buf, String filename, int width, int height,int textureId) {
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filename));
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            long t = System.currentTimeMillis();
            bmp.copyPixelsFromBuffer(buf);
            Log.e(TAG,"time cost = "+(System.currentTimeMillis() -t));

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            //GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            //GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
            bmp.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void blitTexture(UnityInterface.AppStruct appStruct){
//        Log.e(TAG, "blitTexture1111111111111");
//        GLES31.glBindTexture( GLES30.GL_TEXTURE_2D, UnityInterface.sTextureId);
//        checkGLError();
//        GLES31.glBindTexture( GLES30.GL_TEXTURE_2D, 0);
//        GLES31.glEnable(GLES30.GL_TEXTURE_2D);
//        GLES31.glBindTexture( GLES30.GL_TEXTURE_2D, UnityInterface.sTextureId);
//        checkGLError();
//        GLES31.glBindTexture( GLES30.GL_TEXTURE_2D, 0);
//        GLES31.glEnable(mTargetTexture);
//        GLES31.glBindTexture( GLES30.GL_TEXTURE_2D, UnityInterface.sTextureId);
//        checkGLError();
//        GLES31.glBindTexture( GLES30.GL_TEXTURE_2D, 0);
        checkGLError();
        Log.e(TAG, "blitTexture");
        int[] framebufferIds = new int[2];
        GLES31.glGenFramebuffers(2, framebufferIds, 0);
        checkGLError();
        Log.e(TAG,"-------");
        int framebufferId = framebufferIds[0];
        int framebufferId1 = framebufferIds[1];

        // 将纹理对象绑定到帧缓冲的颜色附件
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, framebufferId);
        //GLES31.glBindTexture( mTargetTexture, appStruct.textureId);
        checkGLError();
        Log.e(TAG,"---");
        GLES31.glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0, mTargetTexture, appStruct.textureId, 0);
        checkGLError();
        Log.e(TAG,"111");


  //   int targetTexture = GLES31.GL_TEXTURE_2D;

        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, framebufferId1);
        GLES31.glBindTexture(mBackupTargetTexture, appStruct.backupTextureId);
        GLES31.glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0, mBackupTargetTexture, appStruct.backupTextureId, 0);
        checkGLError();
        Log.e(TAG,"222");
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0);


        GLES31.glBindFramebuffer(GLES31.GL_READ_FRAMEBUFFER, framebufferId);
        GLES31.glBindFramebuffer(GLES31.GL_DRAW_FRAMEBUFFER, framebufferId1);
        GLES31.glBlitFramebuffer(0,0,mWidth,mHeight,0,0,mWidth,mHeight,GLES31.GL_COLOR_BUFFER_BIT, GLES31.GL_NEAREST);
        checkGLError();
        Log.e(TAG,"333");

        int status =GLES31.glCheckFramebufferStatus(GLES31.GL_READ_FRAMEBUFFER);
        if ( status!= GLES31.GL_FRAMEBUFFER_COMPLETE){
            Log.e(TAG, "READ Framebuffer is not complete"+status);
        }

        GLES31.glBindFramebuffer(GLES31.GL_READ_FRAMEBUFFER, 0);
        GLES31.glBindFramebuffer(GLES31.GL_DRAW_FRAMEBUFFER, 0);

    }

    int mTargetTexture = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

    //int mTargetTexture = GLES20.GL_TEXTURE_2D;
    int mBackupTargetTexture = GLES20.GL_TEXTURE_2D;
    //int mBackupTargetTexture = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

    // 在需要获取纹理像素数据的地方调用方法
    private void getTexturePixelData(int textureId) {
        Log.e("feng","getTexturePixelData");
        // 创建一个帧缓冲对象
        int[] framebufferIds = new int[1];
        GLES31.glGenFramebuffers(1, framebufferIds, 0);
        int framebufferId = framebufferIds[0];
        //int framebufferId1 = framebufferIds[0];

        // 将纹理对象绑定到帧缓冲的颜色附件
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, framebufferId);
        GLES31.glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0, mTargetTexture, textureId, 0);
        //GLES31.glBindFramebuffer(GLES31.GL_DRAW_FRAMEBUFFER, framebufferId1);
        //GLES31.glFramebufferTexture2D(GLES31.GL_DRAW_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, mUnityTextureId, 0);
        //GLES31.glBlitFramebuffer(0,0,mWidth,mHeight,0,0,mWidth,mHeight,GLES31.GL_COLOR_BUFFER_BIT, GLES31.GL_NEAREST);
        //GLES31.glFinish();
        //

        // 检查帧缓冲是否完整
        int status = GLES31.glCheckFramebufferStatus(GLES31.GL_FRAMEBUFFER);
        if (status != GLES31.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "Framebuffer is not complete");
            return;
        }

        readPixelData(textureId);
        // 解绑帧缓冲对象和纹理对象
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0);
        //GLES31.glBindFramebuffer(GLES31.GL_DRAW_FRAMEBUFFER, 0);
    }






    public void readPixelData(int textureId){
        // 从帧缓冲区读取像素数据
        ByteBuffer pixels = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        GLES31.glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, pixels);

        // 处理像素数据
        String path = getExternalCacheDir()+"/"+System.currentTimeMillis()+".png";
        saveRgb2Bitmap(pixels,path,mWidth,mHeight,textureId);
        print(pixels);
    }





    private void getdata(){

        // 创建一个纹理对象
        int[] textureIds = new int[1];
        GLES31.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];

// 将纹理对象绑定到纹理目标
        //GLES31.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

// 设置纹理参数
        GLES31.glTexParameteri(mTargetTexture, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR);
        GLES31.glTexParameteri(mTargetTexture, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR);
        GLES31.glTexParameteri(mTargetTexture, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_CLAMP_TO_EDGE);
        GLES31.glTexParameteri(mTargetTexture, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_CLAMP_TO_EDGE);

// 创建一个SurfaceTexture对象，并将其与纹理目标关联
        //mSurfaceTexture = new SurfaceTexture(textureId);
        //mSurfaceTexture.setDefaultBufferSize(mWidth, mHeight);
        //mSurfaceTexture.detachFromGLContext();
        //mSurfaceTexture.attachToGLContext(UnityInterface.sTextureId);

//        mMainHandler.post(new Runnable() {
//            @Override
//            public void run() {
//
//                mTextureView.setSurfaceTexture(mSurfaceTexture);
//            }
//        });

// 获取SurfaceTexture的转换矩阵
        float[] transformMatrix = new float[16];
        //mSurfaceTexture.getTransformMatrix(transformMatrix);

// 更新纹理图像
        //mSurfaceTexture.updateTexImage();
// 创建一个帧缓冲对象
        int[] framebufferIds = new int[1];
        GLES31.glGenFramebuffers(1, framebufferIds, 0);
        int framebufferId = framebufferIds[0];

// 将纹理对象绑定到帧缓冲的颜色附件
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, framebufferId);
        GLES31.glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0, mTargetTexture, textureId, 0);

// 检查帧缓冲是否完整
        int status = GLES31.glCheckFramebufferStatus(GLES31.GL_FRAMEBUFFER);
        if (status != GLES31.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "Framebuffer is not complete");
            return;
        }

// 从帧缓冲区读取像素数据
        ByteBuffer pixels = ByteBuffer.allocateDirect(mWidth* mHeight * 4);
        GLES31.glReadPixels(0, 0, mWidth, mHeight, GLES31.GL_RGBA, GLES31.GL_UNSIGNED_BYTE, pixels);

// 解绑帧缓冲和纹理对象
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0);
        GLES31.glBindTexture(mTargetTexture, 0);

// 删除帧缓冲对象和纹理对象
        GLES31.glDeleteFramebuffers(1, framebufferIds, 0);
        GLES31.glDeleteTextures(1, textureIds, 0);
    }



    private  void initdata(SurfaceTexture surfaceTexture){
        // 获取纹理数据
        //float[] transformMatrix = new float[16];
        //surfaceTexture.getTransformMatrix(transformMatrix);
        int format = GLES20.GL_RGBA;
        int type = GLES20.GL_UNSIGNED_BYTE;


        // 创建一个帧缓冲对象
        int[] framebufferIds = new int[1];
        GLES20.glGenFramebuffers(1, framebufferIds, 0);
        int framebufferId = framebufferIds[0];

// 将纹理对象绑定到帧缓冲的颜色附件
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, 1, 0);

// 检查帧缓冲是否完整
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "Framebuffer is not complete");
            return;
        }


        ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(mWidth * mHeight * 4);
        pixelBuffer.order(ByteOrder.nativeOrder());
        //GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 1);

        GLES20.glReadPixels(0, 0, mWidth, mHeight, format, type, pixelBuffer);

        // 解绑帧缓冲和纹理对象
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, 0);
        GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, 0);

// 删除帧缓冲对象和纹理对象
        GLES31.glDeleteFramebuffers(1, framebufferIds, 0);
        GLES31.glDeleteTextures(1, new int[]{1}, 0);


    }


    StringBuilder mLastStrB=new StringBuilder();
    StringBuilder mNowStrB=new StringBuilder();



    private void print(ByteBuffer pixelBuffer){
        byte[] bytes = pixelBuffer.array();
        Log.e("feng","texture length = "+bytes.length);
        mNowStrB.delete(0,mLastStrB.length());

        int c = 0;
        int last =0;
        int count = 0;
        for(int i =0;i<bytes.length-7;i+=4){
            int[] values=new int[4];
            values[0] = bytes[i+3]&0xFF;
            values[1] = bytes[i]&0xFF;
            values[2] = bytes[i+1]&0xFF;
            values[3] = bytes[i+2]&0xFF;

            //Log.e("feng","value= "+values[0]+","+values[1]+","+values[2]+","+values[3]);
            int color = Color.argb(values[0],values[1],values[2],values[3]);
            c= color &0xffffffff;

            if(last!=c){
                last = c;
                count++;

                mNowStrB.append(Integer.toHexString(c));
                mNowStrB.append(",");
            }


        }
        Log.e("feng","count = "+count);
        if(!mLastStrB.toString().equals(mNowStrB.toString())){
            mLastStrB.delete(0,mLastStrB.length());
            mLastStrB.append(mNowStrB.toString());
            Log.e("feng",mNowStrB.toString());
        }

    }



    private void unInit(String appName){
        if(TextUtils.isEmpty(appName)){
            Log.e(TAG,"appName is null");
            return;
        }
        UnityInterface.AppStruct app = mApps.remove(appName);
        if(app == null ){
            Log.e(TAG,appName + "is null");
            return;
        }
        int[] textures = {app.textureId,app.backupTextureId};
        GLES30.glDeleteTextures(2,textures,0);
        app.display.release();
        app.surfaceTexture = null;
        app.surface = null;
        app.displayId = -1;
        app.textureId =-1;
        app.backupTextureId=-1;
    }



    private void initOpenGL(String appName) {
        // 创建 OpenGL 渲染环境，设置上下文、加载着色器程序等


        // 绑定纹理对象
        //GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        // 将纹理对象绑定到纹理目标

// 将纹理对象绑定到纹理目标
        //GLES31.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        UnityInterface.AppStruct app = mApps.get(appName);
        // 生成一个纹理 ID
        int[] textureIds = new int[2];
        GLES30.glGenTextures(2, textureIds, 0);
        app.textureId = textureIds[0];
        app.backupTextureId = textureIds[1];



        Log.e("feng","textureid = "+app.textureId+",backup = "+app.backupTextureId);

        //int textureTarget = GLES30.GL_TEXTURE_2D;


        GLES31.glBindTexture(mBackupTargetTexture , app.backupTextureId);
        //ByteBuffer byteBuffer= ByteBuffer.allocateDirect(mWidth*mHeight*4);
        GLES31.glTexImage2D(mBackupTargetTexture,0,GLES30.GL_RGBA,mWidth,mHeight,0,GLES30.GL_RGBA,GLES30.GL_UNSIGNED_BYTE,null);
        GLES31.glBindTexture(mBackupTargetTexture, 0);
        checkGLError();







        //mViewSurfaceTexture = new SurfaceTexture(UnityInterface.sTextureId);
//        GLES31.glActiveTexture(GLES31.GL_TEXTURE1);
//        GLES31.glTexParameteri(mTargetTexture, GLES31.GL_TEXTURE_MIN_FILTER, GLES31.GL_LINEAR);
//        GLES31.glTexParameteri(mTargetTexture, GLES31.GL_TEXTURE_MAG_FILTER, GLES31.GL_LINEAR);
//        GLES31.glTexParameteri(mTargetTexture, GLES31.GL_TEXTURE_WRAP_S, GLES31.GL_CLAMP_TO_EDGE);
//        GLES31.glTexParameteri(mTargetTexture, GLES31.GL_TEXTURE_WRAP_T, GLES31.GL_CLAMP_TO_EDGE);
//        GLES31.glBindTexture(mTargetTexture, UnityInterface.sTextureId);
        //GLES31.glBindTexture(GLES30.GL_TEXTURE_2D, mUnityTextureId);

        //initTexture();

//        mMainHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                initTextureView();
//            }
//        });
        //mSurface=new Surface(mSurfaceTexture);
        createVirtualAndShowPresentation(app);

        PackageUtil.getAppIcon(getApplicationContext(),appName);

        // 将纹理对象与 SurfaceTexture 绑定
        //mSurfaceTexture.attachToGLContext(textureId);

        // 开始渲染或处理帧数据
        //mSurfaceTexture.updateTexImage();
        //initdata(mSurfaceTexture);
    }


    public void checkGLError(){
        int error = GLES30.glGetError();
        if(error != GLES30.GL_NO_ERROR){
            Log.e(TAG,"gl error = "+error);
        }
    }


    public void initTexture() {
        //Log.d(TAG,"updateTexture called by unity");
        mHandler.post(new Runnable() { //java线程内
            @Override
            public void run() {
                //String imageFilePath = getExternalCacheDir().toString()+"/1.png"; //图片路径
                //final Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath);

                if(mBitmap==null){
                    Bitmap.Config config = Bitmap.Config.ARGB_8888;
                    mBitmap = Bitmap.createBitmap(mWidth,mHeight,config);
                    //GLUtils.texImage2D(UnityInterface.sTextureId, 0, mBitmap, 0);
                    checkGLError();
                }

//                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//                GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
//                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
//                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);

                //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            }
        });
    }



    public void createVirtualAndShowPresentation(UnityInterface.AppStruct appStruct) {
        Log.e("feng","createVirtualAndShowPresentation thread id = "+Thread.currentThread().getId());




        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;

        SurfaceTexture surfaceTexture = new SurfaceTexture(appStruct.textureId);
        surfaceTexture.setOnFrameAvailableListener(this);
        surfaceTexture.setDefaultBufferSize(mWidth,mHeight);

        //surfaceTexture.detachFromGLContext();
        //surfaceTexture.attachToGLContext(appStruct.textureId);

        Surface surface = new Surface(surfaceTexture);
        Log.e("feng","mSurface isvalid = "+surface.isValid());

        VirtualDisplay display = mDisplayManager.createVirtualDisplay("VirtualDisplay",mWidth, mHeight, 240, surface,flags);

        int displayId = display.getDisplay().getDisplayId();
        Log.e("feng",mWidth+","+mHeight+",mDisplayid ="+displayId);


        //options.setLaunchBounds(new Rect(0,0,600,800));

        //options.setLaunchWindowingMode(WINDOWING_MODE_MULTI_WINDOW);



//        Intent secondIntent = new Intent();
//        ComponentName cn= new ComponentName("com.android.launcher3","com.android.launcher3.uioverrides.QuickstepLauncher");
//        secondIntent.setComponent(cn);
        //ComponentName cn= new ComponentName("com.example.myapplication","com.example.myapplication.MainActivity2");
        appStruct.display = display;
        appStruct.displayId = displayId;
        appStruct.surfaceTexture = surfaceTexture;
        appStruct.surface = surface;



        //secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);


//        Presentation presentation = new Presentation(this, mDisplay.getDisplay());
//        presentation.getWindow().
//                setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//        presentation.show();

    }



    public void initSurfaceView(){
//        surfaceView = new MySurfaceView(this);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
//        mContainer1.addView(surfaceView,params);
//        surfaceView.getHolder().addCallback(mSurfaceCallback);

    }


    private void initTextureView(){
        //mTextureView = (TextureView) findViewById(R.id.texture_view);
//        mTextureView = new TextureView(MainActivity.this);
//        mTextureView.setSurfaceTexture(mViewSurfaceTexture);
//        mViewSurfaceTexture.detachFromGLContext();
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
//        mContainer1.addView(mTextureView,params);
//        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
//            @Override
//            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
//                Log.e("feng","onSurfaceTextureAvailable ="+(surfaceTexture==mViewSurfaceTexture));
//                mMainHandler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mTextureView.setVisibility(View.INVISIBLE);
//                        mTextureView.setVisibility(View.VISIBLE);
//                    }
//                });
//
//                // 创建 SurfaceTexture 对象并绑定到 TextureView 上
//                //mSurfaceTexture = surfaceTexture;
//                //mSurfaceTexture.setDefaultBufferSize(width,height);
//                // 设置 SurfaceTexture 的帧可用监听器
//                //mSurfaceTexture.setOnFrameAvailableListener(MainActivity.this);
//
//            }
//
//            @Override
//            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
//                // SurfaceTexture 的大小发生变化
//                Log.e("feng","onSurfaceTextureSizeChanged");
//            }
//
//            @Override
//            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
//                Log.e("feng","onSurfaceTextureDestroyed");
//                // 销毁 SurfaceTexture 对象
//                //mSurfaceTexture = null;
//                return false;
//            }
//
//            @Override
//            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
//                // SurfaceTexture 更新
//                Log.e(TAG,"onSurfaceTextureUpdated------"+(surfaceTexture==mViewSurfaceTexture));
//                if(surfaceTexture==mViewSurfaceTexture){
//
//                }
//
//            }
//        });
    }

    int count=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        UnityInterface.sUnityCallback = this;
        mDisplayManager = (DisplayManager)getSystemService(Context.DISPLAY_SERVICE);
        mWidth = 1080;
        mHeight = 1920;




        PackageManager packageManager = getPackageManager();
        // 使用 hasSystemFeature 方法检查是否支持副屏活动
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS)) {
            // 设备支持副屏活动
            // 在这里进行相关操作
            Log.e("feng","Support FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS");
        } else {
            // 设备不支持副屏活动
            // 在这里进行相应的处理
            Log.e("feng","Don't support FEATURE_ACTIVITIES_ON_SECONDARY_DISPLAYS");
        }


        sInstance =this;

        //CallNative.callNative();


        mHandlerThread = new HandlerThread("RenderThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                String appName="";
                switch (msg.what){
                    case MSG_WHAT_REGISTER_APP:
                        appName = (String)msg.obj;
                        initOpenGL(appName);
                        break;
                    case MSG_WHAT_UNREGISTER_APP:
                        appName = (String)msg.obj;
                        unInit(appName);
                        break;
                    case MSG_WHAT_INIT_CONTEXT:
                        createSharedContext();
                        break;
                }

            }
        };

//        mContainer1 = (LinearLayout)findViewById(R.id.container1);
//        mUnityPlayer =new UnityPlayer(this);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
//        mContainer1.addView(mUnityPlayer,params);
//        mUnityPlayer.resume();
        //initTextureView();
        //initOpenGL();
        // 初始化 OpenGL 渲染环境并进行渲染或处理帧数据




        //initSurfaceView();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            SurfaceTexture surfaceTexture =new SurfaceTexture(0);
//            createVirtualAndShowPresentation(surfaceTexture);
//        }


//
//        if(supportsMultiDisplay){
//            ActivityOptions options = ActivityOptions.makeBasic();
//            options.setLaunchDisplayId(2); //这里一直display0是第一块屏；display1是第二块屏
//
//            Intent secondIntent = new Intent();
//            ComponentName cn= new ComponentName("com.example.myapplication","com.example.myapplication.SecondActivity");
//            secondIntent.setComponent(cn);
//
//            //该句很重要，不添加则无法推送到副屏
//            secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
//            startActivity(secondIntent, options.toBundle());
//        }




    }

    static  boolean sFlag = true;

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.e("feng","onFrameAvailable thread id = "+Thread.currentThread().getId());
        UnityInterface.AppStruct app = null;
        Set<Map.Entry<String, UnityInterface.AppStruct>> set= mApps.entrySet();
        for(Map.Entry<String, UnityInterface.AppStruct> entry:set){
            UnityInterface.AppStruct appStruct =entry.getValue();
            if(appStruct.surfaceTexture == surfaceTexture){
                app = appStruct;
                break;
            }
        }

//        if(mViewSurfaceTexture!=null){
//            mViewSurfaceTexture.updateTexImage();
//        }


//        mMainHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                if(mTextureView!=null){
//                    mTextureView.setVisibility(View.VISIBLE);
//                    Log.e("feng","isAvailable="+mTextureView.isAvailable());
//                    mTextureView.bringToFront();
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        Log.e("feng","isReleased = "+mSurfaceTexture.isReleased());
//                    }
//                    mTextureView.invalidate();
//                }
//                String path = getExternalCacheDir()+"/"+System.currentTimeMillis()+".png";
//                BitmapUtil.bitmap2Path(mBitmap,path);
//
//            }
//        });
        if(app != null){
            if(surfaceTexture!=null){
                surfaceTexture.updateTexImage();
            }
            Log.e("feng","onFrameAvailable app != null");
            surfaceTexture.detachFromGLContext();
            surfaceTexture.attachToGLContext(app.textureId);
            //getTexturePixelData(app.textureId);
            blitTexture(app);

        }

    }


 	public boolean dispatchTouchEvent(MotionEvent event) {
//             long when = SystemClock.uptimeMillis();
//
//             MotionEvent e = MotionEvent.obtain(event);
// 			int []location=new int[2];
//         surfaceView.getLocationOnScreen(location);
//         int x1=location[0];//获取当前位置的横坐标
//         int y1=location[1];//获取当前位置的纵坐标
//
//         float x = e.getX()-x1;
//         float y = e.getY()-y1;
//
// 			Log.e("feng1","dispatchTouchEvent 1 x = "+x+"---y="+y);
//             if(x>0 && y>0){
//                 e.setLocation(x,y);
//                 e.setDisplayId(mDisplay.getDisplay().getDisplayId());
//                 e.setSource(InputDevice.SOURCE_TOUCHSCREEN);
//                 final InputManager im = InputManager.getInstance();
//                 im.injectInputEvent(e, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
//             }

 //            Instrumentation mInst = new Instrumentation();
 //            mInst.sendPointerSync(event);
            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN){
                UnityInterface.sDown = MotionEvent.obtain(event);
            }else if(action == MotionEvent.ACTION_MOVE){
                UnityInterface.sMove = MotionEvent.obtain(event);
            }else if(action == MotionEvent.ACTION_UP){
                UnityInterface.sUp = MotionEvent.obtain(event);
            }else if(action == MotionEvent.ACTION_CANCEL){
                UnityInterface.sCancel = MotionEvent.obtain(event);
            }

             return super.dispatchTouchEvent(event);
         }

    @Override
    public void onRegistApp(String appName) {
        UnityInterface.AppStruct app = new UnityInterface.AppStruct();
        app.appName = appName;
        mApps.put(appName,app);
        Message message = Message.obtain();
        message.what = MSG_WHAT_REGISTER_APP;
        message.obj = appName;
        mHandler.sendMessage(message);


    }

    @Override
    public void onUnRegisterApp(String appName) {

        Message message = Message.obtain();
        message.what = MSG_WHAT_UNREGISTER_APP;
        message.obj = appName;
        mHandler.sendMessage(message);

    }

    @Override
    public UnityInterface.AppStruct getApp(String appName) {
        return mApps.get(appName);
    }

    @Override
    public boolean isExternalAppExist(String appName) {
        return PackageUtil.getIntent(this,appName)!=null;
    }

    @Override
    public int UpdateAppStatus(String appName, int status) {
        UnityInterface.AppStruct app = mApps.get(appName);
        if(app!=null){
            if(status == UnityInterface.UnityAppStatus.E_ACTIVE.ordinal()){
                if(app.mUnityAppStatus.ordinal() == status){
                    return status;
                }

                Intent intent =PackageUtil.getIntent(this,app.appName);
                if(intent != null){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ActivityOptions options = ActivityOptions.makeBasic();
                        options.setLaunchDisplayId(app.displayId); //这里一直display0是第一块屏；display1是第二块屏
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
                        Context secondContext = createDisplayContext(app.display.getDisplay());
                        secondContext.startActivity(intent, options.toBundle());
                        Log.e("feng","startActivity");
                        return status;
                    }
                }else{
                    Log.e("feng","intent is null ="+app.appName);
                }

            }
        }
        return UnityInterface.UnityAppStatus.E_UNKNOWN.ordinal();
    }

    @Override
    public String getAppIcon(String appName) {
        return PackageUtil.getAppIcon(getApplicationContext(),appName);
    }


    public static class MySurfaceView extends SurfaceView{
        Context mContext;

        public MySurfaceView(Context context) {
            super(context);
            mContext = context;
        }

        public MySurfaceView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

    }



    public static class TestPresentation extends Presentation {
        Button button;
        int conut = 0;
        public TestPresentation(Context outerContext, Display display) {
            super(outerContext, display);

        }
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            Log.e("maomao"," dispatchTouchEvent 我收到event了吗");

            return super.dispatchTouchEvent(ev);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            Log.e("maomao"," onTouchEvent 我收到event了吗");
            return super.onTouchEvent(event);
        }


    }
}
