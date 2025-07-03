package androidmancala.opengl;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Dennis on 2015-12-11.
 */
public class Sprite {
    float angle;
    float scaleX,scaleY;
    RectF base;
    PointF translation;

    // Geometric variables
    public float vertices[];
    public short indices[];
    public float uvs[];
    public float colors[];
    public FloatBuffer vertexBuffer;
    public ShortBuffer drawListBuffer;
    public FloatBuffer uvBuffer;
    public FloatBuffer colorBuffer;
    //private Bitmap bitmap;
    int textureUnit;//The Unit 0..N

    int activeTextureID;//GLES20.GL_TEXTURE0+textureCount
    //private Image2DShader shader;
    private Image2DColorShader shader;
    //private float width,height;
    //private static int textureCount=0;
    private String name="";
    private boolean isVisible;
    //private boolean needsUpdate=false;

    public Sprite(int textureUnit,int activeTextureID,Image2DColorShader shader,float ssu,float width,float height)
    {
        this.textureUnit=textureUnit;
        this.activeTextureID=activeTextureID;
        this.shader=shader;

        setupBase(width,height);
        // Initial translation
        translation = new PointF((width/2)*ssu,(height/2)*ssu);
        // We start with our inital size
        scaleX = 1f*ssu;
        scaleY = 1f*ssu;
        // We start in our inital angle
        angle = 0f;
        setupColor();
        setupTriangle();
        setupUVS();
        isVisible=true;
    }

    public Sprite(float ssu,float width,float height)
    {
        setupBase(width,height);
        // Initial translation
        translation = new PointF((width/2)*ssu,(height/2)*ssu);
        // We start with our inital size
        scaleX = 1f*ssu;
        scaleY = 1f*ssu;
        // We start in our inital angle
        angle = 0f;
        setupColor();
        setupTriangle();
        setupUVS();
        isVisible=true;
    }

    public void move(float deltaX,float deltaY)
    {
        this.base.left+=deltaX;
        this.base.right+=deltaX;
        this.base.top+=deltaY;
        this.base.bottom+=deltaY;
    }

    private void setupColor()
    {
        colors = new float[]
                {1f, 1f, 1f, 1f,
                 1f, 1f, 1f, 1f,
                 1f, 1f, 1f, 1f,
                 1f, 1f, 1f, 1f};
    }

    //Set a all vertices to same color and alpha
    public void setColor(float r,float g,float b,float a)
    {
       //if (colors==null)
       //    colors=new float[16];
        for(int vertice=0;vertice<1;vertice++) {
            colors[0 + vertice*4]=r;
            colors[1 + (vertice*4)]=g;
            colors[2 + (vertice*4)]=b;
            colors[3 + (vertice*4)]=a;
        }
    }

    public void setColor(float[] colors)
    {
        if (colors.length==16)
            for(int color=0;color<16;color++)
            {
               this.colors[color]=colors[color];
            }
        else
        {
            System.out.println("wrong color size"+colors.length);
            System.exit(0);
        }
    }

    public boolean isVisible()
    {return isVisible;}

    public void setVisible(boolean isVisible)
    {this.isVisible=isVisible;}

    public float getWidth()
    {
       return getUnscaledWidth()*scaleX;
    }

    public float getUnscaledWidth()
    {
        return base.right-base.left;
    }

    //The height before scaling
    public float getUnscaledHeight()
    {
        return base.top-base.bottom;
    }

    public float getHeight()
    {return getUnscaledHeight()*scaleY;}

    public void setScaledHeight(float height)
    {
            //total Heght=heigth=*scale;
        //scale = total heigt/height
        scaleY = getUnscaledHeight()/height;
    }

    public void setScaleY(float scaleY)
    {
        this.scaleY=scaleY;
    }


    public void setName(String name)
    {
        this.name=name;
    }

    public String getName()
    {
       return name;
    }

    public String toString(){
        String spriteName="Sprite"+name;
        spriteName+="TextureUnit"+textureUnit;
        spriteName+="ActiveTextureID"+activeTextureID;
        spriteName+="x="+translation.x;
        spriteName+="y="+translation.y;
        return spriteName;
    }

