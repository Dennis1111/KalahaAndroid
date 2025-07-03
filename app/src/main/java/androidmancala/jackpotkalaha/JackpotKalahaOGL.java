package androidmancala.jackpotkalaha;

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
import androidmancala.animation.MarbleCollectionSound;
import androidmancala.opengl.TextureAtlasSubImage;
import boardgame.BasicMove;
import boardgame.BoardGameUtil;
import boardgame.PitPair;
import boardgame.WinDrawLossEvaluation;
import boardgame.algoritm.GameNodeWDL;
import boardgame.algoritm.GameTreeWDL;

import androidmancala.DiceMinMaxService;
import androidmancala.MancalaUtil;
import androidmancala.Pit;
import androidmancala.TutorialMessage;
import androidmancala.animation.AnimationUtil;
import androidmancala.animation.MarbleAnimation;
import androidmancala.animation.MoveAnimation;
import androidmancala.dice.Dice;
import androidmancala.opengl.Image2DColorShader;
import androidmancala.opengl.Sprite;

import boardgame.jackpotkalaha.JackpotKalaha;
import boardgame.jackpotkalaha.JackpotKalahaMLFN;
import mlfn.MultiLayerFN;

import static androidmancala.MancalaUtil.transfer;
import static boardgame.MancalaBoardGame.FIRST_PLAYER;
import static boardgame.MancalaBoardGame.KALAHA_FIRST_PLAYER;
import static boardgame.MancalaBoardGame.PLAYER_TO_MOVE;
import static boardgame.MancalaBoardGame.SECOND_PLAYER;

public class JackpotKalahaOGL extends AbstractMancalaOGL {

    /* Graphics Variables */
    private Dice dice1, dice2;

    private List<DiceMinMaxService> diceMinMaxServices;

    private static final int hiddens = 30;
    private JackpotKalahaMLFN jackpotKalahaMLFN;

    /* Constructor for tutorial mode */
    public JackpotKalahaOGL(Activity context, Resources resources, int skill, String whoBegins) {
        super(context, resources, 4, skill, whoBegins);
        MultiLayerFN[] mlfns = MancalaUtil.readMLFNs(JackpotKalahaMLFN.getFileNames(hiddens), resources);
        this.jackpotKalahaMLFN = new JackpotKalahaMLFN(FIRST_PLAYER, 4, true, hiddens, mlfns);
        this.boardGame = jackpotKalahaMLFN;
        this.skill = skill;
        initSkill(skill);
        this.diceMinMaxServices = new ArrayList<>();
        this.tutorialMode = true;
    }

    public JackpotKalahaOGL(Activity context, Resources resources, int marbles, int skill, String whoBegins) {
        super(context, resources, marbles, skill, whoBegins);
        MultiLayerFN[] mlfns = MancalaUtil.readMLFNs(JackpotKalahaMLFN.getFileNames(hiddens), resources);
        this.jackpotKalahaMLFN = new JackpotKalahaMLFN(FIRST_PLAYER, marbles, true, hiddens, mlfns);
        this.boardGame = this.jackpotKalahaMLFN;
        this.skill = skill;
        initSkill(skill);
        this.whoBegins = whoBegins;
        this.diceMinMaxServices = new ArrayList<>();
        this.loadgame = true;
        this.loadgameQuestion = true;
        this.tutorialMode = false;
    }

    @Override
    public void startEvaluationService(int position, int player, int maxSearchDepth) {
        startDiceMinMaxService(position, player, maxSearchDepth);
    }

    @Override
    public WinDrawLossEvaluation evaluate(int position) {
        return evaluate(jackpotKalahaMLFN.getBoards().get(position));
    }

    @Override
    public WinDrawLossEvaluation evaluate(int[] board) {
        return jackpotKalahaMLFN.evaluate(board);
    }

    @Override
    protected boolean evaluationServicesRunning() {
        removeFinishedABS();
        return diceMinMaxServices.size() > 0;
    }

    @Override
    protected synchronized void update(long gameTime) {
        int boardIndex = moveList.getSelectedPosition();
        dice1.setRolledValue(jackpotKalahaMLFN.getDie1(boardIndex) + 1);
        dice2.setRolledValue(jackpotKalahaMLFN.getDie2(boardIndex) + 1);

        float y = pits.get(KALAHA_FIRST_PLAYER).getY();
        dice1.setY(y);
        dice2.setY(y);
        super.update(gameTime);
    }

