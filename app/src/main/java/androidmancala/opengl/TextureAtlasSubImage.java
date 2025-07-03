package androidmancala.opengl;

import android.graphics.Bitmap;
import android.graphics.RectF;

/**
 * Created by Dennis on 2016-01-05.
 */
public class TextureAtlasSubImage implements Comparable<TextureAtlasSubImage> {

    private Bitmap bitmap;
    private float width,height;
    private RectF rect;
    private String name;

    public TextureAtlasSubImage(String name,Bitmap bitmap)
    {
        this.name=name;
        this.width=bitmap.getWidth();
        this.height=bitmap.getHeight();
        this.bitmap=bitmap;
        this.rect= new RectF();
    }

    public void setBase(float left,float right,float top,float bottom)
    {
        rect.left=left;
        rect.right=right;
        rect.top=top;
        rect.bottom=bottom;
    }

    public float getBottom()
    {
        return rect.bottom;
    }

    public float getTop()
    {
        return rect.top;
    }

    public float getLeft()
    {
        return rect.left;
    }

    public float getRight()
    {
        return rect.right;
    }

    public String getName()
    {
        return name;
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }

    public Bitmap getBitmap()
    {
        return bitmap;
    }

    protected float getMaxSide()
    {
        return Math.max(width,height);
    }

    public int compareTo(TextureAtlasSubImage o)
    {
        if (getMaxSide()>o.getMaxSide())
          return -1;
        else if
          (getMaxSide()<o.getMaxSide())
           return 1;
        else
           return 0;
    }
}
