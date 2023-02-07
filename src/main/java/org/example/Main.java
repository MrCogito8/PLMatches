package org.example;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

public class Main {

    private static final String API_KEY = "ec409b9ee111417e867e68fbcc4c65ef";

    public static void main(String[] args) throws Exception {

        String code = "";
        System.out.println("Please select league:\n1: Premier League\n2: Serie A\n3: Bundesliga");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String number = reader.readLine();

        switch(number)
        {
            case "1":
                code = "PL";
                break;
            case "2":
                code = "SA";
                break;
            case "3":
                code = "BL1";
                break;
            default:
                System.out.println("Incorrect number was set!");
        }

        HashMap<String, Integer> teamPosition = new HashMap<String, Integer>();

        //String API_URL = "https://api.football-data.org/v2/competitions/SA/standings";
        String API_URL = "https://api.football-data.org/v2/competitions/"+code+"/standings";
        //String API_URL = "https://api.football-data.org/v2/competitions/BL1/standings";

        String response = sendGetRequest(API_URL);
        JSONObject json = new JSONObject(response);

        JSONArray standings = json.getJSONArray("standings");
        JSONObject standingsObject = standings.getJSONObject(0);
        JSONArray table = standingsObject.getJSONArray("table");


        for(int i=0;i<table.length();i++)
        {
            JSONObject position = table.getJSONObject(i);
            Integer positionNumber = position.getInt("position");
            JSONObject team = position.getJSONObject("team");
            String teamName = team.getString("name");
            teamPosition.put(teamName, positionNumber);
        }

        System.out.println("Provide match week number: ");


        // Reading data using readLine




        number = reader.readLine();

        //API_URL = "https://api.football-data.org/v2/competitions/SA/matches?matchday="+number;
        API_URL = "https://api.football-data.org/v2/competitions/"+code+"/matches?matchday="+number;
        //https://api.football-data.org/v2/competitions/BL1/matches?matchday
        //API_URL = "https://api.football-data.org/v2/competitions/BL1/matches?matchday="+number;

        response = sendGetRequest(API_URL);
        json = new JSONObject(response);
        JSONArray matches = json.getJSONArray("matches");
        List<Match> matchList = new ArrayList<>();
        for (int i = 0; i < matches.length(); i++) {
            JSONObject match = matches.getJSONObject(i);
            JSONObject homeTeam = match.getJSONObject("homeTeam");
            JSONObject awayTeam = match.getJSONObject("awayTeam");
            String homeName = homeTeam.getString("name");
            String awayName = awayTeam.getString("name");
            String date = match.getString("utcDate");
            date = date.replace("T","").replace("Z","");
            Date matchDate = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss").parse(date);
            matchDate.setHours(matchDate.getHours()+1);
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            date = dateFormat.format(matchDate);
            Match m = new Match(date, homeName, awayName, teamPosition.get(homeName), teamPosition.get(awayName));
            matchList.add(m);
        }
        Collections.sort(matchList);
        for (Match m : matchList) {
            System.out.println(String.format("%s (%d) : %s (%d)\tDate: %s\tsum:%d", m.homeName, m.homePosition, m.awayName, m.awayPosition, m.date,
                    (m.homePosition+m.awayPosition)));
        }
    }

    private static String sendGetRequest(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-Auth-Token", API_KEY);
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } else {
            throw new Exception("GET request failed with response code: " + responseCode);
        }
    }

    private static class Match implements Comparable<Match> {
        String date;
        String homeName;
        String awayName;
        int homePosition;
        int awayPosition;

        Match( String date, String homeName, String awayName, Integer homePosition, Integer awayPosition) {
            this.date = date;
            this.homeName = homeName;
            this.awayName = awayName;
            this.homePosition = homePosition;
            this.awayPosition = awayPosition;
        }

        @Override
        public int compareTo(Match o) {
            Integer thisSum = homePosition+awayPosition;
            Integer oSum = o.homePosition+o.awayPosition;
            if(thisSum==oSum) return 0;
            else if(thisSum>oSum) return 1;
            else return -1;
        }

    }
}
   
