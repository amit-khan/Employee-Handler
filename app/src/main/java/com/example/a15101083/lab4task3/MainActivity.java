package com.example.a15101083.lab4task3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private ListView lv;
    ArrayList<String> datalist;
    DatabaseHelper myDb;
    private Cursor data;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;
    public static final String KEY_SAVED = "com.example.a15101083.lab4task1_2.saved";
    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Lab04 Task3,4");

        datalist = new ArrayList<>();
        lv = findViewById(R.id.list);
        myDb = new DatabaseHelper(this);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();

        //check for previously saved sharedPrefs
        Boolean firstLaunch = mPreferences.getBoolean(KEY_SAVED, false);

        if (!firstLaunch) {
            new GetContacts().execute();
        } else {
            showData();
        }

        client = LocationServices.getFusedLocationProviderClient(this);

        listen();

    }


    private void listen() {
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            String name,lat,lon;
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                data.moveToPosition(position);
                name = data.getString(1);
                lat = data.getString(2);
                lon = data.getString(3);
                if (lat==null || lon==null) {
                    requestPermission();
                    AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
                    adBuilder.setMessage(name+"'s location is not defined. Submit device's current location as employee's location?");
                    adBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                //no permission
                            }else{
                                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location!=null){
                                            lat= String.valueOf(location.getLatitude());
                                            lon= String.valueOf(location.getLongitude());
                                            Toast.makeText(getApplicationContext(), "Showing " + name + "'s location", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                                            intent.putExtra("name", name);
                                            intent.putExtra("latitude", lat);
                                            intent.putExtra("longitude", lon);
                                            startActivity(intent);
                                        }
                                    }
                                });
                            }
                        }
                    });
                    adBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //finish();
                        }
                    });

                    AlertDialog alertDialog = adBuilder.create();
                    alertDialog.show();
                }else{
                    Toast.makeText(getApplicationContext(), "Showing " + name + "'s location", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("latitude", lat);
                    intent.putExtra("longitude", lon);
                    startActivity(intent);
                }
            }
        });
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,new String[]{ACCESS_FINE_LOCATION},1);
    }



    private class GetContacts extends AsyncTask<Void,Void,Void> {
        Boolean isInserted=true;

        @Override
        protected Void doInBackground(Void... voids) {

            HttpHandler sh = new HttpHandler();
            String url = "http://anontech.info/courses/cse491/employees.json";
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null){
                try {
                    JSONArray jsonAr = new JSONArray(jsonStr);
                    for (int i=0; i<jsonAr.length(); i++){
                        JSONObject c = jsonAr.getJSONObject(i);
                        String name = c.getString("name");
                        String latitude, longitude;
                        if(!c.isNull("location")) {
                            JSONObject location = c.getJSONObject("location");
                            latitude = location.getString("latitude");
                            longitude = location.getString("longitude");
                        } else {
                            latitude = longitude = null;
                        }

                        if(!myDb.insertData(name, latitude, longitude)){
                            isInserted=false;
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else{
                //Toast.makeText(getApplicationContext(),"Couldn't fetch data",Toast.LENGTH_SHORT).show();
                isInserted=false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (isInserted){
                Toast.makeText(getApplicationContext(),"Saved in database",Toast.LENGTH_SHORT).show();
                mEditor.putBoolean(KEY_SAVED,true);
                mEditor.apply();
                showData();
            }else{
                Toast.makeText(getApplicationContext(),"Failed to save in database",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showData() {
        String name,latitude,longitude;
        data = myDb.getAllData();
        while (data.moveToNext()){
            name = data.getString(1);
            latitude = data.getString(2);
            longitude=data.getString(3);
            String temp=name+"\n"+"Latitude: "+latitude+", Longitute: "+longitude;
            datalist.add(temp);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1,datalist);
        lv.setAdapter(adapter);
    }
}
