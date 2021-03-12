package com.example.gpslocation;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    TextView textView6;
    TextView textView7;

    EditText urlTxtBox;

    Button shareBtn;
    Button sendBtn;

    private double Lat ;
    private double Lon ;
    private double alt ;
    private float speed ;

    private String lat;
    private String lon;
    private String res;

    private Location l;

    private boolean isLocationSharing = false;

    private String serverURL;

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
            if(isLocationSharing) {
                textView7.setText(PostLocation(serverURL+"/api/location"));
            }
            handler.postDelayed(this,1000);
        }
    };

    private String  PostLocation(String url){
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JSONObject postData = new JSONObject();
        try {
            postData.put("Latitude", lat);
            postData.put("Longitude", lon);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    res = response.getString("msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(res);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);
        return res;
    }

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
        textView7 = (TextView) findViewById(R.id.textView7);

        urlTxtBox = (EditText) findViewById(R.id.urlTxtBox);

        shareBtn = (Button) findViewById(R.id.shareBtn);
        sendBtn = (Button) findViewById(R.id.sendBtn);

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT,lat + "," + lon);
                startActivity(Intent.createChooser(intent,"Share using"));
            }
        });

        runnable.run();

        //POST req
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLocationSharing = !isLocationSharing;
                if(isLocationSharing) {
                    serverURL = urlTxtBox.getText().toString();
                    urlTxtBox.setInputType(InputType.TYPE_NULL);
                    sendBtn.setText("Stop");
                }
                else {
                    urlTxtBox.setInputType(InputType.TYPE_CLASS_TEXT);
                    sendBtn.setText("Start");
                }
            }
        });
    }
}