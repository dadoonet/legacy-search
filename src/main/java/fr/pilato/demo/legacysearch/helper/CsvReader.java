package fr.pilato.demo.legacysearch.helper;


import java.io.*;
import java.util.ArrayList;

class CsvReader {

    static ArrayList<String> readAsStrings(String url) throws IOException {
        ArrayList<String> data = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(CsvReader.class.getResourceAsStream(url)))) {
            String nextLine = reader.readLine();
            while (nextLine != null) {
                data.add(nextLine);
                nextLine = reader.readLine();
            }
        }

        return data;
    }

    static ArrayList extractFromCommas(String dataLine) {
        //Gives back the data that is found between commas in a String
        ArrayList<String> data = new ArrayList<>();
        String theString = "";
        for (int i = 0; i < dataLine.length(); i++) { //go down the whole string
            if (dataLine.charAt(i) == ',') {
                if (i != 0) {
                    data.add(theString); //this means that the next comma has been reached
                    theString = ""; //reset theString Variable
                }
            } else {
                theString = theString + dataLine.charAt(i); //otherwise, just keep piling the chars onto the cumulative string
            }
        }
        if (!theString.equalsIgnoreCase("")) //only if the last position is not occupied with nothing then add the end on
        {
            data.add(theString);
        }
        return data;
    }
}
