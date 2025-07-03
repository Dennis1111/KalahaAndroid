package androidmancala.opengl;

public class TextObject {

    public String text;
    public float x;
    public float y;
    public float[] color;
    public boolean centerX=true,centerY=true;
    public float uniformScale=1.0f;
    public boolean hasMoved;
    public boolean sizeHasChanged;
    public boolean textHasChanged;


    public void setStateUpdated()
    {
       hasMoved=false;
       sizeHasChanged=false;
       textHasChanged=false;
    }

    public TextObject(String txt, float xcoord, float ycoord)
    {
        text = txt;
        x = xcoord;
        y = ycoord;
        color = new float[] {1f, 1f, 1f, 1.0f};
        hasMoved=true;
        sizeHasChanged=true;
        textHasChanged=true;
    }

    public void setTextHeight(float height)
    {
       this.uniformScale=height/TextManager.getRiTextWidth();
       sizeHasChanged=true;
    }

    public void setX(float x)
    {
        if (this.x!=x)
          hasMoved=true;
        this.x=x;
    }

    public void setY(float y)
    {
        if (this.y!=y)
          hasMoved=true;
        this.y=y;
    }

    public void move(float deltaX,float deltaY)
    {
        if (deltaX!=0 || deltaY!=0)
            hasMoved=true;
        x+=deltaX;
        y+=deltaY;
    }

    public void setPosition(float x,float y)
    {
        setX(x);
        setY(y);
    }

    public void setText(String text)
    {
        if (!this.text.equals(text))
            textHasChanged=true;
        this.text=text;
    }

    public String toString()
    {
        return "TextObject"+text+" x="+x+" y= "+y;
    }
}