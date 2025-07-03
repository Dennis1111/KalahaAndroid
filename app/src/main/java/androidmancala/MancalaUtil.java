package androidmancala;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import androidmancala.animation.MarbleCollectionSound;
import androidmancala.menu.R;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidmancala.movelist.MoveList;
import androidmancala.movelist.MoveListMove;
import boardgame.PitPair;
import mlfn.MultiLayerFN;

/**
 * Created by Dennis on 2017-11-01.
 */

public class MancalaUtil {

    private static final float left = 0f, up = 90f, right = 180f, down = 270f;

    public static final String[] marbleBitmaps = {"blue_marble3","blue_marble2","orange_marble4","green_marble3"};

    public static MarbleCollectionSound getMCSound(int marblesToDrop, int marblesInPit, int dropAtX, int width) {
        float leftEdgeDistance=dropAtX;
        float rightEdgeDistance=width-dropAtX;
        float leftVol = (width - leftEdgeDistance*0.7f)/width;
        float rightVol = (width - rightEdgeDistance * 0.7f)/width;
        return new MarbleCollectionSound(marblesToDrop, marblesInPit, leftVol, rightVol);
    }

    public static MultiLayerFN[] readMLFNs(String[] names, Resources resources) {
        //int nets = KalahaMLFN.getNumberOfNets();
        MultiLayerFN[] mlps = new MultiLayerFN[names.length];
        for (int i = 0; i < names.length; i++) {
            int id = resources.getIdentifier(names[i], "raw", "mancalapackage.menu");
            System.out.println("Read" + names[i]+"ID="+id);

            try {
                DataInputStream is = new DataInputStream(resources.openRawResource(id));
                //ObjectInputStream ois = new ObjectInputStream(is);
                MultiLayerFN mlp = MultiLayerFN.load(is);
                //mlp.updateActiveWeights();
                mlps[i] = mlp;
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mlps;
    }


    /* Assumes there are bitmaps stored as "marble"+n*/
    public static List<Bitmap> getMarbleBitmaps(Context context, int destWidth, int destHeight) {
        Resources res = context.getResources();
        List<Bitmap> bitmaps = new ArrayList<>();
        for (int i = 0; i < marbleBitmaps.length; i++) {
            Bitmap marble = BitmapFactory.decodeResource(res, res.getIdentifier(marbleBitmaps[i], "drawable", context.getPackageName()));
            Bitmap scaledMarble = Bitmap.createScaledBitmap(marble, destWidth, destHeight, false);
            bitmaps.add(scaledMarble);
        }
        return bitmaps;
    }

    /* Sets up the position of marbles and put them int their pits */
    public static void setupPitsAndMarbles(List<Pit> targetPits, List<Marble> marbles, int[] board) {
        int marbleCount = 0;
        Random rand = new Random();
        float maxDist = marbles.get(0).getWidth() * 0.2f;
        int maxTrials = 2;
        for (int pitIndex = 0; pitIndex < 14; pitIndex++) {
            //System.out.println("setup pit" + pitIndex);
            int pitMarbleCount = board[pitIndex];
            Pit pit = targetPits.get(pitIndex);
            pit.clearMarbles();
            for (int pitMarble = 0; pitMarble < pitMarbleCount; pitMarble++) {
                Marble marble = marbles.get(marbleCount);
                int trials = 1;
                do {
                    //System.out.println(pitIndex + "Marble" + pitMarble + " T" + trials + "pitSize" + pit.getMarbles().size());
                    float marbleXPos = (float) ((Math.random() - 0.5) * pit.getWidth() * 0.5) + pit.getX();
                    float marbleYPos = (float) ((Math.random() - 0.5) * pit.getHeight() * 0.5) + pit.getY();
                    marble.setX(marbleXPos);
                    marble.setY(marbleYPos);
                } while (isClose(marble, pit.getMarbles(), maxDist) && trials++ < maxTrials);
                float angle = rand.nextFloat() * 360f;
                marble.rotate(angle);
                pit.addMarble(marble);
                marbleCount++;
            }
        }
    }

    //The static part of the bottomPanel
    public static Bitmap createBottomPanel(Resources resources, Bitmap pitImage, MoveList moveList, int width, int height) {
        //Bitmap backgroundMoveList = BitmapFactory.decodeResource(resources, R.drawable.background_move_list);
        Bitmap backgroundMoveList = BitmapFactory.decodeResource(resources, R.drawable.background);

        Bitmap scaledMoveListBG = Bitmap.createScaledBitmap(backgroundMoveList, width, height, false);
        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        Paint paintText = new Paint();
        //paintText.setColor(Color.BLACK);
        canvas.drawBitmap(scaledMoveListBG, 0, 0, paintText);

        Bitmap previousImage = BitmapFactory.decodeResource(resources, R.drawable.previous);
        int left = (int) (moveList.previousButton.getX() - moveList.previousButton.getWidth() / 2);
        int right = (int) (left + moveList.previousButton.getWidth());
        int top = (int) (moveList.getHeight() / 2f - moveList.previousButton.getHeight() / 2);
        int bottom = (int) (top + moveList.previousButton.getHeight());
        Rect dst = new Rect(left, top, right, bottom);
        canvas.drawBitmap(previousImage, null, dst, null);
        previousImage.recycle();

        Bitmap nextImage = BitmapFactory.decodeResource(resources, R.drawable.next);
        left = (int) (moveList.nextButton.getX() - moveList.nextButton.getWidth() / 2);
        right = (int) (left + moveList.nextButton.getWidth());
        dst = new Rect(left, top, right, bottom);
        canvas.drawBitmap(nextImage, null, dst, null);
        nextImage.recycle();

        Bitmap selectFirstImage = BitmapFactory.decodeResource(resources, R.drawable.first);
        //selectFirstImage = Bitmap.createScaledBitmap(selectFirstImage, (int) (buttonWidth * 1.2), buttonHeight, false);
        left = (int) (moveList.selectFirstButton.getX() - moveList.selectFirstButton.getWidth() / 2);
        right = (int) (left + moveList.selectFirstButton.getWidth());
        dst = new Rect(left, top, right, bottom);
        canvas.drawBitmap(selectFirstImage, null, dst, null);
        selectFirstImage.recycle();

        Bitmap selectLastImage = BitmapFactory.decodeResource(resources, R.drawable.last);
        //selectLastImage = Bitmap.createScaledBitmap(selectLastImage, (int) (buttonWidth * 1.2), buttonHeight, false);
        left = (int) (moveList.selectLastButton.getX() - moveList.selectLastButton.getWidth() / 2);
        right = (int) (left + moveList.selectLastButton.getWidth());
        dst = new Rect(left, top, right, bottom);
        canvas.drawBitmap(selectLastImage, null, dst, null);
        selectLastImage.recycle();

        MoveListMove first = moveList.getMoveListMoves().get(0);
        float mlmHeight= first.getHeight();
        //We make a little space on top and bottom
        float mlmShrinked = mlmHeight*0.96f;
        float mlmTop = (mlmHeight-mlmShrinked)/2.0f;
        for (MoveListMove mlm : moveList.getMoveListMoves()) {
            left = (int) (mlm.getX() - mlm.getWidth() / 2);
            right = (int) (left + mlm.getWidth());
            dst = new Rect(left,(int)mlmTop, right, (int) (mlmTop+mlmShrinked));
            canvas.drawBitmap(pitImage, null, dst, null);//Most paint is ok
        }
        return temp;
    }

    public static Bitmap createCircle(int width, int height, int thickness) {
        Bitmap pitBorder = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(pitBorder);
        RectF rect = new RectF(0, 0, width, height);
        Paint backgroundPaint = new Paint();
        backgroundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawRect(rect, backgroundPaint);
        Paint ovalPaint = new Paint();
        ovalPaint.setColor(Color.WHITE);
        canvas.drawOval(rect, ovalPaint);
        canvas.drawOval(new RectF(thickness, thickness, width - thickness, height - thickness), backgroundPaint);
        return pitBorder;
    }


    //Is the marble Close to any other marble
    public static boolean isClose(Marble marble, List<Marble> marbles, float maxDist) {
        for (Marble compare : marbles) {
            if (marble.equals(compare))
                continue;
            float xDist = marble.getX() - compare.getX();
            float yDist = marble.getY() - compare.getY();
            //System.out.println(maxDist + " : " + xDist + " : " + yDist);
            if (Math.abs(xDist) < maxDist)
                return true;
            if (Math.abs(yDist) < maxDist)
                return true;
        }
        return false;
    }

    /* Move the marbles in a pit depending on their previous vx,vy
    *  and current pit+ other marble impacts
    *  1. Assume the bottom marbles comes first
    * */
    public static void updatePitMarbles(List<Pit> pits) {
        for (Pit pit : pits) {
            List<Marble> marbles = pit.getMarbles();
            calcMarbleZ(marbles);
            updatePitForces(marbles, pit);

        }
    }

    /* Use the forces and the marbles current movement to calculate a new movement vx,vy
    * if the movement causes collision the marbles shuold stop but thats a later fix
    * */
    private void move(Marble marble) {
        marble.vx = marble.vx + marble.xForces;
        marble.vy = marble.vy + marble.yForces;
        marble.move(marble.vx, marble.vy);
    }

    private static void calcMarbleZ(List<Marble> marbles) {
        for (Marble marble : marbles)
            marble.setZ(0);
        for (int first = 0; first < marbles.size(); first++) {
            for (int sec = 0; sec < marbles.size(); sec++) {
                Marble firstMarble = marbles.get(first);
                Marble secMarble = marbles.get(sec);
                if (secMarble.overlap(firstMarble) > 0f)
                    secMarble.setZ(firstMarble.getZ() + 1);
            }
        }
    }

    protected static float[] impact(Marble m1, Marble m2) {
        return null;
    }

    /* Calculate the forces a pit has on a marble (a simplified solution */
    private static void updatePitForces(List<Marble> marbles, Pit pit) {
        for (Marble marble : marbles) {
            //When the marble doesnt touch the pit there is no impact
            if (marble.getZ() != 0f)
                return;
            //The impact is stronger near the edge of the pit
            float xMarbleCenterDist = pit.getX() - marble.getX();
            float xEdgeDistance = Math.abs(xMarbleCenterDist) - (pit.getWidth() / 2);
            marble.xForces = xEdgeDistance / pit.getWidth();
            float yMarbleCenterDist = pit.getY() - marble.getY();
            float yEdgeDistance = Math.abs(xMarbleCenterDist) - (pit.getHeight() / 2);
            marble.yForces = yEdgeDistance / pit.getHeight();

        }
    }

    public static float[] createOpenGLRGBA(float angle, float red, float green, float blue) {
        float rgba[] = new float[16];
        float upDistance = getAngleDistance(angle, up);
        float[] upColor = interpolateWhite(upDistance, red, blue, green);
        float leftDistance = getAngleDistance(angle, left);
        float[] leftColor = interpolateWhite(leftDistance, red, blue, green);
        float rightDistance = getAngleDistance(angle, right);
        float[] rightColor = interpolateWhite(rightDistance, red, blue, green);
        float downDistance = getAngleDistance(angle, down);
        float[] downColor = interpolateWhite(downDistance, red, blue, green);

        rgba[0] = leftColor[0];
        rgba[1] = leftColor[1];
        rgba[2] = leftColor[2];
        rgba[3] = leftColor[3];
        rgba[4] = upColor[0];
        rgba[5] = upColor[1];
        rgba[6] = upColor[2];
        rgba[7] = upColor[3];
        rgba[8] = rightColor[0];
        rgba[9] = rightColor[1];
        rgba[10] = rightColor[2];
        rgba[11] = rightColor[3];
        rgba[12] = downColor[0];
        rgba[13] = downColor[1];
        rgba[14] = downColor[2];
        rgba[15] = downColor[3];
        return rgba;
    }

    private static float getAngleDistance(float angle1, float angle2) {
        float diff = Math.max(angle1, angle2) - Math.min(angle1, angle2);
        if (diff > 180)
            diff = 360 - diff;
        return diff;
    }

    private static float[] interpolateWhite(float distance, float red, float green, float blue) {
        float[] rgba = new float[4];
        float whitePerc = (180f - distance) / 180f;
        rgba[0] = (whitePerc) + ((1 - whitePerc) * red);
        rgba[1] = (whitePerc) + ((1 - whitePerc) * green);
        rgba[2] = (whitePerc) + ((1 - whitePerc) * blue);
        rgba[3] = 0.5f;
        return rgba;
    }

    //Will not copy the positions ot the marbles
    public static List<Pit> copyPits(List<Pit> source) {
        List<Pit> copyPits = new ArrayList<>();
        for (Pit pit : source) {
            Pit copy = new Pit(pit.getX(), pit.getY(), pit.getWidth(), pit.getHeight());
            for (Marble marble : pit.getMarbles())
                copy.addMarble(marble);
            copyPits.add(copy);
        }
        return copyPits;
    }

    public static boolean isSame(int[] board, List<Pit> pits) {
        for (int i = 0; i < pits.size(); i++) {
            if (board[i] != pits.get(i).getMarbles().size())
                return false;
        }
        return true;
    }

    //Will transfer all marbles from one pit to another (given by pitPair)
    public static void transfer(PitPair pitPair, List<Pit> localPits) {
        int first = pitPair.getFirst();
        List<Marble> marbles = localPits.get(first).getMarbles();
        Pit target = localPits.get(pitPair.getSecond());
        for (Marble marble : marbles)
            localPits.get(first).transferToPit(marble, target);
    }
}
