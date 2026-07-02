package com.android.hfsis.database.ncdps;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.ncdpcs.CervicalCancerScreeningEntity;

import java.util.List;

@Dao
public interface CervicalCancerScreeningDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CervicalCancerScreeningEntity screening);

    @Update
    void update(CervicalCancerScreeningEntity screening);

    @Delete
    void delete(CervicalCancerScreeningEntity screening);

    @Query("SELECT * FROM cervical_cancer_screenings WHERE id = :id")
    CervicalCancerScreeningEntity getScreeningById(long id);

    @Query("SELECT * FROM cervical_cancer_screenings ORDER BY date_assessment DESC")
    List<CervicalCancerScreeningEntity> getAllScreenings();

    @Query("SELECT * FROM cervical_cancer_screenings WHERE family_serial = :familySerial")
    List<CervicalCancerScreeningEntity> getScreeningsByFamilySerial(String familySerial);

    @Query("DELETE FROM cervical_cancer_screenings")
    void deleteAll();

    @Query("SELECT * FROM cervical_cancer_screenings ORDER BY date_assessment DESC")
    List<CervicalCancerScreeningEntity> getAll();
}