package androidmancala.opengl;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;


import android.graphics.Bitmap;
import android.opengl.GLES20;

import android.opengl.GLUtils;
import android.opengl.Matrix;

/**
 * Created by Dennis on 2015-12-10.
 */
public class GLRenderer {

    float   ssu = 1.0f;
    float   ssx = 1.0f;
    float   ssy = 1.0f;
    float   swp = 320.0f;
    float   shp = 480.0f;
    private int numberOfObjects=30;


     // Our matrices
     private final float[] mtrxProjection = new float[16];
     private final float[] mtrxView = new float[16];
     private final float[] mtrxProjectionAndView = new float[16];

     // Geometric variables
     public static float vertices[];
     public static short indices[];
     public static float uvs[];
     public FloatBuffer vertexBuffer;
     public ShortBuffer drawListBuffer;
     public FloatBuffer uvBuffer;

     // Our screenresolution
     float   mScreenWidth = 1280;
     float   mScreenHeight = 768;

     // Misc
     //Context mContext;
     long mLastTime;
     int mProgram;
     Bitmap atlas;

     public GLRenderer(Bitmap atlas,float screenWidth,float screenHeight)
     {
         this.atlas=atlas;
         this.mScreenWidth=screenWidth;
         this.mScreenHeight=screenHeight;

         screenSetup((int)screenWidth,(int)screenHeight);
         setupShader();
         setupTriangle();
         setupImage();
     }

    public void render() {
    // Set our shader programm
    GLES20.glUseProgram(riGraphicTools.sp_Image);
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, atlas, 0);
    // get handle to vertex shader's vPosition member
    int mPositionHandle =  GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");

    // Enable generic vertex attribute array
    GLES20.glEnableVertexAttribArray(mPositionHandle);

    // Prepare the triangle coordinate data
    GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

