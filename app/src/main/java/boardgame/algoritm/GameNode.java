package boardgame.algoritm;

import java.util.List;

public interface GameNode
{
  public GameNode getParent();
  public GameNode bestChild();
  public boolean isTerminal();
  public int[] getBoard();
  public List<GameNode> getChildrens();  
  public double getScore();
  //When all chidrens is finished (calculating) this node is finished
  public boolean finished();
  public boolean maximizeChildrens();  
  public boolean isRoot();
  //The best move in this position
  public int bestMove();
  //the move that generated this position  
}
