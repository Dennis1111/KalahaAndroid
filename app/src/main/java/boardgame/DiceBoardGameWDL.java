package boardgame;

/**
 * Created by Dennis on 2017-10-17.
 */

public interface DiceBoardGameWDL extends DiceBoardGame, MinMaxDiceGame {
    WinDrawLossEvaluation evaluate(int[] board);
    String[] getFileNames();
}
