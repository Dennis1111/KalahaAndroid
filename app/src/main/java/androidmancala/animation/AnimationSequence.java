package androidmancala.animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dennis on 2015-09-08.
 */
public class AnimationSequence {

    private List<AnimationPoint> sequence;
    private int index=0;

    public AnimationSequence()
    {
        sequence=new ArrayList<AnimationPoint>();
        index=0;
    }

    public List<AnimationPoint> getSequence()
    {
      return sequence;
    }

    public void add(AnimationPoint point)
    {
        sequence.add(point);
    }

    public AnimationPoint getPosition(int index)
    {
        return sequence.get(index);
    }

    public boolean hasNext()
    {
       return (index<sequence.size()-1) ? true : false;
    }

    public boolean isFinished()
    {
        return index>=sequence.size();
    }

    public AnimationPoint getAnimationPoint()
    {
        return sequence.get(index);
    }

    public void next()
    {
        index++;
    }

    public int getSize()
    {
        return sequence.size();
    }
}
