package edu.rice.starvote.BallotPrinter;

/**
 * Created by cyricc on 11/17/2016.
 */
public class WebData {
    public String title;
    public String subtitle;
    public String instructions;
    public String barcode;
    public String json;

    public WebData(String title, String subtitle, String instructions, String barcode, String json) {
        this.title = title;
        this.subtitle = subtitle;
        this.instructions = instructions;
        this.barcode = barcode;
        this.json = json;
    }
}
