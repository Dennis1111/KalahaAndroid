package boardgame;

// and let the Games implement Move
public interface Move
{ 	
	char[] getMove();
	int getPlayer();
	int getFrom();
	int getTo();
}
