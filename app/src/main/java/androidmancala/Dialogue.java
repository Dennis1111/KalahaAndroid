package androidmancala;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import androidmancala.opengl.GLUtil;
import androidmancala.opengl.Sprite;
import androidmancala.opengl.TextObject;

/**
 * Created by Dennis on 2015-11-25.
 */
public class Dialogue extends Sprite implements GraphicObject {

    private float width;
    private float height;
    private float x,y,z;
    private String text;
    private Paint backgroundPaint,ellipsePaint,textPaint;
    private RectF backgroundRect;
    private Bitmap bitmap;
    private Canvas canvas;
    private TextObject textObject;

    public Dialogue(float ssu,float width,float height,String text,float x,float y)
    {
        super(ssu,width,height);
        setX(x);
        setY(GLUtil.screenYToGLY(y));
        this.text=text;
        this.x=x;
        this.y=y;
        this.z=0;
        this.width=width;
        this.height=height;
        init();
        setBitmap(getBitmap());
        setCenterPosition(x, GLUtil.screenYToGLY(y));
        this.textObject = new TextObject(text,x,GLUtil.screenYToGLY(getY()));
        setVisible(false);
        setName(text);
    }

    public void setVisible(boolean isVisible){
        super.setVisible(isVisible);
        if (isVisible)
            textObject.text=text;
        else
            textObject.text="";
    }

    public TextObject getTextObject(){
        return textObject;
    }

    @Override
    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public float getZ() {
        return z;
    }

    private void init()
    {
        backgroundPaint = new Paint();
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        ellipsePaint= new Paint();
        ellipsePaint.setColor(Color.BLACK);
        ellipsePaint.setAlpha(150);
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(height*0.4f);
        backgroundRect= new RectF(0,0,width,height);
        bitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
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
    {this.x = x;
     super.setX(x);}

    public void setY(float y)
    {this.y=y;
     super.setY(GLUtil.screenYToGLY(y));}
    public float getX()
    {return x;}
    public float getY()
    {return y;}

    public boolean atThis(float touchX,float touchY)
    {
        float xDistance= Math.abs(x-touchX);
        float yDistance= Math.abs(y-touchY);
        if (xDistance<width/2.0f && yDistance<height/2.0f)
          return true;
        else
          return false;
    }

    public Bitmap getBitmapNoText()
    {
        canvas.drawRect(backgroundRect, backgroundPaint);
        canvas.drawOval(backgroundRect, ellipsePaint);
        //canvas.drawOval(backgroundRect, ellipsePaint);

        return bitmap;
    }

    public Bitmap getBitmap()
    {
        canvas.drawRect(backgroundRect, backgroundPaint);
        canvas.drawOval(backgroundRect, ellipsePaint);
        Rect textBounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), textBounds);
        float textWidth = textPaint.measureText(text); // Use measureText to calculate width
        float textHeight = textBounds.height(); // Use height from getTextBounds()
        canvas.drawText(text,(width/2)-(textWidth/2f),(height/2)+(textHeight/2f),textPaint);
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
        getBitmap();
        canvas.drawBitmap(bitmap,x-(width/2f),y-(height/2f),new Paint());//backgroundPaint);
    }

    public String toString()
    {
        return this.text;
    }
}
