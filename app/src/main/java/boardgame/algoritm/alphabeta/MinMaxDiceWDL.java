package boardgame.algoritm.alphabeta;

import boardgame.BoardGameWDL;
import boardgame.DiceBoardGameWDL;
import boardgame.algoritm.GameTreeWDL;

/**
 * Created by Dennis on 2017-10-18.
 */

public interface MinMaxDiceWDL {
    GameTreeWDL evaluate(DiceBoardGameWDL boardgame);
    void setMaxDepth(int i);
}
