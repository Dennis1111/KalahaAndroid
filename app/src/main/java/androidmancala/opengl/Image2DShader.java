package androidmancala.opengl;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.IntBuffer;

/**
 * Created by Dennis on 2015-12-17.
 */
public class Image2DShader {

    //boolean ok;
    public int shaderProgram;
    public int vPosition;
    public int a_texCoord;
    public int mtrxhandle;
    public int samplerLoc;

    public Image2DShader()
    {
        String vertexShaderImage =
                "uniform mat4 uMVPMatrix;" +
                "attribute vec4 vPosition;" +
                "attribute vec2 a_texCoord;" +
                "varying vec2 v_texCoord;" +
                "void main() {" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "  v_texCoord = a_texCoord;" +
                "}";

        String fragmentShaderImage =
                "precision mediump float;" +
                "varying vec2 v_texCoord;" +
                "uniform sampler2D s_texture;" +
                "void main() {" +
                "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
                "}";


        int vertexShader=loadShader(GLES20.GL_VERTEX_SHADER,vertexShaderImage);
        int fragmentShader=loadShader(GLES20.GL_FRAGMENT_SHADER,fragmentShaderImage);
        //ok=true;
        if (vertexShader == 0 || fragmentShader ==0)
        {
            shaderProgram=0;
        }
        else
        {
            shaderProgram = GLES20.glCreateProgram();
            if (shaderProgram>0) {
                GLES20.glAttachShader(shaderProgram, vertexShader);
                GLES20.glAttachShader(shaderProgram, fragmentShader);
                GLES20.glLinkProgram(shaderProgram);
                final int[] status = new int[1];
                GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, status, 0);
                if (status[0] == GLES20.GL_FALSE) {
                    //ok = false;
                    IntBuffer ival = IntBuffer.allocate(1);
                    GLES20.glGetShaderiv(shaderProgram, GLES20.GL_INFO_LOG_LENGTH, ival);
                    int size = ival.get();
                    if (size > 1) {
                        String log = GLES20.glGetShaderInfoLog(shaderProgram) + "\n";
                        Log.e(" main program shader", log);
                    }
                }
            }
        }

        vPosition = GLES20. glGetAttribLocation( shaderProgram, "vPosition");
        if( vPosition == -1)
            Log.e(" MainShader", "vPosition not initialized");
        a_texCoord = GLES20. glGetAttribLocation(shaderProgram, "a_texCoord");
        if( a_texCoord == -1)
            Log.e(" MainShader", "a_texCoord not initialized");
        mtrxhandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
        samplerLoc = GLES20.glGetUniformLocation (shaderProgram, "s_texture");
    }

    public String toString()
    {
        String result="Image2D Shaderprogram= "+shaderProgram+"\n";
        result+="vPos "+vPosition+"\n";
        result+="a_texCoord "+a_texCoord+"\n";
        result+="mtrxHandle "+mtrxhandle+"\n";
        result+="samplerLoc "+samplerLoc+"\n";
        return result;
    }

    private static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shaderProgram = GLES20.glCreateShader(type);
        if (shaderProgram>0) {
            // add the source code to the shader and compile it
            GLES20.glShaderSource(shaderProgram, shaderCode);
            GLES20.glCompileShader(shaderProgram);
            final int[] status = new int[ 1];
            GLES20. glGetShaderiv(shaderProgram, GLES20. GL_COMPILE_STATUS, status, 0);
            if (status[ 0] == GLES20. GL_FALSE){
                //ok = false;
                IntBuffer ival = IntBuffer.allocate( 1);
                GLES20. glGetShaderiv( shaderProgram, GLES20. GL_INFO_LOG_LENGTH, ival);
                int size = ival.get(); if( size > 1){ String log = GLES20. glGetShaderInfoLog(shaderProgram);
                Log.e("shader"+type, log); } }
        }
        // return the shader
        return shaderProgram;
    }
}
