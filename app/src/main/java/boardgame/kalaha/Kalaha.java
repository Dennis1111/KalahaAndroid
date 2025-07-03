package boardgame.kalaha;

import java.util.*;

import boardgame.*;

public class Kalaha extends MancalaBoardGame implements AlphaBetaGame {

    //Should be moved to board though no trouble as long as we can't play two diff kind of games simultan
    protected final boolean emptySteal;

    //With 13 sowing marbles we the last marble will end up where we started
    protected final static int SOWING_PITS = 13;
    protected final static String NAME_OF_THE_GAME = "kalaha";

    protected double newMoveDepth = 0.5;
    protected double fewSeedsDepth1 = 0.2;
    protected double fewSeedsDepth2 = 0.7;

    public Kalaha(int playerThatStarts, int marbles, boolean emptySteal) {
        super();
        this.emptySteal = emptySteal;
        newGame(playerThatStarts, marbles);
    }

    public boolean isEmptySteal(){
        return emptySteal;
    }

    public void setNewMoveDepth(double newMoveDepth) {
        this.newMoveDepth = newMoveDepth;
    }

    public void setFewSeedsDepth1(double fewSeedsDepth1) {
        this.fewSeedsDepth1 = fewSeedsDepth1;
    }

    public void setFewSeedsDepth2(double fewSeedsDepth2) {
        this.fewSeedsDepth2 = fewSeedsDepth2;
    }

    public List<int[]> getBoards() {
        return boards;
    }

    public boolean cyclicEnd() {
        return false;
    }

    public boolean isCyclicCandidate(int[] board1, int[] board2) {
        return false;
    }

    public void back() {
        int movesSize = gameMoves.size();
        int boardsSize = movesSize + 1;
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
        else if (board[KALAHA_FIRST_PLAYER] == halfMarbleAmount && board[KALAHA_SECOND_PLAYER] == halfMarbleAmount)
            return GameState.DRAW;
        else
            return GameState.HEURISTIC;
    }

    public void newGame(int playerThatStarts, int marblesPerPit) {
        this.gameMoves = new ArrayList<>();
        this.boards = new ArrayList<>();
        int[] board = createStartingPosition(playerThatStarts, marblesPerPit);
        boards.add(board);
    }

    /*
      @board board shall be an int[14] corresponding to the pits
      returns true if no exceptions
    */

    public boolean newGame(int playerThatStarts, int marblesPerPit, int[] pits){
        try {
            this.gameMoves = new ArrayList<>();
            this.boards = new ArrayList<>();
            int[] board = createPosition(pits, playerThatStarts, marblesPerPit);
            boards.add(board);
        }
        catch(Exception e)
        {return false;}
        return true;
    }

    /* @pits the pits 0-13 and sum of marbles must be == 12*marblesPerPit*/
    public int[] createPosition(int[] pits, int playerToMove, int marblesPerPit) throws Exception {
        int[] position = new int[16];
        int sum = 0;
        for (int i = 0; i < 14; i++) {
            position[i] = pits[i];
            sum += position[i];
        }
        if (marblesPerPit * 12 != sum)
            throw new Exception("The marbles must sum up to" + marblesPerPit * 12 + "Sum=" + sum);
        position[PLAYER_TO_MOVE] = playerToMove;
        position[MARBLES_PER_PIT] = marblesPerPit;
        return position;
    }

    protected static int[] createStartingPosition(int playerThatStarts, int marblesPerPit) {
        int[] startPos = new int[16];
        for (int i = 0; i < 6; i++) {
            startPos[FIRST_PIT_FIRST_PLAYER + i] = marblesPerPit;
            startPos[FIRST_PIT_SECOND_PLAYER + i] = marblesPerPit;
        }
        startPos[KALAHA_FIRST_PLAYER] = 0;
        startPos[KALAHA_SECOND_PLAYER] = 0;
        startPos[PLAYER_TO_MOVE] = playerThatStarts;
        startPos[MARBLES_PER_PIT] = marblesPerPit;
        return startPos;
    }

