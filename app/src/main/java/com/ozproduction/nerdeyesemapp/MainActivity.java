package com.ozproduction.nerdeyesemapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements LocationListener, MainActivityInterface, Serializable {
    public Restaurant[] list;
    ListView listView;
    private static RestaurantAdapter adapter;
    ProgressDialog loadingdialog;
    private final String TAG = "Nearby";
    private final int CHANGE_LAT_LONG = 1;
    private final int MY_PERMISSION_REQUEST = 1;
    private final int REQUEST_LOCATION = 1;
    double latitude;
    double longitude;
    boolean isshowedbefore;

    // GPS Related Variables
    private Location location;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 5; // 5 minutes
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isshowedbefore = false;
        setTitle("Yakındaki Restoranlar");
        ShowLoadingProgress();
        listView=(ListView)findViewById(R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),RestaurantInfoActivity.class);
                intent.putExtra("name",list[i].getName());
                intent.putExtra("address",list[i].getAddress());
                intent.putExtra("genre",list[i].getGenre());
                intent.putExtra("phone",list[i].getPhone());
                intent.putExtra("pricerange",list[i].getPriceRange());
                intent.putExtra("latitude",list[i].getLatitude());
                intent.putExtra("longitude",list[i].getLongitude());
                startActivity(intent);
            }
        });
        ActivityCompat.requestPermissions( this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        if (!isInternetAvailable())
        {
            Toast.makeText(getApplicationContext(),"Bağlantı hatası!",Toast.LENGTH_LONG).show();
        }
        else {
            // Getting latitude and longitude data
            initLocationTracking();
            displayData();
        }
    }

    private void getListFromZomato(double latitude, double longitude) {
        ZomatoTask connection = new ZomatoTask(MainActivity.this);
        connection.execute(latitude,longitude);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_LAT_LONG && data != null) {
            Log.i(TAG, "Manual location");
            latitude = data.getDoubleExtra("latitude", 0.0);
            longitude = data.getDoubleExtra("longitude", 0.0);
            displayData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permissions granted");
                // Get new location
                initLocationTracking();
                displayData();
            }
        }
    }

    private void initLocationTracking() {
        boolean isGPSEnabled;
        boolean isNetworkEnabled;

        Log.i(TAG, "initLocationTracking");
        // Check if permissions were granted by user
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Requesting location permissions");
            // Request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST);
            // Can't continue, return
            return;
        }

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled || isNetworkEnabled) {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }

                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    Log.d(TAG, "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.i(TAG, "Latitude: " + latitude);
                            Log.i(TAG, "Longitude: " + longitude);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void stopUsingGPS() {
        // Check permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (locationManager != null) {
            Log.i(TAG, "Stop location control");
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        if ((location.getLatitude() != latitude) || (location.getLongitude() != longitude))
        {
            Log.i(TAG, "Location changed");
            this.location = location;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            displayData();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUsingGPS();
    }

    private boolean isInternetAvailable()
    {
        boolean isAvailable = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if ((activeNetwork != null) &&
                activeNetwork.isConnectedOrConnecting())
        {
            isAvailable = true;
        }

        return isAvailable;
    }

    private void displayData()
    {
        getListFromZomato(latitude,longitude);
    }

    @Override
    public void SetRestaurantList(Restaurant[] restaurantList)
    {
        list = restaurantList;
        adapter= new RestaurantAdapter(list,MainActivity.this);
        listView.setAdapter(adapter);
    }

    @Override
    public void RestaurantNotFound()
    {
        if(!isshowedbefore)
        {
            isshowedbefore = true;
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("UYARI")
                    .setMessage("Yakınlarda restoran bulunamadı")
                    .setPositiveButton("TAMAM", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    }

    @Override
    public void CloseLoadingDialog()
    {
        loadingdialog.dismiss();
    }

    public void ShowLoadingProgress()
    {
        loadingdialog = new ProgressDialog(MainActivity.this);
        loadingdialog.setTitle("YÜKLENİYOR");
        loadingdialog.setMessage("Yakındaki restoran listesi yükleniyor...");
        loadingdialog.setCancelable(false);
        loadingdialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void Back(View view)
    {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }
}