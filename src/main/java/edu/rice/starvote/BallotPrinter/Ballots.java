package edu.rice.starvote.BallotPrinter;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Pair;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by cyricc on 11/9/2016.
 */
public class Ballots {

    public static HBox createHeader(String title, String subtitle, String instructions, Image code) {
        final Label titleLabel = new Label(title);
        titleLabel.setPadding(new Insets(0, 0, 7, 0));
        titleLabel.setStyle("-fx-font-weight: bold;" +
                "-fx-font-size: 18");
        final Label subtitleLabel = new Label(subtitle);
        final Label instructionsLabel = new Label(instructions);
        instructionsLabel.setStyle("-fx-font-weight: bold;" +
                "-fx-font-size: 18");
        final ImageView imageView = new ImageView(code);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(100);

        final Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        final VBox leftPane = new VBox(titleLabel, subtitleLabel);
        final VBox rightPane = new VBox(imageView, instructionsLabel);
        rightPane.setAlignment(Pos.TOP_RIGHT);

        return new HBox(leftPane, spacer, rightPane);
    }

    public static HBox createFooter(Image code) {
        final ImageView imageView = new ImageView(code);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(100);

//        final Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);

        return new HBox(imageView);
    }

    public static BorderPane createTestBallot(double width,
                                          double height,
                                          Pane header,
                                          Pane footer) {
        final BallotPage ballotPage = new BallotPage(width, height, header, footer, 3);

        final LinkedList<RaceContainer> raceList = new LinkedList<>();
        for (int i = 1; i < 50; i ++) {
            raceList.add(new RaceContainer("District Judge, Congressional Judicial District " + i, "Jonathan Smith", "IND"));
        }
        final Queue<RaceContainer> remaining = ballotPage.addRaces(raceList);
        return ballotPage;
    }

    public static Pair<BallotPage, Queue<RaceContainer>> createBallot(double width,
                                                                      double height,
                                                                      int columns,
                                                                      Pane header,
                                                                      Pane footer,
                                                                      Collection<RaceContainer> raceList) {
        final BallotPage ballotPage = new BallotPage(width, height, header, footer, columns);
        ballotPage.setStyle("-fx-background-color: white");
        final Queue<RaceContainer> remaining = ballotPage.addRaces(raceList);
        return new Pair<>(ballotPage, remaining);

    }
}