    // Get handle to texture coordinates location
    int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image,"a_texCoord" );

    // Enable generic vertex attribute array
    GLES20.glEnableVertexAttribArray ( mTexCoordLoc );

    // Prepare the texturecoordinates
    GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,false,0, uvBuffer);

    // Get handle to shape's transformation matrix
    int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");

    // Apply the projection and view transformation
    GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mtrxProjectionAndView, 0);

    // Get handle to textures locations
    int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture");

    // Set the sampler texture unit to 0, where we have saved the texture.
    GLES20.glUniform1i(mSamplerLoc, 0);

    // Draw the triangle
    GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

    // Disable vertex array
    GLES20.glDisableVertexAttribArray(mPositionHandle);
    GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }


    public void screenSetup(int width, int height) {
            // We need to know the current width and height.
            mScreenWidth = width;
            mScreenHeight = height;
            setupScaling(width,height);
            // Redo the Viewport, making it fullscreen.
            //Should be transferred to the renderer
            //GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);

            // Clear our matrices
            for(int i=0;i<16;i++)
            {
                mtrxProjection[i] = 0.0f;
                mtrxView[i] = 0.0f;
                mtrxProjectionAndView[i] = 0.0f;
            }

            // Setup our screen width and height for normal sprite translation.
            Matrix.orthoM(mtrxProjection, 0, 0f, mScreenWidth, 0.0f, mScreenHeight, 0, 50);

            // Set the camera position (View matrix)
            Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

            // Calculate the projection and view transformation
            Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);
    }

    public void setupShader() {
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

            // Create the shaders
            int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_SolidColor);
            int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_SolidColor);

            riGraphicTools.sp_SolidColor = GLES20.glCreateProgram();             // create empty OpenGL ES Program
            GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader);   // add the vertex shader to program
            GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader); // add the fragment shader to program
            GLES20.glLinkProgram(riGraphicTools.sp_SolidColor);                  // creates OpenGL ES program executables

            // Create the shaders, images
            vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,riGraphicTools.vs_Image);
            fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Image);

            riGraphicTools.sp_Image = GLES20.glCreateProgram();
            GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);
            GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader);
            GLES20.glLinkProgram(riGraphicTools.sp_Image);
    }

    public void setupTriangle()
    {
        // We will need a randomizer
        Random rnd = new Random();

        // Our collection of vertices
        vertices = new float[numberOfObjects*4*3];

        // Create the vertex data
        // this loop tells where the objects will be drawn and how big (30*ssu) they will be
        for(int i=0;i<numberOfObjects;i++)
        {
            int offset_x = rnd.nextInt((int)swp);
            int offset_y = rnd.nextInt((int)shp);

            // Create the 2D parts of our 3D vertices, others are default 0.0f
            vertices[(i*12) + 0] = offset_x;
            vertices[(i*12) + 1] = offset_y + (30.0f*ssu);
            vertices[(i*12) + 2] = 0f;
            vertices[(i*12) + 3] = offset_x;
            vertices[(i*12) + 4] = offset_y;
            vertices[(i*12) + 5] = 0f;
            vertices[(i*12) + 6] = offset_x + (30.0f*ssu);
            vertices[(i*12) + 7] = offset_y;
            vertices[(i*12) + 8] = 0f;
            vertices[(i*12) + 9] = offset_x + (30.0f*ssu);
            vertices[(i*12) + 10] = offset_y + (30.0f*ssu);
            vertices[(i*12) + 11] = 0f;
        }

        //for(int i=0;i<30*4*3;i++)
          //  System.out.println("vert"+vertices[i]);

        // The indices for all textured quads
        indices = new short[30*6];
        int last = 0;
        for(int i=0;i<numberOfObjects;i++)
        {
            // We need to set the new indices for the new quad
            indices[(i*6) + 0] = (short) (last + 0);
            indices[(i*6) + 1] = (short) (last + 1);
            indices[(i*6) + 2] = (short) (last + 2);
            indices[(i*6) + 3] = (short) (last + 0);
            indices[(i*6) + 4] = (short) (last + 2);
            indices[(i*6) + 5] = (short) (last + 3);

            // Our indices are connected to the vertices so we need to keep them
            // in the correct order.
            // normal quad = 0,1,2,0,2,3 so the next one will be 4,5,6,4,6,7
            last = last + 4;
        }

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

    public void setupImage()
    {
        // We will use a randomizer for randomizing the textures from texture atlas.
        // This is strictly optional as it only effects the output of our app,
        // Not the actual knowledge.
        Random rnd = new Random();

        // 30 imageobjects times 4 vertices times (u and v)
        uvs = new float[numberOfObjects*4*2];

        // We will make 30 randomly textures objects
        for(int i=0; i<numberOfObjects; i++)
        {
            int random_u_offset = rnd.nextInt(2);
            int random_v_offset = rnd.nextInt(2);
            //random_u_offset=0;
            //System.out.println("u_offset="+random_u_offset+", v= "+random_v_offset);
            // Adding the UV's using the offsets
            uvs[(i*8) + 0] = random_u_offset * 0.5f;//0 or 0.5
            uvs[(i*8) + 1] = random_v_offset * 0.5f;
            uvs[(i*8) + 2] = random_u_offset * 0.5f;
            uvs[(i*8) + 3] = (random_v_offset+1) * 0.5f;//0 or 1
            uvs[(i*8) + 4] = (random_u_offset+1) * 0.5f;
            uvs[(i*8) + 5] = (random_v_offset+1) * 0.5f;
            uvs[(i*8) + 6] = (random_u_offset+1) * 0.5f;
            uvs[(i*8) + 7] = random_v_offset * 0.5f;
        }

        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);
        System.out.println("GLRenderer texture binding" + texturenames[0]);
        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, atlas, 0);

        // We are done using the bitmap so we should recycle it.
        //atlas.recycle();
    }

    public void setupScaling(int screenWidth,int screenHeight)
    {
        // The screen resolutions
        swp = screenWidth;//(int) (mContext.getResources().getDisplayMetrics().widthPixels);
        shp = screenHeight;//(int) (mContext.getResources().getDisplayMetrics().heightPixels);

        // Orientation is assumed portrait
        ssx = swp / 320.0f;
        ssy = shp / 480.0f;

        // Get our uniform scaler
        if(ssx > ssy)
            ssu = ssy;
        else
            ssu = ssx;
        System.out.println("SSU="+ssu+"ssx"+ssx+"ssy"+ssy);
    }

    /*
    public void SetupImageOld() {
        // Create our UV coordinates.
        uvs = new float[]{
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        // Generate Textures, if more needed, alter these numbers.
        int[] texturenames = new int[1];
        GLES20.glGenTextures(1, texturenames, 0);

        // Retrieve our image from resources.
        int id = mContext.getResources().getIdentifier("drawable/ic_launcher", null, mContext.getPackageName());

        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);

        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);

        // Set wrapping mode
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        // We are done using the bitmap so we should recycle it.
        bmp.recycle();
    }*/

    /*
    public void processTouchEvent(MotionEvent event) {
        // Get the half of screen value
        int screenhalf = (int) (mScreenWidth / 2);
        int screenheightpart = (int) (mScreenHeight / 3);
        if (event.getX() < screenhalf) {
            // Left screen touch
            if (event.getY() < screenheightpart)
                sprite.scale(-0.01f);
            else if (event.getY() < (screenheightpart * 2))
                sprite.translate(-10f*ssu, -10f*ssu);
            else
                sprite.rotate(0.01f);
        } else {
            // Right screen touch
            if (event.getY() < screenheightpart)
                sprite.scale(0.01f);
            else if (event.getY() < (screenheightpart * 2))
                sprite.translate(10f*ssu, 10f*ssu);
            else
                sprite.rotate(-0.01f);
        }
    }*/


}
