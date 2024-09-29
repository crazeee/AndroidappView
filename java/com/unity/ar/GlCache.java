package com.unity.ar;

import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES30;
import android.opengl.GLES31;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GlCache {


    public static final String TAG="GlCache";
    private static GlCache sGlCache = new GlCache();

    private int mWidth = 1080;
    private int  mHeight = 1920;
    int vertexShader;
    int fragmentShader;
    int programObject;

    //int vbo, vao, ebo;
    IntBuffer vbo, vao, ebo;

//    String vertexStr =
//            "#version 300 es\n"+
//                    "layout (location = 0) in vec3 inPos; \n"+
//                    "layout (location = 1) in vec2 inUV; \n"+
//                    "out vec2 outUV;"+
//                    "void main() \n"+
//                    "{ \n"+
//                    "   gl_Position = vec4(inPos, 1.0);\n"+
//                    "   outUV = inUV; \n"+
//                    "} \n";

//    String fragmentStr =
//            "#version 300 es\n"+
//                    //"precision mediump float; \n"+
//                    "uniform sampler2D texture_sampler;\n"+
//                    "in vec2 outUV; \n"+
//                    "out vec4 outColor; \n"+
//                    "void main() \n"+
//                    "{ \n"+
//                    "   vec4 texColor = texture(texture_sampler, outUV);\n"+
//                    "   outColor = vec4(texColor.z, texColor.y, texColor.x, texColor.w);\n"+
//                    "} \n";


    String vertexStr =
            "#extension GL_OES_EGL_image_external_essl3 : require\n" +
                    "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = aTextureCoord.xy;\n" +
                    "}\n";


 String fragmentStr =
        "#extension GL_OES_EGL_image_external_essl3 : require\n" +
                "precision mediump float;\n" +
                "varying vec2 vTextureCoord;\n" +
                "uniform sampler2D sTexture;\n" +
                "void main() {\n" +
                "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                "}\n";


    public static GlCache getInstance(){
        return sGlCache;
    }



    public void drawCallBlit(int inTextureID,int outTexture){
        int[] frameBufferIds = new int[1];
        GLES31.glGenFramebuffers(1, frameBufferIds, 0);
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, frameBufferIds[0]);
        GLES31.glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0, GLES31.GL_TEXTURE_2D, outTexture, 0);

        BlitHelper.blitToSurface(inTextureID);

        //GLES31.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        //GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, 0);
        //GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER,0);

        GLES30.glFinish();


    }

    public void drawCall(int inTextureID,int outTexture){
        //int[] textureIds = new int[1];
        //GLES30.glGenTextures(1, textureIds, 0);
        //inTextureID = textureIds[0];


        int[] frameBufferIds = new int[1];
        GLES31.glGenFramebuffers(1, frameBufferIds, 0);
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER, frameBufferIds[0]);
        GLES31.glFramebufferTexture2D(GLES31.GL_FRAMEBUFFER, GLES31.GL_COLOR_ATTACHMENT0, GLES31.GL_TEXTURE_2D, outTexture, 0);

        GLES31.glViewport(0, 0, mWidth, mHeight);
        if(TraceUtil.glGetError()){
            Log.e("feng","----"+TraceUtil.getTraceInfo());
        }

        // fprintf(stderr, "LZ: glDrawElements/glGetError() = %d\n", glErr);

        GLES31.glClearColor(0.0f, 0.5f, 0.0f, 1.0f);
        if(TraceUtil.glGetError()){
            TraceUtil.getTraceInfo();
        }
        // fprintf(stderr, "LZ: glClearColor/glGetError() = %d\n", glErr);

        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);
        TraceUtil.glGetError();
        // fprintf(stderr, "LZ: glClear/glGetError() = %d\n", glErr);

        GLES31.glUseProgram(programObject);
        if(TraceUtil.glGetError()){
            Log.e("feng","----"+TraceUtil.getTraceInfo());
        }
        // fprintf(stderr, "LZ: glUseProgram/glGetError() = %d\n", glErr);

        float vertices[] = {
                // positions          // texture coords
                1.0f,  1.0f, 0.0f,   1.0f, 1.0f, // top right
                1.0f, -1.0f, 0.0f,   1.0f, 0.0f, // bottom right
                -1.0f, -1.0f, 0.0f,   0.0f, 0.0f, // bottom left
                -1.0f,  1.0f, 0.0f,   0.0f, 1.0f  // top left
        };
        int indices[] = {
                0, 1, 3, // first triangle
                1, 3, 2  // second triangle
        };


        //Buffer buffer = IntBuffer.allocate(indices.length);

        // 创建一个 FloatBuffer 来保存顶点数据
        FloatBuffer vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

