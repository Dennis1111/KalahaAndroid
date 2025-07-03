package androidmancala;

import boardgame.BoardGameWDL;
import boardgame.algoritm.GameTreeWDL;
import boardgame.algoritm.alphabeta.AlphaBetaSIMLP;

public class AlphaBetaService implements Runnable {
	private BoardGameWDL boardGame;
	private Receiver receiver;
	private long messageInterval;
	private int maxDepth;
	private int boardPosition;
	private int player;
	private boolean finish = false;
	private boolean finished = false;
	private boolean sendMessages = true;
	private boolean pause;
	private boolean pauseStatusChanged;

	public AlphaBetaService(BoardGameWDL boardGame, Receiver receiver, int maxDepth, long messageInterval, int boardPosition, int player) {
		this.boardGame = boardGame;
		this.receiver = receiver;
		this.maxDepth = maxDepth;
		this.messageInterval = messageInterval;
		this.boardPosition = boardPosition;
		this.player = player;
		this.pause = false;
		this.pauseStatusChanged = false;
		//System.out.println("AB"+boardPosition+" : "+player+boardGame.toString()+"maxd"+maxDepth);
		//Not reliable
		//System.out.println("Number of cores" + Memory.getNumberOfCores());
	}

	public int getBoardPosition() {
		return boardPosition;
	}

	public boolean getPause() {
		return pause;
	}

	public BoardGameWDL getBoardGame() {
		return boardGame;
	}

	public void finish() {
		finish = true;
	}

	public boolean hasFinished() {
		return finished;
	}

	public void noMessages() {
		sendMessages = false;
	}

	public void setPause(boolean pause) {
		if (this.pause != pause) {
			this.pause = pause;
			this.pauseStatusChanged = true;
			//System.out.println("AlpaBeta set pause" + pause + "pos" + boardPosition);
		}
	}

	@Override
	public void run() {
		//System.out.println("new alphabetaService run" + boardPosition);
		AlphaBetaSIMLP ab = new AlphaBetaSIMLP(boardGame);
		//ab.setDebug(true);
		//ab.setStats(true);
		//Would be nice to know how manu cpu cores we have but NDK is needed for that
		ab.setMaxThreads(3);
		ab.setMaxDepth(maxDepth);
		//It's seems creating lot of threads is costly
		ab.setMaxNewThreadDepth(1);
		ab.setName("" + boardPosition);
		Thread ab2 = new Thread(ab);
		//ab2.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		ab2.start();
		GameTreeWDL gameTree = null;
		//boolean finished=false;
		double score = -1000000000;
		while (ab2.isAlive()) {
			if (finish) {
				ab.setPause(false);
				ab.finish();
				//sendMessages=false;
			}
			if (pauseStatusChanged) {
				ab.setPause(pause);
				pauseStatusChanged = false;
			}
			try {
				//System.out.println("Max mem"+ Memory.maxMem()/1024l);
				//System.out.println("Toal mem"+ Memory.totalMem()/1024l);
				//System.out.println("Used mem" + Memory.usedMem()/1024l);
				//System.out.println("Free memory"+ Memory.freeMemory()/1024l);

				Thread.sleep(messageInterval);
				gameTree = ab.getGameTree();
				if (gameTree != null) {
					double newScore = gameTree.getRoot().getScore();
					if (newScore != score) {
						score = newScore;
						if (sendMessages)
							receiver.receive(gameTree, false, boardPosition, player);
					}
				}
				//System.out.println("working"+ab.getRecursiveCalls());
				//System.out.println(ab.getGameTree());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		gameTree = ab.getGameTree();
		/*if (true) {
			System.out.println("ABService maxDepth" + maxDepth+"Recursive Calls"+ab.getRecursiveCalls());
			System.out.println("Board=" + boardGame.toString(boardGame.getBoard()));
			System.out.println("Score=" + ab.getGameTree().getRoot().getScore());
			System.out.println("BestMove=" + ab.getGameTree().getRoot().bestMove());
		}*/
		if (sendMessages)
			receiver.receive(gameTree, true, boardPosition, player);
		finished = true;
	}
}
