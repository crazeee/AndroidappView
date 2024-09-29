package com.unity.ar;

import android.opengl.GLES30;
import android.util.Log;

public class TraceUtil {

    private static final String TAG="TraceUtil";
    /**
     * 获取当前程序行数，类名，方法名
     *
     * @return 行数，类名，方法名
     */
    public static String getTraceInfo() {
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        int stacksLen = stacks.length;
        sb.append("class: ").append(stacks[1].getClassName())
                .append("; method: ").append(stacks[1].getMethodName())
                .append("; number: ").append(stacks[1].getLineNumber());
        return sb.toString();
    }

    public static boolean glGetError(){
        int error = GLES30.glGetError();
        if(error != GLES30.GL_NO_ERROR){
            Log.e(TAG,"gl error = "+error);
            return true;
        }
        return false;
    }

}
