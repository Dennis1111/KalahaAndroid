package boardgame.algoritm;

public interface Algoritm
{
  public SearchTree evaluate();
  
  public void setMaxThreads(int numberOfThreads);
  
  public void setMaxDepth(int i);
  
  public void setSkill(int i);

  public void setMaxTime(int seconds);


}
