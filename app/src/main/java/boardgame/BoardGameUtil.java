package boardgame;

/**
 * Created by Dennis on 2016-02-19.
 */
public class BoardGameUtil {

    public static char[] createMove(int move)
    {
        char[] moveChar= new char[1];
        moveChar[0]=(char)(move+48);
        return moveChar;
    }

    public static char[] createMove(char move)
    {
        char[] moveChar= new char[1];
        moveChar[0]=move;
        return moveChar;
    }

    public static int moveAsInt(char[] move)
    {
        return move[0]-48;
    }


    public static boolean isSameMove(char[] m1,char[] m2)
    {
        if (m1.length!=m2.length)
            return false;
        for(int i=0;i<m1.length;i++)
            if (m1[i]!=m2[i])
                return false;
        return true;
    }
}
