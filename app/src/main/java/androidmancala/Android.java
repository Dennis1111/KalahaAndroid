package androidmancala;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

/**
 * Created by Dennis on 2015-09-04.
 */
public class Android {

    private int posX,posY;
    private int width,height;

    private double score=0;
    private RectF androidRect;
    private RectF headRect,bodyRect;

    private Paint backgroundPaint,eyesPaint,headPaint,bodyPaint,mouthPaint,antennaPaint;
    private Bitmap bitmap;
    private Canvas canvas;
    private int maxEyebrowsHeigth;
    private int eyeLeft,eyeRight;
    private int halfEyeBrowseWidth;
    private int eyeBrowseY;
    private int eyesY;
    private int eyeRadius;
    private int mouthMaxHeigth;

    public Android(int posX,int posY,int width,int height)
    {
        this.posX=posX;
        this.posY=posY;
        this.width=width;
        this.height=height;
        init();
    }

    public void setScore(double score)
    {
        this.score=score;
    }

    public int getXPos()
    {
        return posX;
    }

    public int getYPos()
    {
        return posY;
    }

    private void init()
    {
        int headYStart=(int)(height*0.1f);
        int headHeight = (int)(this.height*0.65);
        mouthMaxHeigth= headHeight/5;
        headRect = new RectF(0,headYStart,this.width,headHeight*2);

        int bodyStart = headYStart+(int)(headHeight*0.9);
        bodyRect= new RectF(0,bodyStart,this.width,height);
        antennaPaint = new Paint();
        antennaPaint.setStrokeWidth(width * 0.06f);
        antennaPaint.setColor(Color.GREEN);
        headPaint = new Paint();
        headPaint.setStrokeWidth(width * 0.05f);
        headPaint.setColor(Color.GREEN);
        bodyPaint = new Paint();
        bodyPaint.setColor(Color.GREEN);

        eyesPaint = new Paint();
        eyesPaint.setColor(Color.WHITE);
        eyesPaint.setStrokeWidth(width*0.03f);
        maxEyebrowsHeigth=(int)(width*0.1f);

        eyesY=(int)(headYStart+headHeight*0.5f);
        eyeRadius=(int)(width*.05f);
        eyeLeft=(int)(width*0.32);
        eyeRight=(width-eyeLeft);
        halfEyeBrowseWidth = (int)(width*0.06f);
        eyeBrowseY = (int)(headYStart+headHeight*0.25f);

        backgroundPaint = new Paint();
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mouthPaint = new Paint();
        mouthPaint.setAntiAlias(true);
        mouthPaint.setColor(Color.WHITE);
        mouthPaint.setStyle(Paint.Style.STROKE);
        mouthPaint.setStrokeJoin(Paint.Join.ROUND);
        mouthPaint.setStrokeWidth(width*0.05f);
        androidRect= new RectF(0,0,width,height);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    private float[] createMouthPath(int width,int maxHeight)
    {
        float[] mouthY=new float[width];
        double radian;
        float startY=bodyRect.top;
        for(int x=0;x<width;x++)
        {
            radian=((double)x/width)*Math.PI;
            float y=(float)Math.sin(radian)*maxHeight+startY;
            mouthY[x]=y;
        }
        int numberOfLines=(width-1);
        float[] linesCoord=new float[numberOfLines*4];
        for(int line=0;line<numberOfLines;line++)
        {
            linesCoord[(line*4)]=line;
            linesCoord[(line*4)+1]=mouthY[line];
            linesCoord[(line*4)+2]=line+1;
            linesCoord[(line*4)+3]=mouthY[line+1];
        }
        return linesCoord;
    }

    public Bitmap getBitmap()
    {
        canvas.drawRect(androidRect, backgroundPaint);
        float startAngle = 180f;
        float faceAngle = 180f;

        double angry = getAngry(score,0.0);
        //It was a fun idea with making him red when angry but there is no natural
        //transition
        //double happy = getAngry(-score,0.0);
        //int androidColor=getAndroidColor(score,angry,happy);
        //antennaPaint.setColor(androidColor);
        canvas.drawLine(eyeLeft + 5, eyesY - eyeRadius, eyeLeft - 5, 0, antennaPaint);
        canvas.drawLine(eyeRight - 5, eyesY - eyeRadius, eyeRight + 5, 0, antennaPaint);

        canvas.drawRect(bodyRect, bodyPaint);
        //headPaint.setColor(androidColor);
        canvas.drawArc(headRect, startAngle, faceAngle, false, headPaint);
        canvas.drawCircle(eyeLeft,eyesY,eyeRadius, eyesPaint);
        canvas.drawCircle(eyeRight, eyesY, eyeRadius, eyesPaint);

        drawEyeBrowse(angry);
        int maxHeight=(int)(-score*mouthMaxHeigth);

        float[] mouthPath=createMouthPath(width,maxHeight);

        canvas.drawLines(mouthPath,mouthPaint);
        canvas.drawLines(mouthPath,mouthPaint);
        return bitmap;
    }

    /*
    private void drawCheeks(double angry)
    {
        Paint cheekPaint= new Paint();
        cheekPaint.setColor(getAngryColor(angry));
        canvas.drawCircle(eyeLeft-5,eyesY+10,eyeRadius+2,cheekPaint);
        canvas.drawCircle(eyeRight+5, eyesY+10, eyeRadius+2, cheekPaint);
    }*/

    private void drawEyeBrowse(double angry)
    {
        angry= Math.max(0, angry*0.3);
        int angryAdjustment=(int)(angry*maxEyebrowsHeigth);
        int eyeBrowseLeftStartX= eyeLeft-(halfEyeBrowseWidth)+angryAdjustment;
        int eyeBrowseLeftEndX  = eyeLeft+(halfEyeBrowseWidth)+angryAdjustment;
        int eyeBrowseBottom    = eyeBrowseY+angryAdjustment;
        canvas.drawLine(eyeBrowseLeftStartX, eyeBrowseY, eyeBrowseLeftEndX, eyeBrowseBottom, eyesPaint);
        int eyeBrowseRightStartX= eyeRight+(halfEyeBrowseWidth)-angryAdjustment;
        int eyeBrowseRightEndX  = eyeRight-(halfEyeBrowseWidth)-angryAdjustment;
        canvas.drawLine(eyeBrowseRightStartX, eyeBrowseY, eyeBrowseRightEndX, eyeBrowseBottom, eyesPaint);
    }

    //How angry is the android based on the score ?
    //0 means happy 1 angry
    private double getAngry(double score,double angryMinScore)
    {
        double angryMaxScore=1.0;
        double angryMinOut=0;
        double angryMaxOut=1.0;
        double angry=normalize(score,angryMinScore,angryMaxScore,angryMinOut,angryMaxOut);

        return angry;
    }

    private static double normalize(double value,double dataMin,double dataMax,double outMin,double outMax)
    {
        double v= Math.max(value,dataMin);
        v= Math.min(v,dataMax);
        double d=dataMax-dataMin;
        v = (v - dataMin) / d;
        v = v * (outMax - outMin) + outMin;
        return v;
    }

    public int getColor(double score)
    {
        double angry = getAngry(score,0.0);
        double happy = getAngry(-score,0.0);
        int androidColor=getAndroidColor(score,angry,happy);
        return androidColor;
    }

    private int getAndroidColor(double score,double angry,double happy)
    {
        if (angry>0.7)
            return getAngryColor(angry);
        else
           return getHappyColor(happy);
    }

    //When score is
    private int getAngryColor(double angry)
    {
        int redRGB=255;
        int scale=200;
        int blueRGB= (int)((1-(angry*0.5))*scale);
        int greenRGB=blueRGB;
        return Color.rgb(redRGB,greenRGB,blueRGB);
    }

    //When score is
    private int getHappyColor(double happy)
    {
        int greenRGB=255;
        int blueRGB= (int)((1-happy)*100);
        int redRGB=blueRGB;
        return Color.rgb(redRGB,greenRGB,blueRGB);
    }
}
