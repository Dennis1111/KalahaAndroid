package androidmancala;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidmancala.menu.R;

/**
 * Created by Dennis on 2017-11-01.
 */

public class GraphicalBoard {

    private Bitmap bitmap;
    private List<Pit> pits;
    private List<Marble> marbles;
    private float yCompPits;
    private float yPlayerPits;

    public GraphicalBoard(Bitmap bitmap,List<Pit> pits,List<Marble> marbles)
    {
        this.bitmap=bitmap;
        this.pits=pits;
        this.marbles=marbles;
        float boardHeight = bitmap.getHeight();
        //should have an abstract 'Mancala' class with fields FIRST_PIT_SEC_PLAYER
        yCompPits = pits.get(7).getY()/boardHeight;
        yPlayerPits = pits.get(0).getY()/boardHeight;
        //System.out.println("bottomPitPers"+yPlayerPits+"top"+yCompPits);

    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public List<Pit> getPits(){return pits;}

    public List<Marble> getMarbles(){return marbles;}

    //The top (computer pits) pits has its y centet at boardheigth*getYPercCompPits
    public float getYPercCompPits(){
        return yCompPits;
    }

    public float getYPercPlayerPits(){
        return yPlayerPits;
    }

    //Setup a board with pits and marbles
    public static GraphicalBoard createTacticBoard(Context context,int destWidth, int destHeight,int stones,float marbleSSU) {

        Bitmap board = BitmapFactory.decodeResource(context.getResources(), R.drawable.board);
        Bitmap boardARGB8888 = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(boardARGB8888);
        Bitmap scaledBackground = Bitmap.createScaledBitmap(board, destWidth, destHeight, false);
        board.recycle();
        Paint pitPaint = new Paint();
        canvas.drawBitmap(scaledBackground, 0, 0, pitPaint);
        scaledBackground.recycle();
        List<Marble> marbles = new ArrayList<>();
        List<Pit> pits = new ArrayList<>();
        int boardWidth = destWidth;
        int boardHeight = destHeight;

        //float pitDistance = boardWidth * 0.1126f;//The distance beetween the center of two house pits
        int pitWidth = (int) (boardWidth * 0.1085f);
        int pitHeight = (int) (boardHeight * 0.36f);
        float yPosBottomPits = boardHeight * 0.752f;
        float marbleWidth = boardWidth * 0.03f;
        //The pos of the houses as perc of  boardwidth
        float[] xPos = {0.184f,0.30f,0.4137f,0.586f,0.698f,0.8135f};
        for(int i=0;i<6;i++)
            xPos[i]*=boardWidth;

        Random rand = new Random();
        for (int pit = 0; pit < 6; pit++) {
            //System.out.println("Xpos"+xPos);
            Pit bottomPit = new Pit(xPos[pit], yPosBottomPits, pitWidth, pitHeight);
            bottomPit.setName("Bottom Player" + pit);
            for (int stone = 0; stone < stones; stone++) {
                float marbleXPos = (float) ((Math.random() - 0.5) * pitWidth * 0.4) + xPos[pit];
                float marbleYPos = (float) ((Math.random() - 0.5) * pitHeight * 0.4) + yPosBottomPits;
                Marble marble = new Marble(marbleSSU, marbleWidth, marbleWidth, marbleXPos, marbleYPos);
                float angle = rand.nextFloat() * 360f;
                marble.rotate(angle);
                marble.setName("Marble" + marbles.size());
                marbles.add(marble);
                bottomPit.addMarble(marble);
            }
            pits.add(bottomPit);
        }
        float kalahaXPosP0 = boardWidth*0.925f;

        float kalahaYPos = boardHeight * 0.5f;
        int kalahaWidth = (int) (boardWidth * 0.12f);
        int kalahaHeight = (int) (boardHeight * 0.9f);
        Pit kalahaP0 = new Pit(kalahaXPosP0, kalahaYPos, kalahaWidth, kalahaHeight);

        kalahaP0.setName("Bottom Player Kalaha");
        pits.add(kalahaP0);

        int yPosTopPits = (int) ((double) boardHeight * 0.243);
        for (int pit = 0; pit < 6; pit++) {
            Pit topPit = new Pit(xPos[5-pit], yPosTopPits, pitWidth, pitHeight);
            topPit.setName("Top Player" + pit);
            for (int stone = 0; stone < stones; stone++) {
                float marbleXPos = ((float) ((Math.random() - 0.5) * pitWidth * 0.4)) + xPos[5-pit];
                float marbleYPos = (float) ((Math.random() - 0.5) * pitHeight * 0.4) + yPosTopPits;
                Marble marble = new Marble(marbleSSU, marbleWidth, marbleWidth, marbleXPos, marbleYPos);
                marble.setName("Marble" + marbles.size());
                marbles.add(marble);
                topPit.addMarble(marble);
            }
            pits.add(topPit);
        }

        float kalahaXPosP1 = boardWidth-kalahaXPosP0;
        Pit kalahaP1 = new Pit(kalahaXPosP1, kalahaYPos, kalahaWidth, kalahaHeight);
        kalahaP1.setName("Top Player Kalaha");
        pits.add(kalahaP1);
        return new GraphicalBoard(boardARGB8888.copy(Bitmap.Config.RGB_565, false), pits, marbles);
    }
}
