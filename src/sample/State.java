package sample;


import java.util.*;

public class State {
    final static int N = 8; //board dimension
    PieceType[][] stateMatrix;


    public State(PieceType[][] stateMatrix) {
        this.stateMatrix = stateMatrix;
    }


    State getClone() {
        PieceType[][] clonedMatrix = new PieceType[N][];

        for (int i = 0; i < N; i++)
            clonedMatrix[i] = stateMatrix[i].clone();

        return new State(clonedMatrix);
    }


    PieceType getPieceType(Position pos) {
        return stateMatrix[pos.row][pos.col];
    }

    PieceType getPieceType(int row, int col) {
        return stateMatrix[row][col];
    }


    State cloneAndMakeMove(Move move) {
        State clone = this.getClone();
        PieceType picked = clone.removePieceFrom(move.from);
        clone.placePieceAt(picked, move.to);

        if (move.doesCapture())
            clone.removePieceFrom(move.capturedPos);

        return clone;
    }


    void makeMove(Move move) {
        PieceType picked = this.removePieceFrom(move.from);
        this.placePieceAt(picked, move.to);

        if (move.doesCapture())
            this.removePieceFrom(move.capturedPos);

        if (move.coronate)
            makeKing(move.to.row, move.to.col);
    }


    PieceType removePieceFrom(Position position) {
        PieceType removed = stateMatrix[position.row][position.col];
        stateMatrix[position.row][position.col] = PieceType.NULL;
        return removed;
    }


    void placePieceAt(PieceType piece, Position position) {
        stateMatrix[position.row][position.col] = piece;
    }


    List<IntermediateState> getAllPossibleNextStatesForTurn(Player player) throws CloneNotSupportedException {
        List<Position> validPieces = new ArrayList<>();
        Stack<IntermediateState> nextStates = new Stack<>();

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {

                if (player == Player.BLACK && (stateMatrix[i][j] == PieceType.NORMAL_BLACK || stateMatrix[i][j] == PieceType.KING_BLACK))
                    validPieces.add(new Position(i, j));

                else if (player == Player.WHITE && (stateMatrix[i][j] == PieceType.NORMAL_WHITE || stateMatrix[i][j] == PieceType.KING_WHITE))
                    validPieces.add(new Position(i, j));
            }
        }

        for (Position piece : validPieces) {
            nextStates.addAll(getPossibleNextStatesFor(piece));
        }

        boolean capturePossible = false;

        for (int i = 0; i < nextStates.size(); i++) {
            if (nextStates.get(i).captured) {
                capturePossible = true;
                break;
            }
        }

        List<IntermediateState> toBeReturned = new ArrayList<>();

        if(IntermediateState.capturingIsCompulsory && capturePossible) {
                while (!nextStates.isEmpty()) {
                    IntermediateState curr = nextStates.pop();
                    if (curr.captured) toBeReturned.add(curr);
                }
        }

        else toBeReturned.addAll(nextStates);
        return toBeReturned;
    }


    List<IntermediateState> getPossibleNextStatesFor(Position piece) throws CloneNotSupportedException {
        IntermediateState startState = new IntermediateState(this, piece);

        List<IntermediateState> terminalStates = new ArrayList<>();

        Queue<IntermediateState> tempQueue = new LinkedList<>(startState.getNextIntermediateStates());

        while (!tempQueue.isEmpty()) {
            IntermediateState currState = tempQueue.poll();

            terminalStates.add(currState);
            tempQueue.addAll(currState.getNextIntermediateStates());
        }

        return terminalStates;
    }


    //assumes position is in range
    void makeKing(int row, int col) {
        if (stateMatrix[row][col] == PieceType.NULL)
            return;

        if (stateMatrix[row][col] == PieceType.NORMAL_BLACK)
            stateMatrix[row][col] = PieceType.KING_BLACK;

        else if (stateMatrix[row][col] == PieceType.NORMAL_WHITE)
            stateMatrix[row][col] = PieceType.KING_WHITE;
    }

    boolean isEmpty(int row, int col) {
        return stateMatrix[row][col] == PieceType.NULL;
    }


}
