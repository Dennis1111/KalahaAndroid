package boardgame;

import java.util.List;

/**
 * Created by Dennis on 2017-10-17.
 */

public interface DiceBoardGame extends BoardGame {
    //If dice is true we return the possible moves give the dice for for the position
    //otherwise we return all possible moves as if we had no dice
    List<char[]> getValidMoves(boolean dice);
    //We store the board in the board and getDiceIndex
    //int getFirstDiceIndex();
    //int getSecondDiceIndex();
}
