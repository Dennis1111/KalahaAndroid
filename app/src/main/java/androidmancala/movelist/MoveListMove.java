package androidmancala.movelist;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import androidmancala.GraphicObject;
import androidmancala.opengl.GLUtil;
import androidmancala.opengl.Sprite;
import androidmancala.opengl.TextObject;

public class MoveListMove extends Sprite
		implements GraphicObject {

	private float x,y,z;
	//private Bitmap background;
	private int moveNumber;
	private TextObject moveNumberTO=null;
	private TextObject moveTO;
	private int move;
	private int playerToMove;
	private boolean hasAMove;
	private boolean isSelected;
	private boolean isSelectable;

	public MoveListMove(float ssu,float width,float height,float x,float y,int moveNumber,boolean selectable)
	{
		this(ssu,width,height,x,y,moveNumber,0,false,0,selectable);
	}

	public MoveListMove(float ssu,float width,float height,
						float x,float y,int moveNumber,int move,boolean hasAMove,int playerToMove,boolean selectable)
	{
		super(ssu,width,height);
		this.x=x;
	  	this.y=y;
		this.z=0;
	  	this.moveNumber=moveNumber;
	  	this.move=move;
	  	this.hasAMove=hasAMove;
	  	this.playerToMove = playerToMove;
		this.isSelected=false;
		this.isSelectable=selectable;
		moveNumberTO = new TextObject(getMoveNumberAsText(),x, GLUtil.screenYToGLY(getY()));
		moveTO = new TextObject(getMoveAsText(),x,GLUtil.screenYToGLY(getY()));
	}
	@Override
	public void setZ(float z) {
		this.z = z;
	}

	@Override
	public float getZ() {
		return z;
	}

	public int getPlayerToMove()
	{
		return playerToMove;
	}

	public Bitmap getBackground()
	{
		return null;
	}

	public boolean increaseSize()
	{
		return false;
	}

	public boolean decreaseSize()
	{
		return false;
	}

	public boolean isSelectable()
	{
		return isSelectable;
	}

	public void setSelectable(boolean selectable)
	{
		if (this.isSelectable!=selectable) {
			this.isSelectable = selectable;
			moveNumberTO.setText(getMoveNumberAsText());
		}
	}

	public int getMoveNumber()
	{                   
	  return moveNumber;  
	}

	public TextObject getMoveNumberAsTextObject()
	{
		return moveNumberTO;
	}

	public TextObject getMoveAsTextObject()
	{
		return moveTO;
	}

	public String getMoveNumberAsText()
	{
		String text="";
		if (isSelectable)
			text+=(moveNumber+1);
		return text;
	}

	public String getMoveAsText()
	{
		String moveText="";
		if (hasAMove)
		  //For both both kalaha and oware we only have 0-5 as possible moves
		  //If adding more games like bao this has to change
		  if (move<0 || move>5)
		 	moveText="-";
		  else
			moveText+=move+1;
		return moveText;
	}

	private void updateMoveTextObject()
	{
		moveTO.setText(getMoveAsText());
		if (playerToMove==0)
			moveTO.setY(GLUtil.screenYToGLY(y)-((int)(getHeight()*0.25)));
		else
			moveTO.setY(GLUtil.screenYToGLY(y));
	}

	public void setMoveNumber(int moveNumber) {
		if (this.moveNumber != moveNumber) {
			//bitmapNeedsUpdate=true;
			this.moveNumber = moveNumber;
			moveNumberTO.setText(getMoveNumberAsText());
			//System.out.println("UPDATING MoveNumberTO"+moveNumber);
		}
	}

	public void move(float deltaX,float deltaY)
	{
		x+=deltaX;
		y+=deltaY;
		super.move(deltaX,-deltaY);
		moveNumberTO.move(deltaX, -deltaY);
		moveTO.move(deltaX,-deltaY);
	}

	public int getMove()
	{                   
	  return move;  
	}

	public void setSelected(boolean isSelected)
	{
		if (this.isSelected!=isSelected) {
			this.isSelected = isSelected;
		}
	}
	
	public void updateMove(int moveNumber,int move,int playerToMove)
	{
		setMoveNumber(moveNumber);
		setMove(move);
		setPlayerToMove(playerToMove);
		this.playerToMove=playerToMove;
		setHasAMove(true);
		updateMoveTextObject();
	}

	private void setPlayerToMove(int playerToMove)
	{
		if(this.playerToMove!=playerToMove)
		{
			this.playerToMove=playerToMove;
		}
	}

	private void setMove(int move)
	{
		if (this.move!=move)
		{
			this.move=move;
		}
	}
	
	public void setHasAMove(boolean hasAMove) {
		if (this.hasAMove != hasAMove) {
			this.hasAMove = hasAMove;
			//bitmapNeedsUpdate=true;
			if (!hasAMove)
			 moveTO.setText("");
			}
	}

	public boolean hasAMove()
	{return hasAMove;}

	public void setX(float x)
	{
		this.x=x;
		moveNumberTO.setX(x);
		moveTO.setX(x);
		super.setX(x);
	}

	public void setY(float y)
	{
		this.y=y;
		float yOGL=GLUtil.screenYToGLY(y);
		moveNumberTO.setY(yOGL + (int) getHeight() * 0.25f);
		if (playerToMove==0)
		  moveTO.setY(yOGL-(int)getHeight()*0.25f);
		else
			moveTO.setY(yOGL);
		super.setY(yOGL);
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
	  	float xDistance = Math.abs(x-touchX);
		float yDistance = Math.abs(y-touchY);
		if (xDistance<getWidth()/2.0f && yDistance<getHeight()/2.0f)
	  	  return true;
	  	else 
	     return false;
	}

	//Not used in openGL version
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
	{  /*
		float bitmapX=x-getWidth()/2.0f;
		float bitmapY=y-getHeight()/2.0f;
		if (isSelected)
		{
			Paint ovalPaint = new Paint();
			ovalPaint.setColor(Color.BLACK);						
			RectF rectOval = new RectF(bitmapX,bitmapY,bitmapX+getWidth(),bitmapY+getHeight());
			canvas.drawOval(rectOval, ovalPaint);
			rectOval = new RectF(bitmapX+10,bitmapY+10,bitmapX+getWidth()-10,bitmapY+getHeight()-10);
			ovalPaint.setColor(Color.WHITE);
			canvas.drawOval(rectOval, ovalPaint);
			ovalPaint.setAlpha(170);
			canvas.drawBitmap(background,bitmapX,bitmapY,ovalPaint);
			ovalPaint.setStyle(Paint.Style.STROKE);
			ovalPaint.setColor(Color.BLACK);
			ovalPaint.setAlpha(255);
			canvas.drawOval(rectOval, ovalPaint);
		}
		else
		  canvas.drawBitmap(background,bitmapX,bitmapY,null);
		float textHeight = textPaint.descent() - textPaint.ascent();
		float textOffset = (textHeight / 2) - textPaint.descent();
		if (isSelectable) {
			canvas.drawText("" + (moveNumber + 1), x, yMoveNumber + textOffset, textPaint);
		}
		String moveText="";
		if (move<0)
			moveText="X";
		else
		   moveText+=move+1;
		if(hasAMove)
		if (playerToMove==0)
		  {				
			 canvas.drawText(moveText,x,yBottomPlayer+textOffset,textPaint);
		     //canvas.drawText(pre+'_',xPos-halfTextWidth,yTopPlayer,paintText);
		  }
	    else
		  {
			 //canvas.drawText(pre+'_',xPos-halfTextWidth,yBottomPlayer,paintText);
		     canvas.drawText(moveText,x,yTopPlayer+textOffset,textPaint);
		  }*/
	}
}
