package edu.rice.starvote.BallotPrinter;

import static spark.Spark.*;
/**
 * Created by cyricc on 11/17/2016.
 */
public class PrintServer {

    void initRoutes() {
        post("/", (request, response) -> {
            final String host = request.host();
            final String ip = request.ip();
            System.out.println("POST received from " + host + " (" + ip + ")");
            final String type = request.contentType();
            System.out.println("Content-Type: " + type);
            final String body = request.body();


            response.status(201);
            return response;
        });
    }
}
