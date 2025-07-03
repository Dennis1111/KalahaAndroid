package androidmancala;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dennis on 2015-09-05.
 */
public class Pit implements GraphicObject{

    private float x,y,z;

    //width and height should  maybe be renamed to xLength,YLength

    private float width,height;
    private List<Marble> marbles;
    private Bitmap bitmap;
    private String name="";

    public Pit(float x,float y,float width,float height)
    {
        this.x=x;
        this.y=y;
        this.z=-5;
        this.width=width;
        this.height=height;
        this.bitmap=null;
        this.marbles=new ArrayList<>();
        //this.world = new World(new Vec2(0.0f, -10.0f));
        //addGround(world,width,50);
    }

    @Override
    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public float getZ() {
        return z;
    }

    public void setBitmap(Bitmap bitmap)
    {
        this.bitmap=bitmap;
    }

    public boolean increaseSize()
    {
        return false;
    }

    public boolean decreaseSize()
    {
        return false;
    }

    public boolean transferToPit(Marble marble,Pit pit)
    {
        if (this.marbles.contains(marble))
        {
            this.marbles.remove(marble);
            pit.addMarble(marble);
            return true;
        }
        else
            return false;
    }

    public void setName(String name)
    {
        this.name=name;
    }

    //Protective get (a cloned list)
    public List<Marble> getMarbles()
    {
        List<Marble> copy= new ArrayList<Marble>();
        for(Marble marble : marbles)
         copy.add(marble);
        return copy;
    }

    public void removeMarble(Marble marble)
    {
        marbles.remove(marble);
    }

    public void setMarbles(List<Marble> marbles)
    {
        this.marbles.clear();
        addMarbles(marbles);
    }

    public void clearMarbles()
    {
        this.marbles.clear();
    }

    public void addMarbles(List<Marble> marbles)
    {
        this.marbles.addAll(marbles);
    }

    public void addMarble(Marble marble)
    {
        this.marbles.add(marble);
    }

    public void setX(float x)
    {
        this.x=x;
    }

    public void setY(float y)
    {
        this.y=y;
    }

    public float getX()
    {
        return x;
    }

    public float getY()
    {
        return y;
    }

    public float getWidth()
    {
        return width;
    }

    public float getHeight()
    {
        return height;
    }

    public boolean atThis(float touchX,float touchY)
    {
        float xDistance = Math.abs(x-touchX);
        float yDistance = Math.abs(y-touchY);
        if (xDistance<width*0.5f && yDistance<height*0.5f)
          return true;
        else
          return false;
    }

    public Bitmap getBitmap()
    {
        return bitmap;
    }

    public void draw(Canvas canvas)
    {

    }

    public float getDistance(GraphicObject g)
    {
        double x2 = Math.pow((this.getX()-g.getX()),2);
        double y2= Math.pow((this.getY()-g.getY()),2);
        return (float)Math.sqrt(x2+y2);
    }

    public String toString()
    {
        return name+"Pit has "+marbles.size()+ " marbles";
    }
}
