package boardgame.algoritm.alphabeta;

import boardgame.*;
import boardgame.algoritm.*;

public interface AlphaBetaSIWDL
{
  GameTreeWDL evaluate(BoardGameWDL boardgame);
  
  void setMaxDepth(int i);
  
  void setSkill(int i);

  void setMaxTime(int seconds);
}