    ///////
    //@ move is supposed to be in range 0..5
    //
    ///////

    public void move(char[] move) {
        int[] board = move(move, getBoard(), null);
        gameMoves.add(move);
        boards.add(board);
    }

    protected int getOpponent(int[] board) {
        return (board[PLAYER_TO_MOVE] + 1) % 2;
    }

    //Some calculations are done in move also so a little speed can be gained if doing this in move instead
    //used in minMax to search deeper in some branches
    //values --> zero means search deeper and in 1 is a normal ply search

    public double getMoveDepth(char[] moveArr, int[] board, int[] childBoard) {
        int numericMove = Character.getNumericValue(moveArr[0]);
        if (!(numericMove >= 0 && numericMove <= 5))
            return 1.0;

        //int[] clonedBoard = Arrays.copyOf(board, board.length);
        //int player = clonedBoard[PLAYER_TO_MOVE];
        int pitToMoveFrom = getPitToMoveFrom(numericMove, board);
        int pitMarbles = board[pitToMoveFrom];

        //When players can make many moves in a row search deeper
        //There could be a penalty for moves that destroys other new moves
        //ie if there is one marble in pit 6 any other new move will destroy move 6 as new move

        if (pitMarbles == 6 - numericMove)//We will get a new move
        {
            return newMoveDepth;
        }

        double starvationScore1=starvationScore(FIRST_PLAYER,board);
        double startvationScore2=starvationScore(SECOND_PLAYER,board);
        double worst=Math.min(startvationScore2,starvationScore1);
        if (worst<10) {
            return Math.max(worst/5,0.2);
        }

        //Its good to look deeper when any player is near running out seeds
        //The simplest way is to just count the seeds but it is often worse
        //if the seeds are near his store
        int seedsFirstPlayer = getHouseSeeds(board, FIRST_PLAYER);
        int seedsSecondPlayer = getHouseSeeds(board, SECOND_PLAYER);
        if (Math.min(seedsFirstPlayer, seedsSecondPlayer) < 5)
            return fewSeedsDepth1;
        if (Math.min(seedsFirstPlayer, seedsSecondPlayer) < 10)
            return fewSeedsDepth2;
        return 1.0;
    }

    //When a players has only marbles near kalaha there is a risk for starvation loss
    //A low starvationScore is bad
    private double starvationScore(int player,int[] board){
        double starvation=0;
        for(int pit=0;pit<5;pit++){
            int marblesInPit;
            if (player==FIRST_PLAYER)
                marblesInPit=board[pit];
            else
                marblesInPit=board[pit+FIRST_PIT_SECOND_PLAYER];
            //Early pits are worth more
            double pitScore=6-pit;
            starvation+=pitScore*Math.min(marblesInPit,6-pit);
        }
        return starvation;
    }

    protected int getHouseSeeds(int[] board, int player) {
        int sum = 0;
        int firstHouse = (player == FIRST_PLAYER) ? FIRST_PIT_FIRST_PLAYER : FIRST_PIT_SECOND_PLAYER;
        for (int i = 0; i < 6; i++) {
            sum += board[firstHouse + i];
        }
        return sum;
    }

    //Will return the pit 0..5 || 7..12 to move from
    protected int getPitToMoveFrom(int move, int[] board) {
        return (board[PLAYER_TO_MOVE] == FIRST_PLAYER) ? move : move + FIRST_PIT_SECOND_PLAYER;
    }

