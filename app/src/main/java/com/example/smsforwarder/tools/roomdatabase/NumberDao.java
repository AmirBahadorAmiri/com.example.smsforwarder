package com.example.smsforwarder.tools.roomdatabase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.smsforwarder.model.NumberModel;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface NumberDao {

    @Insert
    Completable insert(NumberModel number);

    @Delete
    Completable delete(NumberModel number);

    @Query("DELETE FROM number_tb")
    Completable deleteAll();

    @Query("SELECT * FROM number_tb")
    Single<List<NumberModel>> getAll();


}
