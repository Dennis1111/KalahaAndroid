package androidmancala;

import boardgame.algoritm.GameTreeWDL;

public interface Receiver {
	public void receive(GameTreeWDL gameTree,boolean finished,int boardPosition,int player);
}
