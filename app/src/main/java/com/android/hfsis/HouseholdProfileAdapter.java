package com.android.hfsis;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.model.HouseholdProfile;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HouseholdProfileAdapter extends RecyclerView.Adapter<HouseholdProfileAdapter.ProfileViewHolder> {

    private List<HouseholdProfile> profilesList = new ArrayList<>();
    private List<HouseholdProfile> profilesListFull = new ArrayList<>();

    // Listeners for both actions
    private OnClassificationClickListener classificationClickListener;
    private OnEditProfileClickListener editProfileClickListener;

    public interface OnClassificationClickListener {
        void onClassificationClick(HouseholdProfile profile);
    }

    public interface OnEditProfileClickListener {
        void onEditProfileClick(HouseholdProfile profile);
    }

    public void setOnClassificationClickListener(OnClassificationClickListener listener) {
        this.classificationClickListener = listener;
    }

    public void setOnEditProfileClickListener(OnEditProfileClickListener listener) {
        this.editProfileClickListener = listener;
    }

    public void setProfiles(List<HouseholdProfile> profiles) {
        if (profiles != null) {
            this.profilesList = new ArrayList<>(profiles);
            this.profilesListFull = new ArrayList<>(profiles);
        } else {
            this.profilesList = new ArrayList<>();
            this.profilesListFull = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public void filter(String text) {
        profilesList.clear();
        if (text.isEmpty()) {
            profilesList.addAll(profilesListFull);
        } else {
            String filterPattern = text.toLowerCase(Locale.getDefault()).trim();
            for (HouseholdProfile item : profilesListFull) {
                String fullName = (item.memberFirstName + " " + item.memberLastName).toLowerCase(Locale.getDefault());
                String hhNum = item.hhNumber != null ? item.hhNumber.toLowerCase(Locale.getDefault()) : "";

                if (fullName.contains(filterPattern) || hhNum.contains(filterPattern)) {
                    profilesList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_household_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        HouseholdProfile profile = profilesList.get(position);

        holder.tvHHNumber.setText("HH #" + (profile.hhNumber != null ? profile.hhNumber : "N/A"));

        String location = (profile.sitio != null ? profile.sitio : "") + ", " + (profile.barangay != null ? profile.barangay : "");
        holder.tvLocation.setText(location);

        String fullName = "Member: " + profile.memberLastName + ", " + profile.memberFirstName;
        holder.tvMemberName.setText(fullName);
        holder.tvRespondent.setText("Respondent: " + (profile.respondent != null ? profile.respondent : "N/A"));

        String metaDetails = "Sex: " + profile.sex + " | DOB: " + profile.dob;
        holder.tvDetails.setText(metaDetails);

        StringBuilder risks = new StringBuilder();
        if (profile.hpn) risks.append("HPN ");
        if (profile.dm) risks.append("DM ");
        if (profile.tb) risks.append("TB ");

        if (risks.length() > 0) {
            holder.tvRiskBadges.setText(risks.toString().trim());
            holder.tvRiskBadges.setVisibility(View.VISIBLE);
        } else {
            holder.tvRiskBadges.setVisibility(View.GONE);
        }

        // 1. SET UP THE VIEW CLASSIFICATION BUTTON FUNCTION
        holder.btnViewClassification.setOnClickListener(v -> {
            if (classificationClickListener != null) {
                classificationClickListener.onClassificationClick(profile);
            }
        });

        // 2. SET UP THE EDIT BUTTON FUNCTION
        holder.btnEditProfile.setOnClickListener(v -> {
            if (editProfileClickListener != null) {
                editProfileClickListener.onEditProfileClick(profile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return profilesList.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView tvHHNumber, tvLocation, tvMemberName, tvRespondent, tvDetails, tvRiskBadges;
        MaterialButton btnViewClassification, btnEditProfile;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHHNumber = itemView.findViewById(R.id.tvHHNumber);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvRespondent = itemView.findViewById(R.id.tvRespondent);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvRiskBadges = itemView.findViewById(R.id.tvRiskBadges);
            btnViewClassification = itemView.findViewById(R.id.btnViewClassification);
            btnEditProfile = itemView.findViewById(R.id.btnEditProfile); // Ensure this field exists in layout
        }
    }
}