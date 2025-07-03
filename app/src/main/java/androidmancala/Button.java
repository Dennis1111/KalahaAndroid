package androidmancala;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidmancala.opengl.GLUtil;
import androidmancala.opengl.Sprite;

/**
 * Created by Dennis on 2015-11-28.
 */
public class Button extends Sprite implements GraphicObject {

    private float width;
    private float height;
    private float x,y,z;
    private boolean isVisible;

    public Button(String name,float x,float y,float width,float height)
    {
        super(1.0f,width,height);
        super.setCenterPosition(x, GLUtil.screenYToGLY(y));
        this.x=x;
        this.y=y;
        this.width=width;
        this.height=height;
        isVisible=false;
        this.setName(name);
    }

    @Override
    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public float getZ() {
        return z;
    }

    public float getWidth()
    {return width;}

    public float getHeight()
    {
        return height;
    }

    public boolean increaseSize()
    {return false;}
    public boolean decreaseSize()
    {return false;}
    public void setX(float x)
    {this.x = x;}
    public void setY(float y)
    {this.y=y;}
    public float getX()
    {return x;}
    public float getY()
    {return y;}

    /*public boolean isVisible()
    {
        return isVisible;
    }

    public void setVisible(boolean visible)
    {
        this.isVisible=visible;
    }*/

    public boolean atThis(float touchX,float touchY)
    {
        float xDistance= Math.abs(x-touchX);
        float yDistance= Math.abs(y-touchY);
        if (xDistance<width/2.0f && yDistance<height/2.0f)
            return true;
        else
            return false;
    }


    public Bitmap getBitmap()
    {
        return null;
    }

    public float getDistance(GraphicObject g)
    {
        double x2= Math.pow((this.getX()-g.getX()),2);
        double y2= Math.pow((this.getY()-g.getY()),2);
        return (float)Math.sqrt(x2+y2);
    }

    public void draw(Canvas canvas)
    {
    }

    public String toString()
    {
        return "Button";
    }

}
