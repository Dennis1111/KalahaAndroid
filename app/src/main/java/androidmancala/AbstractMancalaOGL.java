package androidmancala;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import androidmancala.animation.MoveAnimation;
import androidmancala.menu.R;
import androidmancala.movelist.MoveList;
import androidmancala.movelist.MoveListMove;
import androidmancala.opengl.GLUtil;
import androidmancala.opengl.Image2DColorShader;
import androidmancala.opengl.Sprite;
import androidmancala.opengl.TextManager;
import androidmancala.opengl.TextObject;
import androidmancala.opengl.TextureAtlas;
import androidmancala.opengl.TextureAtlasSubImage;
import androidmancala.opengl.TextureAtlasUtil;
import boardgame.BasicMove;
import boardgame.BoardGameUtil;
import boardgame.GameState;
import boardgame.MancalaBoardGame;
import boardgame.WinDrawLossEvaluation;
import boardgame.kalaha.KalahaMove;

import static android.opengl.GLES20.GL_MAX_TEXTURE_IMAGE_UNITS;
import static boardgame.MancalaBoardGame.CANT_MOVE;
import static boardgame.MancalaBoardGame.FIRST_PIT_SECOND_PLAYER;
import static boardgame.MancalaBoardGame.FIRST_PLAYER;
import static boardgame.MancalaBoardGame.KALAHA_FIRST_PLAYER;
import static boardgame.MancalaBoardGame.KALAHA_SECOND_PLAYER;
import static boardgame.MancalaBoardGame.NO_PROGRESS;
import static boardgame.MancalaBoardGame.SECOND_PLAYER;

/**
 * Created by Dennis on 2018-01-21.
 */

