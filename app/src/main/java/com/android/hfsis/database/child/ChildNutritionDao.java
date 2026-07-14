package com.android.hfsis.database.child;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.android.hfsis.model.child.ChildNutritionRecord;
import java.util.List;

@Dao
public interface ChildNutritionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ChildNutritionRecord record);

    @Update
    void update(ChildNutritionRecord record);

    @Delete
    void delete(ChildNutritionRecord record);

    @Query("SELECT * FROM child_nutrition_records ORDER BY id DESC")
    List<ChildNutritionRecord> getAllRecords();

    @Query("SELECT * FROM child_nutrition_records WHERE id = :id LIMIT 1")
    ChildNutritionRecord getRecordById(long id);

    @Query("SELECT * FROM child_nutrition_records WHERE childName LIKE '%' || :searchQuery || '%' OR familySerialNumber LIKE '%' || :searchQuery || '%' ORDER BY id DESC")
    List<ChildNutritionRecord> searchRecords(String searchQuery);

    @Query("SELECT * FROM child_nutrition_records WHERE isSynced = 0")
    List<ChildNutritionRecord> getUnsyncedRecords();

    @Query("UPDATE child_nutrition_records SET isSynced = 1 WHERE id IN (:ids)")
    void markAsSynced(List<Long> ids);
}