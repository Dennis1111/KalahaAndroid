package androidmancala.kalaha;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import androidmancala.AbstractMancalaOGL;
import androidmancala.AlphaBetaService;
import androidmancala.MancalaUtil;
import androidmancala.Pit;
import androidmancala.TutorialMessage;
import androidmancala.animation.AnimationUtil;
import androidmancala.animation.MarbleAnimation;
import androidmancala.animation.MarbleCollectionSound;
import androidmancala.animation.MoveAnimation;
import androidmancala.opengl.Image2DColorShader;
import boardgame.BoardGameUtil;
import boardgame.PitPair;
import boardgame.WinDrawLossEvaluation;
import boardgame.algoritm.GameNodeWDL;
import boardgame.algoritm.GameTreeWDL;
import boardgame.kalaha.KalahaMLFN;
import boardgame.kalaha.KalahaMove;
import mlfn.MultiLayerFN;

import static androidmancala.MancalaUtil.transfer;
import static boardgame.MancalaBoardGame.FIRST_PLAYER;
import static boardgame.MancalaBoardGame.PLAYER_TO_MOVE;
import static boardgame.MancalaBoardGame.SECOND_PLAYER;

/*
 * Created by Dennis on 2018-01-22.
 */

public class KalahaOGL extends AbstractMancalaOGL {

    private List<AlphaBetaService> alphaBetaServices;
    private KalahaMLFN kalahaMLFN;
    private static int hiddens=10;

    /* Constructor for tutorial mode */
    public KalahaOGL(Activity context, Resources resources, int skill, String whoBegins) {
        super(context,resources,4,skill,whoBegins);
        MultiLayerFN[] mlfns = MancalaUtil.readMLFNs(KalahaMLFN.getFileNames(hiddens), resources);
        this.kalahaMLFN= new KalahaMLFN(FIRST_PLAYER, 4, true, hiddens,mlfns);
        this.boardGame=kalahaMLFN;
        this.alphaBetaServices=new ArrayList<>();
        initSkill(skill);
        this.tutorialMode = true;
    }

    public KalahaOGL(Activity context, Resources resources, int marbles, int skill, boolean emptySteal, String whoBegins) {
        super(context,resources,marbles,skill,whoBegins);
        MultiLayerFN[] mlfns = MancalaUtil.readMLFNs(KalahaMLFN.getFileNames(hiddens), resources);
        this.kalahaMLFN= new KalahaMLFN(FIRST_PLAYER, marbles, emptySteal, hiddens,mlfns);
        this.boardGame=kalahaMLFN;
        initSkill(skill);
        this.tutorialMode = false;
        this.alphaBetaServices=new ArrayList<>();
        this.loadgame = true;
        this.loadgameQuestion = true;
    }

    @Override
    public void startEvaluationService(int position,int player,int maxSearchDepth){
        alphaBetaService(position,player,maxSearchDepth);
    }

    @Override
    public WinDrawLossEvaluation evaluate(int position){
        return kalahaMLFN.evaluate(kalahaMLFN.getBoards().get(position));
    }

    @Override
    public WinDrawLossEvaluation evaluate(int[] board){
        return kalahaMLFN.evaluate(board);
    }

