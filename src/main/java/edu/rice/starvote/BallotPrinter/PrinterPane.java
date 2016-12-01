package edu.rice.starvote.BallotPrinter;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.Printer;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by cyricc on 11/9/2016.
 */
public class PrinterPane {

    private final PrinterModel printerModel;
    private ObservableList<Printer> printerlist;
    private StringProperty printerInfo = new SimpleStringProperty();
    private final ExecutorService worker = Executors.newFixedThreadPool(2);
    private final GridPane window = new GridPane();
    private final PrintStream userLogger;
    private final TextArea logText = new TextArea();

    public PrinterPane(PrinterModel printerModel, PrintStream userLogger) {
        final ObservableSet<Printer> allPrinters = Printer.getAllPrinters();
        final List<Printer> printers = new ArrayList<>(allPrinters);
        printerlist = FXCollections.observableList(printers);
        initWindow();
        this.printerModel = printerModel;
        this.userLogger = userLogger;
    }

    public PrinterPane(PrinterModel printerModel) {
        final ObservableSet<Printer> allPrinters = Printer.getAllPrinters();
        final List<Printer> printers = new ArrayList<>(allPrinters);
        printerlist = FXCollections.observableList(printers);
        initWindow();
        this.printerModel = printerModel;
        this.userLogger = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                Platform.runLater(() -> {
                    logText.appendText(String.valueOf((char) b));
                });
            }
        });
    }

    private void initWindow() {
        final ChoiceBox<Printer> printerDropdown = new ChoiceBox<>(printerlist);
        final Button printButton = new Button("Print");
        final Button initButton = new Button("Initialize Ballot");
        final Button ballotButton = new Button("Show Ballot");
        final TitledPane testPane = new TitledPane("Test ballot", new HBox(10, initButton, ballotButton, printButton));
        final TextArea infoText = new TextArea();
        final TitledPane infoPane = new TitledPane("Printer information", infoText);
        final LoaderDialog loaderDialog = new LoaderDialog(new LoaderPane());
        final Label printLabel = new Label("Select printer:");
        final HBox printContainer = new HBox(printLabel, printerDropdown);
        final TitledPane logPane = new TitledPane("Log", logText);

        infoPane.setCollapsible(false);
        testPane.setCollapsible(false);
        window.setHgap(10);
        window.setVgap(10);
        window.setPadding(new Insets(0, 10, 10, 0));
        printContainer.setAlignment(Pos.BASELINE_LEFT);
        printContainer.setSpacing(10);
        logText.setWrapText(true);

        printButton.setDisable(true);
        initButton.setDisable(true);
        ballotButton.setDisable(true);

        printButton.addEventHandler(ActionEvent.ACTION, event -> {
            final BallotPage ballot = printerModel.getBallotPages().get(0);
            printerModel.print(ballot, printerDropdown.getSelectionModel().getSelectedItem(), null);
        });
        initButton.addEventHandler(ActionEvent.ACTION, event -> {
            final Optional<LoaderPane.Selections> oSelection = loaderDialog.getDialog().showAndWait();
            oSelection.ifPresent(selection -> {
                userLogger.println(selection.title);
                userLogger.println(selection.subtitle);
                userLogger.println(selection.instructions);
                userLogger.println(selection.barcodePath);
                userLogger.println(selection.jsonPath);
                printerModel.getBallotPages().clear();
                try {
                    printerModel.setBallotPages(printerModel.generateBallot(selection));
                    ballotButton.setDisable(false);
                    printButton.setDisable(false);
                } catch (IOException | JsonIOException e) {
                    e.printStackTrace();
                    final Dialog errorDialog = new Dialog();
                    errorDialog.setContentText("Error loading chosen file (" + e.toString() + ")");
                    errorDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                    errorDialog.show();
                    ballotButton.setDisable(true);
                    printButton.setDisable(true);
                } catch (JsonSyntaxException e) {
                    final Dialog errorDialog = new Dialog();
                    errorDialog.setContentText("Error: selected file is not valid JSON.");
                    errorDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
                    errorDialog.show();
                    ballotButton.setDisable(true);
                    printButton.setDisable(true);
                }
            });

        });
        ballotButton.addEventHandler(ActionEvent.ACTION, event -> {
            showBallot();
        });
        printerDropdown.setConverter(new StringConverter<Printer>() {
            @Override
            public String toString(Printer object) {
                return object == null ? "" : object.getName();
            }

            @Override
            public Printer fromString(String string) {
                return printerlist.filtered(printer -> printer.getName().equals(string)).get(0);
            }
        });
        printerDropdown.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    final Task<String> infoTask = new Task<String>() {

                        @Override
                        protected String call() throws Exception {
                            return printerModel.info(newValue);
                        }
                    };
                    printerInfo.bind(infoTask.valueProperty());
                    infoText.setText("Getting printer information...");
                    worker.submit(infoTask);
                    initButton.setDisable(false);
                }
        );
        printerInfo.addListener((observable, oldValue, newValue) -> {
            infoText.setText(newValue);
        });

        GridPane.setConstraints(printContainer, 1, 1, 1, 1);
        GridPane.setConstraints(testPane, 1, 2, 1, 1);
        GridPane.setConstraints(infoPane, 1, 3, 1, 1);
        GridPane.setConstraints(logPane, 1, 4, 1, 1);
        window.getChildren().addAll(printContainer, testPane, infoPane, logPane);
    }

    public Pane getPane() {
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

    public void showBallot() {
//        final BorderPane testBallot = generateBallot();
        final BallotWindow ballotWindow = new BallotWindow(printerModel.getBallotPages());
        final Scene scene = new Scene(ballotWindow.getPane());
        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Ballot Preview");
        stage.setOnCloseRequest(event -> {
            ballotWindow.getPane().getChildren().clear();
        });
        stage.showAndWait();
        scene.setRoot(new Region());
    }

    public PrintStream getLoggerStream() {
        return userLogger;
    }

}
