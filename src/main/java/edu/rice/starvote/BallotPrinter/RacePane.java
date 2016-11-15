package edu.rice.starvote.BallotPrinter;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

/**
 * Created by cyricc on 11/9/2016.
 */
public class RacePane extends HBox {

    private final List<VBox> columnList = new LinkedList<>();

    public RacePane(int columns, double width, double height) {
        super();
        for (int i = 0; i < columns; i++) {
            final VBox column = new VBox();
            columnList.add(column);
            getChildren().add(column);
        }
        setPrefSize(width, height);
    }

    @Override
    public void setPrefSize(double prefWidth, double prefHeight) {
        super.setPrefSize(prefWidth, prefHeight);
        columnList.forEach((col) -> {
            col.setPrefSize(prefWidth / columnList.size(), prefHeight);
        });
    }

    public Queue<RaceContainer> addRaces(RaceContainer... races) {
        final Queue<RaceContainer> raceList = new LinkedList<>();
        Collections.addAll(raceList, races);
        return layout(raceList);
    }

    public Queue<RaceContainer> addRaces(Collection<RaceContainer> races) {
        final Queue<RaceContainer> raceList = new LinkedList<>(races);
        return layout(raceList);
    }

    private Queue<RaceContainer> layout(Queue<RaceContainer> raceList) {
        columnList.forEach((col) -> col.getChildren().clear());
        final Iterator<VBox> iterator = columnList.iterator();
        VBox currentCol = iterator.next();
        double currentHeight = 0;
        while (!raceList.isEmpty()) {
            final RaceContainer nextRace = raceList.element();
            final Double height = nextRace.calculateSize(currentCol.getPrefWidth()).getValue();
            if (currentHeight + height > getPrefHeight()) {
                if (iterator.hasNext()) {
                    currentCol = iterator.next();
                    currentHeight = 0;
                } else {
                    break;
                }
            }
            currentCol.getChildren().add(raceList.remove());
            nextRace.setPrefWidth(currentCol.getPrefWidth());
            nextRace.setMinWidth(currentCol.getPrefWidth());
            currentHeight += height;
        }

        return raceList;
    }


}
