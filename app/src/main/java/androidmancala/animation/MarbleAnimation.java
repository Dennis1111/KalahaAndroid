package androidmancala.animation;

import androidmancala.MancalaSoundPool;
import androidmancala.Marble;
import androidmancala.Pit;

/**
 * Created by Dennis on 2015-09-08.
 */
public class MarbleAnimation extends Animation
{
    private Pit start;
    private Pit end;
    private Marble marble;
    protected boolean firstAnimation=true;
    protected MarbleCollectionSound sound;
    protected MancalaSoundPool soundPool;

    public MarbleAnimation(Marble marble,AnimationSequence seq,Pit start,Pit end)
    {
        super(marble,seq);
        this.marble=marble;
        this.start=start;
        this.end=end;
        this.sound=null;
    }

    public void setSound(MarbleCollectionSound sound,MancalaSoundPool soundPool){
        this.sound=sound;
        this.soundPool=soundPool;
    }

    public void animate()
    {
        if (firstAnimation)
        {
            start.removeMarble(marble);
            firstAnimation=false;
        }
        super.animate();

        if (isFinished())
        {
            if (sound!=null) {
                soundPool.play(sound);
            }
            if (!end.getMarbles().contains(marble))
            end.addMarble(marble);
        }
    }

    public Marble getMarble()
    {
        return marble;
    }

    public Pit getStartPit()
    {
        return start;
    }

    public Pit getEndPit()
    {
        return end;
    }

    public String toString()
    {
        return "MARBLE Animation"+start+" , "+end;
    }
}