    private void makePreMoves(int[] preMoves, List<int[]> dices) {
        int pos = 0;
        for (int preMove : preMoves) {
            try {
                jackpotKalahaMLFN.setDies(dices.get(pos)[0], dices.get(pos)[1]);
                int playerToMove = jackpotKalahaMLFN.getPlayerToMove();
                jackpotKalahaMLFN.move(BoardGameUtil.createMove(preMove));
                moveList.addMove(new BasicMove(playerToMove, preMove));
                pos++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        moveList.selectLast();
    }

    //Some initiation that will always be the same regardless setup
    protected void initVariables() {
        super.initVariables();
        diceMinMaxServices = new ArrayList<>();
    }

    protected List<TutorialMessage> getTutorialMessages() {
        List<TutorialMessage> tutMessages = new ArrayList<>();
        TutorialMessage intro = new TutorialMessage();
        intro.addMessage("Jackpot Kalah is an experimental version");
        intro.addMessage("of Kalah using dice and a Jackpot rule!");
        tutMessages.add(intro);

        TutorialMessage dice = new TutorialMessage();
        dice.addMessage("Two dice is used and you are not");
        dice.addMessage("allowed to make moves with houses");
        dice.addMessage("that correspond to your dice");
        tutMessages.add(dice);

        /*
        TutorialMessage dice2 = new TutorialMessage();
        dice2.addMessage("Your leftmost house counts as your first house.");
        dice2.addMessage("So if any die shows 1 you can not move from there");
        tutMessages.add(dice2);*/

        TutorialMessage jackpot1 = new TutorialMessage();
        jackpot1.addMessage("Jackpot occurs when the sowing players last seed");
        jackpot1.addMessage("ends up in the players own house and the opponents");
        jackpot1.addMessage("opposite house has the same amount of seeds");
        tutMessages.add(jackpot1);

        TutorialMessage jackpot2 = new TutorialMessage();
        jackpot2.addMessage("When a player gets a jackpot he captures all");
        jackpot2.addMessage("seeds from the opponents store. If the last sowing");
        jackpot2.addMessage("house was empty a normal capture will also occur");
        tutMessages.add(jackpot2);

        TutorialMessage jackpot3 = new TutorialMessage();
        jackpot3.addMessage("Make a move from house 2 to get a jackpot");
        try {
            int[] pits = {0, 2, 0, 1, 7, 1, 4, 1, 2, 2, 7, 5, 7, 9};
            int board[] = jackpotKalahaMLFN.createPosition(pits, FIRST_PLAYER, 4);
            JackpotKalahaMLFN.setDies(3,4,board);
            jackpot3.setupPosition(board);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int move=1;
        jackpot3.setPitMove(pits.get(move), BoardGameUtil.createMove(move));
        tutMessages.add(jackpot3);

        TutorialMessage startAGame = new TutorialMessage();
        startAGame.addMessage("Press back button to setup a game!");
        startAGame.setCanGoNext(false);
        tutMessages.add(startAGame);
        return tutMessages;
    }

    private String getKey(int boardPosition) {
        return "BoardPos" + boardPosition;// + "Move" + pit;
    }

    public synchronized void receive(GameTreeWDL gameTree, boolean finished, int boardPosition, int player) {
        GameNodeWDL root = gameTree.getRoot();

        WinDrawLossEvaluation wdlEval = new WinDrawLossEvaluation(root.getWinningProbability(), root.getDrawProbability(), root.getLooseProbability());
        if (finished) {
            boardEvaluations.put(boardPosition, wdlEval);
            //We will use the evaluation for next positions also until it get's
            //overwritten by next alphaBeta but for the first player we skip the search and
            //simply use the computers evaluation of his last move
            boardEvaluations.put(Integer.valueOf(boardPosition + 1), wdlEval);
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
                    jackpotKalahaMLFN.move(bestMove);
                    moveList.addMove(new BasicMove(SECOND_PLAYER, bestMove));

                    if (!jackpotKalahaMLFN.gameEnded()) {
                        //New Move
                        int playerToMove = jackpotKalahaMLFN.getPlayerToMove();
                        if (playerToMove == SECOND_PLAYER)
                            startDiceMinMaxService(boardPosition + 1, playerToMove, getSearchDepth(playerToMove, skill));
                    }
                }
            }
        }
    }

    private void initSkill(int skill) {
        switch (skill) {
            case 0:
                searchDepth = 2;
                jackpotKalahaMLFN.setNewMoveDepth(1);
                jackpotKalahaMLFN.setFewSeedsDepth1(1);
                jackpotKalahaMLFN.setFewSeedsDepth2(1);
                break;

            case 1:
                searchDepth = 2;
                jackpotKalahaMLFN.setNewMoveDepth(0.4);
                jackpotKalahaMLFN.setFewSeedsDepth1(0.5);
                jackpotKalahaMLFN.setFewSeedsDepth2(1);
                break;
            case 2:
                searchDepth = 2;
                jackpotKalahaMLFN.setNewMoveDepth(0.3);
                jackpotKalahaMLFN.setFewSeedsDepth1(0.3);
                jackpotKalahaMLFN.setFewSeedsDepth2(0.5);
                break;
            case 3:
                searchDepth = 3;
                jackpotKalahaMLFN.setNewMoveDepth(0.3);
                jackpotKalahaMLFN.setFewSeedsDepth1(0.5);
                jackpotKalahaMLFN.setFewSeedsDepth2(0.7);
                break;
            case 4:
                searchDepth = 3;
                jackpotKalahaMLFN.setNewMoveDepth(0.2);
                jackpotKalahaMLFN.setFewSeedsDepth1(0.4);
                jackpotKalahaMLFN.setFewSeedsDepth2(0.7);
                break;
        }
    }

    //Think deeper for the computer moves also think deep when less marbles left
    @Override
    protected int getSearchDepth(int playerToMove, int skill) {
        if (tutorialMode)
            return 1;
        return searchDepth;
    }

    private void removeFinishedABS() {
        List<DiceMinMaxService> finishedABS = new ArrayList<>();
        for (DiceMinMaxService abs : diceMinMaxServices)
            if (abs.hasFinished())
                finishedABS.add(abs);
        for (DiceMinMaxService finished : finishedABS) {
            diceMinMaxServices.remove(finished);
        }
    }

    /*
     @wait if true we won't return until finished
     */
    protected void finishAlphaBetaServices(boolean wait) {
        if (diceMinMaxServices == null)
            return;
        for (DiceMinMaxService abs : diceMinMaxServices) {
            abs.noMessages();
            abs.finish();
        }
        boolean allFinished = false;
        if (wait)
            while (!allFinished) {
                allFinished = true;
                for (DiceMinMaxService abs : diceMinMaxServices) {
                    if (!abs.hasFinished()) {
                        allFinished = false;
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {

                    }
                }
            }
        removeFinishedABS();
    }

    private void startDiceMinMaxService(int position, int player, int maxSearchDepth) {
        DiceMinMaxService abs = new DiceMinMaxService(jackpotKalahaMLFN, this, maxSearchDepth, alphaBetaMessageInterval, position, player);
        diceMinMaxServices.add(abs);
        alphaBetaMove = new Thread(abs);
        alphaBetaMove.start();
    }

    @Override
    public void startTheGame() {
        if (jackpotKalahaMLFN.getPlayerToMove() == SECOND_PLAYER)
            startDiceMinMaxService(0, SECOND_PLAYER, getSearchDepth(jackpotKalahaMLFN.getPlayerToMove(), skill));
    }

    private String getFilename() {
        return "jpkal" + marblesPerPit + "s" + skill + "w" + whoBegins;
    }

    private boolean hasGameMoves() {
        return !tutorialMode && jackpotKalahaMLFN.getMoves().size() > 0;
    }

    @Override
    public void saveGame(Context context) throws IOException {
        if (!hasGameMoves())
            return;
        File file = new File(context.getFilesDir(), getFilename());
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        out.writeInt(jackpotKalahaMLFN.getBoards().get(0)[PLAYER_TO_MOVE]);
        List<char[]> moves = jackpotKalahaMLFN.getMoves();
        int nrOfMoves = moves.size();
        out.writeInt(nrOfMoves);
        for (char[] move : moves) {
            out.writeChar(move[0]);
        }

        for (int i = 0; i <= nrOfMoves; i++) {
            //int[] board = jackpotKalahaMLFN.getBoards().get(i);
            out.writeInt(jackpotKalahaMLFN.getDie1(i));
            out.writeInt(jackpotKalahaMLFN.getDie2(i));
        }
        out.close();
    }

    /*
    * return the as an int[] and the dies for each position in a list
    */
    protected JackpotKalahaMLFN loadGame(Context context) throws IOException {
        File file = new File(context.getFilesDir(), getFilename());
        if (!file.exists())
            return null;
        DataInputStream in = new DataInputStream(new FileInputStream(file));
        this.playerThatStarts = in.readInt();
        jackpotKalahaMLFN.newGame(playerThatStarts, marblesPerPit);
        int nrOfMoves = in.readInt();
        int[] moves = new int[nrOfMoves];
        for (int move = 0; move < nrOfMoves; move++) {
            moves[move] = in.readChar() - 48;
        }
        List<int[]> diceList = new ArrayList<>();
        for (int i = 0; i <= nrOfMoves; i++) {
            int[] dice = new int[2];
            dice[0] = in.readInt();
            dice[1] = in.readInt();
            diceList.add(dice);
        }
        in.close();
        makePreMoves(moves, diceList);
        return this.jackpotKalahaMLFN;
    }

    @Override
    protected MoveAnimation getSowingAnimation(int move, int[] board, List<Pit> sourcePits) {
        MoveAnimation moveAnimationLocal = new MoveAnimation(atlasTexture, soundPool);
        if (move > 5)
            return moveAnimationLocal;
        List<Pit> clonedPits = MancalaUtil.copyPits(sourcePits);
        int[] clonedBoard = Arrays.copyOf(board, board.length);

        List<Integer> sowingPitsIndex = jackpotKalahaMLFN.getSowingPits(clonedBoard, move);
        List<MarbleAnimation> sowingAnimations = AnimationUtil.getSowingAnimation(sowingPitsIndex, clonedPits, sourcePits);
        for (MarbleAnimation sow : sowingAnimations)
            sow.setSound(MancalaUtil.getMCSound(1, 0, (int) sow.getEndPit().getX(), boardWidth), soundPool);
        moveAnimationLocal.add(sowingAnimations);
        pitSowing(sowingPitsIndex, clonedPits);

        int lastPit = jackpotKalahaMLFN.sow(clonedBoard, move);

        if (jackpotKalahaMLFN.isJackpot(clonedBoard, lastPit)) {
            List<PitPair> pitPairs = jackpotKalahaMLFN.getJackpotPits(clonedBoard);
            for (PitPair pitPair : pitPairs) {
                List<MarbleAnimation> jpAnimations = AnimationUtil.getAnimation(pitPair, clonedPits, sourcePits);
                //The kalaha might be empty
                if (jpAnimations.size() == 0)
                    continue;
                MarbleCollectionSound mcSound = MancalaUtil.getMCSound(jpAnimations.size(), 0, pitPair.getSecond(), boardWidth);
                jpAnimations.get(0).setSound(mcSound, soundPool);

                moveAnimationLocal.add(jpAnimations);
                transfer(pitPair, clonedPits);
            }
            jackpotKalahaMLFN.jackpot(clonedBoard);
        }

        if (jackpotKalahaMLFN.isSteal(clonedBoard, lastPit)) {
            List<PitPair> pitPairs = jackpotKalahaMLFN.getStealPits(clonedBoard, lastPit);
            for (PitPair pitPair : pitPairs) {
                List<MarbleAnimation> stealAnimations = AnimationUtil.getAnimation(pitPair, clonedPits, sourcePits);
                MarbleCollectionSound mcSound = MancalaUtil.getMCSound(stealAnimations.size(), 0, pitPair.getSecond(), boardWidth);
                stealAnimations.get(0).setSound(mcSound, soundPool);
                moveAnimationLocal.add(stealAnimations);
                transfer(pitPair, clonedPits);
            }
            jackpotKalahaMLFN.steal(clonedBoard, lastPit);
        }

        if (jackpotKalahaMLFN.oneSideIsEmpty(clonedBoard) && !jackpotKalahaMLFN.gameEnded(clonedBoard)) {
            List<PitPair> pitPairs = jackpotKalahaMLFN.getFinishGamePits(clonedBoard);
            for (PitPair pitPair : pitPairs) {
                List<MarbleAnimation> marbleAnimations = AnimationUtil.getAnimation(pitPair, clonedPits, sourcePits);
                MarbleCollectionSound mcSound = MancalaUtil.getMCSound(marbleAnimations.size(), 0, pitPair.getSecond(), boardWidth);
                marbleAnimations.get(0).setSound(mcSound, soundPool);
                moveAnimationLocal.add(marbleAnimations);
                transfer(pitPair, clonedPits);
            }
        }
        jackpotKalahaMLFN.updatePlayerToMove(clonedBoard, lastPit);
        return moveAnimationLocal;
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
        if (onSurfaceChangedNeeded()){
            super.onSurfaceChangedInit(width, height);
            super.initSprites(width, height);
            float diceWidth = boardHeightShrinked * 0.06f;
            this.dice1 = new Dice(ssu, diceWidth, screenWidth * 0.5f - diceWidth, boardHeightShrinked * 0.5f);
            this.dice2 = new Dice(ssu, diceWidth, screenWidth * 0.5f + diceWidth, boardHeightShrinked * 0.5f);
            this.dice1.setVisible(true);
            this.dice2.setVisible(true);

            List<TextureAtlasSubImage> subImages = new ArrayList<>();
            for (int die = 0; die < 6; die++) {
                dice1.setRolledValue(die + 1);
                String name = dice1.getName();
                subImages.add(new TextureAtlasSubImage(name, dice1.getBitmap(die)));
            }
            super.createTextureAtlas(subImages);
            List<Sprite> sprites = new ArrayList<>();
            sprites.add(dice1);
            sprites.add(dice2);
            super.atlasSpriteSetup(sprites);
            super.onSurfaceChangedRest();
        }
    }
}
