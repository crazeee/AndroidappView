package com.unity.ar;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.io.FileOutputStream;
import java.io.OutputStream;

public class BitmapUtil {
    /**
     * 文字生成图片
     * @param text
     * @param textSize
     * @param textColor
     * @param bgColor
     * @param padding
     * @return
     */
    public static Bitmap text2Bitmap(String text, int textSize, String textColor, String bgColor, int padding) {

        Paint paint = new Paint();
        paint.setColor(Color.parseColor(textColor));
        paint.setTextSize(textSize);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        float width = paint.measureText(text, 0, text.length());

        float top = paint.getFontMetrics().top;
        float bottom = paint.getFontMetrics().bottom;

        Bitmap bm = Bitmap.createBitmap((int) (width + padding * 2), (int) ((bottom - top) + padding * 2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);

        canvas.drawColor(Color.parseColor(bgColor));
        canvas.drawText(text, padding, - top + padding, paint);
        return bm;
    }

    /**
     * 将bitmap转换为本地的图片
     *
     * @param bitmap
     * @return
     */
    public static String bitmap2Path(Bitmap bitmap, String path) {
        try {
            OutputStream os = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e("TAG", "", e);
        }
        return path;
    }

}