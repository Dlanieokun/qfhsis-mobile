package com.android.hfsis.morbidity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.R;
import com.android.hfsis.model.morbidity.MorbidityRecord;

import java.util.List;

public class MorbidityAdapter extends RecyclerView.Adapter<MorbidityAdapter.ViewHolder> {

    public interface OnRecordClickListener {
        void onClick(MorbidityRecord record);
    }

    private List<MorbidityRecord> records;
    private final OnRecordClickListener onView;
    private final OnRecordClickListener onEdit;
    private final OnRecordClickListener onDelete;

    public MorbidityAdapter(List<MorbidityRecord> records,
                            OnRecordClickListener onView,
                            OnRecordClickListener onEdit,
                            OnRecordClickListener onDelete) {
        this.records = records;
        this.onView = onView;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    public void updateData(List<MorbidityRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_morbidity_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MorbidityRecord record = records.get(position);
        holder.bind(record);
    }

    @Override
    public int getItemCount() {
        return records != null ? records.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDiseaseName, tvIcdCode, tvPeriod, tvGrandTotal;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiseaseName = itemView.findViewById(R.id.tv_disease_name);
            tvIcdCode = itemView.findViewById(R.id.tv_icd_code);
            tvPeriod = itemView.findViewById(R.id.tv_period);
            tvGrandTotal = itemView.findViewById(R.id.tv_grand_total);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(MorbidityRecord record) {
            tvDiseaseName.setText(record.getDiseaseName() != null ? record.getDiseaseName() : "—");
            tvIcdCode.setText(record.getIcdCode() != null ? "ICD: " + record.getIcdCode() : "");
            tvPeriod.setText(record.getReportMonth() + " " + record.getReportYear());
            tvGrandTotal.setText("Total Cases: " + record.getGrandTotal());

            itemView.setOnClickListener(v -> onView.onClick(record));
            btnEdit.setOnClickListener(v -> onEdit.onClick(record));
            btnDelete.setOnClickListener(v -> onDelete.onClick(record));
        }
    }
}