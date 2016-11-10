package edu.rice.starvote.BallotPrinter;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Pair;

/**
 * Created by cyricc on 11/9/2016.
 */
public class RaceContainer extends GridPane {

    static final String NOSELECTION = "YOU DID NOT SELECT ANYTHING";

    public RaceContainer(String race, String candidate, String party) {
        super();
        final Label raceLabel = new Label(race);
        raceLabel.setWrapText(true);
        raceLabel.setStyle("-fx-font-weight: bold");
        if (candidate == null || candidate.isEmpty()) {
            final Label nullLabel = new Label(NOSELECTION);
            nullLabel.setStyle("-fx-font-weight: bold");
            GridPane.setConstraints(raceLabel, 1, 1);
            GridPane.setConstraints(nullLabel, 1, 2);
            getChildren().addAll(raceLabel, nullLabel);
        } else {
            final Label candidateLabel = new Label(candidate);
            candidateLabel.setPadding(new Insets(0, 0, 0, 6));
            final Label partyLabel = new Label(party);
            final HBox spacer = new HBox();
            GridPane.setConstraints(raceLabel, 1, 1, 2, 1);
            GridPane.setConstraints(spacer, 3, 1);
            GridPane.setHgrow(spacer, Priority.ALWAYS);
            GridPane.setConstraints(candidateLabel, 1, 2, 1, 1);
            GridPane.setConstraints(partyLabel, 2, 2, 2, 1, HPos.RIGHT, VPos.BASELINE);
            getChildren().addAll(raceLabel, candidateLabel, partyLabel, spacer);
        }
        setPadding(new Insets(4, 7, 4, 7));
    }

    public Pair<Double, Double> calculateSize(double maxWidth) {
        setMaxWidth(maxWidth);
        final Group group = new Group(this);
        new Scene(group);
        group.applyCss();
        group.layout();
        final double width = getBoundsInParent().getWidth();
        final double height = getBoundsInParent().getHeight();
        group.getChildren().clear();
        setPrefSize(width, height);
        return new Pair<>(width, height);
    }
}
