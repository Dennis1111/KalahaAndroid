package boardgame.algoritm.alphabeta;

import java.util.*;
import java.util.concurrent.*;

import boardgame.*;
import boardgame.algoritm.*;

public class AlphaBetaSIMLP implements AlphaBetaSIWDL, AlgoritmThreaded, Runnable {
    private int seconds = 10000000;
    private int maxDepthGlobal = 3;
    private int skill;

    private volatile GameTreeWDL tree;
    private BoardGameWDL boardGame;

    private static int childThreads = 0;
    private boolean debug = false;
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

    public AlphaBetaSIMLP(BoardGameWDL boardGame) {
        this.boardGame = boardGame;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public int getRecursiveCalls() {
        return recursiveCalls;
    }

    //Not threadsafe (boardGame shouldnt make any moves from this call until clonedBoard is created)
    public GameTreeWDL evaluate(BoardGameWDL boardGame) {
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
            //System.out.println("ROOT CCS=" + rootCCs.size());
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
        //System.out.println(node.getTrueDepth()+"Enter alphaBeta" +recursiveCalls+ Arrays.toString(board));
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
        if (recursiveCalls % 50000 == 0) {
            System.out.println("leafs" + leafs + "Recurs" + recursiveCalls + ", HRC" + recursiveHeuristicCalls + ", Depth=" + depth + ", Node=" + node + "MaxDepth" + maxDepth);
        }*/
        //List<Integer> moves = boardGame.getValidMoves(board);

        if (debug)// || depth<=1.0)
        {
            System.out.println("leafs" + leafs);
            System.out.println("enter ab at depth=" + depth + "maxD=" + maxDepth + boardGame.toString(board) + Thread.currentThread().getName());
            System.out.println(node);
        }

        /*
        if (board == null) {
            System.out.print("NULLBOARD" + node);
            System.out.print("Parent" + boardGame.toString(node.getParent().getBoard()));
            System.exit(0);
        }*/

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
                System.out.println("MAXDEPTH" + boardGame.toString(board) + "score" + score + node + wdlEv);
                System.out.println("WIN" + wdlEv.getWinningProbability() + "Draw" + wdlEv.getDrawProbability() + "Loss" + wdlEv.getLossProbability());
            }
            return node;
        }

        List<AlphaBetaWDLNodeSync> children = heuristicIncrSearch(node, 0);

