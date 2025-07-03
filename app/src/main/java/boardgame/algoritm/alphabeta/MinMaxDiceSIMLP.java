package boardgame.algoritm.alphabeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import boardgame.BasicMove;
import boardgame.DiceBoardGameWDL;
import boardgame.GameState;
import boardgame.WinDrawLossEvaluation;
import boardgame.algoritm.AlgoritmThreaded;
import boardgame.algoritm.GameTreeWDL;

/**
 * Created by Dennis on 2017-10-17.
 */

public class MinMaxDiceSIMLP implements MinMaxDiceWDL, AlgoritmThreaded, Runnable {
    private int seconds = 10000000;
    private int maxDepthGlobal = 3;
    private int skill;

    private volatile GameTreeWDL tree;
    private DiceBoardGameWDL boardGame;

    private static int childThreads = 0;
    private boolean debug = false;
    private boolean debugScore = false;
    private boolean alphaBetaStats = false;

    //Statistic variables
    private static int recursiveCalls = 0;
    private static int recursiveHeuristicCalls = 0;
    private static int leafs = 0;

    private static double alphaCut = 0;
    private static double betaCut = 0;
    private static double alphaCutCand = 0;
    private static double betaCutCand = 0;
    //private ExecutorService executorService;
    private int maxThreads = 3;
    private long keepAliveTime = 1000000;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private ThreadPoolExecutor threadPoolExecutor;
    private long computationTime;
    private long mlpTime;
    private boolean finish = false;
    private boolean pause = false;
    private String name = "";//for debugging
    private int sleepTrials = 0;
    private int sleepFailures = 0;
    private int maxNewThreadDepth = 1;

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void finish() {
        finish = true;
    }

