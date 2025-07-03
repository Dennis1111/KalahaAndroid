package androidmancala;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidmancala.opengl.GLUtil;
import androidmancala.opengl.Sprite;

public class Marble extends Sprite implements GraphicObject {

	private float x,y,z;
	protected float vx,vy;
	protected float xForces,yForces;
	private String name="marble";
	private int marbleIndex=0;
	private Bitmap bitmap;

	public Marble(float ssu,float width,float height,float x,float y)
	{
		super(ssu,width,height);
		setCenterPosition(x, GLUtil.screenYToGLY(y));
		this.x=x;
		this.y=y;
		this.vx=0;
		this.vy=0;
		this.z=0;
		this.xForces=0;
		this.yForces=0;
	}

	/* assumes all marbles has same shape
	* and returns a measurement of of much they overlap in percent */

	public float overlap(Marble marble){
		double xDist = marble.x-x;
		double yDist = marble.y-y;
		return (float)Math.sqrt(xDist*xDist+yDist*yDist)-getWidth();
	}

	@Override
	public void setZ(float z) {
		this.z = z;
	}

	@Override
	public float getZ() {
		return z;
	}

	public boolean increaseSize()
	{
		if (marbleIndex<10) {
			marbleIndex++;
			scale(0.01f);
			return true;
		}
		return false;
	}

	public boolean decreaseSize()
	{
		if (marbleIndex>0) {
			marbleIndex--;
			scale(-0.01f);
			return true;
		}
		return false;
	}

	public void setX(float x)
	{
	   super.setX(x);
	   this.x=x;
	}
	
	public void setY(float y)
	{
		super.setY(GLUtil.screenYToGLY(y));
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
	
	public boolean atThis(float touchX,float touchY)
	{
	  	float xDistance= Math.abs(x-touchX);
	  	if (xDistance<getWidth()*0.5f && xDistance<getHeight()*0.5f)
	  	  return true;
	  	else 
	     return false;
	}

	public void setBitmap(Bitmap bitmap){
		this.bitmap=bitmap;
	}

	public Bitmap getBitmap() {
		return bitmap;
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
		return this.name;
	}
}
