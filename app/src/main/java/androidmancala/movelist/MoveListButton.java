package androidmancala.movelist;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidmancala.GraphicObject;

public class MoveListButton implements GraphicObject {
	
	private float x,y,z;
	private float width,height;
	private Bitmap bitmap;

	/*
	public MoveListButton(Bitmap bitmap,float x,float y)
	{		
	  	this.bitmap=bitmap;
	  	this.x=x;
	  	this.y=y;
	}*/

	public MoveListButton(float x,float y,float width,float height)
	{
		this.bitmap=null;
		this.width=width;
		this.height=height;
		this.x=x;
		this.y=y;
		this.z=0;
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
		return false;
	}

	public boolean decreaseSize()
	{
		return false;
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
	  	float xDistance= Math.abs(x-touchX);
		float yDistance= Math.abs(y-touchY);
	  	if (xDistance<width*0.5f && yDistance<height*0.5f)
	  	  return true;
	  	else 
	     return false;
	}

	public float getDistance(GraphicObject g)
	{
		double x2= Math.pow((this.getX()-g.getX()),2);
		double y2= Math.pow((this.getY()-g.getY()),2);
		return (float)Math.sqrt(x2+y2);
	}

	public Bitmap getBitmap()
	{
		return null;//to be implemented later
	}

	public void draw(Canvas canvas)
	{
		float drawX=x-getWidth()/2.0f;
		float drawY=y-getHeight()/2.0f;
		if (bitmap!=null)
		 canvas.drawBitmap(bitmap,drawX,drawY,null);
	}	
}
