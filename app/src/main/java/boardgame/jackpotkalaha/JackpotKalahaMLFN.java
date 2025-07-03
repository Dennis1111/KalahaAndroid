package boardgame.jackpotkalaha;

import java.util.Arrays;
import java.io.IOException;
import boardgame.BoardGameWDL;
import boardgame.DiceBoardGameWDL;
import boardgame.GameState;
import boardgame.WinDrawLossEvaluation;
import boardgame.algoritm.GameTreeWDL;
import boardgame.algoritm.WinDrawLossNode;
import boardgame.algoritm.alphabeta.AlphaBetaSIMLP;
import mlfn.*;

/**
 * Created by Dennis on 2017-10-07.
 */

public class JackpotKalahaMLFN extends JackpotKalaha implements DiceBoardGameWDL {
    protected MultiLayerFN[] mlfns;
    private double idealMin = 0.0;
    private double idealMax = 1.0;

    private static final double inputMin = -1;
    private static final double inputMax = 1;
    private static final int continousInput = 17;
    private static final int neuronsWinDiff = 8;
    private static final int neuronsPerPit = 14;
    private static final int neuronsPerKalaha = 10;
    private static final int neuronsKalahaDiff = 21;
    private static final int stealNeurons = 4;
    private static final int jackpotNeurons = 5;
    private static final int NUMBER_OF_INPUTS = continousInput + (neuronsWinDiff * 2) + (neuronsPerPit * 12) + (neuronsPerKalaha * 4) + neuronsKalahaDiff + stealNeurons * 2 + jackpotNeurons;
    private static String[] inputNames;

    private static final int IDEAL_WIN = 0;
    private static final int IDEAL_LOSS = 1;
    private static final int IDEAL_DRAW = 2;
    private static final int[] marbleTresholdsEven = {10, 20, 34, 48, 60, 1000};
    private static final int[] marbleTresholdsAhead = {10, 20, 34, 48, 55, 1000};
    private int hiddens;

  public JackpotKalahaMLFN(int playerThatStarts, int marbles, int hiddens, boolean emptySteal,String path) {
        super(playerThatStarts, marbles, emptySteal);

        String[] files = new String[getNumberOfNets()];
        mlfns = new MultiLayerFN[getNumberOfNets()];
        this.hiddens=hiddens;
	System.out.println("::path" + path);
        int ideals = 3;
        int[] layers = new int[]{NUMBER_OF_INPUTS, hiddens, ideals};
        for (int net = 0; net < getNumberOfNets(); net++) {
	  String file = path + "/" + getFilename(net, hiddens);
	  MultiLayerFN mlp;
	  try{
	    mlp = MultiLayerFN.load(file);
            //if a weight for a neuron is 10 times smaller then the biggest weight it will not be used (meaning wieghts from prev layer)
            //double weightsFactor = 10;
	  } catch(IOException e)
	      {
                System.out.println("Creating new MLFN");
                mlp = new MultiLayerFN(NUMBER_OF_INPUTS,hiddens,ideals);
	      }
            //mlp.setFilename(file);
            this.mlfns[net] = mlp;
	  }
	  inputNames = new String[NUMBER_OF_INPUTS];
        //Make a neural input to get set the inputnames
        getNeuralInput(getBoard());
    }

    public JackpotKalahaMLFN(int playerThatStarts, int marbles, boolean emptySteal,int hiddens, MultiLayerFN[] mlps) {
        super(playerThatStarts, marbles, emptySteal);
        this.mlfns = mlps;
        this.hiddens=hiddens;
        inputNames = new String[NUMBER_OF_INPUTS];
        getNeuralInput(getBoard());
    }

    public JackpotKalahaMLFN(int playerThatStarts, int marbles, boolean emptySteal, int hiddens) {
        super(playerThatStarts, marbles, emptySteal);
        this.hiddens=hiddens;
        inputNames = new String[NUMBER_OF_INPUTS];
        getNeuralInput(getBoard());
    }


    public static int getNumberOfNets() {
        return marbleTresholdsEven.length * 2;
    }
  
    public static String getFilename(int net, int hiddens) {
        return NAME_OF_THE_GAME + net + "in" + NUMBER_OF_INPUTS + "h" + hiddens+".nn";
    }

    public static String getRawFilename(int net, int hiddens) {
        return NAME_OF_THE_GAME + net + "in" + NUMBER_OF_INPUTS + "h" + hiddens;
    }

    public static String[] getFileNames(int hiddens) {
        String[] fileNames = new String[getNumberOfNets()];
        for (int net = 0; net < fileNames.length; net++) {
            fileNames[net] = getRawFilename(net, hiddens);
        }
        return fileNames;
    }

