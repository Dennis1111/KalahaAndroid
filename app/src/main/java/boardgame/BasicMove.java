package boardgame;

/**
 * Created by Dennis on 2017-10-17.
 */

public class BasicMove implements Move {
    private int player;
    private char[] move;

    public BasicMove(int player,int move)
    {
        this.player=player;
        this.move = BoardGameUtil.createMove(move);
    }

    public BasicMove(int player,char[] move)
    {
        this.player=player;
        this.move=move;
    }

    public char[] getMove()
    {
        return move;
    }

    public int getPlayer()
    {
        return player;
    }

    public int getFrom()
    {
        return move[0]-48;
    }

    public static int getFrom(char[] move)
    {
        return move[0]-48;
    }

    //Not needed in kalaha,oware
    public int getTo()
    {
        return 0;
    }
}