    public void setBitmap(Bitmap bitmap)
    {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0+textureUnit);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, activeTextureID);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        int level=0;
        int border=0;
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, bitmap, border);
    }

    public void setCenterPosition(float x,float y)
    {
       translation.set(x, y);
       setupTriangle();
    }

    private void setupBase(float width,float height)
    {
        float halfWidth=width/2.0f;
        float halfHeight=height/2.0f;
        base = new RectF(-halfWidth,halfHeight,halfWidth,-halfHeight);
    }

    public void setHeight(float height)
    {
        float centerY=(base.bottom+base.top)/2f;
        base.bottom=centerY-height/2;
        base.top=centerY+height/2;
    }

    public void translate(float deltax, float deltay)
    {
        // Update our location.
        translation.x += deltax;
        translation.y += deltay;
    }

    public void setX(float x)
    {
        translation.x=x;
    }

    public void setY(float y)
    {
        translation.y=y;
    }

    public void scale(float deltas)
    {
        scaleX += deltas;
        scaleY += deltas;
    }

    public void rotate(float delta)
    {
        angle += delta;
    }

    public float[] getTransformedVertices()
    {
        // Start with scaling
        float x1 = base.left * scaleX;
        float x2 = base.right* scaleX;
        float y1 = base.bottom * scaleY;
        float y2 = base.top * scaleY;
        //float width=800;

        //x1=0-width/2;
        //x2=0+width/2;
        //System.out.println("x1"+x1+"x2"+x2+" , "+base.left * scale);
        // We now detach from our Rect because when rotating,
        // we need the seperate points, so we do so in opengl order
        PointF one = new PointF(x1, y2);
        PointF two = new PointF(x1, y1);
        PointF three = new PointF(x2, y1);
        PointF four = new PointF(x2, y2);

        // We create the sin and cos function once,
        // so we do not have calculate them each time.
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);

        // Then we rotate each point
        one.x = x1 * c - y2 * s;
        one.y = x1 * s + y2 * c;
        two.x = x1 * c - y1 * s;
        two.y = x1 * s + y1 * c;
        three.x = x2 * c - y1 * s;
        three.y = x2 * s + y1 * c;
        four.x = x2 * c - y2 * s;
        four.y = x2 * s + y2 * c;

        // Finally we translate the sprite to its correct position.
        one.x += translation.x;
        one.y += translation.y;
        two.x += translation.x;
        two.y += translation.y;
        three.x += translation.x;
        three.y += translation.y;
        four.x += translation.x;
        four.y += translation.y;
        //System.out.println("One"+one.x+","+one.y);
        //System.out.println("Two"+two.x+","+two.y);
        //System.out.println("Three"+three.x+","+three.y);
        //System.out.println("Four"+four.x+","+four.y);
        // We now return our float array of vertices.
        return new float[]
        {
           one.x, one.y, 0.0f,
           two.x, two.y, 0.0f,
           three.x, three.y, 0.0f,
           four.x, four.y, 0.0f,
        };
    }

    public void updateSprite()
    {
        // Get new transformed vertices
        vertices = getTransformedVertices();

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    public void setupUVS()
    {
        // Create our UV coordinates.
        uvs = new float[] {
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
    }

    public float[] getColors()
    {
        return colors;
    }

    public void setupTriangle() {
        // Get information of sprite.
        vertices = getTransformedVertices();

        // The order of vertexrendering for a quad
        indices = new short[]{0, 1, 2, 0, 2, 3};

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

        ByteBuffer cb = ByteBuffer.allocateDirect(colors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);
    }

    public void render(float[] mtrxProjectionAndView)
    {
        GLES20.glUseProgram(shader.shaderProgram);
        // get handle to vertex shader's vPosition member
        //System.out.println("Sprite txtID" + textureID);

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(shader.vPosition);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(shader.vPosition, 3,GLES20.GL_FLOAT, false,0, vertexBuffer);

        // Get handle to texture coordinates location
        //int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord" );

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray (shader.a_texCoord );

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer ( shader.a_texCoord, 2, GLES20.GL_FLOAT,false, 0, uvBuffer);

        // Get handle to shape's transformation matrix
        //int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(shader.mtrxhandle, 1, false, mtrxProjectionAndView, 0);

        // Get handle to textures locations
        //int mSamplerLoc = GLES20.glGetUniformLocation (riGraphicTools.sp_Image, "s_texture" );
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( shader.samplerLoc, textureUnit);//Must change passing of textureID

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(shader.vPosition);
        GLES20.glDisableVertexAttribArray(shader.a_texCoord);

        GLES20.glUseProgram(0);
    }
}