    public String[] getFileNames() {
        String[] fileNames = new String[getNumberOfNets()];
        for (int net = 0; net < fileNames.length; net++) {
            fileNames[net] = getRawFilename(net, hiddens);
        }
        return fileNames;
    }

    public double getIdealMax() {
        return idealMax;
    }

    public double getIdealMin() {
        return idealMin;
    }

    public String[] getInputNames() {
        return inputNames;
    }

    protected boolean endGame(int[] board) {
        int marblesInPlay = marblesInPlay(board);
        int numberOfPossibleMoves = numberOfPossibleMoves(board);
        if (marblesInPlay < 6 || numberOfPossibleMoves + marblesInPlay < 8)
            return true;
        else
            return false;
    }

    /* Here we get an evaluation of the board withouth the dice */
    public WinDrawLossEvaluation evaluate(int[] evBoard) {
        double[] evaluation = evaluateArr(evBoard);
        return new WinDrawLossEvaluation(evaluation[IDEAL_WIN], evaluation[IDEAL_DRAW], evaluation[IDEAL_LOSS]);
    }

    //To evaluate a board we always evaluate (neural) the board as FIRST_PLAYER is on turn
    //so if second player is on turn wi first mirror the board
    //and then negate (swap) the evaluation
    public double[] evaluateArr(int[] evBoard)//,MultiLayerFN mlp)
    {
        if (gameEnded(evBoard))
            return getIdeals(getGameState(evBoard));
        //int marblesInPlay = marblesInPlay(evBoard);

        int mlpIndex = getMLFNIndex(evBoard);
        MultiLayerFN mlp = getMLFN(mlpIndex);
        int player = getPlayerToMove(evBoard);
        double[] input;
        if (player == FIRST_PLAYER) {
            input = getNeuralInput(evBoard);
            return mlp.predict(input);
        } else {
            input = getNeuralInput(mirrorBoard(evBoard));
            double[] output = mlp.predict(input);
            output = swapOutput(output);
            return output;
        }
    }

    //To evaluate a board we always evaluate (neural) the board as FIRST_PLAYER is on turn
    //so if second player is on turn wi first mirror the board
    //and then negate (swap) the evaluation
    public double[] evaluateAlphaBeta(BoardGameWDL boardGameWDL, int depth)//,MultiLayerFN mlp)
    {
        //if (gameEnded(evBoard))
        //  return getIdeals(getGameState(evBoard));
        AlphaBetaSIMLP ab = new AlphaBetaSIMLP(boardGameWDL);
        ab.setMaxDepth(depth);
        GameTreeWDL gameTree = ab.evaluate(boardGameWDL);
        WinDrawLossNode root = gameTree.getRoot();
        double[] eval = new double[3];
        eval[0] = root.getWinningProbability();
        eval[1] = root.getLooseProbability();
        eval[2] = root.getDrawProbability();
        return eval;
    }

    //public int getMLFNIndex(int marblesLeft,int kalahaDiff)
    public int getMLFNIndex(int[] board) {
        int marblesLeft = marblesInPlay(board);
        int nrOfTresholds = marbleTresholdsEven.length;
        int kalahaDiff = Math.abs(board[KALAHA_FIRST_PLAYER] - board[KALAHA_SECOND_PLAYER]);
        boolean evenGame = kalahaDiff <= 6;
        for (int mlp = 0; mlp < nrOfTresholds; mlp++) {
            if (evenGame)
                if (marblesLeft <= marbleTresholdsEven[mlp])
                    return mlp;
            //Not even game
            if (marblesLeft <= marbleTresholdsAhead[mlp])
                return mlp + nrOfTresholds;
    /*
    if (marblesLeft<=marbleTresholds[mlp])
	  if (kalahaDiff>4)
	    return mlp+nrOfTresholds;
	  else
	  return mlp;*/
        }
        System.out.println("shouldnt get here mlpindex");
        System.exit(0);
        return mlfns.length - 1;
    }

    public MultiLayerFN getMLFN(int index) {
        return this.mlfns[index];
    }

    protected int getNumberOfMLFNS() {
        return this.mlfns.length;
    }

    public int getNeuralInputSize() {
        return NUMBER_OF_INPUTS;
    }

