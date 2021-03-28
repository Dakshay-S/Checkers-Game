package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        VBox vBox = new VBox();
        vBox.setAlignment(Pos.TOP_CENTER);
        Board board = new Board();

        Button resetBoard = new Button("Reset Board");
        resetBoard.setOnAction(actionEvent -> board.resetBoard());

        Button switchTurn = new Button("Switch Turn");
        switchTurn.setOnAction(actionEvent -> board.switchPlayer());

        Button showHint = new Button("Show Hint");
        showHint.setOnAction(actionEvent -> board.showHint());


        HBox controls = new HBox();
        controls.setAlignment(Pos.BASELINE_CENTER);
        controls.setSpacing(40);
        controls.setPadding(new Insets(10));

        controls.getChildren().add(resetBoard);
        controls.getChildren().add(switchTurn);
        controls.getChildren().add(showHint);



        vBox.getChildren().add(board);
        vBox.getChildren().add(controls);

        primaryStage.setTitle("IIT2018011: White player is Computer. Black is human player");
        primaryStage.setOnCloseRequest(windowEvent -> System.exit(0));

        primaryStage.setScene(new Scene(vBox, board.width , board.height + 50));
        primaryStage.setResizable(false);

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
