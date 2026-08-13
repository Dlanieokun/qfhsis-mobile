package com.android.hfsis.vital_statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.hfsis.R;
import com.android.hfsis.model.vital_statistics.MaternalDeathRecord;

import java.util.List;

public class MaternalDeathAdapter extends ArrayAdapter<MaternalDeathRecord> {

    private List<MaternalDeathRecord> records;
    private Context context;

    public MaternalDeathAdapter(Context context, List<MaternalDeathRecord> records) {
        super(context, 0, records);
        this.context = context;
        this.records = records;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_death_record, parent, false);
        }

        MaternalDeathRecord record = records.get(position);

        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvAddress = convertView.findViewById(R.id.tvAddress);
        TextView tvAge = convertView.findViewById(R.id.tvAge);

        tvName.setText(record.fullName != null ? record.fullName : "N/A");
        tvDate.setText("Date: " + (record.dateOfRegistration != null ? record.dateOfRegistration : "N/A"));
        tvAddress.setText("Address: " + (record.completeAddress != null ? record.completeAddress : "N/A"));
        tvAge.setText("Age: " + record.age + " years");

        return convertView;
    }
}