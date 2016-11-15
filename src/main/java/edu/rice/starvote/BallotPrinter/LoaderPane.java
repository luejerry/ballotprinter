package edu.rice.starvote.BallotPrinter;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Created by cyricc on 11/11/2016.
 */
public class LoaderPane {

    private final GridPane window = new GridPane();
    private final TextArea titleText;
    private final TextArea subtitleText;
    private final TextArea instructionsText;
    private final TextField barcodeField;
    private final TextField jsonField;

    public LoaderPane() {
        titleText = new TextArea("Official Ballot");
        subtitleText = new TextArea("November 8, 2016, General Election\nHarris County, Texas Precinct 361");
        instructionsText = new TextArea("PLACE THIS IN BALLOT BOX");
        barcodeField = new TextField();
        jsonField = new TextField();
        initWindow();
    }

    private void initWindow() {
        final Label titleLabel = new Label("Title");
        final Label subtitleLabel = new Label("Subtitle");
        final Label instructionsLabel = new Label("Instructions");
        final Label barcodeLabel = new Label("Barcode image");
        final Label jsonLabel = new Label("Ballot selection JSON");
        final Button barcodeButton = new Button("Open file...");
        final Button jsonButton = new Button("Open file...");

        GridPane.setConstraints(titleLabel, 1, 1);
        GridPane.setConstraints(subtitleLabel, 1, 2);
        GridPane.setConstraints(instructionsLabel, 1, 3);
        GridPane.setConstraints(barcodeLabel, 1, 4);
        GridPane.setConstraints(jsonLabel, 1, 5);
        GridPane.setConstraints(titleText, 2, 1, GridPane.REMAINING, 1);
        GridPane.setConstraints(subtitleText, 2, 2, GridPane.REMAINING, 1);
        GridPane.setConstraints(instructionsText, 2, 3, GridPane.REMAINING, 1);
        GridPane.setConstraints(barcodeField, 2, 4);
        GridPane.setConstraints(jsonField, 2, 5);
        GridPane.setConstraints(barcodeButton, 3, 4);
        GridPane.setConstraints(jsonButton, 3, 5);


        window.getChildren().addAll(titleLabel, titleText,
                subtitleLabel, subtitleText,
                instructionsLabel, instructionsText,
                barcodeLabel, barcodeField, barcodeButton,
                jsonLabel, jsonField, jsonButton);

        barcodeButton.addEventHandler(ActionEvent.ACTION, event -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open barcode image file");
            fileChooser.setInitialDirectory(new File("."));
            final File file = fileChooser.showOpenDialog(window.getScene().getWindow());
            if (file != null) {
                barcodeField.setText(file.getAbsolutePath());
            }

        });

        jsonButton.addEventHandler(ActionEvent.ACTION, event -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open ballot JSON file");
            fileChooser.setInitialDirectory(new File("."));
            final File file = fileChooser.showOpenDialog(window.getScene().getWindow());
            if (file != null) {
                jsonField.setText(file.getAbsolutePath());
            }

        });
    }

    public Pane getPane() {
        return window;
    }

    public Selections getSelections() {
        return new Selections(titleText.getText(),
                subtitleText.getText(),
                instructionsText.getText(),
                barcodeField.getText(),
                jsonField.getText());
    }

    public static class Selections {
        public String title;
        public String subtitle;
        public String instructions;
        public String barcodePath;
        public String jsonPath;

        public Selections(String title, String subtitle, String instructions, String barcodePath, String jsonPath) {
            this.title = title;
            this.subtitle = subtitle;
            this.instructions = instructions;
            this.barcodePath = barcodePath;
            this.jsonPath = jsonPath;
        }
    }
}
