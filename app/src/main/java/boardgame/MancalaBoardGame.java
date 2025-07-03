package boardgame;

import java.util.List;

/**
 * Created by Dennis on 2018-01-22.
 */

public abstract class MancalaBoardGame implements BoardGame {
    //We store all information about a game position in the board
    //This way we can generate new positions with static methods making concurrency easier since
    //we are not snsitive to local game variables that can change.

    //Since multiple inheritence is now allowed and the Mancala gui needs to know Mancala board Specific Details
    //I made these Vairaibles VISIBLE
    public static final int FIRST_PLAYER = 0;
    public static final int SECOND_PLAYER = 1;

    //Store the whole games state in the board the following constants
    //are used as indexes in the board
    //board[0..5] will be player 0 pits to move from
    //board[6] will be the pit for player 0's won marbles
    public static final int FIRST_PIT_FIRST_PLAYER = 0;
    public static final int KALAHA_FIRST_PLAYER = 6;
    public static final int FIRST_PIT_SECOND_PLAYER = 7;
    public static final int KALAHA_SECOND_PLAYER = 13;
    public static final int PLAYER_TO_MOVE = 14;
    public static final int MARBLES_PER_PIT= 15;

    public static final char[] CANT_MOVE = {'n'};
    public static final char[] NO_PROGRESS ={'p'};
    protected List<char[]> gameMoves;
    protected List<int[]> boards;

    public MancalaBoardGame(){

    }
}
