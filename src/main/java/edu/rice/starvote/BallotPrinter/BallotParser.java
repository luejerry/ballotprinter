package edu.rice.starvote.BallotPrinter;

import com.google.gson.Gson;

import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by cyricc on 11/10/2016.
 */
public class BallotParser {

    private static Gson gson = new Gson();

    public static Collection<RaceData> parseJson(Reader jsonReader) {
        final RaceData[] raceData = gson.fromJson(jsonReader, RaceData[].class);
        final LinkedList<RaceData> raceList = new LinkedList<>();
        Collections.addAll(raceList, raceData);
        return raceList;
    }

    public static Collection<RaceData> parseJson(String json) {
        final RaceData[] raceData = gson.fromJson(json, RaceData[].class);
        final LinkedList<RaceData> raceList = new LinkedList<>();
        Collections.addAll(raceList, raceData);
        return raceList;
    }

    public static Collection<RaceContainer> generateRaceContainers(Collection<RaceData> raceList) {
        return raceList.stream()
                .map((raceData) ->
                        new RaceContainer(raceData.RACE, raceData.IDENTIFIER, raceData.GROUP))
                .collect(Collectors.toList());
    }


}
