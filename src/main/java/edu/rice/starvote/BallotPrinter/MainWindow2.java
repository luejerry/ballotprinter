package edu.rice.starvote.BallotPrinter;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.PrintStream;

/**
 * Created by cyricc on 11/10/2016.
 */
public class MainWindow2 extends Application {

    private PrinterModel printerModel;
    private PrinterPane2 printerPane;
    private PrintServer printServer;

    @Override
    public void init() throws Exception {
        super.init();
        printerModel = new PrinterModel();
        printServer = new PrintServer(printerModel);
        printServer.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        printerPane = new PrinterPane2(printerModel);
        printerModel.setUserLogger(printerPane.getLoggerStream());
//        System.setErr(new PrintStream(printerPane.getLoggerStream(), true));
//        System.setOut(new PrintStream(printerPane.getLoggerStream(), true));
        System.out.println("test");
        final Pane window = printerPane.getPane();
        final Scene scene = new Scene(window);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Print test");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        printServer.stop();
        printerPane.shutdown();
    }
}
