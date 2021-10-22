package com.example.travelbook.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.travelbook.R;
import com.example.travelbook.adapter.PlaceAdapter;
import com.example.travelbook.databinding.ActivityMainBinding;
import com.example.travelbook.model.Place;
import com.example.travelbook.roomdb.PlaceDao;
import com.example.travelbook.roomdb.PlaceDatabase;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PlaceDatabase db;
    private PlaceDao placeDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = activityMainBinding.getRoot();
        setContentView(view);

        db = Room.databaseBuilder(getApplicationContext(), PlaceDatabase.class, "places-database-name").build();
        placeDao = db.placeDao();

        compositeDisposable.add(placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MainActivity.this::handleResponse)
        );

    }

    private void handleResponse(List<Place> placeList) {
        activityMainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //after this line, i created placeadapter folder to link the adapter and recyclerview gooooo adapter/PlaceAdapter

        PlaceAdapter placeAdapter = new PlaceAdapter(placeList);
        activityMainBinding.recyclerView.setAdapter(placeAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_place){

            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("info", "new");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}