package com.unity.ar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PackageUtil {

    public static boolean getPackageInfo(Context context,String packageName){

        Intent intent =getIntent(context,packageName);

        if (intent != null) {
            context.startActivity(intent);
            return true;
        } else {
            // 处理无法启动应用的情况
        }
        return false;
    }


    public static Intent getIntent(Context context,String packageName){
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        return intent;
    }


    public static String getAppIcon(Context context,String packageName){
        Log.e("feng",packageName+" getAppIcon");
        String path = context.getExternalCacheDir()+"/icon";
        File f = new File(path);
        if(!f.exists()){
            f.mkdirs();
        }
        String filename = context.getExternalCacheDir()+"/icon/"+packageName+".png";
        f = new File(filename);
        if(f.exists()){
            Log.e("feng",packageName+" is exist");
            return filename;
        }

        PackageManager pm = context.getPackageManager();
        try {
            Drawable drawable = pm.getApplicationIcon(packageName);
            if(drawable == null){
                Log.e("feng",packageName+"-----drawable is null");
                return "";
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Bitmap bitmap=null;
                if (drawable instanceof BitmapDrawable) {
                    bitmap = ((BitmapDrawable)drawable).getBitmap();


                    //ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
                    //return applicationInfo.loadIcon(pm);


                }else{
                    int width = drawable.getIntrinsicWidth();
                    int height = drawable.getIntrinsicHeight();
                    bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    drawable.draw(canvas);

                }
                if (bitmap!=null){
                    Log.e("feng",packageName+"-----size= "+bitmap.getByteCount());
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filename));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    bos.flush();
                    bos.close();
                }
            }
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}
