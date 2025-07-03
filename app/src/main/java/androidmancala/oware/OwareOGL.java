package androidmancala.oware;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidmancala.AbstractMancalaOGL;
import androidmancala.MancalaUtil;
import androidmancala.TutorialMessage;
import boardgame.BoardGameUtil;
import boardgame.MancalaBoardGame;
import boardgame.PitPair;
import boardgame.WinDrawLossEvaluation;
import boardgame.algoritm.GameNodeWDL;
import boardgame.algoritm.GameTreeWDL;
import androidmancala.AlphaBetaService;
import androidmancala.Pit;
import boardgame.kalaha.KalahaMove;
import androidmancala.animation.AnimationUtil;
import androidmancala.animation.MarbleAnimation;
import androidmancala.animation.MoveAnimation;
import androidmancala.opengl.Image2DColorShader;
import androidmancala.opengl.TextObject;
import boardgame.oware.Oware;
import boardgame.oware.OwareMLFN;
import mlfn.MultiLayerFN;

import static boardgame.MancalaBoardGame.FIRST_PLAYER;
import static boardgame.MancalaBoardGame.PLAYER_TO_MOVE;
import static boardgame.MancalaBoardGame.SECOND_PLAYER;

public class OwareOGL extends AbstractMancalaOGL {

    private List<AlphaBetaService> alphaBetaServices;
    private OwareMLFN owareMLFN;
    private static int hiddens = 10;
    private TextObject noProgress;

    public OwareOGL(Activity context, Resources resources, int skill, String whoBegins, int variation, boolean tutorial) {
        super(context, resources, 4, skill, whoBegins);
        MultiLayerFN[] mlfns = MancalaUtil.readMLFNs(OwareMLFN.getFileNames(hiddens, variation), resources);
        this.owareMLFN = new OwareMLFN(MancalaBoardGame.FIRST_PLAYER, marblesPerPit, variation, hiddens, mlfns);
        this.boardGame = owareMLFN;
        this.context = context;
        this.resources = resources;
        this.alphaBetaServices = new ArrayList<>();
        this.loadgame = !tutorial;
        this.loadgameQuestion = !tutorial;
        this.tutorialMode = tutorial;
        if (tutorialMode)
            initSkill(1);
        else
            initSkill(skill);
    }

    @Override
    public void startEvaluationService(int position, int player, int maxSearchDepth) {
        alphaBetaService(position, player, maxSearchDepth);
    }

    @Override
    public WinDrawLossEvaluation evaluate(int position) {
        return owareMLFN.evaluate(owareMLFN.getBoards().get(position));
    }

    @Override
    public WinDrawLossEvaluation evaluate(int[] board) {
        return owareMLFN.evaluate(board);
    }

    @Override
    protected boolean evaluationServicesRunning() {
        removeFinishedABS();
        return alphaBetaServices.size() > 0;
    }

