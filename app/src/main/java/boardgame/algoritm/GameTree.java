package boardgame.algoritm;

import java.util.List;

public interface GameTree
{
  public int getBestMove();
  
  public double getEvaluation();
  
  //public List<String> bestMoveSequence();
  
  public List<GameNode> bestMoveSequence();

  public GameNode getRoot();
}
