package edu.rice.starvote.BallotPrinter;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by cyricc on 11/10/2016.
 */
public class BallotParserTest {

    private Reader jsonReader;

    @Before
    public void setUp() throws Exception {
        final InputStream jsonStream = getClass().getClassLoader().getResourceAsStream("testballot.json");
        jsonReader = new BufferedReader(new InputStreamReader(jsonStream));
    }

    @Test
    public void test() throws Exception {
        final Collection<RaceData> raceList = BallotParser.parseJson(jsonReader);
        assertEquals(27, raceList.size());
        final List<RaceData> orderedList = new ArrayList<>(raceList);
        assertEquals("President and Vice President", orderedList.get(0).RACE);
        assertEquals("Janette Froman  and Chris Aponte", orderedList.get(0).IDENTIFIER);
        assertEquals("LIB", orderedList.get(0).GROUP);

        assertEquals("Proposition 6", orderedList.get(26).RACE);
        assertEquals("Yes", orderedList.get(26).IDENTIFIER);
        assertEquals("", orderedList.get(26).GROUP);

    }
}