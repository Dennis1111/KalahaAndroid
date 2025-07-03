package androidmancala.animation;

/**
 * Created by Dennis on 2018-01-18.
 */

public class MarbleCollectionSound {
    protected float left,right;
    protected int marblesToDrop;
    protected int marblesInPit;

    public MarbleCollectionSound(int marblesToDrop, int marblesInPit, float left, float right){
       this.marblesToDrop=marblesToDrop;
       this.marblesInPit=marblesInPit;
       this.left=left;
       this.right=right;
    }

    public int getMarblesToDrop(){
        return marblesToDrop;
    }

    public int getMarblesInPit(){
        return marblesInPit;
    }

    public float getLeft(){
        return left;
    }

    public float getRight(){
        return right;
    }

    public void setLeft(float left){
        this.left=left;
    }

    public void setRight(float right){
        this.right=right;
    }
}
