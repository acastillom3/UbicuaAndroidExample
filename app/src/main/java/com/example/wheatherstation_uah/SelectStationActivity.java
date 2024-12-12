package com.example.wheatherstation_uah;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.wheatherstation_uah.data.City;
import com.example.wheatherstation_uah.data.Station;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SelectStationActivity extends AppCompatActivity {

    private String tag = "SelectStation";
    private Spinner spinnerCities;
    private Spinner spinnerStations;
    private Button buttonStation;
    ArrayList<String> arrayCities;
    private ArrayList<City> listCities;
    ArrayList<String> arrayStations;
    private ArrayList<Station> listStation;
    private final Context context;
    private int idStation = 0;
    private String nameStation = "";

    public SelectStationActivity() {
        super();
        this.context = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(tag, "onCreate");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_station);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        //Init the spinners and the button
        this.spinnerCities = this.findViewById(R.id.spinnerCity);
        Log.e(tag, "cities");
        this.spinnerStations = this.findViewById(R.id.spinnerStation);
        Log.e(tag, "stations");
        this.buttonStation = this.findViewById(R.id.buttonStation);
        Log.e(tag, "button");

        //init the arraylist to incorpore the information
        this.listCities = new ArrayList<>();
        this.listStation = new ArrayList<>();
        this.arrayCities = new ArrayList<>();
        this.arrayStations = new ArrayList<>();

        //Add action when the spinner of the cities changes
        spinnerCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                int id = listCities.get(i).getId();//Get the id of the selected position
                Log.i(tag, "City selected:" + listCities.get(i).getName());

                //Get the list of stations of the selected city and set them into the spinner
                loadStations(listCities.get(i).getId());
                spinnerStations.setAdapter(new ArrayAdapter<String>
                        (context, android.R.layout.simple_spinner_item, arrayStations));
                if(arrayStations.size()>0) {
                    spinnerStations.setSelection(0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //Add action when the spinner of the stations changes
        spinnerStations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    idStation = listStation.get(i).getId();//Get the id of the selected position
                    nameStation = listStation.get(i).getName();
                    Log.i(tag, "Station selected:" + listStation.get(i).getName());
                }catch (Exception e){
                    Log.e(tag, "Error on selecting Station:" + e.toString());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        buttonStation.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(tag, "Button pressed");
                Intent i = new Intent(SelectStationActivity.this, StationActivity.class);
                i.putExtra("stationId", "station" + idStation);
                i.putExtra("stationName", nameStation);
                startActivity(i);
                finish();
            }
        });
        //Initial load of cities and stations
        loadCities();
        if(arrayCities.size()>0) {
            spinnerCities.setSelection(0);

            Log.e(tag, "mayor");
        }else {

            Log.e(tag, "menor");
        }
    }


    //Search the cities and fill the spinner with the information
    private void loadCities(){
        String url = "http://192.168.1.21:8080/ServerExampleUbicomp/GetCities";

        Log.e(tag, "loadcities");
        ServerConnectionThread thread = new ServerConnectionThread(this, url);
        try {
            thread.join();
        }catch (InterruptedException e){}
    }

    //Search the stations of the selected city and fill the spinner with the information
    private void loadStations(final int cityId){

        String url = "http://192.168.1.21:8080/ServerExampleUbicomp/GetStationsCity?cityId="+cityId;
        this.listStation = new ArrayList<>();
        this.arrayStations = new ArrayList<>();
        ServerConnectionThread thread = new ServerConnectionThread(this, url);
        try {
            thread.join();
        }catch (InterruptedException e){}
    }

    //Select the Cities from JSON response
    public void setListCities(JSONArray jsonCities){
        try {
            for (int i = 0; i < jsonCities.length(); i++) {
                JSONObject jsonobject = jsonCities.getJSONObject(i);
                listCities.add(new City(jsonobject.getInt("id"),
                        jsonobject.getString("name")));
                arrayCities.add(jsonobject.getString("name"));
            }
            spinnerCities.setAdapter(new ArrayAdapter<String>(context,
                    android.R.layout.simple_spinner_item, arrayCities));
        }catch (Exception e){
            Log.e(tag,"Error: " + e);
        }
    }

    //Select the stations from JSON response
    public void setListStations(JSONArray jsonCities){
        Log.e(tag,"Loading stations " + jsonCities);
        try {
            for (int i = 0; i < jsonCities.length(); i++) {
                JSONObject jsonobject = jsonCities.getJSONObject(i);
                listStation.add(new Station(jsonobject.getInt("id"),
                        jsonobject.getString("name"),
                        Double.parseDouble(jsonobject.getString("latitude")),
                        Double.parseDouble(jsonobject.getString("longitude"))));
                arrayStations.add(jsonobject.getString("name"));
                Log.e(tag,"Station " + jsonobject.getString("name"));
            }
        }catch (Exception e){
            Log.e(tag,"Error: " + e);
        }
    }
}