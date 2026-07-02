package com.android.hfsis.database.ncdps;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.ncdpcs.EyesScreeningsData;

import java.util.List;

@Dao
public interface EyesScreeningDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(EyesScreeningsData screeningData);

    @Update
    void update(EyesScreeningsData screeningData);

    @Delete
    void delete(EyesScreeningsData screeningData);

    @Query("SELECT * FROM eyes_screenings ORDER BY id DESC")
    List<EyesScreeningsData> getAllScreenings();

    @Query("SELECT * FROM eyes_screenings WHERE id = :id LIMIT 1")
    EyesScreeningsData getScreeningById(long id);

    @Query("SELECT * FROM eyes_screenings WHERE name LIKE '%' || :searchQuery || '%'")
    List<EyesScreeningsData> searchScreeningsByName(String searchQuery);

    @Query("SELECT * FROM eyes_screenings ORDER BY id DESC")
    List<EyesScreeningsData> getAll();
}