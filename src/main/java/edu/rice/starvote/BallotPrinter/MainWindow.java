package edu.rice.starvote.BallotPrinter;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Created by cyricc on 11/10/2016.
 */
public class MainWindow extends Application {

    private PrinterPane printerPane;

    @Override
    public void start(Stage primaryStage) throws Exception {
        printerPane = new PrinterPane();
        final Pane window = printerPane.getPane();
        final Scene scene = new Scene(window);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Print test");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        printerPane.shutdown();
    }
}