// 将顶点数据复制到 FloatBuffer 中
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        int size = vertices.length * Float.SIZE / 8;
        GLES31.glBindVertexArray(vao.get(0));
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, vbo.get(0));

        GLES31.glBufferData(GLES31.GL_ARRAY_BUFFER, size, vertexBuffer, GLES31.GL_STATIC_DRAW);
        if(TraceUtil.glGetError()){
            Log.e("feng","----"+TraceUtil.getTraceInfo());
        }
        int indexDataSize = indices.length * 4;  // 索引数据的字节数
        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(indexDataSize);
        indexBuffer.order(ByteOrder.nativeOrder());
        IntBuffer intBuffer = indexBuffer.asIntBuffer();
        intBuffer.put(indices);
        intBuffer.position(0);
        GLES31.glBindBuffer(GLES31.GL_ELEMENT_ARRAY_BUFFER, ebo.get(0));
        GLES31.glBufferData(GLES31.GL_ELEMENT_ARRAY_BUFFER, indexDataSize, intBuffer, GLES31.GL_STATIC_DRAW);
        if(TraceUtil.glGetError()){
            Log.e("feng","----"+TraceUtil.getTraceInfo());
        }
        // position attribute
        ByteBuffer temp= ByteBuffer.allocateDirect(1);
        byte b= 0;
        temp.put(b);
        GLES31.glVertexAttribPointer(0, 3, GLES31.GL_FLOAT, false, 5 * Float.SIZE/8, temp);
        GLES31.glEnableVertexAttribArray(0);
        ByteBuffer temp1= ByteBuffer.allocateDirect(1);
        b=3*Float.SIZE/8;
        temp1.put(b);
        // texture coord attribute
        GLES31.glVertexAttribPointer(1, 2, GLES31.GL_FLOAT, false, 5 * Float.SIZE/8,temp1);
        GLES31.glEnableVertexAttribArray(1);

        GLES31.glActiveTexture(GLES31.GL_TEXTURE0);
        if(TraceUtil.glGetError()){
            Log.e("feng","----"+TraceUtil.getTraceInfo());
        }
        GLES31.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, inTextureID);
        //GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, inTextureID);
        if(TraceUtil.glGetError()){
            Log.e("feng","----"+TraceUtil.getTraceInfo());
        }
        GLES31.glUniform1i(GLES31.glGetUniformLocation(programObject, "texture_sampler"), 0);
        if(TraceUtil.glGetError()){
            Log.e("feng","----"+TraceUtil.getTraceInfo());
        }
        GLES31.glDrawElements(GLES31.GL_TRIANGLES, 6, GLES31.GL_UNSIGNED_INT, 0);

        if(TraceUtil.glGetError()){
            Log.e("feng","----"+TraceUtil.getTraceInfo());
        }
        GLES31.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        //GLES31.glBindTexture(GLES31.GL_TEXTURE_2D, 0);
        GLES31.glBindFramebuffer(GLES31.GL_FRAMEBUFFER,0);




    }


    public void initDrawCall(){
        Log.e("feng","initDrawCall");
        vertexShader = LoadShader(vertexStr, GLES31.GL_VERTEX_SHADER);
        fragmentShader = LoadShader(fragmentStr, GLES31.GL_FRAGMENT_SHADER);
        if (vertexShader == 0 || fragmentShader == 0)
            return ;

        programObject = GLES31.glCreateProgram();

        GLES31.glAttachShader(programObject, vertexShader);
        GLES31.glAttachShader(programObject, fragmentShader);
        GLES31.glLinkProgram(programObject);

        int[] linked={GLES31.GL_FALSE};
        IntBuffer linkeds = IntBuffer.wrap(linked);
        GLES31.glGetProgramiv(programObject, GLES31.GL_LINK_STATUS,linkeds);
        if (linkeds.get()==GLES31.GL_FALSE) {
            // fprintf(stderr, "Could not link program");
            linked[0]=GLES31.GL_FALSE;
            IntBuffer infoLogLen = IntBuffer.wrap(linked);
            GLES31.glGetProgramiv(programObject, GLES31.GL_INFO_LOG_LENGTH, infoLogLen);
            if (infoLogLen.get() == GLES31.GL_FALSE) {
                String log = GLES31.glGetProgramInfoLog(programObject);
                Log.e(TAG,"log ="+log);
            }
            GLES31.glDeleteProgram(programObject);
            programObject = 0;
        }
        vao =IntBuffer.allocate(1);
        vbo =IntBuffer.allocate(1);
        ebo =IntBuffer.allocate(1);


        GLES31.glGenVertexArrays(1, vao);
        GLES31.glGenBuffers(1, vbo);
        GLES31.glGenBuffers(1, ebo);

        if(TraceUtil.glGetError()){
            Log.e("feng","----"+TraceUtil.getTraceInfo());
        }
    }


    public int LoadShader(String shaderSrc, int type)
    {
        int shader;
        int compiled;


        // Create the shader object
        shader = GLES31.glCreateShader(type);
        if(shader == 0)
            return 0;
        // Load the shader source
        GLES31.glShaderSource(shader,shaderSrc);

        // Compile the shader
        GLES31.glCompileShader(shader);
        // Check the compile status
        IntBuffer compiler = IntBuffer.allocate(1);
        GLES31.glGetShaderiv(shader, GLES31.GL_COMPILE_STATUS, compiler);

        if(compiler.get() != GLES31.GL_TRUE)
        {
            IntBuffer infoLen =IntBuffer.allocate(1);
            GLES31.glGetShaderiv(shader, GLES31.GL_INFO_LOG_LENGTH, infoLen);

            if(infoLen.get() > 1)
            {
                String log = GLES31.glGetShaderInfoLog(shader);
                // fprintf(stderr, "Error compiling shader:\n%s\n", infoLog);
                Log.e(TAG,"glGetShaderiv log = "+log);

            }
            GLES31.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }
}
