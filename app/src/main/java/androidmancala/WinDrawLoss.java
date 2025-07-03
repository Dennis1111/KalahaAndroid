package androidmancala;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Typeface;

/**
 * Created by denni_000 on 2015-07-28.
 */
public class WinDrawLoss {
    private int posX,posY;
    private int centerX;
    private int width,height;
    //private int minThermometerY,maxThermometerY;
    private double winPerc,drawPerc,lossPerc;
    private Paint backgroundPaint,textPaint,arcPaint;
    //private static final int BACKGROUND_COLOR = Color.YELLOW;
    private static final int WIN_COLOR = Color.RED;
    private static final int DRAW_COLOR = Color.WHITE;
    private static final int LOSS_COLOR = Color.GREEN;
    private Paint winPaint,drawPaint,lossPaint;
    //private List<Point> pointPath;
    private static int numberOfPoints;
    private RectF wdlRect,wdlRectInner;
    private float arcWidth;
    private Bitmap bitmap;
    private Canvas canvas;
    private boolean updated=false;

    public WinDrawLoss(int posX,int posY,int width,int height,double winPerc,double drawPerc)
    {
        this.posX=posX;//startX;
        this.posY=posY;//startY;
        this.width = width;
        this.height = height;
        this.centerX=width/2;
        this.arcWidth=width/5;
        setRectangle();
        setWinPerc(winPerc, drawPerc);
        init();
    }

    public int getWidth()
    {
        return width;
    }
    public int getHeight()
    {return height;}

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
        backgroundPaint = new Paint();
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        //backgroundPaint.setAlpha(0);
        //backgroundPaint.setColor(BACKGROUND_COLOR);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(arcWidth * 0.8f);
        textPaint.setTextScaleX(0.8f);
        arcPaint = new Paint();
        bitmap= Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    private void setRectangle()
    {
        this.wdlRect = new RectF(0,0,this.width,this.height*2);
        this.wdlRectInner = new RectF(arcWidth,arcWidth,this.width-arcWidth,this.height*2-arcWidth);
    }

    public void setWinPerc(double winPerc,double drawPerc) {
        this.winPerc=winPerc;
        this.drawPerc=drawPerc;
        this.lossPerc=1.0-winPerc-drawPerc;
    }

    public Bitmap getBitmap()
    {
        if (!updated)
        {
            updateBitmap();
            updated=true;
        }
        return bitmap;
    }

    public Bitmap updateBitmap()
    {
        //canvas.drawRect(wdlRect,backgroundPaint);

        //int winBottomPlayerY = (int) (height*0.8);
        //int looseBottomPlayerY = (int)(height*0.2);

        float startAngle = 180f;
        float sweepWinAngle = 180f*(float)winPerc;

        arcPaint.setColor(WIN_COLOR);
        arcPaint.setStrokeWidth(1.0f);
        //The winArc
        float winStartAngle= 180f;
        canvas.drawArc(wdlRect,winStartAngle,sweepWinAngle,true,arcPaint);

        //The drawArc
        float sweepDrawAngle = 180f*(float)drawPerc;
        float drawStartAngle = winStartAngle+sweepWinAngle;
        arcPaint.setColor(DRAW_COLOR);
        canvas.drawArc(wdlRect,drawStartAngle,sweepDrawAngle,true,arcPaint);

        float sweepLossAngle = 180f*(float)lossPerc;
        float lossStartAngle = drawStartAngle+sweepDrawAngle;
        arcPaint.setColor(LOSS_COLOR);
        canvas.drawArc(wdlRect, lossStartAngle, sweepLossAngle, true, arcPaint);

        canvas.drawArc(wdlRectInner,winStartAngle,180f,true,backgroundPaint);
        RectF titleRect = new RectF(0,0,700,700);
        Path titlePath = new Path();
        titlePath.addArc(titleRect, 180.0f, 180.0f);
        Path winPath = createAnglePath(wdlRectInner,startAngle,sweepWinAngle);
        if (winPerc>0.25)
         canvas.drawTextOnPath(getPercentage(winPerc)+" %", winPath, 0.0f, 0.02f, textPaint);
        Path drawPath = createAnglePath(wdlRectInner,drawStartAngle,sweepDrawAngle);
        if (drawPerc>0.25)
         canvas.drawTextOnPath(getPercentage(drawPerc)+" %",drawPath, 0.0f, 0.0f, textPaint);
        Path lossPath = createAnglePath(wdlRectInner,lossStartAngle,sweepLossAngle);
        if (lossPerc>0.25)
          canvas.drawTextOnPath(getPercentage(lossPerc)+ "%",lossPath, 0.0f, 0.0f, textPaint);
        return bitmap;
    }

    private String getPercentage(double percentage)
    {
        double temp=percentage*100;
        temp=Math.rint(temp);
        //temp=temp/10;
        //System.out.println(percentage+"rounded"+temp);
        return ""+(int)temp;
    }

    private Path createAnglePath(RectF rectangle,float startAngle,float sweepAngle) {
        Path upperPath = new Path();
        upperPath.addArc(rectangle, startAngle, sweepAngle);
        return upperPath;
    }

    private Path createAnglePath() {
        Path upperPath = new Path();
        upperPath.addArc(wdlRect, 180.0f, 180.0f);
        return upperPath;
    }

    /*
    private Path getTestPath()
    {
        Path path = new Path();
        path.moveTo(0, 0);
        int yPoints=10;
        double increment= this.height/yPoints;
        path.lineTo(100,100);
        path.lineTo(600,200);
        path.lineTo(width,height);
        return path;
    }*/

    /*
    private List<Point> createPath(int nrOfPoints){
        double currentAngle= Math.PI;
        double rotation = Math.PI/(nrOfPoints-1);
        List<Point> points = new ArrayList<Point>();
        double radie = Math.min(height,width)*0.45;
        int centerX=(int)(width/2);
        int centerY=posY;
        for(int i=0;i<nrOfPoints;i++)
        {
            points.add(getCirclePoint(centerX,centerY,currentAngle,radie));
            currentAngle-=rotation;
        }
        return points;
    }*/

    private Point getCirclePoint(double cx,double cy,double angle,double r)
    {
        int x = (int)(cx + r * Math.cos(angle));
        int y = (int)(cy + r * Math.sin(angle));
        //System.out.println("Angle"+angle+","+x+","+y);
        return new Point(x,y);
    }
}
