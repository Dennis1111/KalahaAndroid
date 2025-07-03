package androidmancala.animation;

import androidmancala.Position;

/**
 * Created by Dennis on 2015-09-08.
 */
public class AnimationPoint {
    private Position position;
    private int deltaSize;

    public AnimationPoint(Position position)
    {
        this.position=position;
        this.deltaSize=0;
    }

    public Position getPosition()
    {
        return position;
    }

    public void setDeltaSize(int deltaSize)
    {
        this.deltaSize=deltaSize;
    }

    public int getDeltaSize()
    {
        return deltaSize;
    }
}