    /*
     @wait if true we won't return until finished
    */
    @Override
    protected void finishAlphaBetaServices(boolean wait) {

        if (alphaBetaServices == null)
            return;
        for (AlphaBetaService abs : alphaBetaServices) {
            abs.noMessages();
            abs.finish();
        }
        boolean allFinished = false;
        if (wait)
            while (!allFinished) {
                allFinished = true;
                for (AlphaBetaService abs : alphaBetaServices) {
                    if (!abs.hasFinished()) {
                        allFinished = false;
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        removeFinishedABS();
    }

    private void initSkill(int skill) {
        switch (skill) {
            case 0:
                searchDepth = 1;
                owareMLFN.setCaptureDepth(1);
                break;

            case 1:
                searchDepth = 2;
                owareMLFN.setCaptureDepth(1.0);
                break;
            case 2:
                searchDepth = 2;
                owareMLFN.setCaptureDepth(0.5);
                break;
            case 3:
                searchDepth = 3;
                owareMLFN.setCaptureDepth(1);
                break;
            case 4:
                searchDepth = 3;
                owareMLFN.setCaptureDepth(0.5);
                break;
        }
    }

    public void pause() {
        finishAlphaBetaServices();
    }

    private static MultiLayerFN[] readMLPS(int hiddens, Resources resources, int variation) {
        int nets = OwareMLFN.getNumberOfNets();

        MultiLayerFN[] mlps = new MultiLayerFN[nets];
        for (int i = 0; i < nets; i++) {
            String filename = OwareMLFN.getFilename(i, hiddens, variation);
            int id = resources.getIdentifier(filename, "raw", "boardgame.mancalapackage");
            try {
                DataInputStream is = new DataInputStream(resources.openRawResource(id));
                //ObjectInputStream ois = new ObjectInputStream(is);
                MultiLayerFN mlp = MultiLayerFN.load(is);
                mlps[i] = mlp;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mlps;
    }

    public void receive(GameTreeWDL gameTree, boolean finished, int boardPosition, int player) {
        GameNodeWDL root = gameTree.getRoot();

        WinDrawLossEvaluation wdlEval = new WinDrawLossEvaluation(root.getWinningProbability(), root.getDrawProbability(), root.getLooseProbability());
        //First intended to give intermediate feedback but evaluations are very jumpy
        //early so can be confusing
        if (finished) {
            boardEvaluations.put(new Integer(boardPosition), wdlEval);
            //We will use the evaluation for next positions also until it get's
            //overwritten by next alphaBeta but for the first player we skip the search and
            //simply use the computers evaluation of his last move
            boardEvaluations.put(new Integer(boardPosition + 1), wdlEval);
        }
        //In the tutorial mode we dont want want make new moves (and starting new searches ?)
        if (tutorialMode)
            return;
        List<GameNodeWDL> childrens = root.getChildrens();

        if (!finished) {
            ;//System.out.println("Not Finished");
        } else {
            char[] bestMove = gameTree.getBestMove();
            if (player == SECOND_PLAYER) {
                if (childrens.size() > 0) {
                    int board[] = root.getBoard();
                    //Is seems somehow occasionally we can get here when player should be SECOND_PLAYER
                    //Usually when we restarted a game
                    if (owareMLFN.getPlayerToMove(board) != SECOND_PLAYER) {
                        return;
                    }
                    owareMLFN.move(bestMove);
                    moveList.addMove(new KalahaMove(SECOND_PLAYER, bestMove));

                    if (!owareMLFN.gameEnded()) {
                        //New Move
                        int playerToMove = owareMLFN.getPlayerToMove();
                        if (playerToMove == SECOND_PLAYER)
                            alphaBetaService(boardPosition + 1, playerToMove, getSearchDepth(playerToMove,skill));
                    }
                }
            }
        }
    }

    //Think deeper for the computer moves also think deep when less marbles left
    protected int getSearchDepth(int playerToMove,int skill) {
        if (tutorialMode)
            return 1;
        //We only search the when first players turn for visual result
        return searchDepth;
    }
    /*
    //Think deeper for the computer moves
    protected int getSearchDepth(int playerToMove, int skill) {
        return (playerToMove == FIRST_PLAYER) ? skill : skill + 1;
    }*/

    private void removeFinishedABS() {
        List<AlphaBetaService> finishedABS = new ArrayList<>();
        for (AlphaBetaService abs : alphaBetaServices)
            if (abs.hasFinished())
                finishedABS.add(abs);
        for (AlphaBetaService finished : finishedABS) {
            alphaBetaServices.remove(finished);
            //System.out.println("Remove ABS" + finished.getBoardPosition());
        }
    }

    private void finishAlphaBetaServices() {
        for (AlphaBetaService abs : alphaBetaServices) {
            abs.noMessages();
            abs.finish();
        }
    }

    private void resumeAlphaBetaServices() {
        for (AlphaBetaService abs : alphaBetaServices)
            abs.setPause(false);
    }

    private void pauseAlphaBetaServices() {
        for (AlphaBetaService abs : alphaBetaServices)
            abs.setPause(true);
    }

    @Override
    public void startTheGame() {
        if (owareMLFN.getPlayerToMove() == SECOND_PLAYER)
            alphaBetaService(0, SECOND_PLAYER, getSearchDepth(owareMLFN.getPlayerToMove(), skill));
    }

    private void alphaBetaService(int position, int player, int maxSearchDepth) {
        //Should maybe remove one arg in abs
        AlphaBetaService abs = new AlphaBetaService(owareMLFN, this, maxSearchDepth, alphaBetaMessageInterval, position, player);
        alphaBetaServices.add(abs);
        alphaBetaMove = new Thread(abs);
        alphaBetaMove.start();
    }

    private String getFilename() {
        return "ow" + skill + "who" + whoBegins + "var" + owareMLFN.getVariation();
    }

    //Perhaps better with just outputstream
    public void saveGame(Context context) throws IOException {
        List<char[]> moves = owareMLFN.getMoves();
        //Don't overwrite a saved game with an empty game
        if (moves.size() == 0)
            return;
        File file = new File(context.getFilesDir(), getFilename());
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        out.writeInt(owareMLFN.getBoards().get(0)[PLAYER_TO_MOVE]);
        int nrOfMoves = moves.size();
        out.writeInt(nrOfMoves);
        for (char[] move : moves) {
            out.writeChar(move[0]);
        }
        out.close();
    }

    @Override
    protected OwareMLFN loadGame(Context context) throws IOException {
        File file = new File(context.getFilesDir(), getFilename());
        if (!file.exists())
            return null;
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        this.playerThatStarts = in.readInt();
        int nrOfMoves = in.readInt();
        int[] moves = new int[nrOfMoves];
        for (int move = 0; move < nrOfMoves; move++) {
            moves[move] = in.readChar() - 48;
        }
        in.close();
        makePreMoves(moves);
        return owareMLFN;
    }

    private void makePreMoves(int[] preMoves) {
        for (int preMove : preMoves) {
            try {
                int playerToMove = owareMLFN.getPlayerToMove();
                owareMLFN.move(BoardGameUtil.createMove(preMove));
                moveList.addMove(new KalahaMove(playerToMove, preMove));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        moveList.selectLast();
    }

    protected MoveAnimation getSowingAnimation(int move, int[] board, List<Pit> sourcePits) {
        //System.out.println("Creating Sowing Anim move= " + move + " Board-> " + owareMLFN.toString(board) + "pits size" + sourcePits.size());
        MoveAnimation moveAnimationLocal = new MoveAnimation(atlasTexture, null);
        int[] clonedBoard = Arrays.copyOf(board, board.length);
        List<Pit> clonedPits = MancalaUtil.copyPits(sourcePits);
        /* The game is finished   */
        //System.out.println("SowAnim Finish ?");

        if (owareMLFN.noProgress(clonedBoard) || owareMLFN.isEmpty(clonedBoard, clonedBoard[PLAYER_TO_MOVE])) {
            //System.out.println("Anim Finish Game");
            List<PitPair> pitPairs = owareMLFN.getFinishGamePits(clonedBoard);
            for (PitPair pitPair : pitPairs) {
                List<MarbleAnimation> marbleAnimations = AnimationUtil.getAnimation(pitPair, clonedPits, sourcePits);
                moveAnimationLocal.add(marbleAnimations);
                MancalaUtil.transfer(pitPair, clonedPits);
            }
            return moveAnimationLocal;
        }


        List<Integer> sowingPitsIndex = owareMLFN.getSowingPits(clonedBoard, move);
        List<MarbleAnimation> sowingAnimations = AnimationUtil.getSowingAnimation(sowingPitsIndex, clonedPits, sourcePits);
        for (MarbleAnimation sow : sowingAnimations)
            sow.setSound(MancalaUtil.getMCSound(1, 0, (int) sow.getEndPit().getX(), boardWidth), soundPool);
        moveAnimationLocal.add(sowingAnimations);
        /*
        if (!MancalaUtil.isSame(clonedBoard, clonedPits)) {
            System.out.println("EXIT Sowing Anim Start Check");
            System.out.println(owareMLFN.toString(board));
            System.out.flush();
            System.exit(0);
        }*/

        pitSowing(sowingPitsIndex, clonedPits);

        int lastPit = owareMLFN.sow(clonedBoard, move);
        //int opponentMarbles = clonedBoard[getOppositeBoardPit(player, lastPit)];
        //When the last marble comes into players own empty pit(house)
        //and the oppenents pit contains marbles move stone to players Kalaha
        //int pitmarbles=board[getPitToMoveFrom(move.getFrom(),board)];
        //System.out.println("pitmarbles"+pitmarbles);

        if (owareMLFN.isSteal(clonedBoard, lastPit)) {
            List<PitPair> pitPairs = owareMLFN.getStealPits(clonedBoard, lastPit);
            //System.out.println("Calling steal from sowing" + owareMLFN.toString(clonedBoard));
            for (PitPair pitPair : pitPairs) {
                List<MarbleAnimation> stealAnimations = AnimationUtil.getAnimation(pitPair, clonedPits, sourcePits);
                for (MarbleAnimation steal : stealAnimations)
                    steal.setSound(MancalaUtil.getMCSound(stealAnimations.size(), 0, pitPair.getSecond(), boardWidth), soundPool);
                moveAnimationLocal.add(stealAnimations);
                MancalaUtil.transfer(pitPair, clonedPits);
                //System.out.println("PitPair steal from sowing" + owareMLFN.toString(clonedBoard));
            }
            //moveAnimationLocal.add(stealAnim);
            //moveAnimationState.addSteal(lastPit);
            //System.out.println("Calling steal from sowing" + owareMLFN.toString(clonedBoard));
            owareMLFN.steal(clonedBoard, lastPit, false);
        }

        clonedBoard[PLAYER_TO_MOVE] = owareMLFN.getOpponent(clonedBoard);

        //moveAnimationState.addFinishGame();
        //moveAnimationState.addComplete();
        return moveAnimationLocal;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.1f, 0.0f, 0.5f, 1);
        if (textureIDCounter > 0) {
            //System.out.println("skipping recreation");
            return;
        }
        image2DColorShader = new Image2DColorShader();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (onSurfaceChangedNeeded()) {
            onSurfaceChangedInit(width, height);
            initSprites(width, height);
            createTextureAtlas(null);
            atlasSpriteSetup(null);
            onSurfaceChangedRest();
            noProgress = new TextObject("No Progress", boardWidth * 0.5f, screenHeight - boardHeight * 0.04f);
            noProgress.setTextHeight(screenWidth * 0.02f);
            textManager.addText(noProgress);
        }
    }

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        int boardIndex = moveList.getSelectedPosition();
        //int noProg = owareMLFN.getNoProgress(boardIndex);
        this.noProgress.setText("If no capture the game will end in " + owareMLFN.getNoProgressCountdown(boardIndex) + " moves");
        super.onDrawFrame(gl);
    }

    @Override
    protected List<TutorialMessage> getTutorialMessages() {
        List<TutorialMessage> tutMessages = new ArrayList<>();
        TutorialMessage sow = new TutorialMessage();
        sow.addMessage("In Oware sowing is only done in the players houses");
        sow.addMessage("and never in the house you started from");
        tutMessages.add(sow);
        TutorialMessage sow2 = new TutorialMessage();
        sow2.addMessage("If your move leaves the opponent with only");
        sow2.addMessage("empty houses that move is not allowed");
        tutMessages.add(sow2);

        TutorialMessage winning = new TutorialMessage();
        winning.addMessage("Winning is done by collecting more than");
        winning.addMessage("half of the seeds into your store");
        tutMessages.add(winning);

        TutorialMessage steal1 = new TutorialMessage();
        steal1.addMessage("When the last seed beeing sowed ends up in the");
        steal1.addMessage("opponents house and that house contains 2 or 3 seeds");
        steal1.addMessage("then the player collects those seeds to his store");
        try {
            int[] pits = {0, 0, 1, 0, 4, 0, 18, 3, 2, 2, 0, 0, 0, 18};
            int board[] = owareMLFN.createPosition(pits, FIRST_PLAYER, 4);
            steal1.setupPosition(board);
        } catch (Exception e) {
            e.printStackTrace();
        }

        tutMessages.add(steal1);

        TutorialMessage steal2 = new TutorialMessage();
        steal2.addMessage("If the previous opponents house to the last captured");
        steal2.addMessage("pit also contains 2 or 3 seeds capturing continues");
        steal2.addMessage("Make a move from pit 5 to make a capture");
        steal2.setPitMove(pits.get(4), BoardGameUtil.createMove(4));
        tutMessages.add(steal2);

        TutorialMessage grandslam = new TutorialMessage();
        grandslam.addMessage("When capturing all opponents seeds it is called a");
        grandslam.addMessage("Grand Slam and there exists different rule variations.");
        grandslam.addMessage("In this game grand slams are not a legal move");

        try {
            int[] pits = {0, 0, 1, 0, 4, 2, 18, 1, 2, 2, 0, 0, 0, 18};
            int board[] = owareMLFN.createPosition(pits, FIRST_PLAYER, 4);
            grandslam.setupPosition(board);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tutMessages.add(grandslam);

        TutorialMessage repetition = new TutorialMessage();
        repetition.addMessage("For some positions the game can go on forever.");
        repetition.addMessage("In real games the players can agree to end the game");
        tutMessages.add(repetition);

        TutorialMessage repetition2 = new TutorialMessage();
        repetition2.addMessage("Here the game is ended if no capture has");
        repetition2.addMessage("occurred for " + Oware.MAX_NO_PROGRESS + " moves, the players moves the");
        repetition2.addMessage("seeds in their houses to their respective store");
        tutMessages.add(repetition2);

        TutorialMessage startAGame = new TutorialMessage();
        startAGame.addMessage("Press back button to setup a game!");
        startAGame.setCanGoNext(false);
        tutMessages.add(startAGame);
        return tutMessages;
    }
}