public abstract class AbstractMancalaOGL implements Receiver, GLSurfaceView.Renderer, MyAndroidGame {
    private List<MotionEvent> eventQueue;
    /* Graphics Variables */
    protected float ssu = 1.0f;
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];
    //Only needed for Creating of atlas
    private int[] textureNames;
    protected TextManager textManager;
    private List<TextObject> pitMarblesTO;
    protected Image2DColorShader image2DColorShader;
    private static final String PIT_BOARDER = "PIT_BOARDER";
    private static final String BOARD = "BOARD";
    private static final String PIT = "PIT";
    private static final String BOTTOM_PANEL = "BOTTOM_PANEL";
    private static final String DOWN_ARROW = "DOWN_ARROW";
    private static final String UP_ARROW = "UP_ARROW";
    private static final String DIALOGUE_NO_TEXT = "DIALOGUE_NO_TEXT";

    private Sprite boardSprite;
    protected Sprite selectedPositionSprite;
    private Sprite bottomPanelBackgroundSprite, winDrawLossSprite, androidSprite;
    private Dialogue compWinDialogue, drawDialogue, playerWinDialogue;
    private Dialogue newGameDialogue;
    private Dialogue playFromHereDialogue, yesDialogue, noDialogue;
    private Dialogue loadGameDialogue;
    protected Dialogue next;
    private List<Dialogue> dialogues;
    protected List<Sprite> compPitBorders, playerPitBorders;
    protected float pitBorderAngle = 0;
    protected static final float left = 0f, up = 90f, right = 180f, down = 270f;
    protected TextureAtlas atlasTexture;
    protected int textureIDCounter = 0;

    private MoveAnimation moveAnimation;

    protected MoveList moveList;
    private boolean showMoveList = true;
    private boolean focusOnLast = true;
    protected List<Marble> marbles;
    private WinDrawLoss winDrawLoss;
    protected Map<Integer, WinDrawLossEvaluation> boardEvaluations;
    private Map<Integer, WinDrawLossEvaluation> boardEvaluationsCashe;

    protected Map<String, LocalGameNode> childBoardEvaluations;

    protected GraphicalBoard board;
    //How often to update the screen in seconds when computer is thinking
    protected final static long alphaBetaMessageInterval = 5000;

    //Screen info
    protected static int screenWidth;
    protected static int screenHeight;
    protected static int boardWidth;
    protected static int boardHeight;
    private static int remainingHeight;
    private static long backTime;
    protected Button enlargeBoardButton, diminishBoardButton;
    protected Android android;

    protected int marblesPerPit = 1;
    protected int skill;
    //protected static int marbleHeight = 0;
    //All pits on the board 0..14 including kalaha
    protected static List<Pit> pits;

    protected int playerThatStarts;
    //any moves to animate ?
    private int animationQueue = 0;
    private boolean moveListJump;
    protected Thread alphaBetaMove = null;

    private long playFromHereTime = 0;
    private boolean playFromHereTimer = false;
    private static final long PLAY_FROM_HERE_MIN_TIME = 1000;//One Second
    protected Resources resources;
    private boolean fullScreen;
    private boolean resizing;
    protected int boardHeightShrinked;
    protected Activity context;
    protected String whoBegins;
    private int tutorialCounter = 0;
    private boolean tutorialNextActionDown = false;
    protected boolean tutorialMode;

    private List<TutorialMessage> tutMessages;
    private List<TextObject> tutTextObjects;
    private List<Sprite> tutHideSprites;
    protected boolean loadgame = false;
    protected boolean loadgameQuestion = false;

    protected MancalaSoundPool soundPool;
    protected int searchDepth = 1;
    protected MancalaBoardGame boardGame;
    private boolean useAndroid = true;

    //Cashe the valid moves for a pos (last) for better gui performance
    private boolean[] validMovesCashe = new boolean[6];
    private int validMovesCashePos = -1;

    public AbstractMancalaOGL(Activity context, Resources resources, int marblesPerPit, int skill, String whoBegins) {
        this.context = context;
        this.resources = resources;
        this.marblesPerPit = marblesPerPit;
        this.skill = skill;
        this.whoBegins = whoBegins;
    }

    /*protected void useAndroid(boolean use) {
        this.useAndroid = use;
    }
     */

    /*The kalaha y pos is the y center of the board */
    protected int getBoardYCenter() {
        return (int) pits.get(KALAHA_FIRST_PLAYER).getY();
    }

    public void pause() {
        if (soundPool != null)
            soundPool.realease();
        finishAlphaBetaServices(false);
    }

    public void resume() {
        soundPool = new MancalaSoundPool(context);
    }

    protected boolean onSurfaceChangedNeeded() {
        //System.out.println("texture id counter"+textureIDCounter);
        return textureIDCounter == 0;
    }

    /**
     * Tell the game to finish any alpha beta searches
     *
     * @param wait when true the method doesn't exit before all alpa beta processes are finished
     *             which is more thread safe
     */
    protected abstract void finishAlphaBetaServices(boolean wait);

    protected abstract MancalaBoardGame loadGame(Context context) throws IOException;

    public abstract void saveGame(Context context) throws IOException;

    public abstract void startTheGame();

    protected abstract MoveAnimation getSowingAnimation(int move, int[] board, List<Pit> sourcePits);

    //This will start a service such that when finished we receive an evaluation of the position
    protected abstract void startEvaluationService(int position, int player, int maxSearchDepth);

    protected abstract int getSearchDepth(int playerToMove, int skill);

    protected abstract WinDrawLossEvaluation evaluate(int boardIndex);

    protected abstract WinDrawLossEvaluation evaluate(int[] board);

    protected abstract List<TutorialMessage> getTutorialMessages();

    /*
     * Returns true as long there is an active evluation service running an evaluation
     */
    protected abstract boolean evaluationServicesRunning();

    public synchronized void postEvent(MotionEvent event) {
        //When might get called from gamepanel with an Event
        //before all initialization is done
        if (eventQueue == null)
            return;
        eventQueue.add(event);
    }

    private void updateTutMode() {
        try {
            TutorialMessage message = tutMessages.get(tutorialCounter);

            if (!message.started()) {
                message.start();
                if (message.getBoard() != null) {
                    int player = boardGame.getPlayerToMove(message.getBoard());
                    setupPosition(player, message.getBoard());
                }
                if (!message.canGoNext())
                    next.setVisible(false);
                if (message.getSprite() != null) {

                    tutHideSprites.remove(message.getSprite());
                    message.getSprite().setVisible(true);
                }
            }

            List<String> messages = message.getCurrentMessages();
            for (int textObjCount = 0; textObjCount < tutTextObjects.size(); textObjCount++) {
                if (textObjCount >= messages.size())
                    tutTextObjects.get(textObjCount).setText("");
                else
                    tutTextObjects.get(textObjCount).setText(messages.get(textObjCount));
            }
            if (message.hasMove())
                if (message.hasComputermove()) {
                    char[] move = message.consumeMove();
                    boardGame.move(move);
                    moveList.addMove(new KalahaMove(SECOND_PLAYER, move));
                }
                //When we have a that is not from the computer we wait for the users action first
                else if (message.moveIsReady()) {
                    int player = boardGame.getPlayerToMove();
                    char[] move = message.consumeMove();
                    boardGame.move(move);
                    moveList.addMove(new KalahaMove(player, move));
                    message.setReadyForNext();
                }

            if (message.canGoNext())
                next.setVisible(true);

            if (tutorialNextActionDown) {
                if (message.getSprite() != null)
                    tutHideSprites.add(message.getSprite());
                tutorialCounter++;
                tutorialNextActionDown = false;
            }

            if (!(tutorialCounter < tutMessages.size())) {
                tutorialMode = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Game update method.
     * Since both update and actionEvents can modify the game the need to be synchronized
     *
     * @param gameTime Elapsed game time in millisecondgames.
     */
    protected synchronized void update(long gameTime) {
        if (tutorialMode)
            updateTutMode();

        if (resizing && animationQueue == 0)
            resize();
        //When the loadGame questions is u we dont want to show new gameQuestion
        //When the game has ended and we are at last position
        //We show the winner and asks for new game or new gamesetup
        if (!loadgameQuestion && boardGame.gameEnded() && moveList.isLastPositionSelected()) {
            //newGameSetupDialogue.setVisible(true);
            newGameDialogue.setVisible(true);
            //We dont show text for all pits here
            updatePitText();
            int result = boardGame.getGameState();
            if (result == GameState.DRAW)
                drawDialogue.setVisible(true);
            else if (result == GameState.WIN)
                playerWinDialogue.setVisible(true);
            else
                compWinDialogue.setVisible(true);
        } else {
            //newGameSetupDialogue.setVisible(false);
            newGameDialogue.setVisible(false);
            compWinDialogue.setVisible(false);
            playerWinDialogue.setVisible(false);
            drawDialogue.setVisible(false);
        }

        int boardIndex = moveList.getSelectedPosition();
        int[] board = boardGame.getBoards().get(boardIndex);
        boolean isLastPosition = moveList.isLastPositionSelected();

        //If its the players turn and he has no move we add a CANT_MOVE
        //since it's a move he can't make himself
        if (isLastPosition && boardGame.getPlayerToMove() == FIRST_PLAYER &&
                !boardGame.gameEnded()) {
            List<char[]> moves = boardGame.getValidMoves();
            if (moves.size() == 1 && (moves.get(0) == CANT_MOVE || moves.get(0) == NO_PROGRESS)) {
                //System.out.println("adding no progress"+CANT_MOVE);
                boardGame.move(moves.get(0));
                moveList.addMove(new BasicMove(FIRST_PLAYER, moves.get(0)));
            }
        }

        //update android while animation can make it stutter
        if (moveAnimation.isFinished())
            updateWinDrawLossAndroid(boardIndex);
        pitBorderAngle = (pitBorderAngle + 1) % 360;

        //The animation might have advanced the boardIndex
        //boardIndex = moveList.getSelectedPosition();
        //In tutorialmode this only needs to be called once to setup positions
        //but it is no performance problem a little bad coding though
        updatePitBorders(boardIndex, isLastPosition, boardGame.getPlayerToMove(board));
        if (tutorialMode)
            updateTutPitboarders(tutMessages.get(tutorialCounter));

        if (loadgameQuestion) {
            loadGameDialogue.setVisible(true);
            yesDialogue.setVisible(true);
            noDialogue.setVisible(true);
            //We loadgameQuestion is active we only want want to wait for the users
            //yes or no choice
            return;
        } else {
            loadGameDialogue.setVisible(false);
            yesDialogue.setVisible(false);
            noDialogue.setVisible(false);
        }


        //Should make simpler code here
        //If if the computers turn and there is no alphaBetaMove in work start one
        if (!boardGame.gameEnded() && boardGame.getPlayerToMove() == SECOND_PLAYER
                && serviceNotActive()) {
            startEvaluationService(boardGame.getMoves().size(), SECOND_PLAYER, getSearchDepth(SECOND_PLAYER, skill));
        }

        if (moveListJump) {
            //The user want's to back so we finish the evaluation searches
            finishAlphaBetaServices(true);
            MancalaUtil.setupPitsAndMarbles(pits, marbles, board);
            updatePitText();
            moveListJump = false;
        } else {

            //When we focusOnLastPosition and there is no ongGoing animation and the current position isn't the last
            //and the queue is empty then increase the queue
            if (focusOnLast && moveAnimation.isFinished() && animationQueue == 0 &&
                    moveList.getSelectedPosition() < (moveList.getNumberOfPositions() - 1)) {
                animationQueue++;
            }

            //2. If there is moves queued up and no active animation create an animation
            if (moveAnimation.isFinished() && animationQueue > 0) {
                int move = moveList.getMove().getFrom();
                try {
                    moveAnimation = getSowingAnimation(move, board, pits);
                } catch (Exception e) {
                    e.printStackTrace();
                    boardGame.toString(board);
                }
                //When the move is a CANT_Move move the move is finished at once
                if (moveAnimation.isFinished()) {
                    moveList.next();
                    animationQueue--;
                }
            }

            if (!moveAnimation.isFinished()) {
                //We have some animation to do
                moveAnimation.animate();
                if (moveAnimation.isFinished()) {
                    moveList.next();
                    animationQueue--;
                    //resumeAlphaBetaServices();
                }
                updatePitText();
            }
        }

        if (playFromHereTimer && System.currentTimeMillis() - playFromHereTime > 20000)
            playFromHereTimer = false;
        if (playFromHereTimer && System.currentTimeMillis() - playFromHereTime > PLAY_FROM_HERE_MIN_TIME) {
            playFromHereDialogue.setVisible(true);
            yesDialogue.setVisible(true);
            noDialogue.setVisible(true);
        } else {
            playFromHereDialogue.setVisible(false);
            yesDialogue.setVisible(false);
            noDialogue.setVisible(false);
        }

        if (tutorialMode)
            for (Sprite hide : tutHideSprites) {
                hide.setVisible(false);
            }
    }

    private boolean serviceNotActive() {
        return (alphaBetaMove == null || !alphaBetaMove.isAlive());
    }

    /*        */
    protected void updateTutPitboarders(TutorialMessage message) {
        for (int i = 0; i < 6; i++) {
            playerPitBorders.get(i).setVisible(false);
            compPitBorders.get(i).setVisible(false);
        }
        if (message.getMove() == null)
            return;
        else {
            int move = Character.getNumericValue(message.getMove()[0]);
            if (move >= 0 && move <= 5 && !message.hasComputermove())
                playerPitBorders.get(Character.getNumericValue(message.getMove()[0])).setVisible(true);
        }
    }

    /*          */
    protected void updatePitBorders(int boardIndex, boolean isLastPosition, int playerToMove) {
        int selectedPit = moveList.getSelectedPit();
        selectedPositionSprite.setX(moveList.getMoveListMoves().get(selectedPit).getX());

        if (boardGame.gameEnded()) {
            for (Sprite sprite : playerPitBorders)
                sprite.setVisible(false);
            for (Sprite sprite : compPitBorders)
                sprite.setVisible(false);
            return;
        }

        if (!moveList.isLastPositionSelected()) {
            for (int pitNr = 0; pitNr < 6; pitNr++) {
                playerPitBorders.get(pitNr).setVisible(false);
                compPitBorders.get(pitNr);
            }
            return;
        }

        //float scaleY = ((float) boardHeight) / boardHeightShrinked;

        if (boardIndex != validMovesCashePos) {
            validMovesCashePos = boardIndex;
            updateValidMovesCashe();
        }

        for (int pitNr = 0; pitNr < 6; pitNr++) {
            Sprite playerPBSprite = playerPitBorders.get(pitNr);
            Sprite compPBSprite = compPitBorders.get(pitNr);
            if (playerToMove == MancalaBoardGame.SECOND_PLAYER) {
                compPBSprite.setVisible(validMovesCashe[pitNr]);
                playerPBSprite.setVisible(false);
            } else {
                compPBSprite.setVisible(false);
                playerPBSprite.setVisible(validMovesCashe[pitNr]);
            }
        }
    }

    private void updatePitBoardersPos() {
        //System.out.println("updatePitBoardersPos");
        float scaleY = ((float) boardHeight) / boardHeightShrinked;
        //float[] rgbaOpenGL = MancalaUtil.createOpenGLRGBA(pitBorderAngle, 0.95f, 0.95f, 0.95f);
        for (int pitNr = 0; pitNr < 6; pitNr++) {
            //The first part here is only needed at init and when changing board size
            Sprite playerPBSprite = playerPitBorders.get(pitNr);
            Pit playerPit = pits.get(pitNr);
            playerPBSprite.setCenterPosition(playerPit.getX(), GLUtil.screenYToGLY(playerPit.getY()));
            Sprite compPBSprite = compPitBorders.get(pitNr);
            compPBSprite.setScaleY(scaleY);
            Pit computerPit = pits.get(pitNr + FIRST_PIT_SECOND_PLAYER);
            compPBSprite.setCenterPosition(computerPit.getX(), GLUtil.screenYToGLY(computerPit.getY()));
            playerPBSprite.setVisible(false);
            playerPBSprite.setScaleY(scaleY);
            compPBSprite.setVisible(false);
            //playerPBSprite.setColor(rgbaOpenGL);
        }
    }

    private void updateValidMovesCashe() {
        Arrays.fill(validMovesCashe, false);
        for (char[] move : boardGame.getValidMoves()) {
            int moveIndex = BoardGameUtil.moveAsInt(move);
            if (moveIndex >= 0 && moveIndex <= 5)
                validMovesCashe[moveIndex] = true;
        }
        //System.out.println(boardGame.toString() + "ValidMove" + Arrays.toString(validMovesCashe));
    }

    private String getKey(int boardPosition, int pit) {
        return "BoardPos" + boardPosition + "Move" + pit;
    }

    private void updateWinDrawLossAndroid(int boardIndex) {
        Integer key = new Integer(boardIndex);
        //If there is no existing evaluation for this position we make a quick mlp evaluation
        if (!boardEvaluations.containsKey(key)) {
            try {
                WinDrawLossEvaluation eval;
                //Using a quick evaluation for th e player give often very different
                //evaluation compared with the computers previous evalution of his move
                //So its more consistent to just pass on the previous evaluation
                //or do a costly search just for visualisation
                if (boardEvaluations.containsKey(boardIndex - 1))
                    eval = boardEvaluations.get(boardIndex - 1);
                else
                    eval = evaluate(boardIndex);
                boardEvaluations.put(key, eval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        WinDrawLossEvaluation boardEvaluation = boardEvaluations.get(key);
        boolean evalationInCashe = boardEvaluationsCashe.containsKey(key) && boardEvaluationsCashe.get(key).equals(boardEvaluation);
        if (!evalationInCashe)
            boardEvaluationsCashe.put(key, boardEvaluation);

        //We dont want to update sprites if we have the evaluation or we have selected a new position
        if (!moveListJump && evalationInCashe)
            return;

        //This costs a lot of cpu so we don't want to it every round
        if (boardEvaluation != null) {
            double winPerc = boardEvaluation.getNormalizedWinningProbability();
            double drawPerc = boardEvaluation.getNormalizedDrawProbability();
            double lossPerc = boardEvaluation.getNormalizedLossProbability();
            winDrawLoss.setWinPerc(winPerc, drawPerc);
            winDrawLoss.updateBitmap();
            winDrawLossSprite.setBitmap(winDrawLoss.getBitmap());
            if (useAndroid) {
                android.setScore(winPerc - lossPerc);
                androidSprite.setBitmap(android.getBitmap());
            }
        }
    }

    /**
     * When touch on screen is detected.
     *
     * @param event MotionEvent
     */
    public synchronized void touchEvent_actionDown(MotionEvent event) {
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();
        if (tutorialMode) {

            tutMessages.get(tutorialCounter).actionDown(touchX, touchY);
            if (next.isVisible() && next.atThis(touchX, touchY)) {
                tutorialNextActionDown = true;
            }
            return;
        }

        //Wait for the computer to make his move (it can be a bit laggy when he is thinking)
        /*if (boardGame.getPlayerToMove() == SECOND_PLAYER && evaluationServicesRunning()) {
            return;
        }*/

        /*//To be safe it seem i can get in trouble if doing to much simultan
        if (animationQueue != 0) {
            return;
        }*/

        if (loadGameDialogue.isVisible()) {
            if (yesDialogue.atThis(touchX, touchY)) {
                playFromHereDialogue.setVisible(false);
                yesDialogue.setVisible(false);
                noDialogue.setVisible(false);
                loadgameQuestion = false;

            } else if (noDialogue.atThis(touchX, touchY)) {
                playFromHereDialogue.setVisible(false);
                yesDialogue.setVisible(false);
                noDialogue.setVisible(false);
                newGame();
                loadgameQuestion = false;
            }
            if (!boardGame.gameEnded())
                startTheGame();
            return;
        }

        if (newGameDialogue.isVisible() && newGameDialogue.atThis(touchX, touchY)) {
            newGame();
            startTheGame();
            return;
        }

        //Is the actionDown at the movelist ?
        if (showMoveList && touchY > boardHeight) {
            int numberOfPositions = moveList.getNumberOfPositions();
            int selectedPosition = moveList.getSelectedPosition();
            if (moveList.actionDownIsFirst(touchX, touchY) && selectedPosition != 0) {
                playFromHereTimer = false;
                moveList.selectFirst();
                animationQueue = 0;
                focusOnLast = false;
                moveListJump = true;
                moveAnimation.clear();
            }

            //The user selects last position and something else was selected before
            else if (moveList.actionDownIsLast(touchX, touchY) && selectedPosition != moveList.getNumberOfPositions() - 1) {
                playFromHereTimer = false;
                int largestShownPosition = moveList.getLargestShownPosition();
                moveList.selectPosition(largestShownPosition);
                int newMoves = moveList.getNumberOfPositions() - largestShownPosition;

                try {
                    if (moveList.getNumberOfPositions() <= moveList.movesToShow()) {
                        moveList.selectPit(largestShownPosition, largestShownPosition);
                    } else {
                        int selectedPit = moveList.movesToShow() - newMoves;
                        moveList.selectPit(selectedPit, largestShownPosition);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                moveListJump = true;
                focusOnLast = true;
            } else if (moveList.actionDownIsNext(touchX, touchY)) {
                playFromHereTimer = false;
                if (animationQueue == 0 && animationQueue + selectedPosition < numberOfPositions - 1)
                    animationQueue++;
            } else if (moveList.actionDownIsPrevious(touchX, touchY)
                    && System.currentTimeMillis() - backTime > 500) //We can back 2 moves/second
            {
                if (moveList.getSelectedPosition() > 0) {
                    playFromHereTimer = false;
                    moveList.back();
                    animationQueue = 0;
                    focusOnLast = false;
                    moveListJump = true;
                    moveAnimation.clear();
                    backTime = System.currentTimeMillis();
                }
            } else {
                int currentSelectedPit = moveList.getSelectedPit();
                int pitSelectionCand = moveList.actionAtPit(touchX, touchY);

                boolean samePit = pitSelectionCand == currentSelectedPit;
                if (pitSelectionCand >= 0 && moveList.pitIsSelectable(pitSelectionCand)) {
                    playFromHereTimer = false;
                    int playerToMoveFromHere = moveList.getMoveListMoves().get(pitSelectionCand).getPlayerToMove();
                    int position = moveList.getPosition(pitSelectionCand);
                    //We have pressed a pit in the movelist that corresponds to a game position
                    if (!samePit) {
                        animationQueue = 0;
                        focusOnLast = false;
                        moveListJump = true;
                        moveAnimation.clear();
                        moveList.selectPit(pitSelectionCand);

                        //Only allow back to positions where its the first players turn

                        if (playerToMoveFromHere == FIRST_PLAYER && position < (moveList.getNumberOfPositions() - 1)) {
                            playFromHereTime = System.currentTimeMillis();
                            playFromHereTimer = true;
                        }
                        if (moveList.isLastPositionSelected()) {
                            focusOnLast = true;
                        }
                    } else {
                        if (!playFromHereTimer && playerToMoveFromHere == FIRST_PLAYER && position < (moveList.getNumberOfPositions() - 1)) {
                            playFromHereTime = System.currentTimeMillis();
                            playFromHereTimer = true;
                        }
                    }
                }
            }
        }

        //We have pressed something within the boardsize
        else {
            if (enlargeBoardButton.isVisible() && enlargeBoardButton.atThis(touchX, touchY)) {
                enlargeBoardButton.setVisible(false);
                resizing = true;
            } else if (diminishBoardButton.isVisible() && diminishBoardButton.atThis(touchX, touchY)) {
                diminishBoardButton.setVisible(false);
                resizing = true;
            }   //When the moveList wasn't pressed we check for backing position && moves
            else if (playFromHereDialogue.isVisible()) {
                if (yesDialogue.atThis(touchX, touchY)) {
                    playFromHere(moveList.getSelectedPosition());
                    playFromHereDialogue.setVisible(false);
                    yesDialogue.setVisible(false);
                    noDialogue.setVisible(false);
                    playFromHereTimer = false;
                } else if (noDialogue.atThis(touchX, touchY)) {
                    playFromHereDialogue.setVisible(false);
                    yesDialogue.setVisible(false);
                    noDialogue.setVisible(false);
                    playFromHereTimer = false;
                }
            }

            //prevent making moves when focus isn't on last position
            else if (!boardGame.gameEnded() && moveList.isLastPositionSelected() && boardGame.getPlayerToMove() == FIRST_PLAYER) {
                char[] pitChoice = this.getTouchMove(touchX, touchY);
                if (pitChoice != null) {
                    if (boardGame.isValidMove(pitChoice)) {
                        boardGame.move(pitChoice);
                        moveList.addMove(new KalahaMove(FIRST_PLAYER, pitChoice));
                        if (boardGame.getPlayerToMove() == SECOND_PLAYER && !boardGame.gameEnded())
                            startEvaluationService(boardGame.getMoves().size(), SECOND_PLAYER, getSearchDepth(SECOND_PLAYER, skill));
                        focusOnLast = true;
                    }
                }
            }
        }
    }

    private void playFromHere(int position) {
        //1 Shut down all AlphaBetaServices

        //2 Back the game to this position
        int[] board = boardGame.getBoards().get(position);
        while (boardGame.getBoard() != board) {
            boardGame.back();
            moveList.removeLastMove();
        }
        focusOnLast = true;
    }

    protected void processEventQueue() {
        if (!eventQueue.isEmpty()) {
            MotionEvent event = eventQueue.get(0);
            eventQueue.remove(event);
            int action = event.getAction();

            if (action == MotionEvent.ACTION_DOWN) {
                touchEvent_actionDown(event);
            }

            if (action == MotionEvent.ACTION_MOVE) {
                touchEvent_actionMove(event);
            }

            if (action == MotionEvent.ACTION_UP) {
                touchEvent_actionUp(event);
            }
        }
    }

    /**
     * When moving on screen is detected.
     *
     * @param event MotionEvent
     */
    public void touchEvent_actionMove(MotionEvent event) {

    }

    /**
     * When touch on screen is released.
     *
     * @param event MotionEvent
     */
    public void touchEvent_actionUp(MotionEvent event) {
        if (playFromHereTimer && System.currentTimeMillis() - playFromHereTime < PLAY_FROM_HERE_MIN_TIME) {
            playFromHereTimer = false;
        }
    }

    /**
     * Checks if touch is made at at a pit 1..6 for player 0.
     *
     * @param touchX X coordinate of the touch.
     * @param touchY Y coordinate of the touch.
     */
    private char[] getTouchMove(float touchX, float touchY) {
        for (int i = 0; i < 6; i++) {
            Pit pit = pits.get(i);
            double distX = Math.abs((int) touchX - pit.getX());
            double distY = Math.abs((int) touchY - pit.getY());

            if (distX < pit.getWidth() / 2 && distY < pit.getHeight() / 2) {
                return BoardGameUtil.createMove(i);
            }
        }
        return null;
    }

    protected void newGame() {
        initVariables();
        initPlayerThatStarts();
        boardGame.newGame(playerThatStarts, marblesPerPit);
        MancalaUtil.setupPitsAndMarbles(pits, marbles, boardGame.getBoard());
        updatePitText();
    }

    private void setupPosition(int playerThatStarts, int[] boardSetup) {
        initVariables();
        try {
            boardGame.newGame(playerThatStarts, marblesPerPit, boardSetup);
        } catch (Exception e) {
            //Shouldn't happen unless we provide an illegal position
            e.printStackTrace();
        }
        MancalaUtil.setupPitsAndMarbles(pits, marbles, boardGame.getBoard());
        updatePitText();
    }

    protected void initPlayerThatStarts() {
        if (tutorialMode) {
            this.playerThatStarts = FIRST_PLAYER;
            return;
        }
        if (whoBegins.equals("Computer")) {
            playerThatStarts = SECOND_PLAYER;
        } else if (whoBegins.equals("Random")) {
            playerThatStarts = (Math.random() > 0.5) ? FIRST_PLAYER : SECOND_PLAYER;
        } else
            this.playerThatStarts = FIRST_PLAYER;
    }


    //Some initiation that will always be the same regardless setup
    protected void initVariables() {
        this.moveAnimation = new MoveAnimation(atlasTexture, soundPool);
        moveList.reset();
        moveListJump = false;
        animationQueue = 0;
        boardEvaluations = new HashMap<>();
        boardEvaluationsCashe = new HashMap<>();
        childBoardEvaluations = new HashMap<>();
        this.eventQueue = new ArrayList<>();
        for (Dialogue dialogue : dialogues)
            dialogue.setVisible(false);
    }

    protected void setupText() {
        this.pitMarblesTO = new ArrayList<>();
        try {
            for (Dialogue dialogue : dialogues) {
                TextObject textObject = dialogue.getTextObject();
                float textHeight = dialogue.getHeight() / 2;
                textObject.setTextHeight(textHeight);
                textManager.addText(textObject);
            }

            //How many marbles
            for (Pit pit : pits) {
                setUpPitText(pit);
            }

            if (!tutorialMode && !fullScreen)
                for (MoveListMove moveListMove : moveList.getMoveListMoves()) {
                    TextObject moveNumber = moveListMove.getMoveNumberAsTextObject();
                    float textHeight = moveListMove.getHeight() / 3;
                    moveNumber.setTextHeight(textHeight);
                    textManager.addText(moveNumber);
                    TextObject move = moveListMove.getMoveAsTextObject();
                    move.setTextHeight(textHeight);
                    textManager.addText(move);
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        textManager.prepareDraw();
    }

    private void setUpPitText(Pit pit) {
        TextObject to = new TextObject("" + pit.getMarbles().size(), pit.getX(), GLUtil.screenYToGLY(pit.getY()));
        to.setTextHeight(screenWidth * 0.05f);//The screenwidth is most important to choose textsize so XX will fit in the pit
        pitMarblesTO.add(to);
        textManager.addText(to);
    }

    /*
     * Simple set the text for the pits, sometimes when showing dialogues we only want show text kalaha pits
     * since its get a little messy to watch
     * */
    protected void updatePitText() {
        boolean showOnlyKalaha = playerWinDialogue.isVisible() || drawDialogue.isVisible() || compWinDialogue.isVisible() || playFromHereDialogue.isVisible();
        for (int i = 0; i < pits.size(); i++) {
            Pit pit = pits.get(i);
            String txt = "" + pits.get(i).getMarbles().size();
            if ((i != KALAHA_FIRST_PLAYER && i != KALAHA_SECOND_PLAYER && showOnlyKalaha)) {
                txt = "";
            }
            pitMarblesTO.get(i).setText(txt);
            pitMarblesTO.get(i).setPosition(pit.getX(), GLUtil.screenYToGLY(pit.getY()));
        }
    }

    protected int getNextTextureID() {
        textureIDCounter++;
        return textureIDCounter;
    }

    private class LocalGameNode {
        protected WinDrawLossEvaluation wdl;
        protected boolean isFinished;

        public LocalGameNode(WinDrawLossEvaluation wdl, boolean isFinished) {
            this.wdl = wdl;
            this.isFinished = isFinished;
        }
    }

    private void resize() {
        if (fullScreen)
            goSplitScreen();
        else
            goFullScreen();
        resizing = false;
    }


    private void goFullScreen() {
        boardHeight = screenHeight;
        selectedPositionSprite.setVisible(false);
        showMoveList = false;
        for (MoveListMove mlm : moveList.getMoveListMoves()) {
            textManager.remove(mlm.getMoveAsTextObject());
            textManager.remove(mlm.getMoveNumberAsTextObject());

        }
        resizeAll();
        fullScreen = true;
        enlargeBoardButton.setVisible(false);
        diminishBoardButton.setVisible(true);
    }

    private void goSplitScreen() {
        boardHeight = boardHeightShrinked;
        selectedPositionSprite.setVisible(true);
        showMoveList = true;
        for (MoveListMove mlm : moveList.getMoveListMoves()) {
            textManager.addText(mlm.getMoveAsTextObject());
            textManager.addText(mlm.getMoveNumberAsTextObject());

        }
        resizeAll();
        fullScreen = false;
        enlargeBoardButton.setVisible(true);
        diminishBoardButton.setVisible(false);
    }

    private void resizeAll() {
        boardSprite.setHeight(boardHeight);
        boardSprite.setCenterPosition(screenWidth / 2, GLUtil.screenYToGLY(boardHeight / 2));
        boardSprite.updateSprite();
        changePitPositions();
        updatePitBoardersPos();
        updatePitText();
    }

    /* setup a split screen and create MoveListMoves
        create winDrawLoss and android */
    private void splitScreenInit() {
        boardHeight = boardHeightShrinked;
        boardWidth = screenWidth;
        fullScreen = false;
        resizing = false;

        int movesToShow = 6;
        int moveListWidth = (int) (screenWidth * 0.8f);
        int moveListHeight = screenHeight - boardHeightShrinked;

        List<MoveListMove> moveListMoves = new ArrayList<>();
        for (int i = 0; i < movesToShow; i++) {
            MoveListMove moveListMoveSprite = new MoveListMove(1.0f, moveListWidth / 10f, moveListHeight, 0, 0, i, false);
            moveListMoves.add(moveListMoveSprite);
        }

        moveList = new MoveList(0, boardHeight, moveListWidth, moveListHeight, moveListMoves);
        int wdlWidth = screenWidth - moveListWidth;
        int wdlYPos = screenHeight - moveListHeight / 2;
        int wdlXPos = moveListWidth + wdlWidth / 2;
        try {
            WinDrawLossEvaluation evaluation = evaluate(boardGame.getBoard());
            double winPercentage = evaluation.getNormalizedWinningProbability();
            double drawPercentage = evaluation.getNormalizedDrawProbability();
            this.winDrawLoss = new WinDrawLoss(wdlXPos, wdlYPos, wdlWidth, moveListHeight, winPercentage, drawPercentage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int androidWidth = (int) (moveListHeight * 0.7);
        int androidHeight = (int) (androidWidth * 0.8);
        int androidYPos = screenHeight - androidHeight / 2;
        this.android = new Android(wdlXPos, androidYPos, androidWidth, androidHeight);
    }

    private void changePitPositions() {
        float yPosBottomPits = boardHeight * board.getYPercPlayerPits();
        float deltaYBottomPits = yPosBottomPits - pits.get(0).getY();
        for (int pit = 0; pit < 6; pit++) {
            Pit bottomPit = pits.get(pit);
            bottomPit.setY(yPosBottomPits);
            for (Marble marble : bottomPit.getMarbles()) {
                marble.setY(marble.getY() + deltaYBottomPits);
            }
        }

        float kalahaYPos = boardHeight * 0.5f;

        Pit kalahaP0 = pits.get(KALAHA_FIRST_PLAYER);
        float deltaYKalaha = kalahaYPos - kalahaP0.getY();
        kalahaP0.setY(kalahaYPos);
        for (Marble marble : kalahaP0.getMarbles()) {
            marble.setY(marble.getY() + deltaYKalaha);
        }

        int yPosTopPits = (int) ((double) boardHeight * board.getYPercCompPits());
        float deltaYTopPits = yPosTopPits - pits.get(FIRST_PIT_SECOND_PLAYER).getY();
        for (int pit = 0; pit < 6; pit++) {
            Pit topPit = pits.get(pit + FIRST_PIT_SECOND_PLAYER);
            topPit.setY(yPosTopPits);
            for (Marble marble : topPit.getMarbles()) {
                marble.setY(marble.getY() + deltaYTopPits);
            }
        }

        Pit kalahaP1 = pits.get(KALAHA_SECOND_PLAYER);
        kalahaP1.setY(kalahaYPos);
        for (Marble marble : kalahaP1.getMarbles()) {
            marble.setY(marble.getY() + deltaYKalaha);
        }
    }

    protected void onSurfaceChangedInit(int width, int height) {
        textureIDCounter = 0;
        this.screenWidth = width;
        this.screenHeight = height;
        GLUtil.setScreenHeight(height);
        // Clear our matrices
        for (int i = 0; i < 16; i++) {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, screenWidth, 0.0f, screenHeight, 0, 50);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);

        boardHeightShrinked = (int) (0.8 * screenHeight);


        IntBuffer i = IntBuffer.allocate(1);
        GLES20.glGetIntegerv(GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, i);
        //System.out.println("MAX Combined Texture" + i.get());
        IntBuffer i2 = IntBuffer.allocate(1);
        GLES20.glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS, i2);
        //System.out.println("MAX Texture image units" + i2.get());
        //There should always be atleast 8 available !?
        int numberOfTextures = 6;
        textureNames = new int[numberOfTextures];
        GLES20.glGenTextures(numberOfTextures, textureNames, 0);
        //System.out.println("Texturenames -->" + Arrays.toString(textureNames));
        splitScreenInit();
        GLES20.glViewport(0, 0, width, height);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public synchronized void onDrawFrame(GL10 gl) {
        try {
            processEventQueue();
            update(SystemClock.currentThreadTimeMillis());
            //Redraw background color
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            //perhaps update can be improved with some variable so that it doesn't update
            //when nothing has changed don't know how costly this is
            atlasTexture.update();
            atlasTexture.render(mtrxProjectionAndView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (!fullScreen) {
                moveList.updateMoveListMoves();
                winDrawLossSprite.render(mtrxProjectionAndView);
                if (useAndroid)
                    androidSprite.render(mtrxProjectionAndView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            textManager.prepareDraw();
            textManager.render(mtrxProjectionAndView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void initSprites(int width, int height) {
        boardSprite = new Sprite(ssu, width, boardHeightShrinked);
        boardSprite.setCenterPosition(width / 2, height - (boardHeightShrinked / 2));
        boardSprite.setName(BOARD);

        this.dialogues = new ArrayList<>();
        this.playFromHereDialogue = new Dialogue(ssu, boardWidth * 0.4f, boardHeight * 0.15f, "Play from here ?", boardWidth * 0.5f, boardHeight * 0.5f);
        this.dialogues.add(playFromHereDialogue);
        this.yesDialogue = new Dialogue(ssu, boardWidth * 0.15f, boardHeight * 0.15f, "Yes", boardWidth * 0.2f, boardHeight * 0.5f);
        this.dialogues.add(yesDialogue);
        this.noDialogue = new Dialogue(ssu, boardWidth * 0.15f, boardHeight * 0.15f, "No", boardWidth * 0.8f, boardHeight * 0.5f);
        this.dialogues.add(noDialogue);

        this.compWinDialogue = new Dialogue(ssu, boardWidth * 0.6f, boardHeight * 0.2f, "Computer Won !", boardWidth * 0.5f, boardHeight * 0.3f);
        this.dialogues.add(compWinDialogue);
        this.drawDialogue = new Dialogue(ssu, boardWidth * 0.6f, boardHeight * 0.2f, "The Game is a draw !", boardWidth * 0.5f, boardHeight * 0.3f);
        this.dialogues.add(drawDialogue);
        this.playerWinDialogue = new Dialogue(ssu, boardWidth * 0.6f, boardHeight * 0.2f, "Well done  You Won!", boardWidth * 0.5f, boardHeight * 0.35f);
        this.dialogues.add(playerWinDialogue);
        this.newGameDialogue = new Dialogue(ssu, boardWidth * 0.4f, boardHeight * 0.2f, "New Game", boardWidth * 0.5f, boardHeight * 0.65f);
        this.dialogues.add(newGameDialogue);
        this.loadGameDialogue = new Dialogue(ssu, boardWidth * 0.4f, boardHeight * 0.15f, "Use saved game ?", boardWidth * 0.5f, boardHeight * 0.5f);
        this.dialogues.add(loadGameDialogue);
        remainingHeight = screenHeight - boardHeightShrinked;
        this.next = new Dialogue(ssu, remainingHeight, remainingHeight, "Next", remainingHeight / 2, screenHeight - remainingHeight / 2);
        this.dialogues.add(next);
    }

    /*
    * @gameSpecificImages pass TextureAtlasSubimages that is only used for the specific MancalaGame
    * */
    protected void createTextureAtlas(List<TextureAtlasSubImage> gameSpecificImages) {
        List<TextureAtlasSubImage> taSubImages = new ArrayList();
        Bitmap pitImage = BitmapFactory.decodeResource(resources, R.drawable.pit);
        Bitmap bottomPanel = MancalaUtil.createBottomPanel(resources, pitImage, moveList, screenWidth, screenHeight - boardHeightShrinked);

        float marbleSSU = 1.0f;
        board = GraphicalBoard.createTacticBoard(context, boardWidth, boardHeight, marblesPerPit, marbleSSU);

        this.pits = board.getPits();
        this.marbles = board.getMarbles();
        taSubImages.add(new TextureAtlasSubImage(BOARD, board.getBitmap()));
        taSubImages.add(new TextureAtlasSubImage(BOTTOM_PANEL, bottomPanel));
        taSubImages.add(new TextureAtlasSubImage(PIT, pitImage));
        List<Bitmap> marbleBitmaps = MancalaUtil.getMarbleBitmaps(context, 100, 100);
        for (int marble = 0; marble < marbleBitmaps.size(); marble++) {
            taSubImages.add(new TextureAtlasSubImage("marbleBM" + marble, marbleBitmaps.get(marble)));
        }

        taSubImages.add(new TextureAtlasSubImage(DIALOGUE_NO_TEXT, playFromHereDialogue.getBitmapNoText()));
        //The first pit that belongs to the computer
        Pit compPit = pits.get(FIRST_PIT_SECOND_PLAYER);
        Bitmap pitBorder = MancalaUtil.createCircle((int) compPit.getWidth(), (int) compPit.getHeight(), (int) (compPit.getHeight() * 0.02f));
        taSubImages.add(new TextureAtlasSubImage(PIT_BOARDER, pitBorder));
        Bitmap downImage = BitmapFactory.decodeResource(resources, R.drawable.down_arrow);
        taSubImages.add(new TextureAtlasSubImage(DOWN_ARROW, downImage));
        Bitmap upImage = BitmapFactory.decodeResource(resources, R.drawable.up_arrow);
        taSubImages.add(new TextureAtlasSubImage(UP_ARROW, upImage));
        if (gameSpecificImages != null)
            for (TextureAtlasSubImage subImage : gameSpecificImages)
                taSubImages.add(subImage);

        boolean recycleBitmaps = true;
        //float bottomPanelHeight = bottomPanel.getHeight();
        Bitmap atlas = TextureAtlasUtil.createAtlas(taSubImages, recycleBitmaps);
        //Can be interesting to check max texture size
        //int[] max = new int[1];
        //GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, max, 0); //put the maximum texture size in the array.
        //System.out.println("Max texture size" + max[0] + "Bitmap width" + atlas.getWidth() + " , height " + atlas.getHeight());
        this.atlasTexture = new TextureAtlas(atlas, taSubImages, image2DColorShader, textureIDCounter, textureNames[textureIDCounter], ssu);
        getNextTextureID();
        //We dont need the bitmap any more
        board.getBitmap().recycle();
    }

    /*
    * @sprites pass game specific sprites, these sprites will be added last and drawn above the others
    * */
    protected void atlasSpriteSetup(List<Sprite> sprites) {
        //Sometimes we get a glitch between board and movelist so increase height
        int bottomPanelHeight = screenHeight - boardHeightShrinked;
        bottomPanelBackgroundSprite = new Sprite(ssu, screenWidth, bottomPanelHeight + 3);
        bottomPanelBackgroundSprite.setName(BOTTOM_PANEL);
        bottomPanelBackgroundSprite.setCenterPosition(screenWidth / 2, (bottomPanelHeight / 2));
        //It's important to add sprites in correct order (background first)
        atlasTexture.addSprite(bottomPanelBackgroundSprite);
        atlasTexture.addSprite(boardSprite);
        float buttonWidth = screenHeight * 0.06f;
        float buttonXPos = screenWidth - (buttonWidth / 2);
        float buttonYpos = boardHeight - (buttonWidth / 2);
        enlargeBoardButton = new Button("Enlarge", buttonXPos, buttonYpos, buttonWidth, buttonWidth);
        enlargeBoardButton.setVisible(true);

        atlasTexture.addSprite(enlargeBoardButton);

        try {
            atlasTexture.addMapping(enlargeBoardButton.getName(), DOWN_ARROW);
            float buttonEnlargeYPos = screenHeight - (buttonWidth / 2);
            diminishBoardButton = new Button("Diminish", buttonXPos, buttonEnlargeYPos, buttonWidth, buttonWidth);
            diminishBoardButton.setVisible(false);
            atlasTexture.addSprite(diminishBoardButton);
            atlasTexture.addMapping(diminishBoardButton.getName(), UP_ARROW);

            for (Marble marble : marbles) {
                atlasTexture.addSprite(marble);
                int index = (int) (Math.random() * MancalaUtil.marbleBitmaps.length);
                String marbleBitmapName = "marbleBM" + index;
                atlasTexture.addMapping(marble.getName(), marbleBitmapName);
            }

            compPitBorders = new ArrayList<>();
            playerPitBorders = new ArrayList<>();

            for (int pitNr = 0; pitNr < 6; pitNr++) {
                Pit playerPit = pits.get(pitNr);
                Sprite sprite = new Sprite(ssu, playerPit.getWidth(), playerPit.getHeight());
                sprite.setVisible(!tutorialMode);
                sprite.setName("PlayerPitBorder" + pitNr);
                atlasTexture.addSprite(sprite);
                atlasTexture.addMapping(sprite.getName(), PIT_BOARDER);
                playerPitBorders.add(sprite);
                Pit computerPit = pits.get(pitNr + FIRST_PIT_SECOND_PLAYER);
                sprite = new Sprite(ssu, computerPit.getWidth(), computerPit.getHeight());
                sprite.setVisible(!tutorialMode);
                sprite.setName("AndroidPitBorder" + pitNr);
                atlasTexture.addSprite(sprite);
                atlasTexture.addMapping(sprite.getName(), PIT_BOARDER);
                compPitBorders.add(sprite);
            }
            updatePitBoardersPos();
            MoveListMove mlm = moveList.getMoveListMoves().get(0);
            selectedPositionSprite = new Sprite(ssu, mlm.getWidth(), mlm.getHeight() * 1.0f);
            selectedPositionSprite.setName(PIT_BOARDER);
            atlasTexture.addSprite(selectedPositionSprite);

            for (Dialogue dialogue : dialogues) {
                atlasTexture.addSprite(dialogue);
                atlasTexture.addMapping(dialogue.getName(), DIALOGUE_NO_TEXT);
            }
            if (sprites != null)
                for (Sprite sprite : sprites) {
                    atlasTexture.addSprite(sprite);
                }
            atlasTexture.update();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Should maybe be split into more parts
    protected void onSurfaceChangedRest() {
        Bitmap fonts = BitmapFactory.decodeResource(resources, R.drawable.font);
        this.textManager = new TextManager(textureIDCounter, textureNames[textureIDCounter], fonts, image2DColorShader);
        getNextTextureID();
        setupText();

        winDrawLossSprite = new Sprite(textureIDCounter, textureNames[textureIDCounter], image2DColorShader, ssu, winDrawLoss.getWidth(), winDrawLoss.getHeight());
        getNextTextureID();
        float bottomPanelGLYPos = GLUtil.screenYToGLY(winDrawLoss.getYPos());
        winDrawLossSprite.setCenterPosition(winDrawLoss.getXPos(), bottomPanelGLYPos);

        androidSprite = new Sprite(textureIDCounter, textureNames[textureIDCounter], image2DColorShader, ssu, android.getBitmap().getWidth(), android.getBitmap().getHeight());
        getNextTextureID();
        androidSprite.setBitmap(android.getBitmap());
        androidSprite.setCenterPosition(android.getXPos(), GLUtil.screenYToGLY(android.getYPos()));
        soundPool = new MancalaSoundPool(context);

        initVariables();
        if (tutorialMode) {
            tutMessages = getTutorialMessages();
            int x = boardWidth / 2;
            int textHeight = (int) (remainingHeight / 4.2);
            int remainingSpace = remainingHeight - textHeight * 3;
            int textHeightSpace = remainingSpace / 4;
            int y = (remainingHeight / 2) + textHeightSpace + textHeight;
            tutTextObjects = new ArrayList<>();
            //Use the three text rows for messages
            for (int t = 0; t < 3; t++) {
                TextObject to = new TextObject("", x, y);
                to.setTextHeight(textHeight);
                tutTextObjects.add(to);
                y -= (textHeight + textHeightSpace);
                textManager.addText(to);
            }
            next.setVisible(true);
            tutHideSprites = new ArrayList<>();
            tutHideSprites.add(bottomPanelBackgroundSprite);
            tutHideSprites.add(selectedPositionSprite);
            tutHideSprites.add(enlargeBoardButton);
            tutHideSprites.add(newGameDialogue);
            tutHideSprites.add(playerWinDialogue);
            tutHideSprites.add(compWinDialogue);
            tutHideSprites.add(loadGameDialogue);
            startTheGame();
            return;
        } else {
            if (loadgame) {
                try {
                    MancalaBoardGame loadedGame = this.loadGame(context);
                    if (loadedGame == null) {
                        loadgameQuestion = false;
                    } else {
                        MancalaUtil.setupPitsAndMarbles(pits, marbles, boardGame.getBoard());
                        updatePitText();
                        return;
                    }
                } catch (IOException e) {
                    loadgameQuestion = false;
                    e.printStackTrace();
                }
            }
            newGame();
            startTheGame();
        }
    }

    /*
     * Performs sowing by modifying boardPits
     * @pitIndexes the first index tell which pit to empty and then the sequence of pit (indexes) to sow in
     * @List<Pit> boardPits
     */

    protected void pitSowing(List<Integer> pitIndexes, List<Pit> boardPits) {
        int from = pitIndexes.get(0).intValue();
        List<Marble> marbles = boardPits.get(from).getMarbles();
        int sowingPitsIndex = 1;
        Pit fromPit = boardPits.get(from);
        for (Marble marble : marbles) {
            int to = pitIndexes.get(sowingPitsIndex).intValue();
            fromPit.transferToPit(marble, boardPits.get(to));
            sowingPitsIndex++;
        }
    }
}
