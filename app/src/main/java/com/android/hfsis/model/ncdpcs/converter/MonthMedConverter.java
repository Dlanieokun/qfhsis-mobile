package com.android.hfsis.model.ncdpcs.converter;

import androidx.room.TypeConverter;
import com.android.hfsis.model.ncdpcs.PhilPENAssessmentEntity.MonthMed;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class MonthMedConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static com.android.hfsis.model.ncdpcs.PhilPENAssessmentEntity.MonthMed[] fromString(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<MonthMed[]>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArray(MonthMed[] list) {
        if (list == null) return null;
        return gson.toJson(list);
    }
}