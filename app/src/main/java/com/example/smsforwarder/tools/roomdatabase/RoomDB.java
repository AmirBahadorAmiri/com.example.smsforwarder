package com.example.smsforwarder.tools.roomdatabase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.smsforwarder.model.NumberModel;

@Database(entities = {NumberModel.class}, version = 1)
public abstract class RoomDB extends RoomDatabase {
    private static RoomDB roomDB;

    public static RoomDB getInstance(Context context) {

        if (roomDB == null)
            roomDB = Room.databaseBuilder(context, RoomDB.class, "number_database")
                    .fallbackToDestructiveMigration()
                    .build();

        return roomDB;
    }

    public abstract NumberDao numberDao();
}
