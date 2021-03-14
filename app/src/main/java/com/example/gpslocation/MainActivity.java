package com.example.gpslocation;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
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

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private TextView textView6;
    private TextView textView7;

    private EditText urlTxtBox;

    private Button shareBtn;
    private Button sendBtn;

    private double lat ;
    private double lon ;
    private double alt ;
    private float speed ;

    private String latStr;
    private String lonStr;
    private String altStr;
    private String speedStr;
    private String res;

    private Location l;

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
            lat = l.getLatitude();
            lon = l.getLongitude();

            alt = l.getAltitude();
            speed = l.getSpeed();

            latStr = Double.toString(lat);
            lonStr = Double.toString(lon);
            speedStr = String.valueOf(speed);
            altStr = String.valueOf(alt);

            textView1.setText("Latitude : " + latStr);
            textView2.setText("Longitude : " + lonStr);

            textView3.setText(converterNS(lat));
            textView4.setText(converterEW(lon));

            textView5.setText("Altitude : " + altStr + " m");

            textView6.setText("Speed : " + speedStr + " m/s");
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

    private String  PostLocation(String url){
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JSONObject postData = new JSONObject();
        try {
            postData.put("Latitude", latStr);
            postData.put("Longitude", lonStr);
            postData.put("Speed", speedStr);

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
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostLocationTask();
            }
        });
    }

    public void PostLocationTask(){
        PostData postData = new PostData(this);
        serverURL = urlTxtBox.getText().toString();
        postData.execute(serverURL);
    }

    private static class PostData extends AsyncTask<String,Void,String> {

        //For fix memory leaks
        private WeakReference<MainActivity> activityWeakReference;
        PostData(MainActivity activity){
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            MainActivity activity = activityWeakReference.get();
            if(activity == null || activity.isFinishing()){
                return;
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            MainActivity activity = activityWeakReference.get();
            if(activity == null || activity.isFinishing()){
                return "error";
            }
            return activity.PostLocation(strings[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MainActivity activity = activityWeakReference.get();
            if(activity == null || activity.isFinishing()){
                return;
            }
            activity.textView7.setText(s);
        }
    }

}