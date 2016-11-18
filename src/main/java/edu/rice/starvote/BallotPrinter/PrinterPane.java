package edu.rice.starvote.BallotPrinter;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created by cyricc on 11/9/2016.
 */
public class PrinterPane {

    private ObservableList<Printer> printerlist;
    private StringProperty printerInfo = new SimpleStringProperty();
    private final ExecutorService worker = Executors.newFixedThreadPool(2);
    private double printableX;
    private double printableY;
    private final GridPane window = new GridPane();
    private List<BallotPage> ballotPages = new LinkedList<>();

    public PrinterPane() {
        final ObservableSet<Printer> allPrinters = Printer.getAllPrinters();
        final List<Printer> printers = allPrinters.stream().collect(Collectors.toList());
        printerlist = FXCollections.observableList(printers);
        initWindow();
    }

    private void initWindow() {
        final ListView<Printer> printerListView = new ListView<>(printerlist);
        final Button printButton = new Button("Print");
        final Button dimButton = new Button("Initialize Ballot");
        final Button ballotButton = new Button("Show Ballot");
        final TextArea textArea = new TextArea();
        final LoaderDialog loaderDialog = new LoaderDialog(new LoaderPane());

        printButton.setDisable(true);
        dimButton.setDisable(true);
        ballotButton.setDisable(true);

        printButton.addEventHandler(ActionEvent.ACTION, event -> {
            final BallotPage ballot = ballotPages.get(0);
            print(ballot, printerListView.getSelectionModel().getSelectedItem(), null);
        });
        dimButton.addEventHandler(ActionEvent.ACTION, event -> {
            final Optional<LoaderPane.Selections> oSelection = loaderDialog.getDialog().showAndWait();
            oSelection.ifPresent(selection -> {
                System.out.println(selection.title);
                System.out.println(selection.subtitle);
                System.out.println(selection.instructions);
                System.out.println(selection.barcodePath);
                System.out.println(selection.jsonPath);
                ballotPages.clear();
                try {
                    ballotPages = generateBallot(selection);
                    ballotButton.setDisable(false);
                    printButton.setDisable(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    final Dialog errorDialog = new Dialog();
                    errorDialog.setContentText("Error loading chosen file!");
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
        printerListView.setCellFactory(param -> new ListCell<Printer>() {
                    @Override
                    protected void updateItem(Printer item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item == null ? "" : item.getName());
                    }
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
                    dimButton.setDisable(false);
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
        node.setLayoutY(0);
        node.setLayoutX(0);
        node.applyCss();
        node.autosize();
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

    public void printPages(Collection<Node> nodes, Printer printer, Stage owner) {
        final PrinterJob job = PrinterJob.createPrinterJob(printer);
        final PageLayout layout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
        nodes.forEach(node -> {
            node.setLayoutX(0);
            node.setLayoutY(0);
            node.applyCss();
            node.autosize();
            final boolean printed = job.printPage(layout, node);
            if (!printed) {
                System.err.println("Printing failed with status: " + job.getJobStatus());
            } else {
                System.out.println("Page spooled");
            }
        });
        job.endJob();
    }

    public void showBallot() {
//        final BorderPane testBallot = generateBallot();
        final BallotWindow ballotWindow = new BallotWindow(ballotPages);
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

    private BorderPane generateBallot() {
        final HBox header = Ballots.createHeader("Official Ballot",
                "November 8, 2016, General Election\nHarris County, Texas Precinct 361",
                "Place this in ballot box",
                new Image("file:testcode.bmp"));
        final HBox footer = Ballots.createFooter(new Image("file:testcode.bmp"));
        final BorderPane testBallot = Ballots.createTestBallot(printableX, printableY, header, footer);
        testBallot.setStyle("-fx-background-color: white");
        return testBallot;
    }

    private List<BallotPage> generateBallot(LoaderPane.Selections selection) throws IOException {
        final Image barcodeImage = selection.barcodeIsPath ? new Image("file:" + selection.barcodePath) : SwingFXUtils.toFXImage(BarcodeTest.generateCode(selection.barcodePath), null);
        final String title = selection.title;
        final String subtitle = selection.subtitle;
        final String instructions = selection.instructions;
        final Path jsonPath = Paths.get(selection.jsonPath);
        final BufferedReader jsonReader = Files.newBufferedReader(jsonPath);
        final Collection<RaceContainer> raceContainers = BallotParser.generateRaceContainers(BallotParser.parseJson(jsonReader));

        return generatePage(barcodeImage, title, subtitle, instructions, raceContainers);
    }

    private List<BallotPage> generatePage(Image barcodeImage, String title, String subtitle, String instructions, Collection<RaceContainer> raceContainers) {
        final HBox header = Ballots.createHeader(title,
                subtitle,
                instructions,
                barcodeImage);
        final HBox footer = Ballots.createFooter(barcodeImage);

        Pair<BallotPage, Queue<RaceContainer>> ballotRemain = Ballots.createBallot(printableX, printableY, 3, header, footer, raceContainers);
        final List<BallotPage> ballotList = new LinkedList<>();

        ballotList.add(ballotRemain.getKey());
        System.out.println("Page generated");

        while (!ballotRemain.getValue().isEmpty()) {
            final HBox nextHeader = Ballots.createHeader(title,
                    subtitle,
                    instructions,
                    barcodeImage);
            final HBox nextFooter = Ballots.createFooter(barcodeImage);
            ballotRemain = Ballots.createBallot(printableX, printableY, 3, nextHeader, nextFooter, ballotRemain.getValue());
            ballotList.add(ballotRemain.getKey());
            System.out.println("Page generated");
        }

        System.out.println("Total pages generated: " + ballotList.size());
        return ballotList;
    }

    private List<BallotPage> generateBallot(WebData data) {
        final WritableImage barcode = SwingFXUtils.toFXImage(BarcodeTest.generateCode(data.barcode), null);
        final String title = data.title;
        final String subtitle = data.subtitle;
        final String instructions = data.instructions;
        final HBox header = Ballots.createHeader(title, subtitle, instructions, barcode);
        final HBox footer = Ballots.createFooter(barcode);
        final Collection<RaceContainer> raceContainers = BallotParser.generateRaceContainers(BallotParser.parseJson(data.json));

        System.out.println("Ballot generation request from webdata");

        return generatePage(barcode, title, subtitle, instructions, raceContainers);
    }

}
