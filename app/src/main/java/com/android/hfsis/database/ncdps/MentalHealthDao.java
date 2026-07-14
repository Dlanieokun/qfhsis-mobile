package com.android.hfsis.database.ncdps;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.android.hfsis.model.ncdpcs.mental.MentalHealthRecord;

import java.util.List;

@Dao
public interface MentalHealthDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MentalHealthRecord record);

    @Update
    void update(MentalHealthRecord record);

    @Delete
    void delete(MentalHealthRecord record);

    // Fixed: Changed column name from 'id' to 'recordNo'
    @Query("SELECT * FROM mental_health_records WHERE recordNo = :id")
    MentalHealthRecord getRecordById(long id);

    // Fixed: Changed column name from 'date_of_assessment' to 'dateOfAssessment'
    @Query("SELECT * FROM mental_health_records ORDER BY dateOfAssessment DESC")
    List<MentalHealthRecord> getAllRecords();

    // Fixed: Changed column name from 'family_serial_number' to 'familySerialNumber'
    @Query("SELECT * FROM mental_health_records WHERE familySerialNumber = :familySerial")
    List<MentalHealthRecord> getRecordsByFamilySerial(String familySerial);

    // Fixed: Changed column names to match 'name', 'familySerialNumber', and 'dateOfAssessment'
    @Query("SELECT * FROM mental_health_records WHERE name LIKE :searchQuery OR familySerialNumber LIKE :searchQuery ORDER BY dateOfAssessment DESC")
    List<MentalHealthRecord> searchRecords(String searchQuery);

    @Query("DELETE FROM mental_health_records")
    void deleteAll();

    @Query("SELECT * FROM mental_health_records ORDER BY dateOfAssessment DESC")
    List<MentalHealthRecord> getAll();

    @Query("SELECT * FROM mental_health_records WHERE isSynced = 0")
    List<MentalHealthRecord> getUnsyncedRecords();

    @Query("UPDATE mental_health_records SET isSynced = 1 WHERE recordNo IN (:ids)")
    void markAsSynced(List<Integer> ids);

    @Query("SELECT * FROM mental_health_records WHERE newInsert = 1")
    List<MentalHealthRecord> getNewInsertRecords();

    @Query("UPDATE mental_health_records SET newInsert = 0 WHERE recordNo IN (:ids)")
    void markAsInserted(List<Integer> ids);
}