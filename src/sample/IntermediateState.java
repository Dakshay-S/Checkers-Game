package sample;

import java.util.*;

public class IntermediateState implements Cloneable {
    State state;
    Position activePiecePos;
    List<Move> path = new ArrayList<>();
    boolean captured = false;
    boolean justCrowned = false;
    boolean pieceMovedAtLeastOnce = false;
    static boolean pawnsCanCaptureBackwards = true;
    static boolean kingCanMoveMultipleSquares = true;
    static boolean capturingIsCompulsory = true;

    public IntermediateState(State state, Position activePiecePos) {
        this.state = state;
        this.activePiecePos = activePiecePos;
    }


    List<IntermediateState> getNextIntermediateStates() throws CloneNotSupportedException {

        List<IntermediateState> nextIntermediateStates = new ArrayList<>();
        List<Move> movesFromCurrState = getValidMovesForCurrIntermediateState();

        //if a capturing move is there, then pick only that
        if(!pieceMovedAtLeastOnce && capturingIsCompulsory)
        {
            List<Move> clonedList = (List<Move>) ((ArrayList<Move>)movesFromCurrState).clone();
            int n = clonedList.size();

            for (int i = 0; i < n ; i++) {
                Move currMove = clonedList.remove(0);

                if(currMove.doesCapture())
                    clonedList.add(currMove);
            }

            if(!clonedList.isEmpty()) {
                movesFromCurrState.clear();
                movesFromCurrState = clonedList;
            }
        }


        for (Move move : movesFromCurrState) {
            IntermediateState clonedAndUpdated = (IntermediateState) this.clone();

            clonedAndUpdated.pieceMovedAtLeastOnce = true;
            clonedAndUpdated.captured = move.doesCapture();
            clonedAndUpdated.state = this.state.cloneAndMakeMove(move);
            clonedAndUpdated.path = (List<Move>) ((ArrayList<Move>) this.path).clone();
            clonedAndUpdated.path.add(move);
            clonedAndUpdated.activePiecePos = move.to;

            clonedAndUpdated.justCrowned = move.coronate;

            nextIntermediateStates.add(clonedAndUpdated);
        }
        return nextIntermediateStates;
    }


    List<Move> getValidMovesForCurrIntermediateState() {
        if (justCrowned)
            return new ArrayList<>();
        if (pieceMovedAtLeastOnce && !captured)
            return new ArrayList<>();

        if (!pieceMovedAtLeastOnce)
            return getValidMovesForActivePiece();

        //if captured:
        List<Move> moves = getValidMovesForActivePiece();
        int numOfMoves = moves.size();

        for (int i = 0; i < numOfMoves; i++) {
            Move curr = moves.remove(0);

            if (curr.doesCapture())
                moves.add(curr);
        }

        return moves;
    }


    List<Move> getValidMovesForActivePiece() {
        if (state.getPieceType(activePiecePos) == PieceType.NORMAL_WHITE)
            return getValidMovesForNormalWhite(activePiecePos);

        else if (state.getPieceType(activePiecePos) == PieceType.NORMAL_BLACK)
            return getValidMovesForNormalBlack(activePiecePos);

        else if (state.getPieceType(activePiecePos) == PieceType.KING_WHITE ||
                state.getPieceType(activePiecePos) == PieceType.KING_BLACK)
            return getValidMovesForKing(activePiecePos);

        else
            System.err.println("An empty square is made active piece " + " Row = " + activePiecePos.row + " Col = " + activePiecePos.col);

        return new ArrayList<>(); // emptyList
    }

