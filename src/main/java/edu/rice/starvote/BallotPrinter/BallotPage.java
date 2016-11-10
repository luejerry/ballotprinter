package edu.rice.starvote.BallotPrinter;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.util.Collection;
import java.util.Queue;

/**
 * Created by cyricc on 11/9/2016.
 */
public class BallotPage extends BorderPane {

    private int columns;

    public BallotPage(double width, double height, Pane header, Pane footer, int columns) {
        super();
        this.columns = columns;
        setPrefSize(width, height);
        setTop(header);
        setBottom(footer);
    }

    private Pair<Double, Double> calculateCenterSize() {
        setCenter(new Pane());
        final Group group = new Group(this);
        final Scene scene = new Scene(group);
        group.applyCss();
        group.layout();
        final double width = getCenter().getBoundsInParent().getWidth();
        final double height = getCenter().getBoundsInParent().getHeight();
        group.getChildren().clear();
        return new Pair<>(width, height);
    }

    public Queue<RaceContainer> addRaces(Collection<RaceContainer> races) {
        final RacePane racePane = new RacePane(columns);
        final Pair<Double, Double> widthHeight = calculateCenterSize();
        racePane.setPrefSize(widthHeight.getKey(), widthHeight.getValue());
        setCenter(racePane);
        return racePane.addRaces(races);
    }

}
