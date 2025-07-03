package androidmancala.animation;

import java.util.ArrayList;
import java.util.List;

import boardgame.PitPair;
import androidmancala.Marble;
import androidmancala.Pit;
import androidmancala.Position;

/**
 * Created by Dennis on 2015-10-27.
 */
public class AnimationUtil {

    //When we lift and drop marbles we increase/decrease their size
    private static int DELTA_SIZE=1;
    //how many loop steps to do the lift or drop
    private static int LIFT_DROP_STEPS=6;
    //how far we move
    private static int deltaYLift=-3;

    public static List<MarbleAnimation> getSowingAnimation(List<Integer> sowingPits,List<Pit> clonedPits,List<Pit> sourcePits)
    {
        //How many animationsteps between two pits
        int animationSteps=20;
        int from=sowingPits.get(0).intValue();

        List<MarbleAnimation> marbleAnimations = new ArrayList<>();
        List<Marble> marbles = clonedPits.get(from).getMarbles();
        int sowingPitsIndex=1;
        List<Pit> animationPits= new ArrayList<>();
        animationPits.add(sourcePits.get(from));
        for(Marble marble : marbles)
        {
            int to= sowingPits.get(sowingPitsIndex).intValue();
            animationPits.add(sourcePits.get(to));
            AnimationSequence animSeq = pitToPitDeltaSequence(animationPits, animationSteps);
            MarbleAnimation marbleAnimation = new MarbleAnimation(marble, animSeq, sourcePits.get(from), sourcePits.get(to));
            marbleAnimations.add(marbleAnimation);
            sowingPitsIndex++;
        }
        return marbleAnimations;
    }

    public static List<MarbleAnimation> getAnimation(PitPair pitPair,List<Pit> clonedPits,List<Pit> sourcePits)
    {
        int from=pitPair.getFirst();
        int to=pitPair.getSecond();
        float xDistance = clonedPits.get(from).getX()-clonedPits.get(to).getX();
        float yDistance = clonedPits.get(from).getY()-clonedPits.get(to).getY();
        double distance = Math.sqrt(xDistance*xDistance+yDistance*yDistance);

        int animationSteps=(int)(distance*0.07);
        List<MarbleAnimation> marbleAnimations = new ArrayList<>();
        List<Marble> marbles = clonedPits.get(from).getMarbles();
        for(Marble marble : marbles)
        {
            List<Pit> animationPits= new ArrayList<>();
            animationPits.add(sourcePits.get(from));
            animationPits.add(sourcePits.get(to));
            AnimationSequence animSeq = pitToPitDeltaSequence(animationPits, animationSteps);
            MarbleAnimation marbleAnimation = new MarbleAnimation(marble, animSeq, sourcePits.get(from), sourcePits.get(to));
            marbleAnimations.add(marbleAnimation);
        }
        return marbleAnimations;
    }

    /*
    //Assumes a marble is lifted and on the way from..to
    private AnimationSequence pitToPitSequence(List<Pit> pits, Marble marble, int pitAnimationSteps) {
        AnimationSequence anim = new AnimationSequence();
        Pit startPit = pits.get(0);
        double x = marble.getX();
        double y = marble.getY();
        //List<Position> path= new ArrayList<Position>();

        for (int pit = 0; pit < pits.size() - 1; pit++) {
            double distanceX = pits.get(pit + 1).getX() - pits.get(pit).getX();
            double distanceY = pits.get(pit + 1).getY() - pits.get(pit).getY();
            //Now we have a fixed number of animationSteps between pits
            //that will cause bigger steps and faster animation if pits far away
            double deltaX = distanceX / pitAnimationSteps;
            double deltaY = distanceY / pitAnimationSteps;
            //System.out.println("pit"+pit);
            for (int step = 0; step < pitAnimationSteps; step++) {
                x += deltaX;
                y += deltaY;
                //System.out.println(step+"add"+x+","+y);
                anim.add(new AnimationPoint(new Position((int) x, (int) y)));
            }
        }
        return anim;
    }*/

    //Assumes a marble is lifted and on the way from..to
    public static AnimationSequence pitToPitDeltaSequence(List<Pit> pits, int pitAnimationSteps) {
        AnimationSequence anim = new AnimationSequence();
        double x = 0;
        double y = 0;

        for(int lifting=0;lifting<LIFT_DROP_STEPS;lifting++) {
               AnimationPoint liftAP = new AnimationPoint(new Position(0, deltaYLift));
               liftAP.setDeltaSize(DELTA_SIZE);
               anim.add(liftAP);
        }

        for (int pit = 0; pit < pits.size() - 1; pit++) {
            double distanceX = pits.get(pit + 1).getX() - pits.get(pit).getX();
            double distanceY = pits.get(pit + 1).getY() - pits.get(pit).getY();
            //Now we have a fixed number of animationSteps between pits
            //that will cause bigger steps and faster animation if pits far away
            double deltaX = distanceX / pitAnimationSteps;
            double deltaY = distanceY / pitAnimationSteps;
            double prevX = x;
            double prevY = y;
            //Lift the marbles

            for (int step = 0; step < pitAnimationSteps; step++) {
                x += deltaX;
                y += deltaY;
                int deltaXInt = (int) x - (int) prevX;
                int deltaYInt = (int) y - (int) prevY;
                //It might seems that deltaX = x-prevX but that might differ after rounding
                anim.add(new AnimationPoint(new Position(deltaXInt, deltaYInt)));
                prevX = x;
                prevY = y;
            }
        }

        for(int lifting=0;lifting<LIFT_DROP_STEPS;lifting++) {
            AnimationPoint dropAP = new AnimationPoint(new Position(0, -deltaYLift));
            dropAP.setDeltaSize(-DELTA_SIZE);
            anim.add(dropAP);
        }
        return anim;
    }
}
