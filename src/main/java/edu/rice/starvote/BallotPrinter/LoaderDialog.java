package edu.rice.starvote.BallotPrinter;

import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.Pane;

/**
 * Created by cyricc on 11/11/2016.
 */
public class LoaderDialog {

    private final Dialog<LoaderPane.Selections> dialog = new Dialog<>();

    public LoaderDialog(LoaderPane loaderPane) {
        initDialog(loaderPane);
    }

    private void initDialog(LoaderPane loaderPane) {
        dialog.setTitle("Render ballot for printing");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.getDialogPane().setContent(loaderPane.getPane());
        dialog.setResultConverter(button ->
                button == ButtonType.OK ?
                        loaderPane.getSelections() :
                        null);
    }

    public Dialog<LoaderPane.Selections> getDialog() {
        return dialog;
    }
}
