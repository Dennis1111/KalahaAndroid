package boardgame.algoritm.alphabeta;

import java.util.List;
import java.util.ArrayList;

import boardgame.algoritm.GameNodeWDL;
import boardgame.BoardGame;

public class AlphaBetaWDLNodeSync implements GameNodeWDL, Comparable<AlphaBetaWDLNodeSync> {
    private char[] bestMove;
    private char[] moveFromParent;
    //private double score;

    private double winProbability, drawProbability, lossProbability;

    //The game state from players 0 view
    private int gamestate = -1;
    private AlphaBetaWDLNodeSync parent;
    private AlphaBetaWDLNodeSync bestChild;

    //To avoid threads waiting to much I use alpha and beta as volatile instead of sunchronize
    private volatile double alpha;
    private volatile double beta;

    private boolean maximizeChildrens;
    private final int[] board;
    private final String name;
    private double searchDepth;
    private List<GameNodeWDL> childrens;
    private boolean isFinished;
    private List<int[]> cyclicCandidates;

    public AlphaBetaWDLNodeSync(String name, double alpha, double beta, boolean maximizeChildrens, int[] board, double searchDepth) {
        this.name = name;
        this.alpha = alpha;
        this.beta = beta;
        this.board = board;
        this.maximizeChildrens = maximizeChildrens;
        this.bestMove = null;
        this.parent = null;
        this.bestChild = null;
        this.searchDepth = searchDepth;
        this.childrens = new ArrayList<>();
        isFinished = false;
        cyclicCandidates = null;
    }

    public int getTrueDepth() {
        int depth = 0;
        AlphaBetaWDLNodeSync parentPtr = parent;
        while (parentPtr != null) {
            depth++;
            parentPtr = parentPtr.getParent();
        }
        return depth;
    }

    protected void setCyclicCandidates(List<int[]> boards) {
        createCyclicCandidates();
        for (int[] board : boards)
            cyclicCandidates.add(board);
    }

    protected void addCyclicCandidate(int[] candidate) {
        cyclicCandidates.add(candidate);
    }

    protected void createCyclicCandidates() {
        cyclicCandidates = new ArrayList<>();
    }

    protected List<int[]> getCyclicCandidates() {
        return cyclicCandidates;
    }

    public char[] getMoveFromParent() {
        return moveFromParent;
    }

    public List<GameNodeWDL> getChildrens() {
        return childrens;
    }

    public void addChildrens(List<AlphaBetaWDLNodeSync> childsToAdd) {
        for (GameNodeWDL child : childsToAdd)
            childrens.add(child);
    }

    public double getSearchDepth() {
        return searchDepth;
    }

    public String getName() {
        return name;
    }

    public boolean isTerminal() {
        return bestChild == null ? true : false;
    }

    public void propagateAlphaDown(double newAlpha) {
        this.alpha = newAlpha;
        while (bestChild != null)
            bestChild.propagateAlphaDown(newAlpha);
    }

    public synchronized void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void propagateBetaDown(double newBeta) {
        this.beta = newBeta;
        while (bestChild != null)
            bestChild.propagateBetaDown(newBeta);
    }

    public synchronized void setBeta(double beta) {
        this.beta = beta;
    }

    public synchronized double getAlpha() {
        return alpha;
    }

    public synchronized double getBeta() {
        return beta;
    }

    public boolean isRoot() {
        return parent == null ? true : false;
    }

    public boolean maximizeChildrens() {
        return maximizeChildrens;
    }

    public int[] getBoard() {
        return board;
    }

    public synchronized void setMoveFromParent(char[] move) {
        this.moveFromParent = move;
    }

    public char[] moveFromParent() {
        return moveFromParent;
    }

    public synchronized char[] bestMove() {
        return bestMove;
    }

    public boolean finished() {
        return isFinished;
    }

    public void setFinished(boolean isFinished) {
        //if (searchDepth<1)
        // System.out.println(this.isFinished+" , "+isFinished+"Setting finished"+this);
        this.isFinished = isFinished;
    }

    public double getScore() {
        //System.out.println("ABSCORE"+winProbability+" , "+lossProbability);
        return winProbability - lossProbability;
    }

    public synchronized void setGameState(double win, double draw, double loss, int gamestate) {
        this.winProbability = win;
        this.drawProbability = draw;
        this.lossProbability = loss;
        this.gamestate = gamestate;
    }

    public synchronized void setGameState(AlphaBetaWDLNodeSync node) {
        this.winProbability = node.winProbability;
        this.drawProbability = node.drawProbability;
        this.lossProbability = node.lossProbability;
        this.gamestate = node.gamestate;
    }

    public synchronized void setScore(double score) {
        ;//this.score=score;
    }


    public synchronized void setBestChild(AlphaBetaWDLNodeSync child) {
        this.bestChild = child;
        this.bestMove = child.moveFromParent;
        //System.out.println("new beestmove"+bestMove);
    }

    public AlphaBetaWDLNodeSync bestChild() {
        return bestChild;
    }

    public synchronized void setParent(AlphaBetaWDLNodeSync parent) {
        this.parent = parent;
    }

    public AlphaBetaWDLNodeSync getParent() {
        return parent;
    }

    public synchronized void setGameState(int state) {
        this.gamestate = state;
    }

    public synchronized int getGameState() {
        return gamestate;
    }

    public synchronized double getDrawProbability() {
        return drawProbability;
    }

    public synchronized double getWinningProbability() {
        return winProbability;
    }

    public synchronized double getLooseProbability() {
        return lossProbability;
    }

    public synchronized void update(AlphaBetaWDLNodeSync child) {

    }

    public synchronized void propagateUp() {
        if (parent != null) {
            parent.update(this);
            parent.propagateUp();
        }
    }

    //Assumes we only compare nodes at same depth
    //If a node A has the same player moving again while the other node B hasn't
    //A is greater than, this is because Alpha Beta can't cut nodes in this nodes child but it's score
    //can be used for cutting in child nodes where node.useAlphaBetaCutOff==false
    //Otherwise when the
    //@Override

    public int compareTo(AlphaBetaWDLNodeSync abn) {
        double score = getScore();
        double abnScore = abn.getScore();
        //System.out.println("Diff"+(score-abnScore)+"Comparing"+this+" , With "+abn);

        if (!parent.maximizeChildrens) {
            if (score > abnScore)
                return 1;
            else if (score < abnScore)
                return -1;
            else return 0;
        } else {
            if (score < abnScore)
                return 1;
            else if (score > abnScore)
                return -1;
            else
                return 0;
        }
    }

    public synchronized AlphaBetaWDLNodeSync clone() {
        AlphaBetaWDLNodeSync clone = new AlphaBetaWDLNodeSync(name, alpha, beta, maximizeChildrens, board, searchDepth);
        clone.bestMove = bestMove;
        clone.moveFromParent = moveFromParent;
        clone.winProbability = winProbability;
        clone.drawProbability = drawProbability;
        clone.lossProbability = lossProbability;
        clone.gamestate = gamestate;
        clone.parent = parent;
        clone.bestChild = bestChild;
        clone.searchDepth = searchDepth;
        return clone;
    }

    public String toString() {
        String s = "";
        if (name != null)
            s += "name=" + name;
        s += "ABNODE score=" + getScore();//+" maxChildrens"+maximizeChildrens;
        //s+="move"+move;
        if (moveFromParent != null)
            s += "Parent Move" + moveFromParent[0];
        s += "score" + getScore();
        s += "isFinished" + isFinished;
        s += "alpha" + alpha;
        s += "beta" + beta;
        if (bestChild == null)
            s += "Leaf";
        return s;
    }
}