    @Override
    protected boolean evaluationServicesRunning() {
        removeFinishedABS();
        return alphaBetaServices.size()>0;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.0f, 0.5f, 1);
        if (textureIDCounter > 0) {
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
            }
    }

    private void makePreMoves(int[] preMoves) {
        for (int preMove : preMoves) {
            try {
                int playerToMove = kalahaMLFN.getPlayerToMove();
                kalahaMLFN.move(BoardGameUtil.createMove(preMove));

                moveList.addMove(new KalahaMove(playerToMove, preMove));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        moveList.selectLast();
    }

    public synchronized void receive(GameTreeWDL gameTree, boolean finished, int boardPosition, int player) {
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

        } else {
            char[] bestMove = gameTree.getBestMove();
            if (player == SECOND_PLAYER) {
                if (childrens.size() > 0) {
                    int board[] = root.getBoard();
                    //Is seems somehow occasionally we can get here when player should be SECOND_PLAYER
                    //Usually when we restarted a game
                    if (kalahaMLFN.getPlayerToMove(board) != SECOND_PLAYER) {
                        return;
                    }
                    kalahaMLFN.move(bestMove);
                    moveList.addMove(new KalahaMove(SECOND_PLAYER, bestMove));

                    if (!kalahaMLFN.gameEnded()) {
                        //New Move
                        int playerToMove = kalahaMLFN.getPlayerToMove();
                        if (playerToMove == SECOND_PLAYER)
                            alphaBetaService(boardPosition + 1, playerToMove, getSearchDepth(playerToMove, skill));
                    }
                }
            }
        }
    }

    @Override
    public void saveGame(Context context) throws IOException {
        List<char[]> moves = kalahaMLFN.getMoves();
        //Don't overwrite a saved game with an empty game
        if (moves.size() == 0)
            return;
        File file = new File(context.getFilesDir(), getFilename());
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        out.writeInt(kalahaMLFN.getBoards().get(0)[PLAYER_TO_MOVE]);
        int nrOfMoves = moves.size();
        out.writeInt(nrOfMoves);
        for (char[] move : moves) {
            out.writeChar(move[0]);
        }
        out.close();
    }

    /* The game has to be from start when called */
    @Override
    protected KalahaMLFN loadGame(Context context) throws IOException {
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
        return kalahaMLFN;
    }

    private String getFilename() {
        return "kal" + marblesPerPit + "s" + skill + "w" + whoBegins + "e" + kalahaMLFN.isEmptySteal();
    }

    @Override
    public void startTheGame() {
        if (kalahaMLFN.getPlayerToMove() == SECOND_PLAYER)
            alphaBetaService(0, SECOND_PLAYER, getSearchDepth(kalahaMLFN.getPlayerToMove(), skill));
        else
            alphaBetaService(0, FIRST_PLAYER, getSearchDepth(kalahaMLFN.getPlayerToMove(), skill-1));

    }


    private void alphaBetaService(int position, int player, int maxSearchDepth) {
        //Should maybe remove one arg in abs
        AlphaBetaService abs = new AlphaBetaService(kalahaMLFN, this, maxSearchDepth, alphaBetaMessageInterval, position, player);
        alphaBetaServices.add(abs);
        alphaBetaMove = new Thread(abs);
        alphaBetaMove.start();
    }

    //Think deeper for the computer moves also think deep when less marbles left
    protected int getSearchDepth(int playerToMove, int skill) {
        if (tutorialMode)
            return 1;
        //We only search the when first players turn for visual result
        return searchDepth;
    }

    /*
     @wait if true we won't return until finished
    */
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

    private void removeFinishedABS() {
        List<AlphaBetaService> finishedABS = new ArrayList<>();
        for (AlphaBetaService abs : alphaBetaServices)
            if (abs.hasFinished())
                finishedABS.add(abs);
        for (AlphaBetaService finished : finishedABS) {
            alphaBetaServices.remove(finished);
        }
    }

    protected MoveAnimation getSowingAnimation(int move, int[] board, List<Pit> sourcePits) {
        MoveAnimation moveAnimationLocal = new MoveAnimation(atlasTexture,soundPool);
        List<Pit> clonedPits = MancalaUtil.copyPits(sourcePits);
        int[] clonedBoard = Arrays.copyOf(board, board.length);

        List<Integer> sowingPitsIndex = kalahaMLFN.getSowingPits(clonedBoard, move);
        List<MarbleAnimation> sowingAnimations = AnimationUtil.getSowingAnimation(sowingPitsIndex, clonedPits, sourcePits);
        for(MarbleAnimation sow : sowingAnimations)
            sow.setSound(MancalaUtil.getMCSound(1,0,(int)sow.getEndPit().getX(),boardWidth),soundPool);
        moveAnimationLocal.add(sowingAnimations);
        pitSowing(sowingPitsIndex, clonedPits);

        int lastPit = kalahaMLFN.sow(clonedBoard, move);

        if (kalahaMLFN.isSteal(clonedBoard, lastPit)) {
            List<PitPair> pitPairs = kalahaMLFN.getStealPits(clonedBoard, lastPit);
            for (PitPair pitPair : pitPairs) {
                List<MarbleAnimation> stealAnimations = AnimationUtil.getAnimation(pitPair, clonedPits, sourcePits);
                MarbleCollectionSound mcSound= MancalaUtil.getMCSound(stealAnimations.size(),0,pitPair.getSecond(),boardWidth);
                //For multidrop we play one soundfile when drop first marble
                stealAnimations.get(0).setSound(mcSound,soundPool);
                moveAnimationLocal.add(stealAnimations);
                transfer(pitPair, clonedPits);
            }

            kalahaMLFN.steal(clonedBoard, lastPit);
        }

        if (kalahaMLFN.oneSideIsEmpty(clonedBoard) && !kalahaMLFN.gameEnded(clonedBoard)) {
            List<PitPair> pitPairs = kalahaMLFN.getFinishGamePits(clonedBoard);
            for (PitPair pitPair : pitPairs) {
                List<MarbleAnimation> marbleAnimations = AnimationUtil.getAnimation(pitPair, clonedPits, sourcePits);
                MarbleCollectionSound mcSound= MancalaUtil.getMCSound(marbleAnimations.size(),0,pitPair.getSecond(),boardWidth);
                marbleAnimations.get(0).setSound(mcSound,soundPool);
                moveAnimationLocal.add(marbleAnimations);
                transfer(pitPair, clonedPits);
            }
        }
        kalahaMLFN.updatePlayerToMove(clonedBoard, lastPit);
        return moveAnimationLocal;
    }

    private void initSkill(int skill) {
        //System.out.println("Skill"+skill);
        switch (skill) {
            case 0:
                searchDepth = 1;
                kalahaMLFN.setNewMoveDepth(1);
                kalahaMLFN.setFewSeedsDepth1(1);
                kalahaMLFN.setFewSeedsDepth2(1);
                break;
            case 1:
                searchDepth = 2;
                kalahaMLFN.setNewMoveDepth(1.0);
                kalahaMLFN.setFewSeedsDepth1(1.0);
                kalahaMLFN.setFewSeedsDepth2(1.0);
                break;
            case 2:
                searchDepth = 2;
                kalahaMLFN.setNewMoveDepth(0.3);
                kalahaMLFN.setFewSeedsDepth1(0.7);
                kalahaMLFN.setFewSeedsDepth2(0.8);
                break;
            case 3:
                searchDepth = 3;
                kalahaMLFN.setNewMoveDepth(0.3);
                kalahaMLFN.setFewSeedsDepth1(0.5);
                kalahaMLFN.setFewSeedsDepth2(0.8);
                break;
            case 4:
                searchDepth = 4;
                kalahaMLFN.setNewMoveDepth(0.3);
                kalahaMLFN.setFewSeedsDepth1(0.4);
                kalahaMLFN.setFewSeedsDepth2(0.8);
                break;
        }
    }

    protected List<TutorialMessage> getTutorialMessages() {
        List<TutorialMessage> tutMessages = new ArrayList<>();

        TutorialMessage intro = new TutorialMessage();
        intro.addMessage("Kalah is played by picking up seeds from a house");
        intro.addMessage("and sowing them counterclockwise. Here marbles");
        intro.addMessage("are used as seeds and the smaller pits are houses");
        tutMessages.add(intro);

        TutorialMessage sowing1 = new TutorialMessage();
        sowing1.addMessage("The two larger pits called store or kalah.");
        sowing1.addMessage("Win the game by collecting more than");
        sowing1.addMessage("half of the seeds into your store");
        tutMessages.add(sowing1);

        TutorialMessage arc = new TutorialMessage();
        arc.addMessage("In the arc red is your estimated winning");
        arc.addMessage("chance, white represents a draw and");
        arc.addMessage("green is the computers winnning chance");
        tutMessages.add(arc);

        TutorialMessage sowing2 = new TutorialMessage();
        sowing2.addMessage("The bottom houses is controlled by you");
        sowing2.addMessage("Start sowing by selecting the third house");
        sowing2.setPitMove(pits.get(2), BoardGameUtil.createMove(2));
        sowing2.addPostMessage("Since the last seed you sowed ended up in");
        sowing2.addPostMessage("your store it will be your turn again");
        tutMessages.add(sowing2);

        TutorialMessage stealing = new TutorialMessage();
        stealing.addMessage("If your last seed ends up in an empty house");
        stealing.addMessage("of yours and the opposite pit contain seeds, then");
        stealing.addMessage("both houses seeds will be captured to your store");
        tutMessages.add(stealing);

        TutorialMessage stealing2 = new TutorialMessage();
        stealing2.addMessage("In this position capture the opponents");
        stealing2.addMessage("seeds by sowing from the first house");
        try {
            int[] pits = {5, 5, 0, 5, 5, 0, 2, 5, 0, 0, 7, 6, 6, 2};
            int board[] = kalahaMLFN.createPosition(pits, FIRST_PLAYER, 4);
            stealing2.setupPosition(board);
        } catch (Exception e) {

        }

        stealing2.setPitMove(pits.get(0), BoardGameUtil.createMove(0));
        stealing2.addPostMessage("You added 6 seeds to the store. When playing");
        stealing2.addPostMessage("with empty capture variation you capture your");
        stealing2.addPostMessage("own last seed also when opposite side is empty");
        tutMessages.add(stealing2);

        TutorialMessage noMoves = new TutorialMessage();
        noMoves.addMessage("When one of the player has no seeds left");
        noMoves.addMessage("the game ends and the opponent moves");
        noMoves.addMessage("all remaining seeds to his store");

        try {
            int[] pits = {0, 10, 4, 1, 0, 3, 10, 0, 0, 0, 0, 0, 2, 18};
            int board[] = kalahaMLFN.createPosition(pits, SECOND_PLAYER, 4);
            noMoves.setupPosition(board);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tutMessages.add(noMoves);

        TutorialMessage noMoves2 = new TutorialMessage();
        noMoves2.addMessage("Here it is the computers turn and");
        noMoves2.addMessage("after the only possible move his side");
        noMoves2.addMessage("will be empty so the game ends");
        noMoves2.setComputerMove(BoardGameUtil.createMove(5));
        tutMessages.add(noMoves2);

        TutorialMessage fullScreen = new TutorialMessage();
        fullScreen.addMessage("When playing you can go fullscreen");
        fullScreen.addMessage("by pressing the blue down arrow");
        fullScreen.setSprite(enlargeBoardButton);
        tutMessages.add(fullScreen);

        TutorialMessage moveList = new TutorialMessage();
        moveList.addMessage("There will also be a list of moves available");
        moveList.addMessage("Use it to go back in the game and if desired");
        moveList.addMessage("regret a move by pressing that position 2 seconds");
        //moveList.setSprite(bottomPanelBackgroundSprite);
        tutMessages.add(moveList);

        TutorialMessage startAGame = new TutorialMessage();
        startAGame.addMessage("Press back button to setup a game");
        startAGame.setCanGoNext(false);
        tutMessages.add(startAGame);
        return tutMessages;
    }
}
