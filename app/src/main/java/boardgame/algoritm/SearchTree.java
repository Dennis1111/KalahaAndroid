package boardgame.algoritm;

import java.util.List;

public interface SearchTree
{
  public int getBestMove();
  
  public double getEvaluation();
  
  public List<String> bestMoveSequence();
  
  public SearchTree getParent();
}
