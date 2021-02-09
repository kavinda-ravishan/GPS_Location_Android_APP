package com.example.gpslocation;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    TextView textView6;

    Button button;

    private double Lat ;
    private double Lon ;
    private double alt ;
    private float speed ;

    private String lat;
    private String lon;

    private Location l;

    private String converterNS(double code) {

        String NS;
        if (code < 0) {
            NS = "S";
            code = (-1) * code;
        } else NS = "N";

        int dig = (int) code;
        String digS = String.valueOf(dig);

        double flot = code - dig;
        flot = flot * 60;

        int min = (int) flot;
        String minS = String.valueOf(min);

        double sec = (flot - min) * 60;
        String secS = String.valueOf(sec);

        return (digS + " " + minS + "' " + secS + "'' " + NS);
    }
    private String converterEW(double code) {

        String EW;
        if (code < 0) {
            EW = "W";
            code = (-1) * code;
        } else EW = "E";

        int dig = (int) code;
        String digS = String.valueOf(dig);

        double flot = code - dig;
        flot = flot * 60;

        int min = (int) flot;
        String minS = String.valueOf(min);

        double sec = (flot - min) * 60;
        String secS = String.valueOf(sec);

        return (digS + " " + minS + "' " + secS + "'' " + EW);
    }
    private void display() {

        GPStracker g = new GPStracker(getApplicationContext());

        l = g.getLocation();

        if (l != null) {
            Lat = l.getLatitude();
            Lon = l.getLongitude();

            alt = l.getAltitude();
            speed = l.getSpeed();

            lat = Double.toString(Lat);
            lon = Double.toString(Lon);

            textView1.setText("Latitude : " + lat);
            textView2.setText("Longitude : " + lon);

            textView3.setText(converterNS(Lat));
            textView4.setText(converterEW(Lon));

            textView5.setText("Altitude : " + String.valueOf(alt) + " m");

            textView6.setText("Speed : " + String.valueOf(speed) + " m/s");
        }
    }

    private Handler handler=new Handler();

    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            display();
            handler.postDelayed(this,1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);
        textView6 = (TextView) findViewById(R.id.textView6);

        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,lat + "," + lon);
                startActivity(Intent.createChooser(intent,"Share using"));
            }
        });

        runnable.run();
    }
}