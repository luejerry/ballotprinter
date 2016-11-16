package edu.rice.starvote.BallotPrinter;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code128.Code128Constants;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by cyricc on 11/15/2016.
 */
public class BarcodeTest extends Application {

    public static BufferedImage generateCode(String code){
        final Code128Bean code128Bean = new Code128Bean();
        code128Bean.setCodeset(Code128Constants.CODESET_B);
        code128Bean.setModuleWidth(0.5);
        code128Bean.setBarHeight(20);
        final BitmapCanvasProvider canvas = new BitmapCanvasProvider(200, BufferedImage.TYPE_BYTE_GRAY, true, 0);
        code128Bean.generateBarcode(canvas, code);
        final BufferedImage bufferedImage = canvas.getBufferedImage();
        try {
            canvas.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferedImage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final BufferedImage barcode = generateCode("ABCDabcd1234");
        final WritableImage barcodefx = SwingFXUtils.toFXImage(barcode, null);
        final ImageView imageView = new ImageView(barcodefx);
        final BorderPane pane = new BorderPane(imageView);
        final Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Barcode test");
        primaryStage.show();
    }
}