    //Neural input
    public double[] getNeuralInput(int[] board) {
        //Each pit will have 14 input (not the Kalaha)
        //System.out.println("inputboard"+toString(board));

        double[] inputs = new double[NUMBER_OF_INPUTS];
        int inputCount = 0;
        int marblesToWinGame = getHalfMarbleAmount(board) + 1;
        //Some 'continous' inputs first
        int kalahaDiff = board[KALAHA_FIRST_PLAYER] - board[KALAHA_SECOND_PLAYER];
        int homeMarblesToKalahaP0 = marblesThatCanReachKalaha(board, FIRST_PLAYER);
        int homeMarblesToKalahaP1 = marblesThatCanReachKalaha(board, SECOND_PLAYER);

        boolean[] stealPitsP0 = new boolean[6];//Which pits are steal threats (doesnt look if pit is empty)
        int[] stealsP0 = new int[6];//how many marbles to steal
        int stealThreatsP0 = getStealCandPits(FIRST_PLAYER, board, stealPitsP0, stealsP0);//How many pits are steal threats
        boolean[] stealPitsP1 = new boolean[6];
        int[] stealsP1 = new int[6];
        int stealThreatsP1 = getStealCandPits(SECOND_PLAYER, board, stealPitsP1, stealsP1);
        int maxKalahaDiff = 5;

        //Continous Kalahadiff input
        double contKalahaDiff = getContKalahaDiff(kalahaDiff, maxKalahaDiff);
        inputs[inputCount] = contKalahaDiff;
        inputNames[inputCount] = "cont Kalahadiff, MaxDiff " + maxKalahaDiff;
        inputCount++;

        maxKalahaDiff = 10;
        contKalahaDiff = getContKalahaDiff(kalahaDiff, maxKalahaDiff);
        inputs[inputCount] = contKalahaDiff;
        inputNames[inputCount] = "cont Kalahadiff, MaxDiff " + maxKalahaDiff;
        inputCount++;

        maxKalahaDiff = 15;
        contKalahaDiff = getContKalahaDiff(kalahaDiff, maxKalahaDiff);
        inputs[inputCount] = contKalahaDiff;
        inputNames[inputCount] = "cont Kalahadiff, MaxDiff " + maxKalahaDiff;
        inputCount++;

        //Continouus homeMarblesToKalahaP0
        int minToKalaha = 1;
        int maxToKalaha = 6;
        double contHomeMarblesToKalahaP0 = getContinousValue(homeMarblesToKalahaP0, minToKalaha, maxToKalaha);
        inputs[inputCount] = contHomeMarblesToKalahaP0;
        inputNames[inputCount] = "cont Marbles to Kalaha P0 , " + maxToKalaha;
        inputCount++;

        //Continouus homeMarblesToKalahaP1
        double contHomeMarblesToKalahaP1 = getContinousValue(homeMarblesToKalahaP1, minToKalaha, maxToKalaha);
        inputs[inputCount] = contHomeMarblesToKalahaP1;
        inputNames[inputCount] = "cont Marbles to Kalaha P1 , " + maxToKalaha;
        inputCount++;

        //Player 0 pits
        //If we add 'marblesToKalaha' with marblesInKalaha and compare that with how many marbles that reach Kalaha
        //we should have good indicator for how close to winning we are
        int winDiffP0 = marblesToWinGame - (homeMarblesToKalahaP0 + board[KALAHA_FIRST_PLAYER]);

        for (int compare = 0; compare < neuronsWinDiff; compare++) {
            inputs[inputCount] = (compare - 3 > winDiffP0) ? inputMin : inputMax;
            inputNames[inputCount] = "P0 winDiff* >" + (compare - 3);
            inputCount++;
        }

        int winDiffP1 = marblesToWinGame - (homeMarblesToKalahaP1 + board[KALAHA_SECOND_PLAYER]);
        //System.out.println("winDiff P1 ="+winDiffP1);
        for (int compare = 0; compare < neuronsWinDiff; compare++) {
            inputs[inputCount] = (compare - 3 > winDiffP1) ? inputMin : inputMax;
            inputNames[inputCount] = "P1 winDiff* >" + (compare - 3);
            inputCount++;
        }

        //Player 0 pits
        //Return a list of possible steal pits (a empty pit that can be reached with a move) 0..5
        //Then make a continous input depending of how big each steal is
        //int stealsPlayer0 = getStealCandPits(FIRST_PLAYER,board, stealPits,steals);

        for (int pit = 0; pit < 6; pit++) {
            if (stealPitsP0[pit] && board[pit] == 0)
                inputs[inputCount] = getContinousValue(stealsP0[pit], 0, 6);
            else
                inputs[inputCount] = inputMin;//No steal is possible, use inputMin
            inputNames[inputCount] = "P0StealThreat pit=" + pit;
            inputCount++;
        }

        for (int pit = 0; pit < 6; pit++) {
            if (stealPitsP0[pit] && board[pit] == 0)
                inputs[inputCount] = getContinousValue(stealsP1[pit], 0, 6);
            else
                inputs[inputCount] = inputMin;
            inputNames[inputCount] = "P1StealThreat pit=" + pit;
            inputCount++;
        }

        //Player 0 pits
        for (int pit = 0; pit < 6; pit++) {
            int marbles = board[pit];
            for (int neuralPit = 0; neuralPit < neuronsPerPit; neuralPit++) {
                //with an empty pit all inputs will be -1
                inputs[inputCount] = (marbles > neuralPit) ? inputMin : inputMax;
                inputNames[inputCount] = "P0 pit= " + pit + " mar> " + neuralPit;
                inputCount++;
            }
        }

        //Second player pits
        for (int pit = 0; pit < 6; pit++) {
            int marbles = getMarblesInPit(board, pit, SECOND_PLAYER);
            for (int neuralPit = 0; neuralPit < neuronsPerPit; neuralPit++) {
                //with an empty pit all inputs will be -1
                inputs[inputCount] = (marbles > neuralPit) ? inputMin : inputMax;
                inputNames[inputCount] = "P1 pit= " + pit + " mar> " + neuralPit;
                inputCount++;
            }
        }

        //First player

        int marblesToWinFirstPlayer = marblesToWin(board, FIRST_PLAYER, marblesToWinGame);
        //Since the game is not ended we know the player need atleast one more marble to win
        //hence the first input will be set to true(1) if 2 or more marbles is needed
        //the second is true if 3 or more is needed
        //System.out.println(marblesToWinGame+"First player needs"+marblesToWinFirstPlayer);
        for (int neuralKalaha = 0; neuralKalaha < neuronsPerKalaha; neuralKalaha++) {
            inputs[inputCount] = (marblesToWinFirstPlayer >= neuralKalaha + 2) ? inputMin : inputMax;
            inputNames[inputCount] = "P0 Mar toWin> " + (neuralKalaha + 2);
            //System.out.print("("+neuralKalaha+" , "+inputs[inputCount]+")");
            inputCount++;
        }

        //Second Player Kalaha
        //int kalahaMarblesSecondPlayer = board[KALAHA_SECOND_PLAYER];
        int marblesToWinSecPlayer = marblesToWin(board, SECOND_PLAYER, marblesToWinGame);
        //System.out.println(marblesToWinGame+"Second player needs"+marblesToWinSecPlayer);
        for (int neuralKalaha = 0; neuralKalaha < neuronsPerKalaha; neuralKalaha++) {
            inputs[inputCount] = (marblesToWinSecPlayer > neuralKalaha + 2) ? inputMin : inputMax;
            inputNames[inputCount] = "P1 Mar toWin> " + neuralKalaha + 2;
            //System.out.print("("+neuralKalaha+" , "+inputs[inputCount]+")");
            inputCount++;
        }

        //21 neurons for the Kalaha Diff , enough to represent a lead of 10 marbles

        for (int neuralKalahaDiff = -10; neuralKalahaDiff <= 10; neuralKalahaDiff++) {
            inputs[inputCount] = (kalahaDiff >= neuralKalahaDiff) ? inputMin : inputMax;
            inputNames[inputCount] = "KalahaDiff >=" + neuralKalahaDiff;
            inputCount++;
        }

        //Like neural kalaha but add the marbles in our homeboard
        //(except those that will move to oppoent) to evaluate the distance from winning

        //System.out.println("Can reach Kalaha first player"+homeMarblesToKalaha);
        for (int neuralKalaha = 0; neuralKalaha < neuronsPerKalaha; neuralKalaha++) {
            inputs[inputCount] = (homeMarblesToKalahaP0 > neuralKalaha + 2) ? inputMin : inputMax;
            inputNames[inputCount] = "P0 homeMarblesToKalaha >=" + neuralKalaha;
            //System.out.print("("+neuralKalaha+" , "+inputs[inputCount]+")");
            inputCount++;
        }

        for (int neuralKalaha = 0; neuralKalaha < neuronsPerKalaha; neuralKalaha++) {
            inputs[inputCount] = (homeMarblesToKalahaP1 > neuralKalaha + 2) ? inputMin : inputMax;
            inputNames[inputCount] = "P1 homeMarblesToKalaha >=" + neuralKalaha;
            inputCount++;
        }

        //steals has range 0..6
        //System.out.println("Steals first player"+stealsFirstPlayer);
        for (int steal = 0; steal < stealNeurons; steal++) {
            inputs[inputCount] = (stealThreatsP0 > steal) ? inputMin : inputMax;
            inputNames[inputCount] = "P0 Steal Cand >" + steal;
            inputCount++;
        }

        //System.out.println(inputCount+"Cand Steals second player"+stealsSecondPlayer);
        for (int steal = 0; steal < stealNeurons; steal++) {
            inputs[inputCount] = (stealThreatsP1 > steal) ? inputMin : inputMax;
            inputNames[inputCount] = "P1 Steal Cand >" + steal;
            inputCount++;
        }

        //is there a possible jackpot steal (only check for pits with small amouth of seeds)
        for (int jackpotSteal = 0; jackpotSteal < 5; jackpotSteal++) {
            int marbles = board[jackpotSteal];
            int lastPit = marbles + jackpotSteal;
            if (lastPit > 5 || marbles == 0) {
                inputs[inputCount] = inputMax;
            } else {
                int oppositeBoardPit = getOppositeBoardPit(FIRST_PLAYER, lastPit);
                int opponentMarbles = board[oppositeBoardPit];
                boolean jackpot = board[lastPit] == board[oppositeBoardPit] - 1;
              /*if (jackpot){
	    	  System.out.println(toString(board)+"J"+jackpotSteal+"mar"+marbles+" L"+lastPit+"op"+oppositeBoardPit);
     		  }*/

                inputs[inputCount] = jackpot ? inputMin : inputMax;
            }
            inputNames[inputCount] = "P1 Jackpot " + jackpotSteal;
            inputCount++;
        }
        return inputs;
    }

