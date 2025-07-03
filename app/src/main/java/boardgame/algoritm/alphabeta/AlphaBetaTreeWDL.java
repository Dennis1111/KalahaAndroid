package boardgame.algoritm.alphabeta;

import boardgame.algoritm.GameNodeWDL;
import boardgame.algoritm.GameTreeWDL;
import java.util.*;

public class AlphaBetaTreeWDL implements GameTreeWDL
{
  //The best move
  private GameNodeWDL root;
  private List<GameNodeWDL> moveCandidates;
  private List<GameNodeWDL> childrens; 

  public AlphaBetaTreeWDL(GameNodeWDL root)
  {
    this.root=root;
    this.moveCandidates=new ArrayList<GameNodeWDL>();
    this.childrens=new ArrayList<GameNodeWDL>();
  }

  public List <GameNodeWDL> getChildrens()
  {
    return childrens;
  }

  public void addChild(GameNodeWDL child)
  {
    childrens.add(child);
  }
  
  public GameNodeWDL getRoot()
  {
    return root;
  }
  
  public List<GameNodeWDL> getCandidates()
  {
    return moveCandidates;
  }

  public void addCandidate(GameNodeWDL candNode)
  {
    moveCandidates.add(candNode);
  }
  
  public char[] getBestMove()
  {
    return root.bestMove();
  }
  
  public double getEvaluation()
  {
    return root.getScore();
  }

  public List<GameNodeWDL> bestMoveSequence()
  {
    List<GameNodeWDL> ms= new ArrayList<GameNodeWDL>();
    //ms.add(""+root.bestMove());
    GameNodeWDL node=root;
    ms.add(root);
    while(node.bestChild()!=null)
      {	
	node=node.bestChild();
	ms.add(node);
      }
    return ms;
  }
  
  /*
  public List<String> bestMoveSequence()
  {
    List<String> ms= new ArrayList<String>();
    //ms.add(""+root.bestMove());
    AlphaBetaNode node=root;
    while(node.bestChild()!=null)
      {
	ms.add(""+node.bestMove());
	node=node.bestChild();
      }
    return ms;    
    }*/

  public String toString()
  {
    String treeView="";
    GameNodeWDL node=root;
    while(node.bestChild()!=null)
      {
	treeView+=node;
	node=node.bestChild();
      }
    return treeView;
  }
}