    ///////
    //@move is supposed to be in range 0..5
    //@board The board to generate a new move from
    //@notUsedInKalaha
    ///////
    public int[] move(char[] moveArr, int[] board, List<int[]> notUsedInKalaha) {
        int move = Character.getNumericValue(moveArr[0]);
        int[] clonedBoard = Arrays.copyOf(board, board.length);

        /*
        if (move < 0 || move > 5) {

            System.exit(0);
        }*/

        int lastPit = sow(clonedBoard, move);
        if (isSteal(clonedBoard, lastPit)) {
            steal(clonedBoard, lastPit);
        }
        updatePlayerToMove(clonedBoard, lastPit);
        if (oneSideIsEmpty(clonedBoard) && !gameEnded(clonedBoard)) {
            finishGame(clonedBoard);
        }
        return clonedBoard;
    }

    public boolean oneSideIsEmpty(int[] clonedBoard) {
        int player1Marbles = 0;
        int player2Marbles = 0;

        for (int i = 0; i < 6; i++) {
            player1Marbles += clonedBoard[i];
            player2Marbles += clonedBoard[FIRST_PIT_SECOND_PLAYER + i];
        }
        return player1Marbles == 0 || player2Marbles == 0;
    }

    public List<Integer> getSowingPits(int[] clonedBoard, int move) {
        int player = clonedBoard[PLAYER_TO_MOVE];
        int pitToMoveFrom = getPitToMoveFrom(move, clonedBoard);
        int pitMarbles = clonedBoard[pitToMoveFrom];
        //Put the marbles in the following pits
        int lastBoardPit = pitToMoveFrom;
        List<Integer> sowingPits = new ArrayList<>();
        sowingPits.add(new Integer(lastBoardPit));
        for (int marble = 0; marble < pitMarbles; marble++) {
            lastBoardPit = getNextSowingPit(player, lastBoardPit);
            sowingPits.add(new Integer(lastBoardPit));
        }
        return sowingPits;
    }

    //Perform sowing on the clonedBoard and return which pit the last marble will end
    //up in from the sowing players perspective (will be 6 when last pit is store)
    public int sow(int[] clonedBoard, int move) {
        int player = clonedBoard[PLAYER_TO_MOVE];
        int pitToMoveFrom = getPitToMoveFrom(move, clonedBoard);
        int pitMarbles = clonedBoard[pitToMoveFrom];
        setMarblesInPit(clonedBoard, move, player, 0);
        //Put the marbles in the following pits
        int lastBoardPit = pitToMoveFrom;
        for (int marble = 0; marble < pitMarbles; marble++) {
            lastBoardPit = getNextSowingPit(player, lastBoardPit);
            clonedBoard[lastBoardPit]++;
        }
        int lastPit = (move + pitMarbles) % SOWING_PITS;
        return lastPit;
    }

    public List<PitPair> getStealPits(int[] clonedBoard, int lastPit) {
        int player = clonedBoard[PLAYER_TO_MOVE];
        int oppositeBoardPit = getOppositeBoardPit(player, lastPit);//playersBoardMaps[opponent][5-lastPit];
        int opponentMarbles = clonedBoard[oppositeBoardPit];
        int playerLastBoardPit = getBoardPit(player, lastPit);

        List<PitPair> stealPits = new ArrayList<>();

        if (player == FIRST_PLAYER) {
            if (opponentMarbles > 0)
                stealPits.add(new PitPair(oppositeBoardPit, KALAHA_FIRST_PLAYER));
            stealPits.add(new PitPair(playerLastBoardPit, KALAHA_FIRST_PLAYER));
        } else {
            if (opponentMarbles > 0)
                stealPits.add(new PitPair(oppositeBoardPit, KALAHA_SECOND_PLAYER));
            stealPits.add(new PitPair(playerLastBoardPit, KALAHA_SECOND_PLAYER));
        }

        return stealPits;
    }

