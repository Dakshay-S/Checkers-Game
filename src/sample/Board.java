package sample;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/*Assumptions:
 * 1. board is implemented as array of squares (Indexed 0)
 */

public class Board extends Pane {

    final static Color DARK_COLOR = Color.rgb(100, 100, 100);
    final static Color LIGHT_COLOR = Color.rgb(200, 200, 200);
    final static Color HIGHLIGHT_COLOR = Color.rgb(95, 227, 104);
    final static Color HINT_COLOR = Color.rgb(200,100,0);
    final static float squareDimension = 80f;
    Square[][] squares;
    final int N = 8;
    Pawn[][] pawnsMatrix = new Pawn[N][N];
    final int noOfPawns = 12;
    float width;
    float height;
    State state;
    final HashMap<Position, IntermediateState> DestMap = new HashMap<>();
    Player player = Player.BLACK;
    Pawn selectedPawn = null;
    Player computer = Player.WHITE;
    final Runnable computerRunnable;

    public Board() {
        this.width = N * squareDimension;
        this.height = N * squareDimension;
        this.squares = new Square[N][N];


        initSquares(N);

        resetBoard();

        computerRunnable = () -> {
            try {

                MAX_Player max_player = new MAX_Player(null);
                IntermediateState nexBest = max_player.solveForMax(new IntermediateState(state, null), 8, computer, computer);

                Platform.runLater(() -> {
                    Board.this.makeMoves(nexBest.path);
                    switchTurn();
                });

            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        };

    }

    void resetBoard()
    {
        resetSquaresColors();

        for (int i = 0; i < N ; i++)
            for (int j = 0; j <N ; j++)
                removePawnAt(i,j);

        state = initialiseState();

        initPawnsMatrix();
        setUpPawns(noOfPawns, PieceType.NORMAL_WHITE);
        setUpPawns(noOfPawns, PieceType.NORMAL_BLACK);


    }

    void initSquares(int N) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                squares[i][j] = new Square(i, j, (i + j) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR);
                getChildren().add(squares[i][j]);
            }
        }
    }

    void initPawnsMatrix() {
        for (Pawn[] row : pawnsMatrix)
            Arrays.fill(row, null);
    }

    State initialiseState() {
        PieceType[][] stateMatrix = new PieceType[8][8];

        for (PieceType[] row : stateMatrix)
            Arrays.fill(row, PieceType.NULL);

        return new State(stateMatrix);
    }

    void setUpPawns(int count, PieceType pieceType) {
        int startRow = (pieceType == PieceType.NORMAL_WHITE) || (pieceType == PieceType.KING_WHITE) ? 0 : 5;


        for (int i = startRow; i < N; i++) {
            if (count <= 0)
                break;

            for (int j = 0; j < N; j++) {
                if (count <= 0)
                    break;

                if (squares[i][j].color == DARK_COLOR) {
                    Pawn pawn = new Pawn(pieceType, i, j);
                    getChildren().add(pawn);
                    pawnsMatrix[i][j] = pawn;
                    state.placePieceAt(pieceType, new Position(i, j));

                    count--;
                }
            }
        }
    }


    class Square extends Rectangle {
        int row;
        int col;
        Color color;


        public Square(int row, int col, Color color) {
            super(squareDimension, squareDimension);
            this.row = row;
            this.col = col;
            this.color = color;

            this.setFill(color);
            this.setOnMouseClicked(mouseEvent -> {
                clickAction();
            });

            this.relocate(col * squareDimension, row * squareDimension);
        }


        public void setColor(Color color) {
            this.color = color;
            this.setFill(color);
        }

        void clickAction() {
            Position pos = new Position(row, col);
            if (!DestMap.containsKey(pos)) {
                resetSquaresColors();
                selectedPawn = null;
                DestMap.clear();
                return;
            }

            Board.this.resetSquaresColors();
            Board.this.makeMoves(DestMap.get(pos).path);

            switchTurn();

        }
    }


    class Pawn extends StackPane {
        PieceType pieceType;
        int posRow;
        int posCol;
        Pawn self;
        Circle circle;
        Text text;
        boolean crowned = false;

        public Pawn(PieceType pieceType, int posRow, int posCol) {
            super();
            this.pieceType = pieceType;
            this.self = this;

            circle = new Circle();
            circle.setRadius(squareDimension / 2);
            circle.setFill((pieceType == PieceType.NORMAL_WHITE) || (pieceType == PieceType.KING_WHITE) ? Color.WHITE : Color.BLACK);
            circle.setEffect(new DropShadow());

            text = new Text("");
            text.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
            text.setFill(Color.GOLD);

            if (pieceType == PieceType.KING_WHITE || pieceType == PieceType.KING_BLACK)
                makeKing();

            this.getChildren().addAll(circle, text);

            setMouseListener();

            moveTo(posRow, posCol);
        }


        void moveTo(int row, int col) {

            this.posRow = row;
            this.posCol = col;


            this.relocate(posCol * squareDimension, posRow * squareDimension);
        }

        void makeKing() {
            crowned = true;
            text.setText("King");
        }

        boolean isKing() {
            return crowned;
        }

        void setMouseListener() {
            this.setOnMouseClicked(mouseEvent -> {
                try {

                    resetSquaresColors();
                    selectedPawn = null;

                    if (Board.this.player == Player.BLACK && (self.pieceType != PieceType.NORMAL_BLACK && self.pieceType != PieceType.KING_BLACK))
                        return;

                    if (Board.this.player == Player.WHITE && (self.pieceType != PieceType.NORMAL_WHITE && self.pieceType != PieceType.KING_WHITE))
                        return;

                    selectedPawn = self;

                    highlightDestinationsOf(this.posRow, this.posCol);
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    void highlightDestinationsOf(int row, int col) throws CloneNotSupportedException {


        Board.this.DestMap.clear();

        List<IntermediateState> nextStates = state.getPossibleNextStatesFor(new Position(row, col));

        for (IntermediateState state : nextStates) {

            if (DestMap.containsKey(state.activePiecePos)) {
                IntermediateState oldState = DestMap.get(state.activePiecePos);
                IntermediateState betterState = state.path.size() > oldState.path.size() ? state : oldState;

                DestMap.put(state.activePiecePos, betterState);
            } else
                DestMap.put(state.activePiecePos, state);
        }


        highlightSquares(new ArrayList<>(DestMap.keySet()));
    }


    void highlightSquares(List<Position> positions) {
        for (Position position : positions) {
            if (positionIsInRange(position))
                squares[position.row][position.col].setColor(HIGHLIGHT_COLOR);
        }

    }


    boolean positionIsInRange(Position position) {
        return position.row >= 0 && position.col >= 0 && position.row < State.N && position.col < State.N;
    }


    void resetSquaresColors() {
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                squares[i][j].setColor((i + j) % 2 == 0 ? LIGHT_COLOR : DARK_COLOR);
    }


    Pawn removePawnAt(int row, int col) {

        Pawn toBeRemoved = pawnsMatrix[row][col];

        if (toBeRemoved != null)
            this.getChildren().remove(toBeRemoved);

        pawnsMatrix[row][col] = null;

        return toBeRemoved;
    }


    void makeMoves(List<Move> moves) {
        for (Move move : moves) {
            makeMove(move);
        }
    }

    void makeMove(Move move) {
        if (pawnsMatrix[move.from.row][move.from.col] == null || pawnsMatrix[move.to.row][move.to.col] != null)
            return;

        if (move.doesCapture())
            this.removePawnAt(move.capturedPos.row, move.capturedPos.col);

        Pawn toBeMoved = pawnsMatrix[move.from.row][move.from.col];
        toBeMoved.moveTo(move.to.row, move.to.col);

        pawnsMatrix[move.to.row][move.to.col] = toBeMoved;
        pawnsMatrix[move.from.row][move.from.col] = null;

        if (move.coronate)
            makeKing(move.to.row, move.to.col);

        state.makeMove(move);
    }


    void switchTurn() {
        this.player = this.player == Player.WHITE ? Player.BLACK : Player.WHITE;

        try {
            if (state.getAllPossibleNextStatesForTurn(this.player).isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText((this.player == Player.WHITE ? Player.BLACK : Player.WHITE) + " Won!!");
                alert.showAndWait();
            }

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }


        if (computer != null && computer == this.player) {
            Thread backgroundThread = new Thread(computerRunnable);
            backgroundThread.start();
        }

    }

    void switchPlayer()
    {
        this.computer = this.computer == Player.WHITE ? Player.BLACK : Player.WHITE;

        if (computer != null && computer == this.player) {
            Thread backgroundThread = new Thread(computerRunnable);
            backgroundThread.start();
        }
    }

    void makeKing(int row, int col) {
        if (positionIsInRange(new Position(row, col)) && pawnsMatrix[row][col] != null && !pawnsMatrix[row][col].isKing()) {
            pawnsMatrix[row][col].makeKing();
        }
    }

    void showHint()
    {
        Runnable runnable = () -> {
            {
                try {

                    MAX_Player max_player = new MAX_Player(null);
                    IntermediateState nexBest = max_player.solveForMax(new IntermediateState(state, null), 10, Board.this.player, Board.this.player);
                    Position from = nexBest.path.get(0).from;
                    Position to = nexBest.path.get(nexBest.path.size()-1).to;

                    //mark the next best position
                    Platform.runLater(() -> {
                        squares[from.row][from.col].setColor(HINT_COLOR);
                        squares[to.row][to.col].setColor(HINT_COLOR);
                    });

                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

    }



}
