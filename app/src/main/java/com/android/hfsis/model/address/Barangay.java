package com.android.hfsis.model.address;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Mirrors the "barangays" table on the Laravel server.
 * brgyCode     - e.g. "01280101"
 * brgyDesc     - e.g. "Adams (Pob.)"
 * citymunCode  - parent municipality code, e.g. "012801"
 * provCode     - parent province code, e.g. "0128"
 * regCode      - parent region code, e.g. "01"
 */
@Entity(tableName = "barangays")
public class Barangay {

    @PrimaryKey
    @NonNull
    public String brgyCode = "";

    public String brgyDesc;

    public String citymunCode;

    public String provCode;

    public String regCode;
}
