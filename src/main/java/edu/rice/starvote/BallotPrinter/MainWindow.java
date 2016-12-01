package edu.rice.starvote.BallotPrinter;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Created by cyricc on 11/10/2016.
 */
public class MainWindow extends Application {

    private PrinterModel printerModel;
    private PrinterPane printerPane;
    private PrintServer printServer;

    @Override
    public void init() throws Exception {
        super.init();
        printerModel = new PrinterModel();
        printServer = new PrintServer(printerModel);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        printerPane = new PrinterPane(printerModel);
        printerModel.setUserLogger(printerPane.getLoggerStream());
        printServer.setUserLogger(printerPane.getLoggerStream());
        printServer.start();
        final Pane window = printerPane.getPane();
        final Scene scene = new Scene(window);
        primaryStage.setScene(scene);
        primaryStage.setTitle("STAR-Vote Print Server");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        printServer.stop();
        printerPane.shutdown();
    }
}
