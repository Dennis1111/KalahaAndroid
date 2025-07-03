package boardgame;

import java.util.List;

/**
 * Created by Dennis on 2017-10-18.
 */

public interface MinMaxDiceGame {
    //@cyclicTest all prior boards before the 'board' position that is relevant for cyclic testing
    //+ the current board (though in oware (and all games?) some moves before a cyclic pos is reached so the last board isn't needed in cyclicTest)
    int[] move(char[] move,int[] board,List<int[]> cyclicTest);

    double getMoveDepth(char[] move,int[] board,int[] childBoard);
    /*@ cyclicTest all prior boards before the 'board' position that is relevant for cyclic testing
    * a solution to add the game oware where the game can end up in infinite cycles but an ugly (temp?) design
    * @useDice assumes the dice is stored in board and if false we will get all possible moves
    * */
    List<char[]> getValidMoves(int[] board,List<int[]> cyclicTest,List<int[]> childrens,boolean useDice);
    int getGameState(int[] board);
    double getWinningProbability(double score,double maxScore);
    double getScore(int[] board);
    int getPlayerToMove(int[] board);
    boolean isCyclicCandidate(int[] board1,int[] board2);
}
