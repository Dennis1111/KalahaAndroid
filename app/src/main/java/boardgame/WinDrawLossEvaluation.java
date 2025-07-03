package boardgame;

public class WinDrawLossEvaluation
{
  private double win;
  private double loss;
  private double draw;
  private double sum;
  public WinDrawLossEvaluation(double win,double draw,double loss)
  {
    this.win=win;
    this.draw=draw;
    this.loss=loss;
    sum=win+draw+loss;
  }

  public double getWinningProbability()
  {
    return win;
  }

  public double getLossProbability()
  {
    return loss;
  }

  public double getDrawProbability()
  {
    return draw;
  }

  public double getNormalizedWinningProbability()
  {
    return win/sum;
  }

  public double getNormalizedLossProbability()
  {
    return loss/sum;
  }

  public double getNormalizedDrawProbability()
  {
    return draw/sum;
  }
  
  public String toString()
  {
    return "Win="+win+" ,Loss="+loss+" ,Draw="+draw;
  }
}
