package boardgame.oware;

import java.util.*;

import boardgame.*;

public class Oware extends MancalaBoardGame implements AlphaBetaGame {
    protected final static int GS_NOT_LEGAL = 0;
    protected final static int GS_NO_CAPTURE = 1;
    protected final static int GS_REMAINING_STONES_TO_OPPONENT = 2;
    protected final static int GS_LAST_HOUSE_NO_CAPTURE = 3;

    protected final static int NO_PROGRESS = 16;
    protected final static int GRAND_SLAM_VARIATION = 17;
    //We has 12 possible pits to sow in but a full sowing round is 11 since we dont sow where we started
    protected final static int SOWING_PITS = 12;
    //protected final static String NAME_OF_THE_GAME = "oware";
    //protected final static int CYCLIC = 20;
    public final static int MAX_NO_PROGRESS = 30;
    //protected final static int REMAINING_MARBLES_TO_KALAHA = 51;//Just something unique that isn't a normal move
    //private final char CANT_FEED_OPPONENT='r';
    protected List<char[]> gameMoves;

    protected List<int[]> boards;
    protected int variation;
    //protected boolean debug = false;
    protected double captureDepth = 1.0;

    public Oware(int playerThatStarts, int marbles, int variation) {
        super();
        this.variation = variation;
        if (variation > 3 || variation < 0) {
            System.out.println("Unknown game variation");
            System.exit(0);
        }
        newGame(playerThatStarts, marbles);
    }

    public void setCaptureDepth(double captureDepth){
        this.captureDepth=captureDepth;
    }

    public int getNoProgress(int pos) {
        return boards.get(pos)[NO_PROGRESS];
    }

    public int getNoProgressCountdown(int pos) {
        return MAX_NO_PROGRESS - boards.get(pos)[NO_PROGRESS];
    }

    private boolean badBoardSum(int[] board) {
        int sum = 0;
        for (int i = 0; i < 14; i++)
            sum += board[i];
        return sum != 48;
    }

    public List<int[]> getBoards() {
        return boards;
    }

    public boolean cyclicEnd() {
        return false;
    }

    public int getVaration() {
        return variation;
    }

    public static String getNameOfTheGame(int variation) {
        String nameOfTheGame;
        switch (variation) {
            case GS_NOT_LEGAL:
                nameOfTheGame = "gsnl";
                break;
            case GS_NO_CAPTURE:
                nameOfTheGame = "gsnoc";
                break;
            case GS_REMAINING_STONES_TO_OPPONENT:
                nameOfTheGame = "gsrsto";
                break;
            case GS_LAST_HOUSE_NO_CAPTURE:
                nameOfTheGame = "gslhnoc";
            default:
                nameOfTheGame = "gsnl";
                break;
        }
        return "oware_" + nameOfTheGame;
    }

    public void back() {
        int movesSize = gameMoves.size();
        int boardsSize = movesSize + 1;
        //System.out.println(size+","+gameMoves.size());
        if (movesSize > 0) {
            gameMoves.remove(movesSize - 1);
            boards.remove(boardsSize - 1);
        }
    }

    public int[] getBoard() {
        return boards.get(boards.size() - 1);
    }

    public int getGameState() {
        return getGameState(getBoard());
    }

    public int getGameState(int[] board) {
        int halfMarbleAmount = getHalfMarbleAmount(board);
        if (board[KALAHA_FIRST_PLAYER] > halfMarbleAmount)
            return GameState.WIN;
        else if (board[KALAHA_SECOND_PLAYER] > halfMarbleAmount)
            return GameState.LOSS;
        else if ((board[KALAHA_FIRST_PLAYER] == halfMarbleAmount && board[KALAHA_SECOND_PLAYER] == halfMarbleAmount)/* ||
                board[NO_PROGRESS] == MAX_NO_PROGRESS*/)
            return GameState.DRAW;
        else if (hasNoMove(board)) {
            //System.out.println("No moves --> winner" + board[PLAYER_TO_MOVE] + toString(board));
            //The play who can't move Wins
            if (board[PLAYER_TO_MOVE] == FIRST_PLAYER)
                return GameState.WIN;
            else
                return GameState.LOSS;
        }
        return GameState.HEURISTIC;
    }

    //iterare backwards as long as the boards has same kalahasizes
    public List<int[]> getCyclicCandidates() {
        List<int[]> candidates = new ArrayList<>();

        for (int i = 0; i < boards.size(); i++) {
            int[] previousBoard = boards.get(boards.size() - i - 1);
            //int compareValue=compareBoards(board,previousBoard);
            if (!kalahaHasChanged(previousBoard, getBoard())) {
                //System.out.println("previous"+previousBoard+"size"+boards.size()+this+
                //		       "found cyclic pos"+toString(boards.get(previousBoard)));
                candidates.add(previousBoard);
            }
        }
        return candidates;
    }