    //Only call this when we know the position is a steal
    //@lastpit the sum of move + marbles in pit
    public void steal(int[] clonedBoard, int lastPit) {
        int player = clonedBoard[PLAYER_TO_MOVE];
        int oppositeBoardPit = getOppositeBoardPit(player, lastPit);//playersBoardMaps[opponent][5-lastPit];
        int opponentMarbles = clonedBoard[oppositeBoardPit];
        int playerLastBoardPit = getBoardPit(player, lastPit);
        //We only steal if the opponent has marbles in opposite pit

        //if (opponentMarbles > 0) {
        if (player == FIRST_PLAYER)
            clonedBoard[KALAHA_FIRST_PLAYER] += (opponentMarbles + 1);
        else
            clonedBoard[KALAHA_SECOND_PLAYER] += (opponentMarbles + 1);

        clonedBoard[oppositeBoardPit] = 0;
        clonedBoard[playerLastBoardPit] = 0;
    }

    /*
      @clonedBoard the board position we want to evaluate
      @lastPit tells where the last seed ends up from players perspective 0-5 for possible steal
    */
    public boolean isSteal(int[] clonedBoard, int lastPit) {
        int player = clonedBoard[PLAYER_TO_MOVE];
        //Where on the board the last seed ends
        int lastBoardPit = getBoardPit(player, lastPit);
        boolean endsInPlayersEmptyPit = lastPit < 6 && clonedBoard[lastBoardPit] == 1;

        if (emptySteal) {
            //if (endsInPlayersEmptyPit && clonedBoard[getOppositeBoardPit(player, lastPit)] == 0)
            //  System.out.println("P"+player+"L"+lastPit+"empty steal"+toString(clonedBoard));
            return endsInPlayersEmptyPit;
        } else
            return endsInPlayersEmptyPit && clonedBoard[getOppositeBoardPit(player, lastPit)] > 0;
    }

    /*
     * Unless last sowing pit is kalaha (6) we swap players turn
     * @clonedBoard
     * @lastPit0..12
     */
    public void updatePlayerToMove(int[] clonedBoard, int lastPit) {
        if (lastPit != 6)//When lastPit==6 (Kalaha) the player gets a new more
            clonedBoard[PLAYER_TO_MOVE] = getOpponent(clonedBoard);
    }

