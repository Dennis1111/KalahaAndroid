package boardgame.algoritm;

import java.util.List;

public interface GameTreeWDL
{
  public char[] getBestMove();
  
  public double getEvaluation();
  
  //public List<String> bestMoveSequence();
  
  public List<GameNodeWDL> bestMoveSequence();

  public List<GameNodeWDL> getChildrens();
  
  public GameNodeWDL getRoot();
}
