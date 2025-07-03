package boardgame;

public class WDLDepthEvaluation
{
  private double win;
  private double loss;
  private double draw;
  private double depth;
  
  public WDLDepthEvaluation(double win,double draw,double loss,double depth)
  {
    this.win=win;
    this.draw=draw;
    this.loss=loss;
    this.depth=depth;
  }

  public double getDepth()
  {
    return depth;
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
}