    List<Move> getValidMovesForNormalWhite(Position activePiece) {
        List<Move> moves = new ArrayList<>();

        moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.LOWER_LEFT, false));
        moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.LOWER_RIGHT, false));

        if (pawnsCanCaptureBackwards) {
            moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.UPPER_LEFT, true));
            moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.UPPER_RIGHT, true));
        }

        return moves;
    }

    List<Move> getValidMovesForNormalBlack(Position activePiece) {
        List<Move> moves = new ArrayList<>();

        moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.UPPER_LEFT, false));
        moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.UPPER_RIGHT, false));

        if (pawnsCanCaptureBackwards) {
            moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.LOWER_LEFT, true));
            moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.LOWER_RIGHT, true));
        }

        return moves;
    }

    List<Move> getValidMovesForKing(Position activePiece) {
        List<Move> moves = new ArrayList<>();

        if (kingCanMoveMultipleSquares) {

            for (Direction dir : Direction.values()) {

                Position captured = null;

                for (int i = 1; true; i++) {
                    int currRow = activePiece.row + i * dir.delRow;
                    int currCol = activePiece.col + i * dir.delCol;

                    if (!positionIsInRange(currRow, currCol))
                        break;

                    if (isEmpty(currRow, currCol)) {
                        moves.add(new Move(activePiece, new Position(currRow, currCol), captured));
                        continue;
                    }

                    else if(!hasOpponentPieceAt(activePiece, currRow, currCol))
                        break;

                    else if(captured != null && !isEmpty(currRow, currCol) )
                        break;

                    //opponent piece there
                    else if (positionIsInRange(currRow + dir.delRow, currCol + dir.delCol) &&
                            isEmpty(currRow + dir.delRow, currCol + dir.delCol)) {

                        captured = new Position(currRow, currCol);
                        moves.add(new Move(activePiece, new Position(currRow + dir.delRow, currCol + dir.delCol), captured));
                    }

                }
            }
        }
        else {
            moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.LOWER_LEFT, false));
            moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.LOWER_RIGHT, false));
            moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.UPPER_LEFT, false));
            moves.addAll(getValidMoveInGivenDirection(activePiece, Direction.UPPER_RIGHT, false));
        }

        return moves;
    }

    List<Move> getValidMoveInGivenDirection(Position activePiece, Direction direction, boolean compulsoryCapture) {
        List<Move> moves = new ArrayList<>();

        int newRow = activePiece.row + direction.delRow;
        int newCol = activePiece.col + direction.delCol;
        Move move = null;

        if (positionIsInRange(newRow, newCol)) {
            // simple move
            if (!compulsoryCapture && isEmpty(newRow, newCol))
                move = new Move(activePiece, new Position(newRow, newCol), null);

                // capture move
            else if (canCapture(activePiece, new Position(newRow, newCol)))
                move = new Move(activePiece, new Position(newRow + direction.delRow, newCol + direction.delCol), new Position(newRow, newCol));
        }

        if (move != null) {
            if (state.getPieceType(activePiecePos) == PieceType.NORMAL_WHITE && move.to.row == State.N - 1)
                move.coronate = true;
            else if (state.getPieceType(activePiecePos) == PieceType.NORMAL_BLACK && move.to.row == 0)
                move.coronate = true;

            moves.add(move);
        }

        return moves;
    }


    boolean positionIsInRange(int row, int col) {
        return row >= 0 && col >= 0 && row < State.N && col < State.N;
    }

    boolean isEmpty(int row, int col) {
        return state.getPieceType(new Position(row, col)) == PieceType.NULL;
    }

    boolean hasOpponentPieceAt(Position activePiece, int row, int col) {
        Position targetPos = new Position(row, col);

        if ((state.getPieceType(activePiece) == PieceType.NORMAL_BLACK || state.getPieceType(activePiece) == PieceType.KING_BLACK) &&
                (state.getPieceType(targetPos) == PieceType.NORMAL_WHITE || state.getPieceType(targetPos) == PieceType.KING_WHITE))
            return true;

        else if ((state.getPieceType(targetPos) == PieceType.NORMAL_BLACK || state.getPieceType(targetPos) == PieceType.KING_BLACK) &&
                (state.getPieceType(activePiece) == PieceType.NORMAL_WHITE || state.getPieceType(activePiece) == PieceType.KING_WHITE))
            return true;

        else return false;
    }


    List<IntermediateState> getAllPossibleNextStatesForTurn(Player player) throws CloneNotSupportedException {
        List<Position> validPieces = new ArrayList<>();
        Stack<IntermediateState> nextStates = new Stack<>();

        for (int i = 0; i < State.N; i++) {
            for (int j = 0; j < State.N; j++) {

                if (player == Player.BLACK && (state.stateMatrix[i][j] == PieceType.NORMAL_BLACK || state.stateMatrix[i][j] == PieceType.KING_BLACK))
                    validPieces.add(new Position(i, j));

                else if (player == Player.WHITE && (state.stateMatrix[i][j] == PieceType.NORMAL_WHITE || state.stateMatrix[i][j] == PieceType.KING_WHITE))
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
        IntermediateState startState = new IntermediateState(state, piece);

        List<IntermediateState> terminalStates = new ArrayList<>();

        Queue<IntermediateState> tempQueue = new LinkedList<>(startState.getNextIntermediateStates());

        while (!tempQueue.isEmpty()) {
            IntermediateState currState = tempQueue.poll();

            terminalStates.add(currState);
            tempQueue.addAll(currState.getNextIntermediateStates());
        }

        return terminalStates;
    }


    // assumes both the activePiece and targetPiece are valid positions
    boolean canCapture(Position activePiece, Position targetPiece) {
        if (!hasOpponentPieceAt(activePiece, targetPiece.row, targetPiece.col))
            return false;

        if (state.getPieceType(activePiece) == PieceType.NORMAL_WHITE) {

            if (targetPiece.row == activePiece.row + 1) {
                if (targetPiece.col == activePiece.col + 1 && positionIsInRange(activePiece.row + 2, activePiece.col + 2)
                        && isEmpty(activePiece.row + 2, activePiece.col + 2)) {
                    return true;
                } else if (targetPiece.col == activePiece.col - 1 && positionIsInRange(activePiece.row + 2, activePiece.col - 2)
                        && isEmpty(activePiece.row + 2, activePiece.col - 2)) {
                    return true;
                }
            }

            // if norma pawns cannot capture backwards, delete these lines
            if (pawnsCanCaptureBackwards && targetPiece.row == activePiece.row - 1) {
                if (targetPiece.col == activePiece.col + 1 && positionIsInRange(activePiece.row - 2, activePiece.col + 2)
                        && isEmpty(activePiece.row - 2, activePiece.col + 2)) {
                    return true;
                } else if (targetPiece.col == activePiece.col - 1 && positionIsInRange(activePiece.row - 2, activePiece.col - 2)
                        && isEmpty(activePiece.row - 2, activePiece.col - 2)) {
                    return true;
                }
            }

        } else if (state.getPieceType(activePiece) == PieceType.NORMAL_BLACK) {

            if (targetPiece.row == activePiece.row - 1) {
                if (targetPiece.col == activePiece.col + 1 && positionIsInRange(activePiece.row - 2, activePiece.col + 2)
                        && isEmpty(activePiece.row - 2, activePiece.col + 2)) {
                    return true;
                } else if (targetPiece.col == activePiece.col - 1 && positionIsInRange(activePiece.row - 2, activePiece.col - 2)
                        && isEmpty(activePiece.row - 2, activePiece.col - 2)) {
                    return true;
                }
            }

            if (pawnsCanCaptureBackwards && targetPiece.row == activePiece.row + 1) {
                if (targetPiece.col == activePiece.col + 1 && positionIsInRange(activePiece.row + 2, activePiece.col + 2)
                        && isEmpty(activePiece.row + 2, activePiece.col + 2)) {
                    return true;
                } else if (targetPiece.col == activePiece.col - 1 && positionIsInRange(activePiece.row + 2, activePiece.col - 2)
                        && isEmpty(activePiece.row + 2, activePiece.col - 2)) {
                    return true;
                }
            }

        }

        // if control comes here, then the activePiece is King

        else if (targetPiece.row == activePiece.row + 1) {
            if (targetPiece.col == activePiece.col + 1 && positionIsInRange(activePiece.row + 2, activePiece.col + 2)
                    && isEmpty(activePiece.row + 2, activePiece.col + 2)) {
                return true;
            } else if (targetPiece.col == activePiece.col - 1 && positionIsInRange(activePiece.row + 2, activePiece.col - 2)
                    && isEmpty(activePiece.row + 2, activePiece.col - 2)) {
                return true;
            }

        } else if (targetPiece.row == activePiece.row - 1) {

            if (targetPiece.col == activePiece.col + 1 && positionIsInRange(activePiece.row - 2, activePiece.col + 2)
                    && isEmpty(activePiece.row - 2, activePiece.col + 2)) {
                return true;
            } else if (targetPiece.col == activePiece.col - 1 && positionIsInRange(activePiece.row - 2, activePiece.col - 2)
                    && isEmpty(activePiece.row - 2, activePiece.col - 2)) {
                return true;
            }

        }


        return false;
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


}
