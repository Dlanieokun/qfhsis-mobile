package com.android.hfsis.model.address;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Mirrors the "provinces" table on the Laravel server.
 * provCode - e.g. "0128"
 * provDesc - e.g. "ILOCOS NORTE"
 * regCode  - parent region code, e.g. "01"
 */
@Entity(tableName = "provinces")
public class Province {

    @PrimaryKey
    @NonNull
    public String provCode = "";

    public String provDesc;

    public String regCode;
}
