package com.android.hfsis;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.database.DatabaseHelper;
import com.android.hfsis.model.FamilyPlanningRecord;
import com.android.hfsis.model.HouseholdProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FamilyPlanningAdapter extends RecyclerView.Adapter<FamilyPlanningAdapter.RecordViewHolder> {

    private List<FamilyPlanningRecord> recordsList = new ArrayList<>();
    private final Context context;
    private final DatabaseHelper db;
    private final OnRecordActionListener actionListener;

    // Interface structure handling multiple action routing clicks
    public interface OnRecordActionListener {
        void onEditClick(FamilyPlanningRecord record);
        void onFollowUpClick(FamilyPlanningRecord record);
        void onDropOutClick(FamilyPlanningRecord record);
    }

    public FamilyPlanningAdapter(Context context, OnRecordActionListener actionListener) {
        this.context = context;
        this.db = DatabaseHelper.getDatabase(context.getApplicationContext());
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_family_planning, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        FamilyPlanningRecord record = recordsList.get(position);

        // Bind texts safely avoiding null pointer exceptions
        holder.tvRegDate.setText(record.registrationDate != null ? record.registrationDate : "N/A");

        String serial = record.familySerialNumber != null ? record.familySerialNumber : "N/A";
        String birthDate = record.birthDate != null ? record.birthDate : "N/A";
        holder.tvDemographics.setText("SN: " + serial + " • " + record.age + " yrs • " + birthDate);

        holder.tvMethod.setText(record.previousMethod != null ? record.previousMethod : "None");
        holder.tvAddress.setText(record.address != null ? record.address : "N/A");

        // Set custom click triggers mapping back to parent Fragment listeners
        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEditClick(record);
        });

        holder.btnFollowUp.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onFollowUpClick(record);
        });

        holder.btnDropOut.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDropOutClick(record);
        });

        // Run Room database querying off the main thread asynchronously
        holder.tvClientName.setText("Loading client name...");
        Executors.newSingleThreadExecutor().execute(() -> {
            HouseholdProfile profile = db.householdProfileDao().getProfileById(record.profileId);
            if (profile != null && holder.getAdapterPosition() == position) {
                String fullName = profile.memberLastName + ", " + profile.memberFirstName;
                if (profile.memberMiddleName != null && !profile.memberMiddleName.isEmpty()) {
                    fullName += " " + profile.memberMiddleName;
                }
                final String finalizedName = fullName;

                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> holder.tvClientName.setText(finalizedName));
                }
            } else if (profile == null && holder.getAdapterPosition() == position) {
                if (context instanceof android.app.Activity) {
                    ((android.app.Activity) context).runOnUiThread(() -> holder.tvClientName.setText("Unknown Profile"));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordsList.size();
    }

    public void setRecords(List<FamilyPlanningRecord> records) {
        this.recordsList = records;
        notifyDataSetChanged();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvClientName, tvRegDate, tvDemographics, tvMethod, tvAddress;
        Button btnEdit, btnFollowUp, btnDropOut;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvClientName = itemView.findViewById(R.id.tvCardFullName);
            tvRegDate = itemView.findViewById(R.id.tvCardRegDate);
            tvDemographics = itemView.findViewById(R.id.tvCardDemographics);
            tvMethod = itemView.findViewById(R.id.tvCardMethod);
            tvAddress = itemView.findViewById(R.id.tvCardAddress);

            btnEdit = itemView.findViewById(R.id.btnCardEdit);
            btnFollowUp = itemView.findViewById(R.id.btnCardFollowUp);
            btnDropOut = itemView.findViewById(R.id.btnCardDropOut);
        }
    }
}