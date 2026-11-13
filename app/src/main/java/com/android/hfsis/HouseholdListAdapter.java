package com.android.hfsis;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.hfsis.R;
import com.android.hfsis.model.HouseholdProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HouseholdListAdapter extends RecyclerView.Adapter<HouseholdListAdapter.HouseholdViewHolder> {

    public interface OnHouseholdClickListener {
        void onHouseholdClick(HouseholdProfile profile);
    }

    private final List<HouseholdProfile> fullList = new ArrayList<>();
    private final List<HouseholdProfile> filteredList = new ArrayList<>();
    private final OnHouseholdClickListener listener;

    public HouseholdListAdapter(OnHouseholdClickListener listener) {
        this.listener = listener;
    }

    /** Replace the full dataset (call after loading from the DAO). */
    public void submitList(List<HouseholdProfile> profiles) {
        fullList.clear();
        if (profiles != null) {
            fullList.addAll(profiles);
        }
        filteredList.clear();
        filteredList.addAll(fullList);
        notifyDataSetChanged();
    }

    /** Filter the currently displayed list by name, hhNumber, sitio, or barangay. */
    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String q = query.trim().toLowerCase(Locale.getDefault());
            for (HouseholdProfile p : fullList) {
                if (matches(p, q)) {
                    filteredList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    /** Number of profiles currently shown — handy for an "N Active" header badge. */
    public int getVisibleCount() {
        return filteredList.size();
    }

    private boolean matches(HouseholdProfile p, String q) {
        return containsIgnoreCase(p.memberLastName, q)
                || containsIgnoreCase(p.memberFirstName, q)
                || containsIgnoreCase(p.memberMiddleName, q)
                || containsIgnoreCase(p.hhNumber, q)
                || containsIgnoreCase(p.sitio, q)
                || containsIgnoreCase(p.barangay, q);
    }

    private boolean containsIgnoreCase(String field, String q) {
        return field != null && field.toLowerCase(Locale.getDefault()).contains(q);
    }

    @NonNull
    @Override
    public HouseholdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_household, parent, false);
        return new HouseholdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HouseholdViewHolder holder, int position) {
        HouseholdProfile profile = filteredList.get(position);
        holder.bind(profile, listener);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    static class HouseholdViewHolder extends RecyclerView.ViewHolder {
        private final TextView textHhNumber;
        private final TextView textName;
        private final TextView textLocation;
        private final ImageButton btnMore;

        HouseholdViewHolder(@NonNull View itemView) {
            super(itemView);
            textHhNumber = itemView.findViewById(R.id.textHhNumber);
            textName = itemView.findViewById(R.id.textMemberName);
            textLocation = itemView.findViewById(R.id.textLocation);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        void bind(HouseholdProfile profile, OnHouseholdClickListener listener) {

            // "Household No. X" title
            String hhTitle = itemView.getResources()
                    .getString(R.string.household_title_label, safe(profile.hhNumber));
            textHhNumber.setText(hhTitle);

            // Member name
            String fullName = safe(profile.memberLastName) + ", " + safe(profile.memberFirstName)
                    + (isNotEmpty(profile.memberMiddleName) ? " " + profile.memberMiddleName : "");
            textName.setText(fullName.trim());

            // Address
            String location = safe(profile.sitio) + ", " + safe(profile.barangay);
            textLocation.setText(location);

            View.OnClickListener openProfile = v -> {
                if (listener != null) {
                    listener.onHouseholdClick(profile);
                }
            };
            itemView.setOnClickListener(openProfile);
            btnMore.setOnClickListener(openProfile);
        }

        private String safe(String value) {
            return value == null ? "" : value;
        }

        private boolean isNotEmpty(String value) {
            return value != null && !value.trim().isEmpty();
        }
    }
}
