package boardgame;

public interface BoardGameWDL extends BoardGame, AlphaBetaGame
{
  WinDrawLossEvaluation evaluate(int[] board);
  String[] getFileNames();
}
