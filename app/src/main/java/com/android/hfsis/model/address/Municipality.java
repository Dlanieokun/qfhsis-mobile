package com.android.hfsis.model.address;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Mirrors the "municipalities" table on the Laravel server.
 * citymunCode - e.g. "012801"
 * citymunDesc - e.g. "ADAMS"
 * provCode    - parent province code, e.g. "0128"
 * regCode     - parent region code, e.g. "01"
 */
@Entity(tableName = "municipalities")
public class Municipality {

    @PrimaryKey
    @NonNull
    public String citymunCode = "";

    public String citymunDesc;

    public String provCode;

    public String regCode;
}