package boardgame.algoritm.alphabeta;

public class Value
{
  private double value;

  public Value(double value)
  {
    this.value=value;
  }

  public double getValue()
  {
    return value;
  }

  public void setValue(double value)
  {
    this.value=value;    
  }
}
