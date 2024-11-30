package com.example.wheatherstation_uah;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class ServerConnectionThread extends Thread{
    private SelectStationActivity activity;
    private String tag = "ServerConnectionThread";
    private String urlStr = "";

    public ServerConnectionThread(SelectStationActivity activ, String url)    {
        activity = activ;
        urlStr = url;
        start();
    }

    @Override
    public void run()    {
        String response = "";
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConnection = null;
            urlConnection = (HttpURLConnection) url.openConnection();
            //Get the information from the url
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = convertStreamToString(in);
            Log.d(tag, "get json: " + response);
            JSONArray jsonarray = new JSONArray(response);
            //Read Responses and fill the spinner
            if(urlStr.contains("GetCities")){
                activity.setListCities(jsonarray);
            }else{
                if (urlStr.contains("GetStationsCity")){
                    activity.setListStations(jsonarray);
                }
            }
        }
        catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    //Get the input strean and convert into String
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}



