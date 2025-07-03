package androidmancala.animation;

import androidmancala.GraphicObject;

/**
 * Created by Dennis on 2015-09-08.
 */
public class Animation {

    private GraphicObject go;
    private AnimationSequence animSequence;
    //private int animationCount=0;

    public Animation(GraphicObject go,AnimationSequence seq)
    {
        this.go=go;
        this.animSequence = new AnimationSequence();
        this.animSequence = seq;
    }

    public boolean isFinished()
    {
        return animSequence.isFinished();
    }

    //Return true if there is more to animate
    public void animate()
    {
        if (!animSequence.isFinished())
          {
             AnimationPoint point = animSequence.getAnimationPoint();
             //AnimationPoint point = animSequence.getNext();
             go.setX(go.getX() + point.getPosition().getX());
             go.setY(go.getY() +point.getPosition().getY());
             int sizeChange = point.getDeltaSize();
             if (sizeChange>0)
                go.increaseSize();
              if (sizeChange<0)
                go.decreaseSize();
             //animationCount++;
             //System.out.println("New Anim pos"+go.getX()+go.getY()+","+animationCount);
             animSequence.next();
          }
    }
}