    private boolean kalahaHasChanged(int[] board1, int[] board2) {
        return board1[KALAHA_FIRST_PLAYER] != board2[KALAHA_FIRST_PLAYER]
                || board1[KALAHA_SECOND_PLAYER] != board2[KALAHA_SECOND_PLAYER];
    }

    //Will return 0 if is same position
    //Will return 1 if any of the kalaha differs
    //Will return 2 if differns elsewhere
    private boolean sameBoards(int board1[], int board2[]) {
        for (int i = 0; i < 12; i++)
            if (board1[i] != board2[i])
                return false;
        if (kalahaHasChanged(board1, board2))
            return false;
        return true;
    }

    private boolean hasNoMove(int[] board) {
        if (board[PLAYER_TO_MOVE] == FIRST_PLAYER) {
            for (int i = 0; i < 6; i++) {
                if (board[i] > 0)
                    return false;
            }
            return true;
        } else {
            for (int i = 0; i < 6; i++) {
                if (board[i + FIRST_PIT_SECOND_PLAYER] > 0)
                    return false;
            }
            return true;
        }
    }

    public int getVariation() {
        return variation;
    }

    public void newGame(int playerThatStarts, int marblesPerPit) {
        this.gameMoves = new ArrayList<>();
        this.boards = new ArrayList<>();
        int[] board = createStartingPosition(playerThatStarts, marblesPerPit, variation);
        boards.add(board);
    }

