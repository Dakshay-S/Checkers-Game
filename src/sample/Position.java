package sample;

import javafx.geometry.Pos;

public class Position {
    int row;
    int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public int hashCode() {
        return ("" + row + " " + col).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position))
            return false;
        Position ob = (Position)obj;

        return this.row == ob.row && this.col == ob.col;
    }
}
