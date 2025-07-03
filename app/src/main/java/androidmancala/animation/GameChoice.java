package androidmancala.animation;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidmancala.opengl.riGraphicTools;

/**
 * Created by Dennis on 2016-01-26.
 */
public class GameChoice implements GLSurfaceView.Renderer {
    public enum Game{Kalaha,Oware,Bao}
    private int textureIDCounter=0;
    private float screenWidth,screenHeight;
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];


    public GameChoice()
    {

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        System.out.println("on Surface created");
        // Set the background frame color
        GLES20.glClearColor(0.1f, 0.0f, 0.5f, 1);
        if (textureIDCounter>0) {
            System.out.println("skipping recreation");
            return;
        }
        //image2DShader = new Image2DShader();

        // Get the maximum of texture units we can use.
        // Text shader
        int vshadert = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Text);
        int fshadert = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Text);

        riGraphicTools.sp_Text = GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.sp_Text, vshadert);
        GLES20.glAttachShader(riGraphicTools.sp_Text, fshadert); 		// add the fragment shader to program
        GLES20.glLinkProgram(riGraphicTools.sp_Text);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.screenWidth=width;
        this.screenHeight=height;

        // Clear our matrices
        for(int i=0;i<16;i++)
        {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, screenWidth, 0.0f, screenHeight, 0, 50);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
