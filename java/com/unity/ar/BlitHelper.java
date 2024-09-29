package com.unity.ar;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BlitHelper {

    private static final String TAG = "BlitHelper";
    private ByteBuffer mIndexBuf = null;
    private FloatBuffer mVertexBuf = null;
    private FloatBuffer mTexCoordBuf = null;

    private byte[] mIndices = {
        0, 1, 2, 0, 2, 3
    };
    private float[] mVertices = {
        -1, -1, 0,
        -1, 1, 0,
        1, 1, 0,
        1, -1, 0
    };
    private float[] mTexCoords = {
        0, 0,
        0, 1,
        1, 1,
        1, 0
    };

    // shader
    private int mShaderProgram;
    private int mVertexAttribLoc;
    private int mUVAttribLoc;
    private int mTexAttribLoc;

    private static BlitHelper msInstance;
    public static boolean IsLinearColorSpace = false;

    private BlitHelper() {
        // vertices
        ByteBuffer a = ByteBuffer.allocateDirect(mVertices.length * 4);
        a.order(ByteOrder.nativeOrder());
        mVertexBuf = a.asFloatBuffer();
        mVertexBuf.put(mVertices);
        mVertexBuf.position(0);

        // uv
        ByteBuffer b = ByteBuffer.allocateDirect(mTexCoords.length * 4);
        b.order(ByteOrder.nativeOrder());
        mTexCoordBuf = b.asFloatBuffer();
        mTexCoordBuf.put(mTexCoords);
        mTexCoordBuf.position(0);

        // indices
        mIndexBuf = ByteBuffer.allocateDirect(mIndices.length);
        mIndexBuf.order(ByteOrder.nativeOrder());
        mIndexBuf.put(mIndices);
        mIndexBuf.position(0);

        // shader
        int vertexShader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        int fragmentShader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        GLES30.glShaderSource(vertexShader,
            "in vec3 vPosition;" +
                "in vec2 vTexcoord;" +
                "out vec2 aTexcoord;" +
                "void main() {" +
                "  aTexcoord = vTexcoord;" +
                "  gl_Position = vec4(vPosition, 1.0);" +
                "}"
        );
        String fragShaderText =
                "#extension GL_OES_EGL_image_external_essl3 : require\n" +
                "precision mediump float;" +
            "out vec2 aTexcoord;" +
            "uniform sampler2D sTexture;" +
            "void main() {";
        String linearConvert = "  vec4 srgb = texture2D(sTexture, aTexcoord);" +
            "  gl_FragColor = vec4(pow(srgb.rgb, vec3(1.0 / 2.2)), srgb.a);" +
            "}";
        String gammaConvert = "  gl_FragColor = texture2D(sTexture, aTexcoord);" +
            "}";

        GLES30.glShaderSource(fragmentShader, BlitHelper.IsLinearColorSpace ? fragShaderText + linearConvert : fragShaderText + gammaConvert);
        GLES30.glCompileShader(vertexShader);
        int[] isCompiled = new int[1];
        GLES30.glGetShaderiv(vertexShader, GLES30.GL_COMPILE_STATUS, isCompiled, 0);
        if (isCompiled[0] == GLES30.GL_FALSE) {
            Log.e(TAG, "InitGL: " + GLES30.glGetShaderInfoLog(vertexShader));
            GLES30.glDeleteShader(vertexShader);
        }
        GLES30.glCompileShader(fragmentShader);
        GLES30.glGetShaderiv(fragmentShader, GLES30.GL_COMPILE_STATUS, isCompiled, 0);
        if (isCompiled[0] == GLES30.GL_FALSE) {
            Log.e(TAG, "InitGL: " + GLES30.glGetShaderInfoLog(fragmentShader));
            GLES30.glDeleteShader(fragmentShader);
        }
        mShaderProgram = GLES30.glCreateProgram();
        GLES30.glAttachShader(mShaderProgram, vertexShader);
        GLES30.glAttachShader(mShaderProgram, fragmentShader);
        GLES30.glLinkProgram(mShaderProgram);
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(mShaderProgram, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES30.GL_TRUE) {
            Log.e(TAG, "Could not link program:" + GLES30.glGetProgramInfoLog(mShaderProgram));
            GLES30.glDeleteProgram(mShaderProgram);
            mShaderProgram = 0;
        }

        mVertexAttribLoc = GLES30.glGetAttribLocation(mShaderProgram, "vPosition");
        mTexAttribLoc = GLES30.glGetUniformLocation(mShaderProgram, "sTexture");
        mUVAttribLoc = GLES30.glGetAttribLocation(mShaderProgram, "vTexcoord");
    }

    public static void blitToSurface(int textureId) {
        if (msInstance == null)
            msInstance = new BlitHelper();
        msInstance.blitToCurrentSurface(textureId);
    }

    private void blitToCurrentSurface(int textureId) {
        GLES30.glUseProgram(mShaderProgram);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glEnableVertexAttribArray(mVertexAttribLoc);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glEnableVertexAttribArray(mUVAttribLoc);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glVertexAttribPointer(mVertexAttribLoc, 3, GLES30.GL_FLOAT, false, 0, mVertexBuf);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glVertexAttribPointer(mUVAttribLoc, 2, GLES30.GL_FLOAT, false, 0, mTexCoordBuf);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        //GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glUniform1i(mTexAttribLoc, 0);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_BYTE, mIndexBuf);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glDisableVertexAttribArray(mVertexAttribLoc);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        GLES30.glDisableVertexAttribArray(mUVAttribLoc);
        //assert (GLES30.glGetError() == GLES30.GL_NO_ERROR);
        TraceUtil.glGetError();
    }
}

