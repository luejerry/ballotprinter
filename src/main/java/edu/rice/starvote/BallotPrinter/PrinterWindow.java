package edu.rice.starvote.BallotPrinter;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by cyricc on 11/9/2016.
 */
public class PrinterWindow {

    private ObservableList<Printer> printerlist;
    private StringProperty printerInfo = new SimpleStringProperty();
    private final ExecutorService worker = Executors.newFixedThreadPool(2);
    private double printableX;
    private double printableY;
    private final GridPane window = new GridPane();

    public PrinterWindow() {
        final ObservableSet<Printer> allPrinters = Printer.getAllPrinters();
        final List<Printer> printers = allPrinters.stream().collect(Collectors.toList());
        printerlist = FXCollections.observableList(printers);
        initWindow();
    }

    private void initWindow() {
        final ListView<Printer> printerListView = new ListView<>(printerlist);
        final Button printButton = new Button("Print");
        final Button dimButton = new Button("Dimensions");
        final Button ballotButton = new Button("Show Ballot");
        final TextArea textArea = new TextArea();

        printButton.setDisable(true);
        dimButton.setDisable(true);
        ballotButton.setDisable(true);

        printButton.addEventHandler(ActionEvent.ACTION, event -> {
            final BorderPane ballot = generateBallot();
            print(ballot, printerListView.getSelectionModel().getSelectedItem(), null);
        });
        ballotButton.addEventHandler(ActionEvent.ACTION, event -> {
            showBallot();
        });
        printerListView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final Task<String> infoTask = new Task<String>() {

                        @Override
                        protected String call() throws Exception {
                            return info(newValue);
                        }
                    };
                    printerInfo.bind(infoTask.valueProperty());
                    textArea.setText("Getting printer information...");
                    worker.submit(infoTask);
                    printButton.setDisable(false);
                    dimButton.setDisable(false);
                    ballotButton.setDisable(false);
                }
        );
        printerInfo.addListener((observable, oldValue, newValue) -> {
            textArea.setText(newValue);
        });

        GridPane.setConstraints(printerListView, 1, 1, 3, 1);
        GridPane.setConstraints(printButton, 1, 2);
        GridPane.setConstraints(dimButton, 2, 2);
        GridPane.setConstraints(ballotButton, 3, 2);
        GridPane.setConstraints(textArea, 1, 3, 3, 1);
        window.getChildren().addAll(printerListView, printButton, dimButton, ballotButton, textArea);
    }

    public Pane getWindow() {
        return window;
    }

    public void shutdown() throws InterruptedException {
        worker.shutdown();
        final boolean terminated = worker.awaitTermination(2, TimeUnit.SECONDS);
        if (!terminated) {
            System.out.println("Forcing task termination...");
            final List<Runnable> tasks = worker.shutdownNow();
            tasks.forEach((task) -> System.out.println(task.toString()));
        }
    }

    private String info(Printer printer) {
        final StringBuilder buffer = new StringBuilder(64);
        final PageLayout layout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
        System.out.println("Print query running on: " + Thread.currentThread());
        printableX = layout.getPrintableWidth();
        printableY = layout.getPrintableHeight();
        buffer.append("Left margin: ").append(layout.getLeftMargin())
                .append("\nRight margin: ").append(layout.getRightMargin())
                .append("\nTop margin: ").append(layout.getTopMargin())
                .append("\nBottom margin: ").append(layout.getBottomMargin())
                .append("\nPrintable width: ").append(layout.getPrintableWidth())
                .append("\nPrintable height: ").append(layout.getPrintableHeight());
        return buffer.toString();
    }

    public void print(Node node, Printer printer, Stage owner) {
        final PrinterJob job = PrinterJob.createPrinterJob(printer);
        final PageLayout layout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
        final boolean accepted = job.showPrintDialog(owner);
        if (accepted) {
            final boolean printed = job.printPage(layout, node);
            if (printed) {
                job.endJob();
            }
        }
    }

    public void showBallot() {
        final BorderPane testBallot = generateBallot();
        final Scene scene = new Scene(testBallot);
        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    private BorderPane generateBallot() {
        final HBox header = Ballots.createHeader("Official Ballot", "November 8, 2016, General Election\nHarris County, Texas Precinct 361", "Place this in ballot box", new Image("file:testcode.bmp"));
        final HBox footer = Ballots.createFooter(new Image("file:testcode.bmp"));
        final BorderPane testBallot = Ballots.createTestBallot(printableX, printableY, header, footer);
        testBallot.setStyle("-fx-background-color: white");
        return testBallot;
    }
}