    public boolean newGame(int playerThatStarts, int marblesPerPit, int[] pits) {
        try {
            this.gameMoves = new ArrayList<>();
            this.boards = new ArrayList<>();
            int[] board = createStartingPosition(playerThatStarts, marblesPerPit, variation);
            for (int i = 0; i < 14; i++) {
                board[i] = pits[i];
            }
            boards.add(board);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /* @pits the pits 0-13 and sum of marbles must be == 12*marblesPerPit*/
    public int[] createPosition(int[] pits, int playerToMove, int marblesPerPit) throws Exception {
        int[] position = new int[18];
        int sum = 0;
        for (int i = 0; i < 14; i++) {
            position[i] = pits[i];
            sum += position[i];
        }
        if (marblesPerPit * 12 != sum)
            throw new Exception("The marbles must sum up to" + marblesPerPit * 12 + "Sum=" + sum);
        position[PLAYER_TO_MOVE] = playerToMove;
        position[MARBLES_PER_PIT] = marblesPerPit;
        position[NO_PROGRESS] = 0;
        position[GRAND_SLAM_VARIATION] = GS_NOT_LEGAL;
        return position;
    }

    private static int[] createStartingPosition(int playerThatStarts, int marblesPerPit, int variation) {
        int[] startPos = new int[18];
        for (int i = 0; i < 6; i++) {
            startPos[FIRST_PIT_FIRST_PLAYER + i] = marblesPerPit;
            startPos[FIRST_PIT_SECOND_PLAYER + i] = marblesPerPit;
        }
        startPos[KALAHA_FIRST_PLAYER] = 0;
        startPos[KALAHA_SECOND_PLAYER] = 0;
        startPos[PLAYER_TO_MOVE] = playerThatStarts;
        startPos[MARBLES_PER_PIT] = marblesPerPit;
        startPos[NO_PROGRESS] = 0;
        startPos[GRAND_SLAM_VARIATION] = variation;
        return startPos;
    }

    public int getOpponent(int[] board) {
        return (board[PLAYER_TO_MOVE] + 1) % 2;
    }

    //Some calculations are done in move also so a little speed can be gained if doing this in move instead
    //used in minMax to search deeper in some branches
    //values --> zero means search deeper and in 1 is a normal ply search

    public double getMoveDepth(char[] move, int[] board, int[] childBoard) {
        int numericMove = Character.getNumericValue(move[0]);
        if (!(numericMove >= 0 && numericMove <= 5))
            return 1.0;

        boolean capture = (board[KALAHA_FIRST_PLAYER] != childBoard[KALAHA_FIRST_PLAYER]) ||
                (board[KALAHA_SECOND_PLAYER] != childBoard[KALAHA_SECOND_PLAYER]);
        /*int kalahaDiff = board[KALAHA_FIRST_PLAYER] - board[KALAHA_SECOND_PLAYER];
        int childKalahaDiff = childBoard[KALAHA_FIRST_PLAYER] - childBoard[KALAHA_SECOND_PLAYER];
        int kalahaChange = Math.abs(kalahaDiff - childKalahaDiff);
        int remainingMarbles = 48 - board[KALAHA_FIRST_PLAYER] - board[KALAHA_SECOND_PLAYER];
        */
        if (capture)
            return captureDepth;
        /*if (remainingMarbles < 6) {
            return ((double) remainingMarbles) / 6;
        }*/
        return 1.0;
    }

    ///////
    //@ move is supposed to be in range 0..5
    //
    //
    ///////

    public void move(char[] move) {
        int[] board = move(move, getBoard(), getCyclicCandidates());
        gameMoves.add(move);
        //System.out.println("moves"+move+"adding");
        boards.add(board);
    }

    /* When there is no legal moves we finish the game */
    public boolean isFinishGame(char[] move) {
        return move == MancalaBoardGame.CANT_MOVE || move == MancalaBoardGame.NO_PROGRESS;
    }

    ///////
    //@move is supposed to be in range 0..5
    //@board The board to generate a new move from
    ///////

    public int[] move(char[] moveArr, int[] board, List<int[]> previousBoards) {
        int move = Character.getNumericValue(moveArr[0]);
        //System.out.println("Move"+toString(board)+"move"+move);
        //System.out.println(moveArr[0]+"Move"+move);
        int[] clonedBoard = Arrays.copyOf(board, board.length);

        /*
        if (badBoardSum(clonedBoard)) {
            System.out.println(move + "Finish" + toString(board) + "-->" + toString(clonedBoard));
            System.exit(0);
        }*/

        //If opponent has no more moves move players marbles to kalaha
        if (noProgress(clonedBoard) || isEmpty(clonedBoard, clonedBoard[PLAYER_TO_MOVE])) {
            //System.out.println("move(FINISH GAME)");
            finishGame(clonedBoard);
            return clonedBoard;
        }

	     /*else if (move==REMAINING_MARBLES_TO_KALAHA)
        {
	     finishGame(clonedBoard);
	     }*/
        else if (move < 0) {
            //For debugging at the moment
            System.out.println("we shouldnt get here" + move + toString(board));
            System.exit(0);
        } else {
            int pitToMoveFrom = getPitToMoveFrom(move, clonedBoard);
            //int pitMarbles = clonedBoard[pitToMoveFrom];

            int lastSowingPit = sow(clonedBoard, move);
            if (badBoardSum(clonedBoard)) {
                System.out.println(move + "Sowing" + toString(board) + "-->" + toString(clonedBoard));
                System.exit(0);
            }

            if (isSteal(clonedBoard, lastSowingPit)) {
                steal(clonedBoard, lastSowingPit);
                //System.out.println("Board after steal"+toString(clonedBoard));
                clonedBoard[NO_PROGRESS] = 0;

                if (badBoardSum(clonedBoard)) {
                    System.out.println("Steal" + toString(board) + "-->" + toString(clonedBoard));
                    System.exit(0);
                }

                //System.out.println(move + "Before steal" + toString(board));
                //System.out.println("After steal" + toString(clonedBoard));
            } else
                //If no steal for a 'period' we finish the game
                clonedBoard[NO_PROGRESS]++;
        }


        clonedBoard[PLAYER_TO_MOVE] = getOpponent(clonedBoard);

        int marbleCount = marbleCount(clonedBoard);
        int marblesInGame = clonedBoard[MARBLES_PER_PIT] * (SOWING_PITS);

        /*
          for (int[] previousBoard : previousBoards) {
	      if (sameBoards(clonedBoard, previousBoard)) {
	      clonedBoard[REPETITION] ++;
	       break;
	      }
	    }*/

        if (marbleCount != marblesInGame) {
            System.out.println("EXIT move bug" + toString(board));
            System.out.println("move" + move);
            System.out.println("move bug" + toString(clonedBoard));
            System.out.flush();
            System.exit(0);
        }
        return clonedBoard;
    }

    public boolean noProgress(int[] board) {
        return board[NO_PROGRESS] == MAX_NO_PROGRESS;
    }

    public int sow(int[] clonedBoard, int move) {
        int player = clonedBoard[PLAYER_TO_MOVE];
        int pitToMoveFrom = getPitToMoveFrom(move, clonedBoard);
        int pitMarbles = clonedBoard[pitToMoveFrom];
        //System.out.println("Sowing"+toString(clonedBoard)+" "+move+"pitToMoevfrom"+pitToMoveFrom+"pitMarbles"+pitMarbles);
        setMarblesInPit(clonedBoard, move, player, 0);
        int sowingPit = pitToMoveFrom;

        for (int marble = 0; marble < pitMarbles; marble++) {
            sowingPit = getNextSowingPit(pitToMoveFrom, sowingPit);
            /*if (sowingPit == pitToMoveFrom) {
                sowingPit = getNextSowingPit(sowingPit);
            }*/
            clonedBoard[sowingPit]++;
        }

        return sowingPit;
    }

    public boolean isSteal(int[] clonedBoard, int lastSowingPit) {
        int player = clonedBoard[PLAYER_TO_MOVE];
        boolean endsAtOpponentPit;
        if (lastSowingPit >= FIRST_PIT_SECOND_PLAYER)
            endsAtOpponentPit = (player == FIRST_PLAYER);
        else
            endsAtOpponentPit = (player == SECOND_PLAYER);
        return (endsAtOpponentPit && (clonedBoard[lastSowingPit] == 2 || clonedBoard[lastSowingPit] == 3));
    }

    public void steal(int[] clonedBoard, int lastSowingPit) {
        steal(clonedBoard, lastSowingPit, false);
    }

    //check first with issteal
    public void steal(int[] clonedBoard, int lastSowingPit, boolean debug) {
        int player = clonedBoard[PLAYER_TO_MOVE];
        if (debug)
            System.out.println("Steal last sowing pit=" + lastSowingPit + " board-> " + toString(getBoard()) + "Player" + player);
        //When the last marble comes into opponents pit
        //and the oppenents pit contains 2 or 3 marbles the marbles to players Kalaha
        while (clonedBoard[lastSowingPit] == 2 || clonedBoard[lastSowingPit] == 3)//If the last pit was empty before starting the move it now has one marble
        {
            int stealMarbles = clonedBoard[lastSowingPit];

            clonedBoard[lastSowingPit] = 0;
            if (player == FIRST_PLAYER)
                clonedBoard[KALAHA_FIRST_PLAYER] += stealMarbles;
            else
                clonedBoard[KALAHA_SECOND_PLAYER] += stealMarbles;
            if (debug) {
                System.out.println("Stealmarbles" + stealMarbles);
                System.out.println("Kalaha" + clonedBoard[KALAHA_FIRST_PLAYER] + "," + clonedBoard[KALAHA_SECOND_PLAYER] + toString(getBoard()));
                System.out.flush();
            }
            if (lastSowingPit == FIRST_PIT_FIRST_PLAYER || lastSowingPit == FIRST_PIT_SECOND_PLAYER)
                break;
            lastSowingPit--;
        }
    }

    public List<Integer> getSowingPits(int[] clonedBoard, int move) {
        //int player = clonedBoard[PLAYER_TO_MOVE];
        int pitToMoveFrom = getPitToMoveFrom(move, clonedBoard);
        int pitMarbles = clonedBoard[pitToMoveFrom];
        int sowingPit = pitToMoveFrom;
        List<Integer> sowingPits = new ArrayList<>();
        sowingPits.add(new Integer(sowingPit));
        for (int marble = 0; marble < pitMarbles; marble++) {
            sowingPit = getNextSowingPit(pitToMoveFrom, sowingPit);
            /*if ((marble % 12) == 0) {
                //We dont sow in the starting pit
                sowingPit = getNextSowingPit(sowingPit);
            }*/
            sowingPits.add(new Integer(sowingPit));
        }
        return sowingPits;
    }

    public List<PitPair> getStealPits(int[] clonedBoard, int lastSowingPit) {
        List<PitPair> stealPits = new ArrayList<>();
        int player = clonedBoard[PLAYER_TO_MOVE];
        //When the last marble comes into opponents pit
        //and the oppenents pit contains 2 or 3 marbles the marbles to players Kalaha
        while (clonedBoard[lastSowingPit] == 2 || clonedBoard[lastSowingPit] == 3)//If the last pit was empty before starting the move it now has one marble
        {
            //System.out.println("Pitmarbles"+pitMarbles+"STEAL"+move+toString(clonedBoard)+"pitmarbles"+pitMarbles);
            int stealMarbles = clonedBoard[lastSowingPit];
            if (player == FIRST_PLAYER)
                stealPits.add(new PitPair(lastSowingPit, KALAHA_FIRST_PLAYER));
            else
                stealPits.add(new PitPair(lastSowingPit, KALAHA_SECOND_PLAYER));
            if (lastSowingPit == FIRST_PIT_FIRST_PLAYER || lastSowingPit == FIRST_PIT_SECOND_PLAYER)
                break;
            lastSowingPit--;
        }
        return stealPits;
    }

    //Will return the pit 0..5 || 7..12 to move from
    protected int getPitToMoveFrom(int move, int[] board) {
        return (board[PLAYER_TO_MOVE] == FIRST_PLAYER) ? move : move + FIRST_PIT_SECOND_PLAYER;
    }


    protected int marbleCount(int[] board) {
        int marbles = 0;
        for (int i = 0; i < 14; i++)
            marbles += board[i];
        return marbles;
    }

    //If a player is out of moves we finish the game by transferring each players marbles to his kalaha
    public List<PitPair> getFinishGamePits(int[] board) {
        List<PitPair> pitPairs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (board[i] > 0) {
                pitPairs.add(new PitPair(i, KALAHA_FIRST_PLAYER));
            }
        }

        for (int i = 0; i < 6; i++) {
            if (board[i + FIRST_PIT_SECOND_PLAYER] > 0) {
                pitPairs.add(new PitPair(i + FIRST_PIT_SECOND_PLAYER, KALAHA_SECOND_PLAYER));
            }
        }
        return pitPairs;
    }

    //If a player is out of moves we finish the game by transferring each players marbles to his kalaha
    protected void finishGame(int[] board) {
        //System.out.println("Finish game"+toString(board));
        int sum = 0;
        for (int i = 0; i < 6; i++) {
            if (board[i] > 0) {
                sum += board[i];
                board[i] = 0;
            }
        }
        board[KALAHA_FIRST_PLAYER] += sum;
        sum = 0;
        for (int i = 0; i < 6; i++) {
            if (board[i + FIRST_PIT_SECOND_PLAYER] > 0) {
                sum += board[i + FIRST_PIT_SECOND_PLAYER];
                board[i + FIRST_PIT_SECOND_PLAYER] = 0;
            }
        }
        board[KALAHA_SECOND_PLAYER] += sum;
        //System.out.println("Finished game"+toString(board)+getGameState(board));
    }

    public void setPlayersTurn(int player) {
        getBoard()[PLAYER_TO_MOVE] = player;
    }

    public int getPlayerToMove() {
        return getBoard()[PLAYER_TO_MOVE];
    }

    public int getPlayerToMove(int[] board) {
        return board[PLAYER_TO_MOVE];
    }

    public int getNumberOfPlayers() {
        return 2;
    }

    public boolean isEmpty(int[] board, int player) {
        int firstPit = player == FIRST_PLAYER ? FIRST_PIT_FIRST_PLAYER : FIRST_PIT_SECOND_PLAYER;
        for (int i = 0; i < 6; i++)
            if (board[i + firstPit] > 0)
                return false;
        return true;
    }

    //The possible moves the player can do right now
    //If a player has n Possible moves the array will be of length n
    //and each element will tell which move is possible
    public List<char[]> getValidMoves() {
        List<int[]> cyclicCand = getCyclicCandidates();
        List<int[]> childBoards = new ArrayList<>();
        List<char[]> validMoves = getValidMoves(getBoard(), cyclicCand, childBoards);
        /*int[] validMovesArr  = new int[validMoves.size()];
            for(int i=0;i<validMovesArr.length;i++)
            {
            validMovesArr[i]=validMoves.get(i).intValue();
        }*/
        return validMoves;
    }

    public boolean isValidMove(char[] candidate) {
        List<char[]> moves = getValidMoves();
        for (int i = 0; i < moves.size(); i++) {
            //System.out.println("vm"+moves[i]);
            if (BoardGameUtil.isSameMove(candidate, moves.get(i)))
                return true;
        }
        return false;
    }

    protected boolean hasValidMoves(int[] board) {
        int player = board[PLAYER_TO_MOVE];
        for (int pit = 0; pit < 6; pit++) {
            if (getMarblesInPit(board, pit, player) > 0)
                return true;
        }
        return false;
    }

    //The possible moves the player can do right now
    //If a player has n Possible moves the array will be of length n
    //and each element will tell which move is possible
    //@cyclicCandidates provide all earlier boards with the same kalaha amount
    //@childBoards the childBoards each valid move vill generate
    public List<char[]> getValidMoves(int[] board, List<int[]> cyclicCandidates, List<int[]> childBoards) {
        childBoards.clear();
        List<char[]> moves = new ArrayList<>();
        if (board[NO_PROGRESS] == MAX_NO_PROGRESS) {
            //System.out.println("MAX no progress" + toString(board));
            //System.out.println("gamestate" + getGameState(board));
            //char[] move = BoardGameUtil.createMove('m');
            childBoards.add(move(MancalaBoardGame.NO_PROGRESS, board, cyclicCandidates));
            moves.add(MancalaBoardGame.NO_PROGRESS);
            return moves;
        }

        /*
        if (board[NO_PROGRESS] == CYCLIC) {
            char[] move = BoardGameUtil.createMove('c');
            System.out.println("CYCLIC no progress" + toString(board));
            System.out.println("gamestate" + getGameState(board));
            childBoards.add(move(move, board, cyclicCandidates));
            moves.add(move);
            return moves;
        }*/

        int player = board[PLAYER_TO_MOVE];
        int opponent = getOpponent(board);
        boolean opponentHasMarbles = playerHasMarbles(board, opponent);
        //System.out.println(opponentHasMarbles+"Get valid moves"+toString(board));
        for (int pit = 0; pit < 6; pit++) {
            int marblesInPit = getMarblesInPit(board, pit, player);
            //System.out.println("pit"+pit+"mar"+marblesInPit);
            if (marblesInPit == 0)
                continue;
            //When marblesInpit+pit>5 we know opponent wont be empty
            if (marblesInPit + pit > 5 || opponentHasMarbles)
                try {
                    char[] move = BoardGameUtil.createMove(pit);
                    int[] candBoard = move(move, board, cyclicCandidates);
                    //It is possible we steal all opponents marbles so then its not valid
                    if (playerHasMarbles(candBoard, opponent)) {
                        childBoards.add(candBoard);
                        moves.add(move);
                        //Cash the board..
                        //System.out.println(pit+"child"+toString(candBoard));
                    } else {
                        //System.out.println("Grand slam steal not allowed" + toString(candBoard) + "Move" + pit);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            if (moves.size() > 6) {
                System.out.println("EXIT Wrong move size bug EXITING GAME");
                System.out.println("Moves" + moves.size() + "," + (pit + 1) + toString(board));
                System.out.flush();
                System.exit(0);
            }
        }

        //System.out.println("possible moves"+moves.size());
        //When you have marbles (possible moves) but no validMoves because after no one of your moves
        //your opponent can't move we end the game (by transffering remaining marbles to kalaha)
        if (moves.size() == 0 && playerHasMarbles(board, player)) {
            System.out.println("NO Valid moves adding cant move" + toString(board));
            //char[] move = BoardGameUtil.createMove(CANT_FEED_OPPONENT);
            //childBoards.add(move(move, board, cyclicCandidates));
            //moves.add(move);
            moves.add(CANT_MOVE);
            childBoards.add(move(CANT_MOVE, board, null));
        }
        //  moves.add(new Integer(0));
        return moves;
    }

    protected boolean playerHasMarbles(int[] board, int player) {
        int first_pit = 0;
        if (player == SECOND_PLAYER)
            first_pit = FIRST_PIT_SECOND_PLAYER;
        for (int i = 0; i < 6; i++)
            if (board[i + first_pit] > 0)
                return true;
        return false;
    }

  /*
    @board
    @pit the players pit 0..5 
    @player first or second player 
  */

    protected static int getMarblesInPit(int[] board, int pit, int player) {
        return (player == FIRST_PLAYER) ? board[pit] : board[pit + FIRST_PIT_SECOND_PLAYER];
    }

    protected static void setMarblesInPit(int[] board, int move, int player, int marbles) {
        if (player == FIRST_PLAYER)
            board[move] = marbles;
        else
            board[move + FIRST_PIT_SECOND_PLAYER] = marbles;
    }

    protected static int getHalfMarbleAmount(int[] board) {
        return board[MARBLES_PER_PIT] * 6;
    }

    //How many marbles we use per pit
    public int getMarblesPerPit() {
        return getBoard()[MARBLES_PER_PIT];
    }

    /*
          @startPit the first pit we picked the sees from 0..5 || 7-12
          @previousBoardPit where previous pit was
     */
    //The next "BOARD pit" to put marbles in
    protected static int getNextSowingPit(int startPit, int previousBoardPit) {
        //Take two steps if next pit is a kalaha
        int next = previousBoardPit + 1;

        if (next == startPit) {
            next++;
            if (next == KALAHA_FIRST_PLAYER || next == KALAHA_SECOND_PLAYER)
                next++;
        } else if (next == KALAHA_FIRST_PLAYER || next == KALAHA_SECOND_PLAYER) {
            next = (next + 1) % 14;
            if (next == startPit)
                next++;//Now next is second house after opponents kalaha
        }
        return next % 14;
    }

    //@pit 0..5
    private static int getOppositeBoardPit(int player, int pit) {
        if (player == FIRST_PLAYER)
            return 12 - pit;
        else
            return 5 - pit;
    }

    //@pit 0..5
    private static int getBoardPit(int player, int pit) {
        if (player == FIRST_PLAYER)
            return pit;
        else
            return pit + FIRST_PIT_SECOND_PLAYER;
    }

    //The moves in the game so far;
    public char[] getMove(int moveNr) {
        return gameMoves.get(moveNr);
    }

    public List<char[]> getMoves() {
        return gameMoves;
    }

    public boolean backLastMove() {
        return true;
    }

    //Did the last move end the game
    public boolean gameEnded() {
        return gameEnded(getBoard());
    }

    //is the game over ?
    public static boolean gameEnded(int[] board) {
        int halfMarbleAmount = getHalfMarbleAmount(board);

        if (board[KALAHA_FIRST_PLAYER] > halfMarbleAmount || //First player wins
                board[KALAHA_SECOND_PLAYER] > halfMarbleAmount || //Second player wins
                (board[KALAHA_FIRST_PLAYER] == halfMarbleAmount && board[KALAHA_SECOND_PLAYER] == halfMarbleAmount))//A Draw
            return true;
        else
            return false;
    }

  /*
  ////////////
  //Assumes the game has ended
  //return 1 if player 0 wins
  //return 0 if its a draw
  //return -1 if player 1
  /////////////
  
  public int getWinner() //getResult would be better name since there can be a draw also
  {
    int halfMarbleAmount = getHalfMarbleAmount(getBoard());
    if (board[KALAHA_FIRST_PLAYER]>halfMarbleAmount)
      return 1;
    else if (board[KALAHA_SECOND_PLAYER]==halfMarbleAmount)
      return 0;
    else 
      return -1;
      }*/

    public double getWinningProbability(double score, double maxScore) {
        double winningProb;

        //sigmoid solution
        winningProb = 1 / (1 + Math.pow(Math.E, (-0.3 * score)));
        //System.out.println("wp" + score + "," + maxScore);
        return winningProb;
    }

    public double getMaxScore(int[] board) {
        int marbleCount = 0;
        for (int i = 0; i < 14; i++)
            marbleCount += board[i];
        double maxScore = marbleCount;
        return maxScore;
    }

    protected int marblesToWin(int[] board, int player, int marblesToWin) {
        if (player == FIRST_PLAYER)
            return marblesToWin - board[KALAHA_FIRST_PLAYER];
        else
            return marblesToWin - board[KALAHA_SECOND_PLAYER];
    }

    //How many marbles that a player has left to move with
    protected int marblesInPlay(int[] board, int player) {
        int marblesInPlay = 0;
        for (int pit = 0; pit < 6; pit++) {
            if (player == FIRST_PLAYER)
                marblesInPlay += board[pit];
            else
                marblesInPlay += board[pit + FIRST_PIT_SECOND_PLAYER];
        }
        return marblesInPlay;
    }

    protected int marblesInPlay(int[] board) {
        int marblesInPlay = 0;
        for (int pit = 0; pit < 6; pit++) {
            marblesInPlay += board[pit];
            marblesInPlay += board[pit + FIRST_PIT_SECOND_PLAYER];
        }
        return marblesInPlay;
    }

    protected int emptyPits(int[] board, int player) {
        int emptyPits = 0;
        for (int pit = 0; pit < 6; pit++) {
            if (player == FIRST_PLAYER)
                emptyPits += board[pit];
            else
                emptyPits += board[pit + FIRST_PIT_SECOND_PLAYER];
        }
        return emptyPits;
    }

    protected int numberOfPossibleMoves(int[] board, int player) {
        int moves = 0;
        if (player == FIRST_PLAYER)
            for (int pit = 0; pit < 6; pit++) {
                if (board[pit] > 0)
                    moves++;
            }
        else
            for (int pit = 0; pit < 6; pit++)
                if (board[pit + FIRST_PIT_SECOND_PLAYER] > 0)
                    moves++;
        return moves;
    }

    protected int numberOfPossibleMoves(int[] board) {
        int moves = 0;
        for (int pit = 0; pit < 6; pit++) {
            if (board[pit] > 0)
                moves++;
            if (board[pit + FIRST_PIT_SECOND_PLAYER] > 0)
                moves++;
        }
        return moves;
    }

    //If the position is good for player0 return a high score;
    public double getScore(int[] board) {
        double score = 0;

        //1. Kalaha player[0]-player[1]
        int kalahaDiff = board[KALAHA_FIRST_PLAYER] - board[KALAHA_SECOND_PLAYER];
        score += kalahaDiff;

        return score;
    }

  /*
  //Which opponent pits that can be targeted
  protected boolean[] stealCandidates(int player,int board[])
  {
    boolean[] stealCandidates= new boolean[6];
    Arrays.fill(stealCandidates,false);
    
    for(int pit=0;pit<6;pit++)
      {		
	int marbles= getMarblesInPit(board,pit,player);
	int target = (marbles+pit)%SOWING_PITS;
	if (target>=6)
	  stealCandidates[target-6]=true;
      }
      return stealCandidates;
      }*/

    protected List<Integer> pitsThatCanReceiveWithoutGivingBack(int player, int[] board) {
        List<Integer> canReceive = new ArrayList<>();
        if (player == FIRST_PLAYER)
            for (int pit = 0; pit < 6; pit++) {
                if (board[pit] + pit <= 5)
                    canReceive.add(Integer.valueOf(pit));
            }
        else
            for (int pit = 0; pit < 6; pit++) {
                if (board[pit + FIRST_PIT_SECOND_PLAYER] + pit <= 5)
                    canReceive.add(Integer.valueOf(pit));
            }
        return canReceive;
    }

  /*
    This feature is used for the neural net inputs
    If its possible to make a move that end up in you opponents pit x
    then target[x] will be set to 1 since its a candidate for future steals
    Will return the number of different pits the player can reach on his side with the last stone
    Increasing future steal chances
    This will return wrong result if the sowing is very big but that will be very unusual so skipped for now  protected int
  */

    protected int getStealCandPits(int player, int[] board, boolean[] targets, int[] steals) {
        int homePits = 6;
        Arrays.fill(targets, false);
        Arrays.fill(steals, 0);
        int nrOfTargets = 0;

        for (int pit = 0; pit < homePits; pit++) {
            int sowingMarbles = getMarblesInPit(board, pit, player);
            int boardTargetPit = getTargetPit(player, pit, sowingMarbles);
            //We can only steal when last sowing is at opponent
            if (!isOpponentPit(player, boardTargetPit))
                continue;
            //System.out.println("pit"+pit+"SowM "+sowingMarbles+toString(board)+"Target Pit"+boardTargetPit+"\n");
            int targetMarbles = board[boardTargetPit];
            int sowingRounds = 0;
            while (sowingMarbles > 11) {
                sowingRounds++;
                sowingMarbles -= 11;
            }
            //Convert boardTargetPit to 0..5
            int targetPit = boardTargetPit;
            if (targetPit > 5)
                targetPit -= 7;
            //System.out.println("targetpit"+targetPit);
            //After sowing we need to if targetpits will be 2 or 3
            if ((targetMarbles + sowingRounds == 2 || targetMarbles + sowingRounds == 3)
                    && targetPit >= 6 && !targets[targetPit - 6]) {
                //System.out.println("targetpit"+targetPit);
                nrOfTargets++;
                targets[targetPit - 6] = true;
                steals[targetPit - 6] = board[targetPit] + sowingRounds;
            }
        }
        return nrOfTargets;
    }

    private boolean isOpponentPit(int player, int boardPit) {
        if (player == FIRST_PLAYER)
            return boardPit <= 6;
        else
            return boardPit >= 7;
    }

    //@startpit 0..5
    //return the actual boardindex where the last marble ends
    private int getTargetPit(int player, int startPit, int marblesToSow) {
        while (marblesToSow > 11) {
            marblesToSow -= 11;
        }
        int endPit = startPit;

        for (int i = 0; i < marblesToSow; i++) {
            endPit++;
            if (endPit == KALAHA_FIRST_PLAYER || endPit == KALAHA_SECOND_PLAYER)
                endPit++;
            endPit = endPit % 14;
        }
        if (player == SECOND_PLAYER)
            endPit = (endPit + 7) % 14;
        //now marblesToSow 1..11
        return endPit;
    }

    public String toString() {
        return toString(getBoard());
    }

    public String toString(int[] board) {
        String boardS = "----------------------------------------------\n";
        boardS += "Player To move   " + board[PLAYER_TO_MOVE] + "\n";
        boardS += "No improvement for" + board[NO_PROGRESS] + "\n";
        boardS += "      " + board[12] + "  " + board[11] + "  " + board[10] + "  " + board[9] + "  " + board[8] + "  " + board[7] + "\n";
        boardS += board[KALAHA_SECOND_PLAYER] + "                             " + board[KALAHA_FIRST_PLAYER] + "\n";
        boardS += "      " + board[0] + "  " + board[1] + "  " + board[2] + "  " + board[3] + "  " + board[4] + "  " + board[5] + "\n";
        //boardS+="Score =  "+getScore(board);
        return boardS;
    }

    public String toString(List<char[]> moves) {
        String movesStr = "Moves:";
        for (char[] move : moves)
            movesStr += toString(move) + " , ";
        return movesStr;
    }

    public String toString(char[] move) {
        if (move[0] >= '0' && move[0] <= 5)
            return "" + move[0];
        else
            return "" + move[0];
    }

    /*
    public static void main(String[] args) {
        int marbles = 4;
        int variation = GS_NOT_LEGAL;
        Oware oware = new Oware(FIRST_PLAYER, marbles, variation);
        int[] moves = {5, 4, 3, 1};
        System.out.println(oware);
        for (int move : moves) {
            List<char[]> candidates = oware.getValidMoves();
            System.out.println("Cand" + oware.toString(candidates));
            oware.move(BoardGameUtil.createMove(move));
            System.out.println("Moving" + (move + 1) + " , c" + oware);
        }
    }*/

       /*
   private boolean isCyclicGame()
   {
    int[] board=getBoard();
    for(int i=0;i<boards.size()-1;i++)
    {
	int[] previousBoard=boards.get(boards.size()-i-2);
	if (sameBoards(board,previousBoard))
	{
	//System.out.println("previous"+previousBoard+"size"+boards.size()+this+
	//		       "found cyclic pos"+toString(boards.get(previousBoard)));
	return true;
	}
	if (kalahaHasChanged(board,previousBoard))
	return false;
    }
    return false;
   }*/

    //When the same position occur twice we conclude the game is cyclic
    private boolean cyclicGame(int[] board, List<int[]> boards) {
        for (int[] compare : boards) {
            if (sameBoards(board, compare))
                return true;
        }
        return false;
    }

    public boolean isCyclicCandidate(int[] board1, int[] board2) {
        boolean isCyclic = !kalahaHasChanged(board1, board2);
        return isCyclic;
    }

}
