package androidmancala.movelist;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;

import boardgame.Move;
import androidmancala.GraphicObject;

public class MoveList {
    //The the starting position
    private float startX, startY;
    private float width, height;
    private float leftMarginal;
    private float rightMarginal;

    private List<Move> moves;
    //How many moves the user can see to choose between simultanously
    private int movesToShow;
    private List<MoveListMove> moveListMoves;
    //If scrolling along moves this help getting the right move
    //private int firstPitShowsMoveNumber;
    private float moveWidth;
    //The selectedPit of this movelist
    private int selectedPit;
    //The current position that the selected pit points at
    private int selectedPosition;

    //Temporaraily lazy skipping methods
    public GraphicObject previousButton, nextButton, selectFirstButton, selectLastButton;

    //When a user has selected am earlier position and then press next animationQueue is increased
    //The larget position that has been
    private int largestShownPosition = 0;
    //private boolean newSelectedPosition=false;

    public MoveList(int startX, int startY, int width, int height, List<MoveListMove> moveListMoves)
    {
        this.startX = startX;
        this.startY = startY;
        this.moves = new ArrayList<>();
        this.moveListMoves = moveListMoves;
        this.movesToShow = moveListMoves.size();
        this.setSize(width, height);
        int buttonWidth = (int) (height * 0.5f);
        this.previousButton = new MoveListButton(startX + (leftMarginal * 0.75f), startY + height / 2, buttonWidth, height);
        this.nextButton = new MoveListButton(startX + width - (rightMarginal * 0.75f), startY + height / 2, buttonWidth, height);
        buttonWidth = (int) (height * 0.7f);
        this.selectFirstButton = new MoveListButton(startX + (buttonWidth * 0.55f), startY + height / 2, buttonWidth, height);
        this.selectLastButton = new MoveListButton(startX + width - (buttonWidth * 0.55f), startY + height / 2, buttonWidth, height);
        this.largestShownPosition = 0;
        initMoveList();
    }

    public void setVisible(boolean visible){
        //previousButton
    }

    public void reset(){
        selectedPosition=0;
        largestShownPosition=0;
        clearMoves();
        selectFirst();
        updateMoveListMoves();
    }

    public List<MoveListMove> getMoveListMoves() {
        return moveListMoves;
    }

    public int getXPos() {
        return (int) (startX + (width * 0.5f));
    }

    public int getYPos() {
        return (int) (startY + (height * 0.5f));
    }


    public int movesToShow() {
        return movesToShow;
    }

    public int getLargestShownPosition() {
        return largestShownPosition;
    }

    private void updateLargestShownPosition(int position) {
        if (position > largestShownPosition)
            largestShownPosition = position;
    }

    public int getNumberOfPositions() {
        return moves.size() + 1;
    }

    public void addMove(Move move) {
        this.moves.add(move);
    }

    //Requres moves.size()>0
    public void removeLastMove() {
        Move last = moves.get(moves.size() - 1);
        moves.remove(last);
        largestShownPosition--;
    }

