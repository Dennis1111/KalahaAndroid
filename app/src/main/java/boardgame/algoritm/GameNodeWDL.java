package boardgame.algoritm;

import java.util.List;

public interface GameNodeWDL extends WinDrawLossNode
{
  public GameNodeWDL getParent();
  public GameNodeWDL bestChild();
  public boolean isTerminal();
  public char[] getMoveFromParent();
  public int[] getBoard();
  public List<GameNodeWDL> getChildrens(); 
  public double getScore();
    
  //When all chidrens is finished (calculating) this node is finished
  public boolean finished();
  public boolean maximizeChildrens();
  public boolean isRoot();
  //The best move in this position
  public char[] bestMove();
  //the move that generated this position  
}
