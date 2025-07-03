package androidmancala.animation;

import android.graphics.Canvas;

import boardgame.WinDrawLossEvaluation;
import androidmancala.Android;
import androidmancala.WinDrawLoss;
import androidmancala.movelist.MoveList;

/**
 * Created by Dennis on 2015-11-30.
 */
public class BottomPanel {

    private MoveList moveList;
    private WinDrawLoss winDrawLoss;
    private Android android;
    //private Bitmap bitmap;
    private int width;

    //Y is not not the center but start of panel
    public BottomPanel(MoveList moveList,int width,int y, WinDrawLossEvaluation evaluation)
    {
      this.moveList=moveList;
      this.width=width;
      int androidAndWDLWidth=width-(int)moveList.getWidth();
      initWinDrawLoss(androidAndWDLWidth,y,evaluation);
      initAndroid(androidAndWDLWidth,y);
    }

    private void initWinDrawLoss(int wdlWidth,int yPos,WinDrawLossEvaluation evaluation)
    {
        double winPerc = evaluation.getNormalizedWinningProbability();
        double drawPerc = evaluation.getNormalizedDrawProbability();
        int xPos= (int)moveList.getWidth();//+(int)(wdlWidth/2.0);
        winDrawLoss = new WinDrawLoss(xPos, yPos, wdlWidth,(int) moveList.getHeight(), winPerc, drawPerc);
    }

    private void initAndroid(int androidWidth,int yPos)
    {
        int xPos= (int)moveList.getWidth();
        this.android = new Android(xPos, yPos, androidWidth,(int) moveList.getHeight());
    }

    public Android getAndroid()
    {
        return android;
    }

    public WinDrawLoss getWinDrawLoss()
    {
        return winDrawLoss;
    }

    public void draw(Canvas canvas)
    {
        moveList.draw(canvas);
        canvas.drawBitmap(winDrawLoss.getBitmap(), winDrawLoss.getXPos(), winDrawLoss.getYPos(), null);
        canvas.drawBitmap(android.getBitmap(), android.getXPos(), android.getYPos(), null);
    }
}
