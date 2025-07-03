package boardgame.algoritm;

import java.util.List;

public interface TreeSearchSupport
{
  //The gamestate for the player on turn; 
  public enum GAMESTATE{WIN,DRAW,LOSS,HEURISTIC};
  public int[] getBoard();
  public void move(int move);
  //Create a game where the player to begin will be random
  public void newGame();
  public void setPlayersTurn(int player);
  public int getPlayerToMove();
  public int getNumberOfPlayers();

  //The possible moves the player can do right now
  //If a player has n Possible moves the array will be of length n 
  //and each element will tell which move is possible
  public int[] getValidMoves(int[] board);
  //The moves in the game so far;
  public int getMove(int moveNr);
  public List<Integer> getMoves();
  public boolean backLastMove();
  //Did the last move win the game
  public GAMESTATE gameState();  
}
