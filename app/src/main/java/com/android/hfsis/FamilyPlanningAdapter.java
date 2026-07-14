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
import com.android.hfsis.model.DropOutEntity;
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

        // FIX: this used to check record.previousMethod for null but then display
        // record.methodUsed - mismatched field, so it fell back to "None" too often.
        holder.tvMethod.setText(record.methodUsed != null ? record.methodUsed : "None");
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

            // NEW: now that FamilyPlanningDao no longer hides dropped-out clients,
            // look up their drop-out row here so the card can say so instead of
            // quietly looking like an active client.
            DropOutEntity dropOutRecord = db.dropOutDao().getDropOutByRecordId(record.id);

            if (holder.getAdapterPosition() != position) return;

            String fullName;
            if (profile != null) {
                fullName = profile.memberLastName + ", " + profile.memberFirstName;
                if (profile.memberMiddleName != null && !profile.memberMiddleName.isEmpty()) {
                    fullName += " " + profile.memberMiddleName;
                }
            } else {
                fullName = "Unknown Profile";
            }
            final String finalizedName = fullName;

            if (context instanceof android.app.Activity) {
                ((android.app.Activity) context).runOnUiThread(() -> {
                    if (holder.getAdapterPosition() != position) return;

                    holder.tvClientName.setText(finalizedName);

                    if (dropOutRecord != null) {
                        String statusText = "Dropped Out";
                        if (dropOutRecord.dropOutDate != null && !dropOutRecord.dropOutDate.isEmpty()) {
                            statusText += " on " + dropOutRecord.dropOutDate;
                        }
                        holder.tvMethod.setText(statusText);
                    }
                });
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