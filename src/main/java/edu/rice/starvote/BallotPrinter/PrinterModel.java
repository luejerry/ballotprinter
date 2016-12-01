package edu.rice.starvote.BallotPrinter;

import javafx.embed.swing.SwingFXUtils;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PrinterModel {

    private final static Logger log = LoggerFactory.getLogger(PrinterModel.class);
    private double printableX;
    private double printableY;
    private List<BallotPage> ballotPages = new LinkedList<>();
    private Printer currentPrinter;
    private PrintStream userLogger = System.out;

    public PrinterModel() { }

    public List<BallotPage> getBallotPages() {
        return ballotPages;
    }

    public void setBallotPages(List<BallotPage> ballotPages) {
        this.ballotPages = ballotPages;
    }

    public void setUserLogger(PrintStream userLogger) {
        this.userLogger = userLogger;
    }

    String info(Printer printer) {
        final StringBuilder buffer = new StringBuilder(64);
        log.debug("Print query running on: {}", Thread.currentThread());
        final PageLayout layout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
        currentPrinter = printer;
        printableX = layout.getPrintableWidth();
        printableY = layout.getPrintableHeight();
        buffer.append("Left margin: ").append(layout.getLeftMargin())
                .append("\nRight margin: ").append(layout.getRightMargin())
                .append("\nTop margin: ").append(layout.getTopMargin())
                .append("\nBottom margin: ").append(layout.getBottomMargin())
                .append("\nPrintable width: ").append(layout.getPrintableWidth())
                .append("\nPrintable height: ").append(layout.getPrintableHeight());
        userLogger.format("Future jobs will print using: %s\n", currentPrinter.getName());
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

    public void printPages(Collection<? extends Node> nodes, Printer printer) {
        final PrinterJob job = PrinterJob.createPrinterJob(printer);
        final PageLayout layout = printer.createPageLayout(Paper.NA_LETTER, PageOrientation.PORTRAIT, Printer.MarginType.HARDWARE_MINIMUM);
//        System.out.println("Print job spooling " + nodes.size() + " pages");
        log.info("Print job spooling {} pages...", nodes.size());
        userLogger.format("Print job spooling %d pages\n", nodes.size());
        nodes.forEach(node -> {
            node.setLayoutX(0);
            node.setLayoutY(0);
            node.applyCss();
            node.autosize();
            final boolean printed = job.printPage(layout, node);
            if (!printed) {
                log.error("Printing failed with status: {}", job.getJobStatus());
                userLogger.format("Printing failed with status: %s\n", job.getJobStatus());
//                System.err.println("Printing failed with status: " + job.getJobStatus());
            } else {
                log.info("Page spooled");
//                System.out.println("Page spooled");
            }
        });

        if (job.endJob()) {
            log.info("Print job completed");
            userLogger.println("Print job sent to printer");
        }
    }

    List<BallotPage> generateBallot(LoaderPane.Selections selection) throws IOException {
        final Image barcodeImage = selection.barcodeIsPath ? new Image("file:" + selection.barcodePath) : SwingFXUtils.toFXImage(BarcodeTest.generateCode(selection.barcodePath), null);
        final String title = selection.title;
        final String subtitle = selection.subtitle;
        final String instructions = selection.instructions;
        final Path jsonPath = Paths.get(selection.jsonPath);
        final BufferedReader jsonReader = Files.newBufferedReader(jsonPath);
        final Collection<RaceContainer> raceContainers = BallotParser.generateRaceContainers(BallotParser.parseJson(jsonReader));

        return generatePages(barcodeImage, title, subtitle, instructions, raceContainers);
    }

    private List<BallotPage> generatePages(Image barcodeImage, String title, String subtitle, String instructions, Collection<RaceContainer> raceContainers) {
        final HBox header = Ballots.createHeader(title,
                subtitle,
                instructions,
                barcodeImage);
        final HBox footer = Ballots.createFooter(barcodeImage);

        Pair<BallotPage, Queue<RaceContainer>> ballotRemain = Ballots.createBallot(printableX, printableY, 3, header, footer, raceContainers);
        final List<BallotPage> ballotList = new LinkedList<BallotPage>();

        ballotList.add(ballotRemain.getKey());
//        System.out.println("Page generated");
        log.debug("Page generated");

        while (!ballotRemain.getValue().isEmpty()) {
            final HBox nextHeader = Ballots.createHeader(title,
                    subtitle,
                    instructions,
                    barcodeImage);
            final HBox nextFooter = Ballots.createFooter(barcodeImage);
            ballotRemain = Ballots.createBallot(printableX, printableY, 3, nextHeader, nextFooter, ballotRemain.getValue());
            ballotList.add(ballotRemain.getKey());
//            System.out.println("Page generated");
            log.debug("Page generated");
        }

//        System.out.println("Total pages generated: " + ballotList.size());
        log.info("Total pages generated: {}", ballotList.size());
        userLogger.format("Pages generated: %d\n", ballotList.size());
        return ballotList;
    }

    List<BallotPage> generateBallot(WebData data) {
        final WritableImage barcode = SwingFXUtils.toFXImage(BarcodeTest.generateCode(data.barcode), null);
        final String title = data.title;
        final String subtitle = data.subtitle;
        final String instructions = data.instructions;
        final HBox header = Ballots.createHeader(title, subtitle, instructions, barcode);
        final HBox footer = Ballots.createFooter(barcode);
        final Collection<RaceContainer> raceContainers = BallotParser.generateRaceContainers(BallotParser.parseJson(data.json));

        log.debug("Ballot generation request from webdata");
//        System.out.println("Ballot generation request from webdata");

        return generatePages(barcode, title, subtitle, instructions, raceContainers);
    }

    public Printer getCurrentPrinter() {
        return currentPrinter;
    }

    public void setToDefaultPrinter() {
        currentPrinter = Printer.getDefaultPrinter();
    }

    public void printFromWeb(WebData data) {
        final List<BallotPage> pages = generateBallot(data);
        printPages(pages, getCurrentPrinter());
    }

}