        if (finish)
            return node;
        if (isNewThreadDepth(node.getTrueDepth(), maxDepth)) {
            //System.out.println("new AB Thread" + node);
            //AlphaBetaWDLNodeSync clonedNode = node.clone();
            AlphaBetaWDLNodeSync threadedNode = alphaBetaChildThreads(node, children, maxDepth);
            if (maxDepth >= maxDepthGlobal)
                threadedNode.setFinished(true);
            //System.out.println("AB Thread finished" + node);
            return threadedNode;
        } else {
            AlphaBetaWDLNodeSync nodeAB = alphaBetaChildrens(node, children, maxDepth);
            if (maxDepth >= maxDepthGlobal)
                nodeAB.setFinished(true);
            return nodeAB;
        }
    }

    //Do a incremental heuristicSearch
    private List<AlphaBetaWDLNodeSync> heuristicIncrSearch(AlphaBetaWDLNodeSync node, int heuristicMaxDepth) {
        List<AlphaBetaWDLNodeSync> heuristicResult = null;
        List<AlphaBetaWDLNodeSync> children = createChildren(node);
        if (node.getSearchDepth() < 1)
            node.addChildrens(children);
        int startMaxDepth = 1 + (int) node.getSearchDepth();

        //System.out.println("startMaxDepth"+startMaxDepth+","+node.getSearchDepth()+"hmax"+heuristicMaxDepth);
        //System.out.println(boardGame.toString(node.getBoard()));
        for (int maxDepth = startMaxDepth; maxDepth < heuristicMaxDepth; maxDepth++) {
            heuristicResult = heuristicSearch(node, children, maxDepth);
            if (finish)
                return heuristicResult;
        }
        if (heuristicResult == null) {
            for (AlphaBetaWDLNodeSync child : children) {
                WinDrawLossEvaluation wdlEv = boardGame.evaluate(child.getBoard());
                double heuristicScore = wdlEv.getWinningProbability() - wdlEv.getLossProbability();
                child.setScore(heuristicScore);
            }
            Collections.sort(children);
            heuristicResult = children;
        }

        for (AlphaBetaWDLNodeSync child : heuristicResult) {
            if (!(child.getSearchDepth() > node.getSearchDepth()))
                System.exit(0);
        }
        //Perhaps check if still null also
        return heuristicResult;
    }

    //Do a heuriristicSeaerch and sort the result
    private List<AlphaBetaWDLNodeSync> heuristicSearch(AlphaBetaWDLNodeSync node, List<AlphaBetaWDLNodeSync> childrens, int maxDepth) {
        double currentDepth = node.getSearchDepth();
        alphaBetaChildrens(node, childrens, maxDepth);
        Collections.sort(childrens);
        node.setScore(childrens.get(0).getScore());
        node.setBestChild(childrens.get(0));
        return childrens;
    }

    private List<AlphaBetaWDLNodeSync> createChildren(AlphaBetaWDLNodeSync node) {
        List<AlphaBetaWDLNodeSync> childrens = new ArrayList<>();
        List<int[]> childBoards = new ArrayList<>();
        List<int[]> cyclicCandidates;
        if (boardGame.cyclicEnd())
            cyclicCandidates = getCyclicCandidates(node);
        else
            cyclicCandidates = null;

        List<char[]> moves = boardGame.getValidMoves(node.getBoard(), cyclicCandidates, childBoards);

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
        return childrens;
    }

    private List<int[]> getCyclicCandidates(AlphaBetaWDLNodeSync node) {
        List<int[]> cyclicCandidates = new ArrayList<>();
        int[] leafBoard = node.getBoard();
        int counter = 0;
        while (node.getParent() != null) {
            node = node.getParent();
            //if (cyclicCandidates.size() > 400)
            //    System.out.println(counter + ">400node" + node + "board" + boardGame.toString(node.getBoard()));
            counter++;
            if (boardGame.isCyclicCandidate(leafBoard, node.getBoard())) {
                cyclicCandidates.add(node.getBoard());
                //System.out.println("adding parent"+boardGame.toString(node.getBoard()));
            } else {
                //System.out.println("Not CC"+boardGame.toString(node.getBoard()));
                return cyclicCandidates;
            }

            if (cyclicCandidates.size() > 450)
                System.exit(0);
        }
        //The node has no parent so the node equals the root
        //check the roots candidates
        List<int[]> rootCCs = node.getCyclicCandidates();
        //Must iterate towards the game starting position so depends on on the order of rootCC
        for (int i = 0; i < rootCCs.size(); i++) {
            int[] rootCC = rootCCs.get(i);
            if (boardGame.isCyclicCandidate(rootCC, leafBoard)) {
                cyclicCandidates.add(rootCC);
            } else {
                return cyclicCandidates;
            }
        }
        return cyclicCandidates;
    }

    /*
    private void debugSorting(List<AlphaBetaWDLNodeSync> childrens) {
        System.out.println("\n Parent maximize=" + childrens.get(0).getParent().maximizeChildrens());
        for (AlphaBetaWDLNodeSync child : childrens) {
            System.out.print(child.getScore() + " , ");
        }
    }*/

    private AlphaBetaWDLNodeSync alphaBetaChildrens(AlphaBetaWDLNodeSync node, List<AlphaBetaWDLNodeSync> childrens, int maxDepth) {
        double alpha = node.getAlpha();
        double beta = node.getBeta();

        AlphaBetaWDLNodeSync bestChild = childrens.get(0);
        if (node.maximizeChildrens()) {
            boolean condTest = false;
            for (AlphaBetaWDLNodeSync child : childrens) {
                child.setAlpha(alpha);
                AlphaBetaWDLNodeSync alphaCandidate;
                alphaCandidate = alphaBeta(child, maxDepth);
                if (alphaCandidate.getScore() > alpha) {
                    alpha = alphaCandidate.getScore();
                    bestChild = child;
                    node.setGameState(alphaCandidate);
                    node.setBestChild(bestChild);
                    condTest = true;
                }
                betaCutCand++;

                if (beta <= alpha) {
                    betaCut++;
                    break;//Beta Cut Off
                }
            }
            node.setGameState(bestChild);
            node.setBestChild(bestChild);
        } else {
            //minimize
            for (AlphaBetaWDLNodeSync child : childrens) {
                child.setBeta(beta);
                AlphaBetaWDLNodeSync betaCandidate;
                betaCandidate = alphaBeta(child, maxDepth);
                if (debug)
                    System.out.println("nt betaCand" + betaCandidate);
                if (betaCandidate.getScore() < beta) {
                    beta = betaCandidate.getScore();
                    bestChild = child;
                    node.setGameState(betaCandidate);
                    node.setBestChild(bestChild);
                }
                alphaCutCand++;
                if (beta <= alpha) {
                    alphaCut++;
                    break;//Alpha Cut Off
                }
            }
            node.setGameState(bestChild);
            node.setBestChild(bestChild);
            if (debug) {
                System.out.println(node.getSearchDepth() + "best beta child" + bestChild);
                System.out.println("Best Max Child" + bestChild);
            }
        }
        return node;
    }

    private AlphaBetaWDLNodeSync alphaBetaChildThreads(AlphaBetaWDLNodeSync node, List<AlphaBetaWDLNodeSync> childrens, int maxDepth) {
        ExecutorCompletionService<AlphaBetaWDLNodeSync> ecs = new ExecutorCompletionService<>(threadPoolExecutor);
        List<Future<AlphaBetaWDLNodeSync>> childFutures = new ArrayList<>();
        int futureCount = 0;
        for (AlphaBetaWDLNodeSync child : childrens) {
            AlphaBetaCallable abcChild;
            abcChild = new AlphaBetaCallable(child, maxDepth);
            if (debug)
                System.out.println("Submitting child" + boardGame.toString(child.getBoard()) + "at depth" + child.getSearchDepth());
            Future<AlphaBetaWDLNodeSync> futureABC = ecs.submit(abcChild);
            childFutures.add(futureABC);
            futureCount++;
        }

        boolean updateSiblings;
        double alpha = node.getAlpha();
        double beta = node.getBeta();
        AlphaBetaWDLNodeSync alphaCandidate, betaCandidate;
        AlphaBetaWDLNodeSync bestChild = childrens.get(0);
        List<Future<AlphaBetaWDLNodeSync>> completedFutures = new ArrayList<>();
        if (node.maximizeChildrens()) {
            if (debug)
                System.out.println("maximize");
            for (int childCount = 0; childCount < childrens.size(); childCount++) {
                try {
                    Future<AlphaBetaWDLNodeSync> abcCompleted = ecs.take();
                    futureCount--;
                    if (completedFutures.contains(abcCompleted))
                        System.out.println("future already taken");
                    completedFutures.add(abcCompleted);
                    alphaCandidate = abcCompleted.get();
                    if (debug)
                        System.out.println("Child Future" + alphaCandidate + boardGame.toString(alphaCandidate.getBoard()));
                    updateSiblings = false;
                    if (alphaCandidate.getScore() > alpha) {
                        alpha = alphaCandidate.getScore();
                        bestChild = alphaCandidate;
                        updateSiblings = true;
                        //For intermediate results update now
                        node.setBestChild(bestChild);
                        node.setGameState(alphaCandidate);
                    }

                    if (beta <= alpha) {
                        //Need to be more complex if only one executor for all threads
                        for (Future<AlphaBetaWDLNodeSync> f : childFutures)
                            f.cancel(true);

                        //stop other childs
                        break;
                    }
                    if (updateSiblings) {
                        //Send alpha to siblings and propagate down
                        //for(AlphaBetaWDLNodeSync sibling : childrens)
                        // if (sibling != alphaCandidate)
                        //  sibling.propagateAlphaDown(alpha);

                        ;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (debug) {
                System.out.println("node maximize" + boardGame.toString(node.getBoard()) + node);
                System.out.println("bestChild" + boardGame.toString(bestChild.getBoard()) + bestChild);
            }
            node.setBestChild(bestChild);
            node.setGameState(bestChild);
            return node;
        } else //minimize
        {
            if (debug) {
                System.out.println("minimize");
                for (int childCount = 0; childCount < childrens.size(); childCount++)
                    System.out.println("Child Beta Thread Future" + childFutures.get(childCount));
            }
            for (int childCount = 0; childCount < childrens.size(); childCount++) {
                try {
                    Future<AlphaBetaWDLNodeSync> abcCompleted = ecs.take();
                    futureCount--;
                    //if (completedFutures.contains(abcCompleted))
                    //    System.out.println("future already taken");
                    completedFutures.add(abcCompleted);
                    betaCandidate = abcCompleted.get();
                    if (debug)
                        System.out.println(abcCompleted + "Thread Beta Cand" + betaCandidate + boardGame.toString(betaCandidate.getBoard()) + "score" + betaCandidate.getScore() + "Cand End");
                    updateSiblings = false;
                    if (betaCandidate.getScore() < beta) {
                        beta = betaCandidate.getScore();
                        bestChild = betaCandidate;
                        updateSiblings = true;
                        //Intermediate
                        node.setBestChild(bestChild);
                        node.setGameState(bestChild);
                    }

                    if (beta <= alpha) {
                        if (debug) {
                            System.out.println("min cut");
                        }
                        for (Future<AlphaBetaWDLNodeSync> f : childFutures)
                            f.cancel(true);
                        //stop other childs
                        break;
                    }

                    if (updateSiblings) {
                        //Send beta to siblings and propagate down
                        //for(AlphaBetaWDLNodeSync sibling : childrens)
                        //if (sibling != betaCandidate)
                        //sibling.propagateBetaDown(beta);
                        ;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (debug) {
                System.out.println("node minimize" + boardGame.toString(node.getBoard()) + node+"futureCount"+futureCount);
                System.out.println("best thread Child" + boardGame.toString(bestChild.getBoard()) + bestChild);
                System.out.println("recursive" + recursiveCalls);

            }
            node.setBestChild(bestChild);
            node.setGameState(bestChild);
            return node;
        }
    }
}