    public void clearMoves() {
        moves.clear();
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        this.leftMarginal = (int) (width * 0.2);
        this.rightMarginal = (int) (width * 0.2);
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public boolean pitIsSelectable(int pit) {
        return moveListMoves.get(pit).isSelectable();
    }

    // If user touches a pit in movelist return the pits index
    // otherwise return -1
    public int actionAtPit(int touchX, int touchY) {
        for (int pit = 0; pit < moveListMoves.size(); pit++) {
            if (moveListMoves.get(pit).atThis(touchX, touchY))// && pitIsSelectable(pit))
            {
                //System.out.println("SELECTED MOVE " + selectedPosition + "selectedPit" + selectedPit);
                //System.out.println(isLastPositionSelected());
                return pit;
            }
        }
        //no valid pit choosen
        return -1;
    }

    //Should remove sideeffects
    public boolean actionDownIsFirst(int touchX, int touchY) {
        //System.out.println("Test action down is first");
        if (selectFirstButton.atThis(touchX, touchY))
            return true;
        else
            return false;
    }

    //Should remove sideeffects
    public boolean actionDownIsLast(int touchX, int touchY) {
        //System.out.println("Test Action down is last");
        if (selectLastButton.atThis(touchX, touchY)) {
            return true;
        } else
            return false;
    }

    //Should remove sideeffects
    public boolean actionDownIsNext(int touchX, int touchY) {
        if (nextButton.atThis(touchX, touchY)) {
            return true;
        } else
            return false;
    }


    //Move to the next position if possible
    //increase selectedPosition and decrease the queue
    public synchronized boolean next() {
        if (nextIsPossible()) {
            selectedPosition++;
            if (nextPitIsPossible() && !moveListMoves.get(moveListMoves.size() - 1).hasAMove()) {
                nextPit();
            } else {
            }
            updateLargestShownPosition(selectedPosition);
            return true;
        } else
            System.exit(0);
        return false;
    }

    private boolean nextPitIsPossible() {
        return (selectedPit < movesToShow - 1) ? true : false;
    }

    private void nextPit() {
        if (nextPitIsPossible())
            selectedPit++;
    }

    private void backPit() {
        if (selectedPit > 0)
            selectedPit--;
    }

    public synchronized void back() {
        if (selectedPosition > 0) {
            selectedPosition--;
            largestShownPosition--;
            backPit();
        }
    }

    public int getPosition(int pit) {
        return moveListMoves.get(pit).getMoveNumber();
    }

    public void selectPit(int pit, int position)throws Exception {
        if (!(pit > 0 && pit < moveListMoves.size())) {
            throw new Exception("Cant select pit" + pit + "must be in range 0 to" + moveListMoves.size());
            //System.exit(0);
        }
        this.selectedPit = pit;
        this.selectedPosition = position;
    }

    //Sets selectedPosition the moveListmoves corresponding position
    public boolean selectPit(int pit) {
        MoveListMove mlm = moveListMoves.get(pit);
        //System.out.println("Select Pit" + pit + "Selected Position" + mlm.getMoveNumber());

        if (mlm.isSelectable()) {
            //If the pit is selectable and has a moverNumber
            //the position == moveNumber
            if (mlm.hasAMove())
                selectedPosition = mlm.getMoveNumber();
            else
                //The last selectable position has no moveNumber
                selectedPosition = moves.size();
            selectedPit = pit;
            updateLargestShownPosition(selectedPosition);
            return true;
        } else
            return false;
    }

    public void selectPosition(int position) {
        selectedPosition = position;
        updateLargestShownPosition(selectedPosition);
    }

    //There is always one more position available then there is moves
    public boolean nextIsPossible() {
        return (selectedPosition < moves.size() + 1);
    }

    public boolean actionDownIsPrevious(int touchX, int touchY) {
        //System.out.println("Action down is prevoius");
        if (previousButton.atThis(touchX, touchY))
            return true;
        else
            return false;
    }

    public void actionUp(int touchX, int touchY) {
        ;
    }

    //Assumes a move is queued
    public Move getMove() {
        return moves.get(selectedPosition);
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public int getSelectedPit() {
        return selectedPit;
    }

    public boolean isLastPositionSelected() {
        return (selectedPosition == moves.size());
    }

    public void draw(Canvas canvas) {
		/*
        previousButton.draw(canvas);
        nextButton.draw(canvas);
        selectFirstButton.draw(canvas);
        selectLastButton.draw(canvas);
        updateMoveListMoves();
        for(int moveCount=0;moveCount<movesToShow;moveCount++)
		{
        	moveListMoves.get(moveCount).draw(canvas);
		}*/
    }

    public void selectFirst() {
        selectedPosition = 0;
        selectedPit = 0;
    }

    public void selectLast() {
        //We always have one more position than moves
        selectedPosition = moves.size();
        selectedPit = Math.min(moveListMoves.size()-1,selectedPosition);
        /*if (moves.size() > moveListMoves.size()) {
            selectedPit = moveListMoves.size() - 1;
        } else {
            selectedPit = selectedPosition;
        }*/
    }

    public void updateMoveListMoves() {

        for (int moveCount = 0; moveCount < movesToShow; moveCount++) {
            MoveListMove moveListMove = moveListMoves.get(moveCount);

            int moveNumber = moveCount - selectedPit + selectedPosition;
            boolean isSelectable = moveNumber < moves.size() + 1;
            if (!isSelectable)
                moveListMove.setHasAMove(false);
            moveListMove.setSelectable(isSelectable);

            if (moveNumber < moves.size()) {
                int move = moves.get(moveNumber).getFrom();
                int player = moves.get(moveNumber).getPlayer();
                moveListMove.updateMove(moveNumber, move, player);
            } else if (moveNumber == moves.size()) {
                moveListMove.setMoveNumber(moveNumber);
                moveListMove.setHasAMove(false);
            }
			/*else
			{
				moveListMove.setHasAMove(false);
				moveListMove.setSelectable(false);
			}*/

            if (moveCount == selectedPit) {
                moveListMove.setSelected(true);
            } else
                moveListMove.setSelected(false);
            //moveListMove.updateBitmap();
        }
        //System.out.println("ML UPDATED selected pit"+selectedPit+"selectedPostion"+selectedPosition);
    }

    private void initMoveList() {
        float halfHeight = height / 2;
        float movesWidth = width - leftMarginal - rightMarginal;
        moveWidth = movesWidth / movesToShow;
        for (int i = 0; i < movesToShow; i++) {
            float xPos = startX + (moveWidth * (i + 0.5f)) + leftMarginal;

            float yPos = startY + halfHeight;
            boolean isSelectable = i <= moves.size();
            moveListMoves.get(i).setX(xPos);
            moveListMoves.get(i).setY(yPos);
            moveListMoves.get(i).setSelectable(isSelectable);
            moveListMoves.get(i).updateSprite();
        }
    }
}
