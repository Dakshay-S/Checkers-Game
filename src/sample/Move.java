package sample;

public class Move implements Cloneable{
    Position from;
    Position to;
    boolean captures = false;
    Position capturedPos = null;
    boolean coronate = false;

    public Move(Position from, Position to, Position capturedPos) {
        this.from = from;
        this.to = to;
        setCapturedPos(capturedPos);
    }

    public boolean doesCapture() {
        return captures;
    }

    public void setCaptures(boolean captures) {
        this.captures = captures;
    }


    public void setCapturedPos(Position capturedPos) {
        setCaptures(capturedPos != null);
        this.capturedPos = capturedPos;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        String str =  "FromRow: "+from.row+"  FromCol: "+from.col+"  ToRow: "+to.row+"  ToCol: "+to.col+"  captured: ";
        str += capturedPos == null? "null":"row: "+capturedPos.row+" col: "+capturedPos.col;
        str += "\n";
        return str;
    }
}
