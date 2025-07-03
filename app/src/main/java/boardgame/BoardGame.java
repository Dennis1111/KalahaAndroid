package boardgame;

import java.util.List;

public interface BoardGame
{
  int[] getBoard();
  List<int[]> getBoards();
  void move(char[] move);
  //The game is finished if cyclic postion occurs
  boolean cyclicEnd();
  //Create a game where the player to begin will be random
  void newGame(int playerToMove,int marblesPerPit);
  boolean newGame(int playerThatStarts,int marbles, int[] boardSetup);
  int[] createPosition(int[] pits, int player, int marbles) throws Exception;
  //public void setPlayersTurn(int player);
  int getPlayerToMove();
  int getPlayerToMove(int[] board);
  int getNumberOfPlayers();
  List<Integer> getSowingPits(int[] board, int move);
  int sow(int[] clonedBoard, int move);
  /*
  * @last which pit the last sowing marble ended up
  * */
  boolean isSteal(int[] board,int last);
  //The possible moves the player can do right now
  //If a player has n Possible moves the array will be of length n 
  //and each element will tell which move is possible
  List<char[]> getValidMoves();
  boolean isValidMove(char[] move);
  //return the move a player did  at moveNumber (0..moves.length-1)
  char[] getMove(int moveNumber);
  List<char[]> getMoves();
  boolean backLastMove();
  //Did the last move win the game
  int getGameState();
  boolean gameEnded();
  void back();
  //return all boards that that might be a cyclic pos after our next move
  List<int[]> getCyclicCandidates();
  String toString(int[] board);
  String toString(char[] board);
}