    public void setStats(boolean stats) {
        this.alphaBetaStats = stats;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MinMaxDiceSIMLP(DiceBoardGameWDL boardGame) {
        this.boardGame = boardGame;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getRecursiveCalls() {
        return recursiveCalls;
    }

    //Not threadsafe (boardGame shouldnt make any moves from this call until clonedBoard is created)
    public GameTreeWDL evaluate(DiceBoardGameWDL boardGame) {
        childThreads = 0;
        recursiveCalls = 0;
        recursiveHeuristicCalls = 0;
        alphaCut = 0;
        betaCut = 0;
        mlpTime = 0;
        long startTime = System.currentTimeMillis();
        this.boardGame = boardGame;

        int[] board = boardGame.getBoard();
        int[] clonedBoard = Arrays.copyOf(board, board.length);
        int player = boardGame.getPlayerToMove(board);
        boolean maximizePlayer = (player == 0);
        double alpha = -10000000;
        double beta = 10000000;
        double depth = 0;
        AlphaBetaWDLNodeSync root = new AlphaBetaWDLNodeSync("Root", alpha, beta, maximizePlayer, clonedBoard, depth);

        if (boardGame.cyclicEnd()) {
            List<int[]> rootCCs = boardGame.getCyclicCandidates();
            root.setCyclicCandidates(rootCCs);
        }
        this.tree = new AlphaBetaTreeWDL(root);

        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        /*Important if we try create create new recursive threads when pool is full we will never terminate */
        threadPoolExecutor = new ThreadPoolExecutor(maxThreads, maxThreads, keepAliveTime, timeUnit, workQueue);
        alphaBeta(root, maxDepthGlobal);
        computationTime = System.currentTimeMillis() - startTime;
        if (alphaBetaStats) {
            System.out.println("childThreads" + childThreads + "skill" + skill + "sec" + seconds);
            System.out.println("recursiveCalls" + recursiveCalls + " HeuristicRecursion" + recursiveHeuristicCalls);
            System.out.println("AlphaCuts" + alphaCut + " AlphaCutCand" + alphaCutCand + "Alpha%" + ((double) alphaCut) / alphaCutCand);
            System.out.println("BetaCuts" + betaCut + "BetaCutCand" + betaCutCand + "Beta%" + ((double) betaCut) / betaCutCand);
            System.out.println("MLPPerc" + ((double) mlpTime / computationTime) + " , " + mlpTime);
            System.out.println("ComputationTime = " + computationTime);
        }
        return tree;
    }

    public GameTreeWDL getGameTree() {
        return tree;
    }

    public void run() {
        evaluate(boardGame);
    }

    public void setMaxTime(int seconds) {
        this.seconds = seconds;
    }

    public void setMaxThreads(int threads) {
        this.maxThreads = threads;
    }

    public void setMaxDepth(int depth) {
        this.maxDepthGlobal = depth;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public void setMaxNewThreadDepth(int depth) {
        this.maxNewThreadDepth = depth;
    }

    //It costs some to start a new thread so we dont wanna start threads close to the leafs
    //and we dont need a new thread for the root nood
    //however having allowing new threads deeper will finish the branch faster which
    //and that might result be valuable for alpha beta cuts
    private boolean isNewThreadDepth(int depth, int maxDepth) {
        boolean isNewThread = depth > 0 && depth <= maxNewThreadDepth && maxThreads > 1;
        //if (isNewThread)
        //    System.out.println("Is new thread" + depth + " : " + Thread.currentThread().getName());
        return isNewThread;
    }

    private class AlphaBetaCallable implements Callable<AlphaBetaWDLNodeSync> {
        private AlphaBetaWDLNodeSync abNode;
        private int maxDepth;

        private AlphaBetaCallable(AlphaBetaWDLNodeSync abNode, int maxDepth) {
            this.abNode = abNode;
            this.maxDepth = maxDepth;

            //System.out.println("Creating callable" + abNode + "maxdepth" + maxDepth + "ch" + childThreads + boardGame.toString(abNode.getBoard()));
            childThreads++;
        }

        public AlphaBetaWDLNodeSync call() {
            Thread t = Thread.currentThread();
            t.setName(abNode.getName());
            alphaBeta(abNode, maxDepth);
            if (debug)
                System.out.println(abNode.getSearchDepth() + "CALLABLE RESULT" + abNode + boardGame.toString(abNode.getBoard()));
            return abNode;
        }

        public String toString() {
            String abCallable = "abcallable";
            abCallable = abNode.toString();
            return abCallable;
        }
    }

    public AlphaBetaWDLNodeSync alphaBeta(AlphaBetaWDLNodeSync node, int maxDepth) {
        int[] board = node.getBoard();
        if (debugScore)
            System.out.println("T" + node.getTrueDepth() + "D" + node.getSearchDepth() + ":" + maxDepth + "Enter alphaBeta" + recursiveCalls + Arrays.toString(board));
        while (pause) {
            long start = System.currentTimeMillis();
            try {
                sleepTrials++;
                Thread.sleep(1000);
                //System.out.println("Slept a sec" + name);
            } catch (Exception e) {
                sleepFailures++;
                long end = System.currentTimeMillis();
                //System.out.println("Failure Sleep for" + (end - start) + "millisec" + name);
                e.printStackTrace();
            }
            //System.out.println("AB Sleep" + name + "sleet triails" + sleepTrials + "failures" + sleepFailures);
        }
        double depth = node.getSearchDepth();
        recursiveCalls++;
        /*
        if (recursiveCalls % 2000000 == 0) {
            System.out.println("leafs" + leafs + "Recurs" + recursiveCalls + ", HRC" + recursiveHeuristicCalls + ", Depth=" + depth + ", Node=" + node + "MaxDepth" + maxDepth);
        }*/

        if (debug)// || depth<=1.0)
        {
            System.out.println("leafs" + leafs);
            System.out.println("enter ab at depth=" + depth + "maxD=" + maxDepth + boardGame.toString(board) + Thread.currentThread().getName());
            System.out.println(node);
        }

        int gameState = boardGame.getGameState(board);
        //node.setGameState(gameState);

        if (gameState != GameState.HEURISTIC) {
            switch (gameState) {
                case GameState.WIN:
                    node.setGameState(1, 0, 0, gameState);
                    break;
                case GameState.LOSS:
                    node.setGameState(0, 0, 1, gameState);
                    break;
                default: {
                    node.setGameState(0, 1, 0, gameState);
                }
            }
            leafs++;
            return node;
        } else if (depth >= maxDepth || finish) {
            long mlpBegin = System.currentTimeMillis();
            WinDrawLossEvaluation wdlEv = boardGame.evaluate(board);
            mlpTime += (System.currentTimeMillis() - mlpBegin);
            double score = wdlEv.getWinningProbability() - wdlEv.getLossProbability();
            node.setGameState(wdlEv.getWinningProbability(), wdlEv.getDrawProbability(), wdlEv.getLossProbability(), boardGame.getGameState(board));
            if (debug) {
                System.out.println(node.getTrueDepth() + "MAXDEPTH" + maxDepth + boardGame.toString(board) + "score" + score + node + wdlEv);
                System.out.println("WIN" + wdlEv.getWinningProbability() + "Draw" + wdlEv.getDrawProbability() + "Loss" + wdlEv.getLossProbability());
            }
            return node;
        }

        boolean isRootNode = node.isRoot();
        List<AlphaBetaWDLNodeSync> children = createChildren(node, isRootNode);
        if (isRootNode)
            minMaxWithDice(node, children, maxDepth);
        else
            minMax(node, children, maxDepth);
        if (maxDepth >= maxDepthGlobal)
            node.setFinished(true);
        return node;
    }

    private List<AlphaBetaWDLNodeSync> createChildren(AlphaBetaWDLNodeSync node, boolean useDice) {
        List<AlphaBetaWDLNodeSync> childrens = new ArrayList<>();
        List<int[]> childBoards = new ArrayList<>();
        List<char[]> moves = boardGame.getValidMoves(node.getBoard(), null, childBoards, useDice);
        /*System.out.println("Creating children" + boardGame.toString(node.getBoard()));
        for (char[] move : moves)
            System.out.println("Move" + BasicMove.getFrom(move));
        if (useDice)
            System.out.println("root" + useDice);
        */
        for (int i = 0; i < moves.size(); i++) {
            int[] childBoard = childBoards.get(i);
            char[] move = moves.get(i);
            //For some positions deepsearch is more valuable and moveDepth
            //will be closer to zero for those
            double moveDepth = boardGame.getMoveDepth(move, node.getBoard(), childBoard);
            int childNodePlayer = boardGame.getPlayerToMove(childBoard);
            boolean maximizeChildPlayer = (childNodePlayer == 0) ? true : false;
            double childSearchDept = moveDepth + node.getSearchDepth();
            String moveAsString = boardGame.toString(move);
            AlphaBetaWDLNodeSync child = new AlphaBetaWDLNodeSync(node.getName() + "," + (moveAsString), node.getAlpha(), node.getBeta(),
                    maximizeChildPlayer, childBoard, childSearchDept);
            child.setMoveFromParent(move);
            child.setParent(node);
            childrens.add(child);
        }
        //For the root node we add the childrens
        if (useDice)
            node.addChildrens(childrens);
        return childrens;
    }

    /* Only evaluate the chlidren generated with the postions dice */
    private AlphaBetaWDLNodeSync minMaxWithDice(AlphaBetaWDLNodeSync node, List<AlphaBetaWDLNodeSync> childrens, int maxDepth) {
        if (debugScore)
            System.out.println("MinMax With Dice root" + boardGame.toString(node.getBoard()));
        //1. Store the minMax val in child
        for (AlphaBetaWDLNodeSync child : childrens) {
            alphaBeta(child, maxDepth);
            if (debugScore) {
                System.out.println("MinMax With Dice Child score" + child.getScore() + boardGame.toString(child.getBoard()));
                System.out.println("GameState" + child.getGameState() + " ,WinProb" + child.getWinningProbability());
            }
        }

        AlphaBetaWDLNodeSync best = childrens.get(0);
        if (node.maximizeChildrens())
            for (AlphaBetaWDLNodeSync child : childrens) {
                if (best.getScore() < child.getScore())
                    best = child;
            }
        else
            for (AlphaBetaWDLNodeSync child : childrens) {
                if (best.getScore() > child.getScore())
                    best = child;
            }
        node.setBestChild(best);
        node.setGameState(best.getWinningProbability(), best.getDrawProbability(), best.getLooseProbability(), node.getGameState());
        return node;
    }

    /*
    *  Evaluate an position without knowing the dice
    *  1. First associate each possible move with an score
    *  2. Then when we know the scores calculate the score for each dice comb
    *  using the best move for given dice then we average all scores with their priors
    */
    private AlphaBetaWDLNodeSync minMax(AlphaBetaWDLNodeSync node, List<AlphaBetaWDLNodeSync> children, int maxDepth) {
        //AlphaBetaWDLNodeSync bestChild = childrens.get(0);
        //1. Store the minMax val in child
        for (AlphaBetaWDLNodeSync child : children) {
            alphaBeta(child, maxDepth);
            if (debugScore)
                System.out.println("MinMax Child score" + child.getScore());
        }
        //System.out.println("MinMax Parent" + boardGame.toString(node.getBoard()) + "children" + children.size());
        //for (AlphaBetaWDLNodeSync child : children)
        //    System.out.println("Child" + boardGame.toString(child.getBoard()));
        //double average = 0;
        double win = 0, draw = 0, loss = 0;
        //GameState gameState=
        double rescale = 0;
        for (int dice1 = 0; dice1 < 6; dice1++)
            for (int dice2 = dice1; dice2 < 6; dice2++) {
                List<AlphaBetaWDLNodeSync> subset = getSubset(children, dice1, dice2);
                if (subset.size() == 0)
                    continue;
                AlphaBetaWDLNodeSync bestChild = node.maximizeChildrens() ? maximize(subset) : minimize(subset);
                if (dice1 == dice2) {
                    win += bestChild.getWinningProbability() * 0.5;
                    loss += bestChild.getLooseProbability() * 0.5;
                    draw += bestChild.getDrawProbability() * 0.5;
                    rescale += 0.5;
                } else {
                    win += bestChild.getWinningProbability();
                    loss += bestChild.getLooseProbability();
                    draw += bestChild.getDrawProbability();
                    rescale += 1.0;
                }
                if (debug) {
                    System.out.println(node.getSearchDepth());
                }
                //System.out.println(dice1 + " : " + dice2 + "win" + win+"draw"+draw+"loss"+loss + "subset" + subset.size());
            }
        win /= rescale;
        draw /= rescale;
        loss /= rescale;
        //Getting gamestate is a little complex and not needed for now in jackpot kalaha
        node.setGameState(win, draw, loss, GameState.HEURISTIC);
        if (debugScore)
            System.out.println(node.getScore() + "Maximize=" + node.maximizeChildrens() + "score" + node.getScore() + "Board" + boardGame.toString(node.getBoard()));
        return node;
    }

    //Though normally we use dice 1..6 i use 0..5 here
    private List<AlphaBetaWDLNodeSync> getSubset(List<AlphaBetaWDLNodeSync> children, int dice1, int dice2) {
        List<AlphaBetaWDLNodeSync> subset = new ArrayList<>();
        for (AlphaBetaWDLNodeSync child : children) {
            int move = BasicMove.getFrom(child.getMoveFromParent());
            if (move == dice1 || move == dice2)
                continue;
            subset.add(child);
        }
        return subset;
    }

    private AlphaBetaWDLNodeSync maximize(List<AlphaBetaWDLNodeSync> children) {
        double max = -10000000;
        AlphaBetaWDLNodeSync maxChild = children.get(0);
        for (AlphaBetaWDLNodeSync child : children)
            if (child.getScore() > max) {
                max = child.getScore();
                maxChild = child;
            }
        return maxChild;
    }

    private AlphaBetaWDLNodeSync minimize(List<AlphaBetaWDLNodeSync> children) {
        double min = +1000000;
        AlphaBetaWDLNodeSync minChild = children.get(0);
        for (AlphaBetaWDLNodeSync child : children)
            if (child.getScore() < min) {
                min = child.getScore();
                minChild = child;
            }
        return minChild;
    }
}