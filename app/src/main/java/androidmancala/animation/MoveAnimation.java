package androidmancala.animation;

import java.util.ArrayList;
import java.util.List;

import androidmancala.MancalaSoundPool;
import androidmancala.MancalaUtil;
import androidmancala.opengl.TextureAtlas;

/**
 * Created by Dennis on 2015-09-10.
 */
public class MoveAnimation {

    private List<List<MarbleAnimation>> marbleAnimations;
    //private List<Integer> sameTargetPit;
    //private List<Boolean> sowing;
    private TextureAtlas atlas;
    //Points at the the first list of marbleAnimations not finished
    private int animationIndex;
    //Point at the the first inner marbleAnimation not finished

    private int innerAnimIndex;
    private MancalaSoundPool soundPool;
    //private boolean firstAnim=true;

    public MoveAnimation(TextureAtlas atlas, MancalaSoundPool soundPool) {
        this.marbleAnimations = new ArrayList<>();
        //this.sameTargetPit = new ArrayList<>();
        //marbleCollectionSound = new ArrayList<>();
        this.soundPool = soundPool;
        animationIndex = 0;
        innerAnimIndex = 0;
        this.atlas = atlas;
    }

    public void clear() {
        this.marbleAnimations = new ArrayList<>();
    }

    /*public void add(List<MarbleAnimation> animations) {
        this.marbleAnimations.add(animations);
        //sameTargetPit.add(0);
    }*/

    //When we have marbleAnimations that ends up in the same pit we store that co we can
    //later choose a correct soundfile
    public void add(List<MarbleAnimation> animations) {
        this.marbleAnimations.add(animations);
        //sameTargetPit.add(animations.size());
        //marbleCollectionSound.add(sound);
    }

    public boolean isFinished() {
        //System.out.println("isfinished"+marbleAnimations.size());
        return animationIndex >= marbleAnimations.size();
        //return (marbleAnimations.size()==0);
    }

    public void animate() {
        /*firstAnim=false;
        if (marbleAnimations.size()==0)
            return;*/
        //System.out.println("anim"+animationIndex+" , "+innerAnimIndex);
        List<MarbleAnimation> animationsFirst = marbleAnimations.get(animationIndex);
        //List<MarbleAnimation> finishedAnimations = new ArrayList<>();
        //MarbleCollectionSound marbleCollSound = marbleCollectionSound.get(animationIndex);
        boolean hasMoreAnimations = true;
        for (int inner = innerAnimIndex; inner < animationsFirst.size(); inner++)
        //for(MarbleAnimation animation : animationsFirst)
        {
            MarbleAnimation animation = animationsFirst.get(inner);
            if (animation.firstAnimation)
                atlas.putLast(animation.getMarble());
            animation.animate();
            if (animation.isFinished()) {
                if (innerAnimIndex == animationsFirst.size() - 1) {
                    //We have just finished the last marbleAnimation at animationIndex
                    //so set the indexes to point at next animation seq
                    animationIndex++;
                    innerAnimIndex = 0;
                } else
                    innerAnimIndex++;
             }
        }
    }

    public String toString() {
        String moveAnim = "Move Animation";
        int i = 0;
        for (List<MarbleAnimation> marbleAnimList : marbleAnimations) {
            moveAnim += "list " + i + " size " + marbleAnimList.size() + " , ";
        }
        return moveAnim;
    }
}