    private double getContKalahaDiff(int kalahaDiff, int maxDiff) {
        return normalize(kalahaDiff, -maxDiff, maxDiff, inputMin, inputMax);
    }

    private double getContinousValue(int value, int minValue, int maxValue) {
        return normalize(value, minValue, maxValue, inputMin, inputMax);
    }

    private static double normalize(double value, double dataMin, double dataMax, double normMin, double normMax) {
        double maxDiff = dataMax - dataMin;
        double cut = value;
        if (value > dataMax)
            cut = dataMax;
        else if (value < dataMin)
            cut = dataMin;

        double normalized = (cut - dataMin) / maxDiff;//normalized 0..1
        normalized = normalized * (normMax - normMin) + normMin;
        return normalized;
    }

    //The ideals from a alhabeta that is evaluated to all leafs
    private double[] getIdeals(double score) {
        double[] ideals = new double[3];
        Arrays.fill(ideals, idealMin);
        if (score > 0)
            ideals[IDEAL_WIN] = idealMax;
        else if (score < 0)
            ideals[IDEAL_LOSS] = idealMax;
        else
            ideals[IDEAL_DRAW] = idealMax;
        //System.out.println("ABscore"+score);
        return ideals;
    }

    //Assumes the game has ended
    private double[] getIdeals(int gameState) {
        double[] ideals = new double[3];
        Arrays.fill(ideals, idealMin);
        if (gameState == GameState.DRAW)
            ideals[IDEAL_DRAW] = idealMax;
        else if (gameState == GameState.WIN)
            ideals[IDEAL_WIN] = idealMax;
        else
            ideals[IDEAL_LOSS] = idealMax;
        return ideals;
    }

