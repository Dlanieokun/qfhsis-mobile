package com.android.hfsis.model.address;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Mirrors the "regions" table on the Laravel server.
 * regCode  - e.g. "01", "13" (NCR), "14" (CAR)
 * regDesc  - e.g. "REGION I (ILOCOS REGION)"
 */
@Entity(tableName = "regions")
public class Region {

    @PrimaryKey
    @NonNull
    public String regCode = "";

    public String regDesc;
}