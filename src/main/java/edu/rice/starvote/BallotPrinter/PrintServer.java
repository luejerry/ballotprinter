package edu.rice.starvote.BallotPrinter;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.BufferedWriter;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collection;
import java.util.List;

/**
 * Created by cyricc on 11/17/2016.
 */
public class PrintServer {

    static final String TITLE = "Official Ballot";
    static final String SUBTITLE = "November 8, 2016, General Election\nHarris County, Texas Precinct 361";
    static final String INSTRUCTIONS = "PLACE THIS IN BALLOT BOX";
    static final String BARCODE = "ASDF-asdf-1234";

    static final int PORT = 8888;

    private String lastJSON;

    private final PrinterModel printerModel;
    private PrintStream userLogger = System.out;

    private static final Gson gson = new Gson();
    private static final Logger log = LoggerFactory.getLogger(PrintServer.class);

    public PrintServer(PrinterModel printerModel) {
        this.printerModel = printerModel;
    }

    void initRoutes() {
        Spark.port(PORT);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd@kk.mm.ss");
        Spark.post("/", (request, response) -> {
            final String host = request.host();
            final String ip = request.ip();
            log.info("POST received from {} ({}) on print route", host, ip);
//            System.out.println("POST received from " + host + " (" + ip + ")");
            final String type = request.contentType();
            log.info("Content-Type: {}", type);
//            System.out.println("Content-Type: " + type);
            final String body = request.body();
//            System.out.println(body);
            log.debug("Body: {}", body);
            try {
                gson.fromJson(body, RaceData[].class);
                lastJSON = body;
                final WebData webData = new WebData(TITLE, SUBTITLE, INSTRUCTIONS, BARCODE, body);
                printerModel.printFromWeb(webData);
                response.status(201); // resource created
                userLogger.format("Received print job from %s (%s)\n", host, ip);
            } catch (JsonSyntaxException e) {
//                System.out.println("Error: unexpected content in POST body. Ignoring request.");
                log.error("Error: unexpected content in POST body. Ignoring request.");
                userLogger.format("Error: received unrecognized request from %s (%s)\n", host, ip);
                response.status(400); // bad request
            }
            return response;
        });
        Spark.post("/report", ((request, response) -> {
            final Path reportDir = Files.createDirectory(Paths.get("reports"));
            final LocalDateTime time = LocalDateTime.now();
            final String fileName = "results" + time.format(formatter) + ".txt";
            final Path file = reportDir.resolve(fileName);
            final BufferedWriter bufferedWriter = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            final String body = request.body();
            final String host = request.host();
            final String ip = request.ip();
            log.info("POST received from {} ({}) on report route", host, ip);
            bufferedWriter.write(body);
            bufferedWriter.close();
            log.info("Report written to {}", file.toAbsolutePath().toString());
            userLogger.format("Report generated at %s\n", file.toAbsolutePath().toString());
            response.status(201);
            return response;
        }));
        Spark.get("/", (request, response) -> {
            if (lastJSON == null) {
                response.status(500);
                return response;
            }
            response.header("Content-Type", "application/json");
            response.body(lastJSON);
            response.status(200);
            return response;
        });
        userLogger.format("Print server listening on port %d\n", PORT);
    }

    public void start() {
        initRoutes();
    }

    public void stop() {
        Spark.stop();
    }

    public void setUserLogger(PrintStream userLogger) {
        this.userLogger = userLogger;
    }
}
