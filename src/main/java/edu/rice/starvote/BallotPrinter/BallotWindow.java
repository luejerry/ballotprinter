package edu.rice.starvote.BallotPrinter;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Created by cyricc on 11/15/2016.
 */
public class BallotWindow {

    private final VBox window = new VBox();
    private final List<BallotPage> ballotPages;
    private BallotPage currentPage;
    private int currentPageNumber;

    public BallotWindow(List<BallotPage> ballotPages) {
        this.ballotPages = ballotPages;
        currentPageNumber = 1;
        currentPage = ballotPages.get(currentPageNumber - 1);
        init();
    }

    public Pane getPane() {
        return window;
    }

    private void init() {
        final Button prevButton = new Button("Previous");
        final Button nextButton = new Button("Next");
        final Label pageLabel = new Label();
        final HBox controlPane = new HBox(prevButton, nextButton, pageLabel);
        controlPane.setSpacing(3);
        controlPane.setAlignment(Pos.CENTER_LEFT);
        window.getChildren().add(controlPane);
        update(prevButton, nextButton, pageLabel);

        prevButton.addEventHandler(ActionEvent.ACTION, event -> {
            if (currentPageNumber > 1) {
                window.getChildren().remove(currentPage);
                currentPageNumber--;
                update(prevButton, nextButton, pageLabel);
            }
        });

        nextButton.addEventHandler(ActionEvent.ACTION, event -> {
            if (currentPageNumber < ballotPages.size()) {
                window.getChildren().remove(currentPage);
                currentPageNumber++;
                update(prevButton, nextButton, pageLabel);
            }
        });

    }

    private void update(Button prevButton, Button nextButton, Label pageLabel) {
        currentPage = ballotPages.get(currentPageNumber - 1);
        window.getChildren().add(currentPage);
        pageLabel.setText(currentPageNumber + "/" + ballotPages.size());
        if (ballotPages.size() <= 1) {
            prevButton.setDisable(true);
            nextButton.setDisable(true);
        } else {
            if (currentPageNumber <= 1) {
                prevButton.setDisable(true);
            } else {
                prevButton.setDisable(false);
            }
            if (currentPageNumber >= ballotPages.size()) {
                nextButton.setDisable(true);
            } else {
                nextButton.setDisable(false);
            }
        }
    }
}
