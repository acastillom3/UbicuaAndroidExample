package com.example.wheatherstation_uah;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.UnsupportedEncodingException;

public class StationActivity extends AppCompatActivity {

    private MqttAndroidClient client;
    private final static String CHANNEL_ID = "stationId";
    private final static int NOTIFICATION_ID=0;
    private String station = "";
    private String stationName = "";
    private String tag = "StationActivity";
    private TextView tvstationname;
    private TextView tvstationinfo;

    public StationActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_station);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //Init the TextViews
        this.tvstationname = this.findViewById(R.id.stationname);
        this.tvstationinfo = this.findViewById(R.id.stationInfo);

        //Get the value of the station id from the previous activity
        station = getIntent().getStringExtra("stationId");
        stationName = getIntent().getStringExtra("stationName");

        tvstationname.setText(stationName);

        //Configure the mqtt client who will send the notifications about the selected station
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://192.168.1.21:1883", clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    //If the connection is ok
                    Log.i(tag, "MQTT connected");
                    //Suscribe the topics
                    suscripcionTopics(station);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.i(tag, "Error connecting MQTT");
                }
            });
        } catch (MqttException e) {e.printStackTrace();}

        //Callback of MQTT to process the information received by MQTT
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {}
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception
            {
                //New alert from the wheater station
                if(topic.contains("alert")){
                    String mqttText = new String(message.getPayload());
                    Log.i(tag, "New Alert: + " + (new String(message.getPayload())));

                    //Create a notification with the alert
                    createNotificationChannel();
                    createNotification("Alert", mqttText);
                }else{
                    //The message is about a sensor type
                    String mqttText = new String(message.getPayload());
                    Log.i(tag, "New Message: + " + (new String(message.getPayload())));

                    tvstationinfo.setText(mqttText);
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
    }

    //Method to create the notification channel in new versions
    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O)
        {
            CharSequence name = "Notificación";
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    //Method to create a notfication with the title and the message
    private void createNotification(String title, String msn){
        //Configure the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);
        builder.setSmallIcon(R.drawable.logo);
        builder.setContentTitle(title);
        builder.setContentText(msn);
        builder.setColor(Color.BLUE);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setLights(Color.BLUE, 1000, 1000);
        builder.setVibrate(new long[]{1000,1000,1000,1000,1000});
        builder.setDefaults(Notification.DEFAULT_SOUND);

        //Show the notification
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    //MQTT topics to suscribe the application
    private void suscripcionTopics(String station){
        try{
            Log.i(tag, "station = " + station);
            client.subscribe(station,0);
            client.subscribe(station + "/alert",0);
            client.subscribe(station + "/sensor",0);

        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    //Publish a new MQTT message in a topic
    private void publish(String topic, String payload){
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            Log.e(tag, "Error mqtt "+ e);
        }
    }
}