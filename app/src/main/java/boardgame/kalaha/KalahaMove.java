package boardgame.kalaha;

import boardgame.BoardGameUtil;
import boardgame.Move;

public class KalahaMove implements Move {
    private int player;
    //private int pit;
	private char[] move;

    public KalahaMove(int player,int move)
    {
        this.player=player;
        this.move= BoardGameUtil.createMove(move);
    }


    public KalahaMove(int player,char[] move)
    {
		this.player=player;
		this.move=move;
    }		
		
    public char[] getMove()
    {		
	   return move;
    }
		
	public int getPlayer()
	{
    	return player;
	}

   public int getFrom()
   {
	  return move[0]-48;
   }
   
   //Not needed in kalaha
   public int getTo()
   {
	  return 0;	  
   }
}
