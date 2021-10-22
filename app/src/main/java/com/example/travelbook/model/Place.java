package com.example.travelbook.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity
public class Place implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "place_name")
    public String placeName;

    @ColumnInfo(name = "place_latitude")
    public Double latitude;

    @ColumnInfo(name = "place_longitude")
    public Double longitude;

    public Place(String placeName, Double latitude, Double longitude) {
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
