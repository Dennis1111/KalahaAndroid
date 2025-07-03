package androidmancala.dice;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import androidmancala.opengl.GLUtil;
import androidmancala.opengl.Sprite;

/**
 * Created by Dennis on 2017-10-08.
 */

public class Dice extends Sprite {
    private Bitmap[] bitmap= new Bitmap[6];
    //private float z;
    private int rolledValue=1;

    public Dice(float ssu,float width,float x,float y){
        super(ssu,width,width);
        super.setName("Dice");
        setCenterPosition(x, GLUtil.screenYToGLY(y));
        createBitmaps();
    }

    public void setX(float x)
    {
        super.setX(x);
    }

    public void setY(float y)
    {
        super.setY(GLUtil.screenYToGLY(y));
    }

    public String getName(){
        return super.getName()+rolledValue;
    }

    //@value 1..6
    public void setRolledValue(int value){
        this.rolledValue=value;
    }

    public Bitmap getBitmap(int i){
        return bitmap[i];
    }

    private void createBitmaps(){
        int size=100;
        Paint backgroundPaint = new Paint();
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        Paint face = new Paint();
        face.setColor(Color.BLUE);
        RectF rect= new RectF(0,0,size,size);
        float radie=size/2;
        float euc=(float)Math.sqrt(radie*radie+radie*radie);
        float rx=euc*0.4f;
        Canvas[] canvas= new Canvas[6];
        for(int i=0;i<6;i++) {
            bitmap[i] = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            canvas[i]= new Canvas(bitmap[i]);
            canvas[i].drawRect(rect, backgroundPaint);
            canvas[i].drawRoundRect(rect,rx,rx,face);
        }

        Paint dotPaint = new Paint();
        dotPaint.setColor(Color.WHITE);
        float dotRadie= size*0.08f;//--> diam 1/5
        float center=size/2;
        float centerDist=size*0.25f;
        //Draw die 1
        canvas[0].drawCircle(center,center,dotRadie,dotPaint);

        //Draw die 2
        canvas[1].setBitmap(bitmap[1]);
        canvas[1].drawCircle(center,center+centerDist,dotRadie,dotPaint);
        canvas[1].drawCircle(center,center-centerDist,dotRadie,dotPaint);

        //Draw die 3
        canvas[2].setBitmap(bitmap[2]);
        canvas[2].drawCircle(center,center,dotRadie,dotPaint);
        canvas[2].drawCircle(center-centerDist,center-centerDist,dotRadie,dotPaint);
        canvas[2].drawCircle(center+centerDist,center+centerDist,dotRadie,dotPaint);

        //Draw die4
        canvas[3].setBitmap(bitmap[3]);
        canvas[3].drawCircle(center+centerDist,center+centerDist,dotRadie,dotPaint);
        canvas[3].drawCircle(center+centerDist,center-centerDist,dotRadie,dotPaint);
        canvas[3].drawCircle(center-centerDist,center+centerDist,dotRadie,dotPaint);
        canvas[3].drawCircle(center-centerDist,center-centerDist,dotRadie,dotPaint);

        //Draw die 5
        canvas[4].setBitmap(bitmap[4]);
        canvas[4].drawCircle(center,center,dotRadie,dotPaint);
        canvas[4].drawCircle(center+centerDist,center+centerDist,dotRadie,dotPaint);
        canvas[4].drawCircle(center-centerDist,center-centerDist,dotRadie,dotPaint);
        canvas[4].drawCircle(center+centerDist,center-centerDist,dotRadie,dotPaint);
        canvas[4].drawCircle(center-centerDist,center+centerDist,dotRadie,dotPaint);

        //Draw die 6
        canvas[5].setBitmap(bitmap[5]);
        float y=center;
        canvas[5].drawCircle(center-centerDist,y,dotRadie,dotPaint);
        canvas[5].drawCircle(center+centerDist,y,dotRadie,dotPaint);
        y=center+centerDist;
        canvas[5].drawCircle(center+centerDist,y,dotRadie,dotPaint);
        canvas[5].drawCircle(center-centerDist,y,dotRadie,dotPaint);
        y=center-centerDist;
        canvas[5].drawCircle(center+centerDist,y,dotRadie,dotPaint);
        canvas[5].drawCircle(center-centerDist,y,dotRadie,dotPaint);
    }
}
