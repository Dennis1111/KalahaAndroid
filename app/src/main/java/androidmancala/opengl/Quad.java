package androidmancala.opengl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Dennis on 2015-12-09.
 */
public class Quad {
    private float[] vertices= {
            0.0f, -1.0f,0.0f,
            0.0f,0.0f,0.0f,
            1.0f,0.0f,0.0f,
            1.0f,1,0f,0.0f
    };

    private short[] indices= {0,1,2,0,2,3};

    private FloatBuffer vertexBuffer;
    private ShortBuffer indexBuffer;

    public float x;
    public float y;
    public float width;
    public float height;

    public Quad()
    {
        ByteBuffer vbb= ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    public void draw(GL10 gl,float x,float y,float width,float height)
    {
        try {
            gl.glTranslatef(x, y, 0.0f);
            gl.glScalef(width, height, 1.0f);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glColor4f(1.0f,0.0f,0.0f,1.0f);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, vertices.length / 3);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        }
        catch(Exception e)
        {e.printStackTrace();
        }
    }
}
