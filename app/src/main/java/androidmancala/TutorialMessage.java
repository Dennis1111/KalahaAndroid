package androidmancala;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidmancala.opengl.Sprite;

/**
 * Created by Dennis on 2017-10-01.
 */

public class TutorialMessage {
    private boolean computerMove=false;
    private int[] boardSetup;
    private char[] move;
    private Pit pit;
    private boolean moveIsReady = false;
    private List<String> messages;
    private List<String> postMoveMessages;
    private boolean started = false;
    private boolean canGoNext = true;
    private Sprite showSprite=null;

    public TutorialMessage() {
        this.messages = new ArrayList<>();
        this.postMoveMessages = new ArrayList<>();
    }

    public void setSprite(Sprite sprite){
         this.showSprite=sprite;
    }

    public Sprite getSprite(){
        return showSprite;
    }

    public void setupPosition(int[] board){
        this.boardSetup=board;
    }

    public int[] getBoard(){
        return boardSetup;
    }

    public boolean hasMove(){
        return move!=null;
    }

    public char[] consumeMove() {
        moveIsReady = false;
        char[] copy = Arrays.copyOf(move, move.length);
        move = null;
        return copy;
    }
    public void setCanGoNext(boolean canGoNext){
        this.canGoNext=canGoNext;
    }
    public char[] getMove() {
        return move;
    }

    public void setReadyForNext() {
        this.canGoNext = true;
    }

    public boolean canGoNext() {
        return canGoNext;
    }

    public boolean started() {
        return started;
    }

    public void start() {
        this.started = true;
    }

    public boolean hasComputermove(){
        return computerMove;
    }

    public void setComputerMove(char[] move) {
        this.pit = null;
        this.move = move;
        this.computerMove=true;
        this.canGoNext = true;
    }

    public void setPitMove(Pit pit, char[] move) {
        this.pit = pit;
        this.move = move;
        this.canGoNext = false;
    }

    public boolean actionDown(int x, int y) {
        if (pit != null && pit.atThis(x, y)) {
            moveIsReady = true;
        }
        return moveIsReady;
    }

    public boolean moveIsReady() {
        return moveIsReady;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addPostMessage(String message) {
        postMoveMessages.add(message);
    }

    public List<String> getPostMoveMessages() {
        return postMoveMessages;
    }

    public List<String> getCurrentMessages() {
        if (postMoveMessages.size() > 0 && canGoNext)
            return postMoveMessages;
        else
            return messages;
    }
}
