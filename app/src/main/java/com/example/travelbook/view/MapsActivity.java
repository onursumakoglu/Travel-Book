package com.example.travelbook.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import com.example.travelbook.R;
import com.example.travelbook.databinding.ActivityMainBinding;
import com.example.travelbook.model.Place;
import com.example.travelbook.roomdb.PlaceDao;
import com.example.travelbook.roomdb.PlaceDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.travelbook.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean flag;
    PlaceDatabase db;
    PlaceDao placeDao;
    Double selectedLatitude;
    Double selectedLongitude;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    Place selectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();

        sharedPreferences = MapsActivity.this.getSharedPreferences("com.example.travelbook",MODE_PRIVATE);

        db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "places-database-name")
              //  .allowMainThreadQueries()
                .build();
        placeDao = db.placeDao();

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;

        binding.saveButton.setEnabled(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")){

            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);
            binding.placeNameText.setEnabled(false);

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    flag = sharedPreferences.getBoolean("flag", false);

                    if (!flag){
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        sharedPreferences.edit().putBoolean("flag", true).apply();
                    }


                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {

                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    Snackbar.make(binding.getRoot(), "Permission needs for map", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }

            }else {
                // permission already given
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null){
                    LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                }
                mMap.setMyLocationEnabled(true);

            }

        }else {

            mMap.clear();

            selectedPlace = (Place) intent.getSerializableExtra("place");
            LatLng latLng = new LatLng(selectedPlace.latitude, selectedPlace.longitude);
            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.placeName));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

            binding.placeNameText.setText(selectedPlace.placeName);
            binding.placeNameText.setEnabled(false);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

        }
    }

    private void registerLauncher() {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    // permission granted
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    // to get user's last location
                    Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (lastLocation != null){
                            LatLng lastUserLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                        }
                }
                else{
                    // permission denied
                }
            }
        });
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        mMap.clear();

        mMap.addMarker(new MarkerOptions().position(latLng).title("İşaretlenen Konum"));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveButton.setEnabled(true);
        binding.placeNameText.setEnabled(true);
    }

    public void save(View view){

        Place place = new Place(binding.placeNameText.getText().toString(), selectedLatitude, selectedLongitude);
        //placeDao.insert(place);

        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())  // not necessary, bcs we'll not do anything with fetching the data end of the this.
                .subscribe(MapsActivity.this::handlerResponse)
        );

    }

    public void handlerResponse(){
        Intent intent = new Intent(MapsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void delete(View view){

        compositeDisposable.add(placeDao.delete(selectedPlace)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MapsActivity.this::handlerResponse)
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}