package edu.rice.starvote.BallotPrinter;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
    private final RadioButton textcodeRadio;
    private final RadioButton filecodeRadio;
    private final ToggleGroup barcodeToggleGroup;

    public LoaderPane() {
        titleText = new TextArea("Official Ballot");
        subtitleText = new TextArea("November 8, 2016, General Election\nHarris County, Texas Precinct 361");
        instructionsText = new TextArea("PLACE THIS IN BALLOT BOX");
        barcodeField = new TextField();
        jsonField = new TextField();
        textcodeRadio = new RadioButton("Text");
        filecodeRadio = new RadioButton("From file");
        barcodeToggleGroup = new ToggleGroup();

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
        final HBox barcodeContainer = new HBox();

        textcodeRadio.setToggleGroup(barcodeToggleGroup);
        filecodeRadio.setToggleGroup(barcodeToggleGroup);
        barcodeToggleGroup.selectToggle(textcodeRadio);

        barcodeContainer.getChildren().addAll(textcodeRadio, filecodeRadio, barcodeField);

        GridPane.setConstraints(titleLabel, 1, 1);
        GridPane.setConstraints(subtitleLabel, 1, 2);
        GridPane.setConstraints(instructionsLabel, 1, 3);
        GridPane.setConstraints(barcodeLabel, 1, 4);
        GridPane.setConstraints(jsonLabel, 1, 5);
        GridPane.setConstraints(titleText, 2, 1, GridPane.REMAINING, 1);
        GridPane.setConstraints(subtitleText, 2, 2, GridPane.REMAINING, 1);
        GridPane.setConstraints(instructionsText, 2, 3, GridPane.REMAINING, 1);
        GridPane.setConstraints(barcodeContainer, 2, 4);
        GridPane.setConstraints(jsonField, 2, 5);
        GridPane.setConstraints(barcodeButton, 3, 4);
        GridPane.setConstraints(jsonButton, 3, 5);


        window.getChildren().addAll(titleLabel, titleText,
                subtitleLabel, subtitleText,
                instructionsLabel, instructionsText,
                barcodeLabel, barcodeContainer, barcodeButton,
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
                barcodeToggleGroup.getSelectedToggle() == filecodeRadio,
                jsonField.getText());
    }

    public static class Selections {
        public String title;
        public String subtitle;
        public String instructions;
        public String barcodePath;
        public boolean barcodeIsPath;
        public String jsonPath;

        public Selections(String title,
                          String subtitle,
                          String instructions,
                          String barcodePath, boolean barcodeIsPath,
                          String jsonPath) {
            this.title = title;
            this.subtitle = subtitle;
            this.instructions = instructions;
            this.barcodePath = barcodePath;
            this.barcodeIsPath = barcodeIsPath;
            this.jsonPath = jsonPath;
        }
    }
}