    //If a player is out of moves we finish the game by transferring each players marbles to his kalaha
    public List<PitPair> getFinishGamePits(int[] board) {
        List<PitPair> pitPairs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            if (board[i] > 0) {
                pitPairs.add(new PitPair(i, KALAHA_FIRST_PLAYER));
            }
        }
        for (int i = 7; i < 13; i++) {
            if (board[i] > 0) {
                pitPairs.add(new PitPair(i, KALAHA_SECOND_PLAYER));
            }
        }
        return pitPairs;
    }

    //If a player is out of moves we finish the game by transferring each players marbles to his kalaha
    protected void finishGame(int[] board) {
        int sum = 0;
        for (int i = 0; i < 6; i++) {
            if (board[i] > 0) {
                sum += board[i];
                board[i] = 0;
            }
        }
        board[KALAHA_FIRST_PLAYER] += sum;
        sum = 0;
        for (int i = 7; i < 13; i++) {
            if (board[i] > 0) {
                sum += board[i];
                board[i] = 0;
            }
        }
        board[KALAHA_SECOND_PLAYER] += sum;
    }

    protected int marbleCount(int[] board) {
        int marbles = 0;
        for (int i = 0; i < 14; i++)
            marbles += board[i];
        return marbles;
    }

    public int getPlayerToMove() {
        return getPlayerToMove(getBoard());
    }

    public int getPlayerToMove(int[] board) {
        return board[PLAYER_TO_MOVE];
    }

    public int getNumberOfPlayers() {
        return 2;
    }

    //The possible moves the player can do right now
    //If a player has n Possible moves the array will be of length n
    //and each element will tell which move is possible
    public List<char[]> getValidMoves() {
        List<int[]> childBoards = new ArrayList<>();
        List<char[]> validMoves = getValidMoves(getBoard(), null, childBoards);
        return validMoves;
    }

    public boolean isValidMove(char[] candidate) {
        List<char[]> moves = getValidMoves();
        for (int i = 0; i < moves.size(); i++) {
            if (BoardGameUtil.isSameMove(moves.get(i), candidate))
                return true;
        }
        return false;
    }

    //Returns true when atleast one pit 1..6 for the player to move contains a marble
    protected boolean hasValidMoves(int[] board) {
        int player = board[PLAYER_TO_MOVE];
        for (int pit = 0; pit < 6; pit++) {
            if (getMarblesInPit(board, pit, player) > 0)
                return true;
        }
        return false;
    }

    @Override
    /* This is used by alphaBeta so it is good if return the children in an heuristic order
       the likely best move first and in kalaha it's almost always correct to move from 6 if its has just one
       and next most likely candidate 5 if has 2, the big exception to when getting a new move if it destroys a steal (rare)
       The possible moves the player can do right now
       If a player has n Possible moves the array will be of length n
       and each element will tell which move is possible*/
    public List<char[]> getValidMoves(int[] board, List<int[]> notInUse, List<int[]> childBoards) {
        childBoards.clear();
        int player = board[PLAYER_TO_MOVE];
        List<char[]> moves = new ArrayList<>();

        //Tells how many new moves we found so far
        //if we find a new move and before that has N newMove we
        //insert the new Move at index n+1
        int newMoves = 0;
        for (int pit = 5; pit >= 0; pit--) {
            int marblesInPit = getMarblesInPit(board, pit, player);
            if (marblesInPit > 0) {
                char[] move = BoardGameUtil.createMove(pit);
                if (pit + marblesInPit == 6)//New Move
                {
                    moves.add(newMoves, move);
                    childBoards.add(newMoves++,move(move, board, null));
                } else {
                    moves.add(move);
                    childBoards.add(move(move, board, null));
                }
            }
        }

        /*
        //Does this work
        if (newMoves > 2) {
            System.out.println("Parent"+toString(board)+"New moves"+newMoves);
            for (int i=0;i<moves.size();i++){
                System.out.println("move"+toString(childBoards.get(i)));
            }
        }*/

        //Shouldnt happen after having changing the rules
        /*if (moves.size() == 0 && !gameEnded(board)) {
            System.out.println("getValidMoves trouble" + toString(board));
            System.exit(0);
            //char[] move = BoardGameUtil.createMove('f');
            //moves.add(move);
            //childBoards.add(move(move, board, null));
        }*/
        return moves;
    }

    protected static int getMarblesInPit(int[] board, int pit, int player) {
        return (player == FIRST_PLAYER) ? board[pit] : board[pit + FIRST_PIT_SECOND_PLAYER];
    }

    protected static void setMarblesInPit(int[] board, int pit, int player, int marbles) {
        if (player == FIRST_PLAYER)
            board[pit] = marbles;
        else
            board[pit + FIRST_PIT_SECOND_PLAYER] = marbles;
    }

    protected static int getHalfMarbleAmount(int[] board) {
        return board[MARBLES_PER_PIT] * 6;
    }

    //How many marbles we use per pit
    public int getMarblesPerPit() {
        return getBoard()[MARBLES_PER_PIT];
    }

    //The next "BOARD pit" to put marbles in
    protected static int getNextSowingPit(int player, int previousBoardPit) {
        if (player == FIRST_PLAYER)
            return ((previousBoardPit + 1) % SOWING_PITS);
        else if (previousBoardPit == 13)
            return 0;
        else {
            previousBoardPit++;
            if (previousBoardPit == KALAHA_FIRST_PLAYER)
                previousBoardPit++;//Skip player 0s Kalaha
            return previousBoardPit;
        }
    }

    //@pit 0..5
    protected static int getOppositeBoardPit(int player, int pit) {
        if (player == FIRST_PLAYER)
            return 12 - pit;
        else
            return 5 - pit;
    }

    //@pit 0..5
    protected static int getBoardPit(int player, int pit) {
        if (player == FIRST_PLAYER)
            return pit;
        else
            return pit + 7;
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

    public double getWinningProbability(double score, double maxScore) {
        double winningProb;

        //sigmoid solution
        winningProb = 1 / (1 + Math.pow(Math.E, (-0.3 * score)));
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

        //3. If many moves can target different pits on the playesr home board you increase steal possibilities
        int stealsPlayer0 = getStealCandPits(FIRST_PLAYER, board, new boolean[6], new int[6]);
        int stealsPlayer1 = getStealCandPits(SECOND_PLAYER, board, new boolean[6], new int[6]);
        int steals = stealsPlayer0 - stealsPlayer1;
        score += steals;

        if (board[14] == 0)
            score += 0.5;
        else
            score -= 0.5;
        return score;
    }

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

    //When a move passes your own kalaha you start feeding your opponent with marbles
    //this function removes this marbles when estimating you marbleCount in homeBoard
    protected int marblesThatCanReachKalaha(int[] board, int player) {
        int marbles = 0;
        if (player == 0)
            for (int pit = 0; pit < 6; pit++) {
                int canReach = Math.min(board[pit], 6 - pit);
                marbles += canReach;
            }
        else
            for (int pit = 0; pit < 6; pit++) {
                int canReach = Math.min(board[pit + FIRST_PIT_SECOND_PLAYER], 6 - pit);
                marbles += canReach;
            }
        //System.out.println("mtcKalaha player"+player+toString(board)+" , "+marbles);
        return marbles;
    }

    //If its possible to make a move that end up in in you own pit x
    //then target[x] will be set to 1 since its a candidate for future steals
    //Will return the number of different pits the player can reach on his side with the last stone
    //Increasing future steal chances
    protected int getStealCandPits(int player, int[] board, boolean[] targets, int[] steals) {
        int homePits = 6;
        //int[] targets= new int[homePits];
        Arrays.fill(targets, false);
        Arrays.fill(steals, 0);
        int opponent = (player + 1) % 2;
        int nrOfTargets = 0;
        for (int pit = 0; pit < homePits; pit++) {
            int marbles = getMarblesInPit(board, pit, player);
            int targetPit = (pit + marbles) % SOWING_PITS;
            if (marbles > 0 && targetPit < 6 && marbles < 14 && !targets[targetPit]) {
                nrOfTargets++;
                targets[targetPit] = true;
                steals[targetPit] = getMarblesInPit(board, 5 - targetPit, opponent);
            }
        }
        return nrOfTargets;
    }

    protected int[] cloneBoard() {
        return Arrays.copyOf(getBoard(), getBoard().length);
    }

    protected List<int[]> cloneBoards() {
        List<int[]> clonedBoards = new ArrayList<int[]>();
        for (int[] board : boards) {
            clonedBoards.add(Arrays.copyOf(board, board.length));
        }
        return clonedBoards;
    }

    public String toString() {
        return toString(getBoard());
    }


    public String toString(char[] move) {
        //String moveStr="";
        if (move[0] >= '0' && move[0] <= 5)
            return "" + move[0];
        else
            return "" + move[0];
    }

    public String toString(int[] board) {
        String boardS = "----------------------------------------------\n";
        boardS += "Player To move   " + board[14] + "\n";
        boardS += "      " + board[12] + "  " + board[11] + "  " + board[10] + "  " + board[9] + "  " + board[8] + "  " + board[7] + "\n";
        boardS += board[KALAHA_SECOND_PLAYER] + "                             " + board[KALAHA_FIRST_PLAYER] + "\n";
        boardS += "      " + board[0] + "  " + board[1] + "  " + board[2] + "  " + board[3] + "  " + board[4] + "  " + board[5] + "\n";
        //boardS+="Score =  "+getScore(board);
        return boardS;
    }

    public List<int[]> getCyclicCandidates() {
        return null;
    }
}
