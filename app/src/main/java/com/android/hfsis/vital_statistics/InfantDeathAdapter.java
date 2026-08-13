package com.android.hfsis.vital_statistics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.hfsis.R;
import com.android.hfsis.model.vital_statistics.InfantDeathRecord;

import java.util.List;

public class InfantDeathAdapter extends ArrayAdapter<InfantDeathRecord> {

    private List<InfantDeathRecord> records;
    private Context context;

    public InfantDeathAdapter(Context context, List<InfantDeathRecord> records) {
        super(context, 0, records);
        this.context = context;
        this.records = records;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_death_record, parent, false);
        }

        InfantDeathRecord record = records.get(position);

        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvAddress = convertView.findViewById(R.id.tvAddress);
        TextView tvAge = convertView.findViewById(R.id.tvAge);

        tvName.setText(record.fullName != null ? record.fullName : "N/A");
        tvDate.setText("Date: " + (record.dateOfRegistration != null ? record.dateOfRegistration : "N/A"));
        tvAddress.setText("Address: " + (record.completeAddress != null ? record.completeAddress : "N/A"));
        tvAge.setText("Sex: " + (record.sex != null ? (record.sex.equals("M") ? "Male" : "Female") : "N/A"));

        return convertView;
    }
}