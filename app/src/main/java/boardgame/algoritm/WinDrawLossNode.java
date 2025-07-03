package boardgame.algoritm;

import java.util.List;
import boardgame.BoardGame;

public interface WinDrawLossNode
{
  public double getWinningProbability();
  public double getLooseProbability();
  public double getDrawProbability();
  public void setGameState(double win,double loss,double draw,int gamestate);  
}