    //Swap the playrs winning outpout, the drawoutput remains the same
    protected double[] swapOutput(double[] output) {
        double[] swap = new double[3];
        swap[IDEAL_WIN] = output[IDEAL_LOSS];
        swap[IDEAL_LOSS] = output[IDEAL_WIN];
        swap[IDEAL_DRAW] = output[IDEAL_DRAW];
        return swap;
    }

    //By mirroring the board when the second player is on turn we can train the net without using the player on turn as input
    protected int[] mirrorBoard(int[] board) {
        int[] mirrorBoard = new int[board.length];
        for (int pit = 0; pit < 6; pit++) {
            mirrorBoard[pit] = board[pit + FIRST_PIT_SECOND_PLAYER];
            mirrorBoard[pit + FIRST_PIT_SECOND_PLAYER] = board[pit];
        }
        mirrorBoard[KALAHA_FIRST_PLAYER] = board[KALAHA_SECOND_PLAYER];
        mirrorBoard[KALAHA_SECOND_PLAYER] = board[KALAHA_FIRST_PLAYER];
        mirrorBoard[PLAYER_TO_MOVE] = getOpponent(board);
        mirrorBoard[MARBLES_PER_PIT] = board[MARBLES_PER_PIT];
        return mirrorBoard;
    }
}

